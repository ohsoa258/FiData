package com.fisk.task.listener.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.enums.PublishTypeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.nifi.IJdbcBuild;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/27 12:57
 * Description:在pgsql库中创建表
 */
@Component
@Slf4j
public class BuildDataInputPgTableListener {
    @Resource
    IJdbcBuild iJdbcBuild;
    @Resource
    DataAccessClient dc;
    @Resource
    TaskPgTableStructureHelper taskPgTableStructureHelper;
    @Resource
    TaskPgTableStructureMapper taskPgTableStructureMapper;
    @Resource
    BuildAtlasTableAndColumnTaskListener buildAtlasTableAndColumnTaskListener;
    @Resource
    UserClient userClient;
    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        log.info("执行pg build table");
        log.info("dataInfo:" + dataInfo);
        //新建发布状态dto
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = PublishTypeEnum.SUCCESS.getValue();
        //dataInfo  ->  buildPhysicalTableDTO
        //将待发布的数据转化为buildPhysicalTableDTO
        BuildPhysicalTableDTO buildPhysicalTableDTO = JSON.parseObject(dataInfo, BuildPhysicalTableDTO.class);
        //为发布状态dto装载物理表id
        modelPublishStatusDTO.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
        //为发布状态dto装载应用id
        modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
        //获取版本号和修改表结构的参数
        ModelPublishTableDTO dto = buildPhysicalTableDTO.modelPublishTableDTO;
        //获取ods数据源的信息  dataSourceOdsId=2 ->  ods
        //开发doris-hive外部目录测试 暂时改为13

