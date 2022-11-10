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
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
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
import java.util.*;

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
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = PublishTypeEnum.SUCCESS.getValue();
        BuildPhysicalTableDTO buildPhysicalTableDTO = JSON.parseObject(dataInfo, BuildPhysicalTableDTO.class);
        modelPublishStatusDTO.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
        modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
        ModelPublishTableDTO dto = buildPhysicalTableDTO.modelPublishTableDTO;
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        DataSourceTypeEnum conType = null;
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            conType = dataSource.conType;
        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
        }
        //分辨库的类别
        IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(conType);
        log.info("开始保存ods版本号,参数为{}", dto);
        // 保存ods版本号
        //获取时间戳版本号
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar calendar = Calendar.getInstance();
        String version = df.format(calendar.getTime());
        ResultEnum resultEnum = taskPgTableStructureHelper.saveTableStructure(dto, version,conType);
        log.info("执行修改语句返回结果:" + resultEnum);
        if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
            taskPgTableStructureMapper.updatevalidVersion(version);
            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
        }
        log.info("保存版本号方法执行成功");
        try {
            List<String> sqlList = dbCommand.buildStgAndOdsTable(buildPhysicalTableDTO);
            log.info("建表语句:" + JSON.toJSONString(sqlList));
            BusinessResult Result = iJdbcBuild.postgreBuildTable(sqlList.get(1), BusinessTypeEnum.DATAINPUT);
            if (!Result.success) {
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
            if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
                iJdbcBuild.postgreBuildTable(sqlList.get(0), BusinessTypeEnum.DATAINPUT);
                log.info("【PGSTG】" + sqlList.get(0));
                log.info("pg：建表完成");
            }
            //实时应用改状态
            if (Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.oracle_cdc)) {
                log.info("oracle_cdc建表完成");
                modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            } else if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                int tableCount = 0;
                modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
                String selectTable = dbCommand.queryTableNum(buildPhysicalTableDTO);
                BusinessResult businessResult = iJdbcBuild.postgreQuery(selectTable, BusinessTypeEnum.DATAINPUT);
                if (businessResult.data != null) {
                    List<Object> countList = JSON.parseArray(businessResult.data.toString(), Object.class);
                    String countString = countList.get(0).toString();
                    Map countMap = JSON.parseObject(countString, Map.class);
                    Object count = countMap.get("count");
                    tableCount = Integer.parseInt(count.toString());
                }
                if (tableCount == buildPhysicalTableDTO.apiTableNames.size()) {
                    dc.updateApiPublishStatus(modelPublishStatusDTO);
                }
            } else {
                buildAtlasTableAndColumnTaskListener.msg(dataInfo, null);
            }

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                modelPublishStatusDTO.publish = PublishTypeEnum.FAIL.getValue();
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            } else {
                ModelPublishStatusDTO modelPublishStatus = new ModelPublishStatusDTO();
                modelPublishStatus.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
                modelPublishStatus.publish = 2;
                modelPublishStatus.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
                dc.updateTablePublishStatus(modelPublishStatus);
            }
            log.error("创建表失败" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }
}
