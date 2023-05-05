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
import com.fisk.task.dto.accessmdm.AccessMdmPublishTableDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.po.mdm.MdmTableNifiSettingPO;
import com.fisk.task.service.nifi.IMdmTableNifiSettingService;
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
    IMdmTableNifiSettingService mdmTableNifiSettingService;
    @Resource
    PublishTaskController pc;
    @Resource
    MdmClient mdmClient;

    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        ResultEnum result = ResultEnum.SUCCESS;
        AccessPublishStatusDTO accessPublishStatusDTO = new AccessPublishStatusDTO();
        int id = 0;
        int tableType = 0;
        log.info("生成nifi流程参数:" + dataInfo);
        //生成nifi流程
        try {
            AccessPublishDataDTO inpData = JSON.parseObject(dataInfo, AccessPublishDataDTO.class);
            AccessMdmPublishTableDTO accessMdmPublishTableDTO = inpData.access;
            ResultEntity<DataSourceDTO> DataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
            DataSourceTypeEnum conType = null;
            if (DataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = DataSource.data;
                conType = dataSource.conType;
            } else {
                log.error("userclient无法查询到ods库的连接信息");
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
                id = Math.toIntExact(accessMdmPublishTableDTO.tableId);
                //生成版本号
                //获取时间戳版本号
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Calendar calendar = Calendar.getInstance();
                String version = df.format(calendar.getTime());
                log.info("开始执行nifi创建数据同步");
                BuildMdmNifiFlowDTO bfd = new BuildMdmNifiFlowDTO();
                bfd.accessId = inpData.accessId;
                bfd.userId = inpData.userId;
                bfd.modelId = inpData.modelId;
                bfd.entityId = inpData.entityId;
                bfd.synchronousTypeEnum = SynchronousTypeEnum.ODSTOMDM;
                //来源为数据接入
                bfd.dataClassifyEnum = DataClassifyEnum.MDM_DATA_ACCESS;
                bfd.id = accessMdmPublishTableDTO.tableId;
                bfd.tableName = accessMdmPublishTableDTO.tableName;
                bfd.selectSql = accessMdmPublishTableDTO.sqlScript;
                //同步方式
                bfd.synMode = accessMdmPublishTableDTO.synMode;
                bfd.queryStartTime = accessMdmPublishTableDTO.queryStartTime;
                bfd.queryEndTime = accessMdmPublishTableDTO.queryEndTime;
                bfd.modelName = inpData.modelName;
                bfd.entityName = inpData.entityName;
                bfd.openTransmission = inpData.openTransmission;
                bfd.dataSourceDbId = accessMdmPublishTableDTO.dataSourceDbId;
                bfd.targetDbId = accessMdmPublishTableDTO.targetDbId;
                bfd.customScriptBefore = accessMdmPublishTableDTO.customScript;
                bfd.customScriptAfter = accessMdmPublishTableDTO.customScriptAfter;
//                // 设置预览SQL执行语句
                bfd.syncStgToMdmSql = accessMdmPublishTableDTO.coverScript;
                bfd.type = OlapTableEnum.MDM_DATA_ACCESS;
                bfd.maxRowsPerFlowFile = accessMdmPublishTableDTO.maxRowsPerFlowFile;
                bfd.fetchSize = accessMdmPublishTableDTO.fetchSize;
                bfd.traceId = inpData.traceId;
                log.info("nifi传入参数：" + JSON.toJSONString(bfd));
                MdmTableNifiSettingPO one = mdmTableNifiSettingService.query().eq("model_id", bfd.modelId).eq("entity_id", bfd.entityId).eq("type", bfd.type.getValue()).one();
                MdmTableNifiSettingPO tableNifiSettingPO = new MdmTableNifiSettingPO();
                if (one != null) {
                    tableNifiSettingPO = one;
                }
                tableNifiSettingPO.modelId = Math.toIntExact(bfd.modelId);
                tableNifiSettingPO.entityId = Math.toIntExact(bfd.entityId);
                tableNifiSettingPO.tableName = bfd.tableName;
                tableNifiSettingPO.selectSql = bfd.selectSql;
                tableNifiSettingPO.type = bfd.type.getValue();
                tableNifiSettingPO.syncMode = 1;
                mdmTableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
                pc.publishAccessMdmNifiFlowTask(bfd);
                log.info("执行完成");
                accessPublishStatusDTO.status = 1;
                accessPublishStatusDTO.id = (int)inpData.accessId;
                mdmClient.updateAccessPublishState(accessPublishStatusDTO);
            return result;
        }catch (Exception e){
            log.error("mdm发布失败,表id为" + id + StackTraceHelper.getStackTraceInfo(e));
            result = ResultEnum.ERROR;
            return result;
        }finally {
            acke.acknowledge();
        }
    }
}