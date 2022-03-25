package com.fisk.task.consumer.olap;

import com.alibaba.fastjson.JSON;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.service.nifi.INifiComponentsBuild;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.task.ITBETLIncremental;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Description: 创建模型
 *
 * @author JinXingWang
 */
@Component
@Slf4j
public class BuildModelTaskListener {

    @Resource
    DataModelClient client;
    @Resource
    IOlap olap;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    PublishTaskController pc;
    @Resource
    IDorisBuild doris;
    @Resource
    INifiComponentsBuild iNifiComponentsBuild;
    @Resource
    private ITBETLIncremental itbetlIncremental;

    public void msg(String dataInfo, Acknowledgment acke) {
        log.info("doris组装参数:" + dataInfo);
        int tableId = 0;
        int tableType = 0;
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.type = 1;

        try {
            BusinessAreaGetDataDTO data = JSON.parseObject(dataInfo, BusinessAreaGetDataDTO.class);
            //创建Doris实际表和外部表
            List<OlapPO> olapPOS = olap.build(data.businessAreaId, data);
            for (OlapPO olapPO : olapPOS) {
                tableId = Math.toIntExact(olapPO.tableId);
                tableType = olapPO.type.getValue();
                log.info("Doris建表开始:" + olapPO.tableName);
                doris.dorisBuildTable("DROP TABLE IF EXISTS " + olapPO.tableName);
                doris.dorisBuildTable(olapPO.createTableSql);
                log.info("Doris建表结束,开始创建nifi配置");
                //添加etl日志,加kpi,区分指标与事实维度
                itbetlIncremental.addEtlIncremental(olapPO.tableName + OlapTableEnum.KPI.getName());
                BuildNifiFlowDTO buildNifiFlowDTO = new BuildNifiFlowDTO();
                log.info("nifi配置结束,开始创建nifi流程");
                buildNifiFlowDTO.userId = data.userId;
                buildNifiFlowDTO.appId = olapPO.businessAreaId;
                OlapPO olapPO1 = olap.selectByName(olapPO.tableName);
                buildNifiFlowDTO.id = olapPO1.id;
                buildNifiFlowDTO.type = OlapTableEnum.KPI;
                buildNifiFlowDTO.dataClassifyEnum = DataClassifyEnum.DATAMODELKPL;
                buildNifiFlowDTO.synchronousTypeEnum = SynchronousTypeEnum.PGTODORIS;
                buildNifiFlowDTO.tableName = olapPO.tableName;
                buildNifiFlowDTO.selectSql = olapPO.selectDataSql;
                buildNifiFlowDTO.openTransmission=true;
                pc.publishBuildNifiFlowTask(buildNifiFlowDTO);
                //1是维度
                if (tableType == 1) {
                    modelPublishStatusDTO.status = 1;
                    modelPublishStatusDTO.id = Math.toIntExact(tableId);
                    client.updateDimensionPublishStatus(modelPublishStatusDTO);
                } else {
                    modelPublishStatusDTO.status = 1;
                    modelPublishStatusDTO.id = Math.toIntExact(tableId);
                    client.updateFactPublishStatus(modelPublishStatusDTO);
                }
                log.info("nifi流程配置结束");
            }
        } catch (Exception e) {
            if (tableType == 1) {
                modelPublishStatusDTO.status = 2;
                modelPublishStatusDTO.id = Math.toIntExact(tableId);
                client.updateDimensionPublishStatus(modelPublishStatusDTO);
            } else {
                modelPublishStatusDTO.status = 2;
                modelPublishStatusDTO.id = Math.toIntExact(tableId);
                client.updateFactPublishStatus(modelPublishStatusDTO);
            }
        } finally {
            acke.acknowledge();
        }
    }
}
