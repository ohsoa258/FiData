package com.fisk.task.listener.access;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.accessmodel.AccessPublishDataDTO;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: wangjian
 */
@Component
@Slf4j
public class BuildMdmAccessETLListener {

    @Resource
    public UserClient userClient;
    @Value("${fiData-data-ods-source}")
    public String dataSourceOdsId;
    @Resource
    ITableNifiSettingService tableNifiSettingService;
    @Resource
    PublishTaskController pc;
    @Resource
    MdmClient mdmClient;
    @Resource
    private TBETLIncrementalMapper incrementalMapper;

    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        ResultEnum result = ResultEnum.SUCCESS;
        AccessPublishStatusDTO accessPublishStatusDTO = new AccessPublishStatusDTO();
        int id = 0;
        int tableType = 0;
        log.info("生成nifi流程参数:[" +dataInfo+"]");
        dataInfo = "[" + dataInfo + "]";
        //生成nifi流程
        try {
            List<AccessPublishDataDTO> inpData = JSON.parseArray(dataInfo, AccessPublishDataDTO.class);
            ResultEntity<DataSourceDTO> DataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
            DataSourceTypeEnum conType = null;
            if (DataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = DataSource.data;
                conType = dataSource.conType;
            } else {
                log.error("userclient无法查询到ods库的连接信息");
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
            for (AccessPublishDataDTO accessPublishDataDTO : inpData) {
                id = Math.toIntExact(accessPublishDataDTO.accessId);
                //生成版本号
                //获取时间戳版本号
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Calendar calendar = Calendar.getInstance();
                String version = df.format(calendar.getTime());
                log.info("开始执行nifi创建数据同步");
                TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
                ETLIncremental.objectName = accessPublishDataDTO.access.tableName;
                ETLIncremental.enableFlag = "1";
                ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
                Map<String, Object> conditionHashMap = new HashMap<>();
                conditionHashMap.put("object_name", ETLIncremental.objectName);
                List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
                if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
                    log.info("此表已有同步记录,无需重复添加");
                } else {
                    incrementalMapper.insert(ETLIncremental);
                }
                BuildMdmNifiFlowDTO bfd = new BuildMdmNifiFlowDTO();
                bfd.tableHistoryId = accessPublishDataDTO.access.tableHistoryId;
                bfd.accessId = accessPublishDataDTO.accessId;
                bfd.versionId =  accessPublishDataDTO.access.versionId;
                bfd.userId = accessPublishDataDTO.access.userId;
                bfd.modelId = accessPublishDataDTO.modelId;
                bfd.entityId = accessPublishDataDTO.entityId;
                bfd.synchronousTypeEnum = SynchronousTypeEnum.ODSTOMDM;
                //来源为数据接入
                bfd.dataClassifyEnum = DataClassifyEnum.MDM_DATA_ACCESS;
                bfd.id = accessPublishDataDTO.access.tableId;
                bfd.tableName = accessPublishDataDTO.access.tableName;
                bfd.selectSql = accessPublishDataDTO.access.sqlScript;
                //同步方式
                bfd.synMode = accessPublishDataDTO.access.synMode;
                bfd.queryStartTime = accessPublishDataDTO.access.queryStartTime;
                bfd.queryEndTime = accessPublishDataDTO.access.queryEndTime;
                bfd.modelName = accessPublishDataDTO.modelName;
                bfd.entityName = accessPublishDataDTO.entityName;
                bfd.openTransmission = accessPublishDataDTO.openTransmission;
                bfd.dataSourceDbId = accessPublishDataDTO.access.dataSourceDbId;
                bfd.targetDbId = accessPublishDataDTO.access.targetDbId;
                bfd.customScriptBefore = accessPublishDataDTO.access.customScript;
                bfd.customScriptAfter = accessPublishDataDTO.access.customScriptAfter;
//                // 设置预览SQL执行语句
                bfd.syncStgToMdmSql = accessPublishDataDTO.access.coverScript;
                bfd.type = OlapTableEnum.MDM_DATA_ACCESS;
                bfd.maxRowsPerFlowFile = accessPublishDataDTO.access.maxRowsPerFlowFile;
                bfd.fetchSize = accessPublishDataDTO.access.fetchSize;
                bfd.traceId = accessPublishDataDTO.traceId;
                log.info("nifi传入参数：" + JSON.toJSONString(bfd));
                TableNifiSettingPO one = tableNifiSettingService.query().eq("app_id", bfd.modelId).eq("type", bfd.type.getValue()).one();
                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
                if (one != null) {
                    tableNifiSettingPO = one;
                }
                tableNifiSettingPO.appId = Math.toIntExact(bfd.modelId);
                tableNifiSettingPO.tableAccessId = Math.toIntExact(bfd.entityId);
                tableNifiSettingPO.tableName = bfd.tableName;
                tableNifiSettingPO.selectSql = bfd.selectSql;
                tableNifiSettingPO.type = bfd.type.getValue();
                tableNifiSettingPO.syncMode = bfd.synMode;
                tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
                pc.publishAccessMdmNifiFlowTask(bfd);
                log.info("执行完成");
                accessPublishStatusDTO.publish = 1;
                accessPublishStatusDTO.id = id;
                mdmClient.updateAccessPublishState(accessPublishStatusDTO);
            }
            return result;
        } catch (Exception e) {
            log.error("mdm发布失败,表id为" + id + StackTraceHelper.getStackTraceInfo(e));
            accessPublishStatusDTO.publish = 2;
            accessPublishStatusDTO.id = id;
            mdmClient.updateAccessPublishState(accessPublishStatusDTO);
            result = ResultEnum.ERROR;
            return result;
        } finally {
            acke.acknowledge();
        }
    }
}