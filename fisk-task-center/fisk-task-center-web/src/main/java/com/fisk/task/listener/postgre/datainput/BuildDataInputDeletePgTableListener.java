package com.fisk.task.listener.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.utils.PostgreHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        log.info("执行pg delete table");
        log.info("dataInfo:" + dataInfo);
        try {

            StringBuilder buildDelSqlStr = new StringBuilder("DROP TABLE IF EXISTS ");
            PgsqlDelTableDTO inputData = JSON.parseObject(dataInfo, PgsqlDelTableDTO.class);
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
                    postgreHelper.postgreExecuteSql(delSqlStr, BusinessTypeEnum.DATAINPUT);
                    log.info("delsql:" + delSqlStr);
                    log.info("执行pg delete table 完成");
                } else {
                    inputData.tableList.forEach((t) -> {
                        buildDelSqlStr.append(t.tableName + ", ");
                        buildDelSqlStr.append("stg_" + t.tableName + ", ");
                        conditionHashMap.put("table_name", t.tableName);
                        taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                        tbetlIncremental.delEtlIncrementalList(t.tableName);
                        //doris.dorisBuildTable("DROP TABLE IF EXISTS " + t.tableName + ";");
                        //doris.dorisBuildTable("DROP TABLE IF EXISTS external_" + t.tableName + ";");
                    });
                    String delSqlStr = buildDelSqlStr.toString();
                    delSqlStr = delSqlStr.substring(0, delSqlStr.lastIndexOf(",")) + " ;";
                    postgreHelper.postgreExecuteSql(delSqlStr, BusinessTypeEnum.DATAMODEL);
                    log.info("delsql:" + delSqlStr);
                    log.info("执行pg delete table 完成");
                }
            }
            //todo 删除的时候同时删除管道
            deletePipleline(inputData);

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            acke.acknowledge();
        }
    }

    /**
     * 删除表的时候,同时删除管道,但不包括非实时api,因为非实时api在调度绑定的是api_id,并不是表id,而这里删的是表级别
     * todo 需要一个表类别
     * @param inputData
     */
    public void deletePipleline(PgsqlDelTableDTO inputData) {
        if (!CollectionUtils.isEmpty(inputData.tableList)) {
            HashMap<String, Object> conditionHashMap = new HashMap<>();
            //接入
            if (Objects.equals(inputData.businessTypeEnum, BusinessTypeEnum.DATAINPUT)) {

                inputData.tableList.forEach((t) -> {

                });

            } else {
                //建模
                inputData.tableList.forEach((t) -> {

                });
            }
        }
    }

}