        //原本固定用的 dmp_system_db库里 tb_datasource_config id为2的 dmp_ods，现改为数据接入 应用在页面选择的目标位置
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(buildPhysicalTableDTO.targetDbId);
        DataSourceTypeEnum conType = null;
        log.info("ods数据源信息{}", JSON.toJSONString(fiDataDataSource));

        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            //获取成功
            //获取系统数据源信息
            DataSourceDTO dataSource = fiDataDataSource.data;
            //获取连接类型
            conType = dataSource.conType;
        } else {
            //获取失败 报错
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
        }
        log.info("连接类型:{}", conType);
        //分辨库的类别，获取对应数据库的建表
        IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(conType);
        log.info("开始保存ods版本号,参数为{}", dto);
        // 保存ods版本号
        //获取时间戳版本号
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar calendar = Calendar.getInstance();
        String version = df.format(calendar.getTime());
        ResultEnum resultEnum = null;
        try {

            //非hudi 原非实时物理表流程和实时api流程
            if (!buildPhysicalTableDTO.ifHive) {
                //保存接入相关表结构数据(保存版本号)
                String msg = null;
                //如果目标数据源类型既不是doris也不是mysql
                if (!DataSourceTypeEnum.DORIS.getName().equalsIgnoreCase(conType.getName())
                        && !DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(conType.getName())
                ) {
                    resultEnum = taskPgTableStructureHelper.saveTableStructure(dto, version, conType);
                    if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                        taskPgTableStructureMapper.updatevalidVersion(version);
                        throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL, "修改表结构失败，请您检查表是否存在或字段数据格式是否能被转换为目标格式");
                    }
                    log.info("数仓执行修改表结构的存储过程返回结果" + resultEnum);
                } else {
                    msg = taskPgTableStructureHelper.saveTableStructureForDoris(dto, version, conType,buildPhysicalTableDTO.targetDbId);
//                    resultEnum = taskPgTableStructureHelper.saveTableStructureForDoris(modelPublishTableDTO, version, conType);
//                    if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                    if (!ResultEnum.TASK_TABLE_NOT_EXIST.getMsg().equals(msg) && !ResultEnum.SUCCESS.getMsg().equals(msg)) {
                        taskPgTableStructureMapper.updatevalidVersion(version);
                        throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL, msg);
                    }
                    log.info("数仓执行修改表结构的存储过程返回结果" + msg);
                }

                log.info("保存版本号方法执行成功");
                //获取建表语句
                List<String> sqlList = dbCommand.buildStgAndOdsTable(buildPhysicalTableDTO);
                log.info("建表语句:" + JSON.toJSONString(sqlList));
                //执行第二条建表语句--创建stg表
                BusinessResult result = null;

                //Mysql 第三方ods  数据接入-restfulApi目标位置是第三方的mysql数据库
                if (conType.getName().equals(DataSourceTypeEnum.MYSQL.getName())) {
                    result = iJdbcBuild.buildTableByTargetDbType(sqlList.get(1), BusinessTypeEnum.DATAINPUT, buildPhysicalTableDTO.targetDbId);
                } else {
                    result = iJdbcBuild.postgreBuildTable(sqlList.get(1), BusinessTypeEnum.DATAINPUT);
                }

                if (!result.success) {
                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                }

                if (resultEnum != null) {
                    if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
                        //执行第一条建表语句--创建目标表
                        //Mysql 第三方ods  数据接入-restfulApi目标位置是第三方的mysql数据库
                        if (conType.getName().equals(DataSourceTypeEnum.MYSQL.getName())) {
                            iJdbcBuild.buildTableByTargetDbType(sqlList.get(0), BusinessTypeEnum.DATAINPUT, buildPhysicalTableDTO.targetDbId);
                        } else {
                            iJdbcBuild.postgreBuildTable(sqlList.get(0), BusinessTypeEnum.DATAINPUT);
                        }
                        log.info("【PGODS】" + sqlList.get(0));
                        log.info("pg：建表完成");
                    }
                } else {
                    if (ResultEnum.TASK_TABLE_NOT_EXIST.getMsg().equals(msg)) {
                        if (conType.getName().equals(DataSourceTypeEnum.MYSQL.getName())) {
                            iJdbcBuild.buildTableByTargetDbType(sqlList.get(0), BusinessTypeEnum.DATAINPUT, buildPhysicalTableDTO.targetDbId);
                        } else {
                            iJdbcBuild.postgreBuildTable(sqlList.get(0), BusinessTypeEnum.DATAINPUT);
                        }
                        log.info("【PGODS】" + sqlList.get(0));
                        log.info("pg：建表完成");
                    }
                }
                //hive(hudi)只建doris的外部目录
            } else {
                //如果是hive 切换成建doris外部目录的实现类
                dbCommand = BuildFactoryHelper.getDBCommand(DataSourceTypeEnum.DORIS_CATALOG);
                //获取建表语句
                List<String> sqlList = dbCommand.buildStgAndOdsTable(buildPhysicalTableDTO);
                log.info("hive_doris建外部目录语句:" + JSON.toJSONString(sqlList));
                //执行建表语句--创建doris-hive外部表
                iJdbcBuild.postgreBuildTable(sqlList.get(0), BusinessTypeEnum.DATAINPUT);
                log.info("【doris-hive】" + sqlList.get(0));
                log.info("doris-hive：建表完成");
            }

            //实时应用改状态
            if (Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.oracle_cdc)) {
                log.info("oracle_cdc建表完成");
                modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            } else if (Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.doris_catalog)) {
                log.info("doris-hive外部目录建立完成");
                ModelPublishStatusDTO modelPublishStatus = new ModelPublishStatusDTO();
                modelPublishStatus.publish = PublishTypeEnum.SUCCESS.getValue();
                modelPublishStatus.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
                dc.updateTablePublishStatus(modelPublishStatus);
                return ResultEnum.SUCCESS;
            } else if ((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0)
//                    || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api)
                    || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.RestfulAPI)
                    || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.webservice)) {
                int tableCount = 0;
                modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
                String selectTable = dbCommand.queryTableNum(buildPhysicalTableDTO);
                log.info("查询是否建成表语句:{}", selectTable);
                BusinessResult businessResult = iJdbcBuild.postgreQuery(selectTable, BusinessTypeEnum.DATAINPUT);
                if (businessResult.data != null) {
                    List<Object> countList = JSON.parseArray(businessResult.data.toString(), Object.class);
                    String countString = countList.get(0).toString();
                    Map countMap = JSON.parseObject(countString, Map.class);
                    Object count = countMap.get("count");
                    if (count == null) {
                        count = countMap.get("");
                    }
                    tableCount = Integer.parseInt(count.toString());
                }
                if (tableCount == buildPhysicalTableDTO.apiTableNames.size()) {
                    dc.updateApiPublishStatus(modelPublishStatusDTO);
                } else if (conType.getName().equals(DataSourceTypeEnum.DORIS.getName())) {
                    dc.updateApiPublishStatus(modelPublishStatusDTO);
                } else if (conType.getName().equals(DataSourceTypeEnum.MYSQL.getName())) {
                    dc.updateApiPublishStatus(modelPublishStatusDTO);
                }
            } else if (Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api)) {
                int tableCount = 0;
                modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
                String selectTable = dbCommand.queryTableNum(buildPhysicalTableDTO);
                log.info("查询是否建成表语句:{}", selectTable);
                BusinessResult businessResult = iJdbcBuild.postgreQuery(selectTable, BusinessTypeEnum.DATAINPUT);
                if (businessResult.data != null) {
                    List<Object> countList = JSON.parseArray(businessResult.data.toString(), Object.class);
                    String countString = countList.get(0).toString();
                    Map countMap = JSON.parseObject(countString, Map.class);
                    Object count = countMap.get("count");
                    if (count == null) {
                        count = countMap.get("");
                    }
                    tableCount = Integer.parseInt(count.toString());
                }
                if (tableCount == buildPhysicalTableDTO.apiTableNames.size()) {
                    dc.updateApiPublishStatus(modelPublishStatusDTO);
                }

                //建成nifi流程
                buildAtlasTableAndColumnTaskListener.msg(dataInfo, null);
            } else {
                buildAtlasTableAndColumnTaskListener.msg(dataInfo, null);
            }

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
//            if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
//                taskPgTableStructureMapper.updatevalidVersion(version);
//            }
            if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                //先更新tb_table_access的发布状态   dmp_datainput_db
                ModelPublishStatusDTO modelPublishStatus = new ModelPublishStatusDTO();
                modelPublishStatus.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
                modelPublishStatus.publish = PublishTypeEnum.FAIL.getValue();
                modelPublishStatus.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
                dc.updateTablePublishStatus(modelPublishStatus);

                //再更新tb_api_config的状态   dmp_datainput_db
                modelPublishStatusDTO.publish = PublishTypeEnum.FAIL.getValue();
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            } else {
                ModelPublishStatusDTO modelPublishStatus = new ModelPublishStatusDTO();
                modelPublishStatus.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
                modelPublishStatus.publish = PublishTypeEnum.FAIL.getValue();
                modelPublishStatus.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
                dc.updateTablePublishStatus(modelPublishStatus);
            }
            log.error("创建表失败" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.ERROR, StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }
}
