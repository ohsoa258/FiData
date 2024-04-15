package com.fisk.task.listener.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.utils.PostgreHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/15 10:40
 * Description: 删除pgsql表，在数据接入删除应用和删除指定的物理表的时候触发。
 */
@Component
@Slf4j
public class BuildDataInputDeletePgTableListener {

    @Resource
    IDorisBuild doris;
    @Resource
    TaskPgTableStructureMapper taskPgTableStructureMapper;
    @Resource
    PostgreHelper postgreHelper;
    @Resource
    TBETLIncrementalMapper tbetlIncremental;

    @Resource
    UserClient userClient;

    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;

    @Value("${fiData-data-dw-source}")
    private String dataSourceDwId;


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        log.info("执行pg delete table");
        log.info("dataInfo:" + dataInfo);
        try {
            PgsqlDelTableDTO inputData = JSON.parseObject(dataInfo, PgsqlDelTableDTO.class);
            List<AppDataSourceDTO> appSources = inputData.getAppSources();
            //应用简称
            String appAbbreviation = inputData.getAppAbbreviation();

            //获取dw数仓数据库类型
            DataSourceDTO data = null;
            ResultEntity<DataSourceDTO> fiDataDataSource = null;
            if (inputData.targetDbId != null) {
                fiDataDataSource = userClient.getFiDataDataSourceById(inputData.targetDbId);
            } else {
                fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            }

            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                data = fiDataDataSource.data;
            } else {
                log.error("userclient无法查询到dw库的连接信息");
                throw new FkException(ResultEnum.ERROR);
            }
            DataSourceTypeEnum conType = data.getConType();

            //判断是否是hive
            boolean ifHive = false;
            if (appSources != null) {
                for (AppDataSourceDTO appSource : appSources) {
                    if (DbTypeEnum.doris_catalog.getName().equalsIgnoreCase(appSource.getDriveType())) {
                        ifHive = true;
                        break;
                    }
                }
            }

            if (ifHive) {
                StringBuilder buildDropCatalogSql = new StringBuilder("DROP CATALOG ");
                if (inputData.tableList != null && inputData.tableList.size() != 0) {
                    for (TableListDTO tableListDTO : inputData.tableList) {
                        buildDropCatalogSql.append(tableListDTO.tableName);
                        postgreHelper.postgreExecuteSql(String.valueOf(buildDropCatalogSql), BusinessTypeEnum.DATAINPUT);
                        log.info("delsql:" + buildDropCatalogSql);
                        log.info("执行pg delete table 完成");
                    }
                }
                return ResultEnum.SUCCESS;
            } else {
                StringBuilder buildDelSqlStr = new StringBuilder("DROP TABLE IF EXISTS ");
                if (inputData.tableList != null && inputData.tableList.size() != 0) {
                    HashMap<String, Object> conditionHashMap = new HashMap<>();
                    if (Objects.equals(inputData.businessTypeEnum, BusinessTypeEnum.DATAINPUT)) {
                        List<String> atlasEntityId = new ArrayList();
                        inputData.tableList.forEach((t) -> {
                            List<String> stgAndTableName = TableNameGenerateUtils.getStgAndTableName(t.tableName);
                            buildDelSqlStr.append(stgAndTableName.get(0) + "," + stgAndTableName.get(1) + ", ");
                            atlasEntityId.add(t.tableAtlasId);
                            conditionHashMap.put("table_name", stgAndTableName.get(0));
                            taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                            tbetlIncremental.delEtlIncrementalList(stgAndTableName.get(0));
                            conditionHashMap.put("table_name", stgAndTableName.get(1));
                            taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                            tbetlIncremental.delEtlIncrementalList(stgAndTableName.get(1));
                        });
                        String delSqlStr = buildDelSqlStr.toString();
                        delSqlStr = delSqlStr.substring(0, delSqlStr.lastIndexOf(",")) + " ;";
                        if (inputData.targetDbId != null) {
                            postgreHelper.postgreExecuteSqlByDbType(delSqlStr, BusinessTypeEnum.DATAINPUT, inputData.targetDbId);
                        } else {
                            postgreHelper.postgreExecuteSql(delSqlStr, BusinessTypeEnum.DATAINPUT);
                        }
                        log.info("delsql:" + delSqlStr);
                        log.info("执行pg delete table 完成");
                    } else {
                        //doris删表语句不一样
                        //sqlserver支持DROP TABLE IF EXISTS fact_dr_01, temp_fact_dr_01 ;
                        //doris不支持  doris只支持：
                        //DROP TABLE IF EXISTS fact_dr_01;DROP TABLE IF EXISTS temp_fact_dr_01;
                        if (Objects.equals(DataSourceTypeEnum.DORIS, conType)) {
                            inputData.tableList.forEach((t) -> {
                                buildDelSqlStr.append(t.tableName).append(" FORCE; ");
                                buildDelSqlStr.append("DROP TABLE IF EXISTS ").append("temp_").append(t.tableName).append(" FORCE, ");
                                conditionHashMap.put("table_name", t.tableName);
                                taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                                tbetlIncremental.delEtlIncrementalList(t.tableName);
                                //doris.dorisBuildTable("DROP TABLE IF EXISTS " + t.tableName + ";");
                                //doris.dorisBuildTable("DROP TABLE IF EXISTS external_" + t.tableName + ";");
                            });
                            String delSqlStr = buildDelSqlStr.toString();
                            delSqlStr = delSqlStr.substring(0, delSqlStr.lastIndexOf(",")) + " ;";
                            log.info("delsql:" + delSqlStr);
                            postgreHelper.postgreExecuteSql(delSqlStr, BusinessTypeEnum.DATAMODEL);
                            log.info("执行pg delete table 完成");
                        } else {
                            inputData.tableList.forEach((t) -> {
                                buildDelSqlStr.append(t.tableName + ", ");
                                buildDelSqlStr.append("temp_" + t.tableName + ", ");
                                conditionHashMap.put("table_name", t.tableName);
                                taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                                tbetlIncremental.delEtlIncrementalList(t.tableName);
                                //doris.dorisBuildTable("DROP TABLE IF EXISTS " + t.tableName + ";");
                                //doris.dorisBuildTable("DROP TABLE IF EXISTS external_" + t.tableName + ";");
                            });
                            String delSqlStr = buildDelSqlStr.toString();
                            delSqlStr = delSqlStr.substring(0, delSqlStr.lastIndexOf(",")) + " ;";
                            log.info("delsql:" + delSqlStr);
                            postgreHelper.postgreExecuteSql(delSqlStr, BusinessTypeEnum.DATAMODEL);
                            log.info("执行pg delete table 完成");
                        }
                    }
                }
                return ResultEnum.SUCCESS;
            }

        } catch (Exception e) {
            log.error("删除表失败：" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            acke.acknowledge();
        }
    }

}
