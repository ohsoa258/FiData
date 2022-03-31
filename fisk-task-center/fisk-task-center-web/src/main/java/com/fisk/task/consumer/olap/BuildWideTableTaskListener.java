package com.fisk.task.consumer.olap;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableSourceFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableSourceTableConfigDTO;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.service.task.impl.TBETLIncrementalImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Description: 宽表监听
 *
 * @author cfk
 */
@Component
@Slf4j
public class BuildWideTableTaskListener {

    @Resource
    PublishTaskController pc;
    @Resource
    IDorisBuild doris;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    TBETLIncrementalImpl tbetlIncremental;

    public void msg(String dataInfo, Acknowledgment acke) {
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.type = 1;
        modelPublishStatusDTO.status = PublicStatusEnum.PUBLIC_SUCCESS.getValue();
        log.info("宽表建表数据:" + dataInfo);
        try {
            WideTableFieldConfigTaskDTO wideTableFieldConfigDTO = JSON.parseObject(dataInfo, WideTableFieldConfigTaskDTO.class);
            tbetlIncremental.addEtlIncremental(wideTableFieldConfigDTO.name);
            modelPublishStatusDTO.id = wideTableFieldConfigDTO.id;
            String createTableSql = buildWideTableSql(wideTableFieldConfigDTO);
            log.info("宽表建表语句:" + createTableSql);
            doris.dorisBuildTable(createTableSql);
            BuildNifiFlowDTO buildNifiFlowDTO = new BuildNifiFlowDTO();
            log.info("nifi配置结束,开始创建nifi流程");
            buildNifiFlowDTO.userId = wideTableFieldConfigDTO.userId;
            buildNifiFlowDTO.appId = (long) wideTableFieldConfigDTO.businessId;
            buildNifiFlowDTO.id = (long) wideTableFieldConfigDTO.id;
            buildNifiFlowDTO.type = OlapTableEnum.WIDETABLE;
            buildNifiFlowDTO.dataClassifyEnum = DataClassifyEnum.DATAMODELWIDETABLE;
            buildNifiFlowDTO.synchronousTypeEnum = SynchronousTypeEnum.PGTODORIS;
            buildNifiFlowDTO.tableName = wideTableFieldConfigDTO.name;
            String insertSql = insertWideSql(wideTableFieldConfigDTO);
            log.info("宽表插入语句:" + insertSql);
            buildNifiFlowDTO.selectSql = insertSql;
            buildNifiFlowDTO.openTransmission = true;
            pc.publishBuildNifiFlowTask(buildNifiFlowDTO);
        } catch (Exception e) {
            log.error("宽表创建失败");
            e.printStackTrace();
            modelPublishStatusDTO.status = PublicStatusEnum.PUBLIC_FAILURE.getValue();
        } finally {
            dataModelClient.updateWideTablePublishStatus(modelPublishStatusDTO);
            acke.acknowledge();
        }
    }


    public String buildWideTableSql(WideTableFieldConfigTaskDTO wideTableFieldConfigDTO) {
        String tableName = wideTableFieldConfigDTO.name.toLowerCase();
        String fistfield = wideTableFieldConfigDTO.entity.get(0).columnConfig.get(0).fieldName.toLowerCase();
        String createTableSql = "DROP TABLE IF EXISTS " + tableName + "; create table " + tableName + " (";
        String field = "";
        List<WideTableSourceTableConfigDTO> entity = wideTableFieldConfigDTO.entity;
        for (WideTableSourceTableConfigDTO wideTableSourceTableConfigDTO : entity) {
            List<WideTableSourceFieldConfigDTO> columnConfig = wideTableSourceTableConfigDTO.columnConfig;
            for (WideTableSourceFieldConfigDTO wideTableSourceFieldConfigDTO : columnConfig) {
                if ("float".equalsIgnoreCase(wideTableSourceFieldConfigDTO.fieldType)) {
                    if (wideTableSourceFieldConfigDTO.alias != null && wideTableSourceFieldConfigDTO.alias.length() > 0) {
                        field += "," + wideTableSourceFieldConfigDTO.alias.toLowerCase() + " " + wideTableSourceFieldConfigDTO.fieldType + " ";
                    } else {
                        field += "," + wideTableSourceFieldConfigDTO.fieldName.toLowerCase() + " " + wideTableSourceFieldConfigDTO.fieldType + " ";
                    }
                } else {
                    if (wideTableSourceFieldConfigDTO.alias != null && wideTableSourceFieldConfigDTO.alias.length() > 0) {
                        field += "," + wideTableSourceFieldConfigDTO.alias.toLowerCase() + " " + wideTableSourceFieldConfigDTO.fieldType + "(" + wideTableSourceFieldConfigDTO.fieldLength + ")";
                    } else {
                        field += "," + wideTableSourceFieldConfigDTO.fieldName.toLowerCase() + " " + wideTableSourceFieldConfigDTO.fieldType + "(" + wideTableSourceFieldConfigDTO.fieldLength + ")";
                    }
                }

            }
        }
        createTableSql += field.substring(1) + ") ";
        createTableSql += "ENGINE=OLAP  duplicate KEY(`" + fistfield + "`) DISTRIBUTED BY HASH(`" + fistfield + "`) BUCKETS 10 PROPERTIES(\"replication_num\" = \"1\")";
        return createTableSql;
    }

    public String insertWideSql(WideTableFieldConfigTaskDTO wideTableFieldConfigDTO) {
        String sql = wideTableFieldConfigDTO.sqlScript;
        String tableName = wideTableFieldConfigDTO.name.toLowerCase();
        String fieldSql = "";
        List<WideTableSourceTableConfigDTO> entity = wideTableFieldConfigDTO.entity;
        for (WideTableSourceTableConfigDTO wideTableSourceTableConfigDTO : entity) {
            List<WideTableSourceFieldConfigDTO> columnConfig = wideTableSourceTableConfigDTO.columnConfig;
            for (WideTableSourceFieldConfigDTO wideTableSourceFieldConfigDTO : columnConfig) {
                if (wideTableSourceFieldConfigDTO.alias != null && wideTableSourceFieldConfigDTO.alias.length() > 0) {
                    fieldSql += wideTableSourceFieldConfigDTO.alias.toLowerCase() + ",";
                } else {
                    fieldSql += wideTableSourceFieldConfigDTO.fieldName.toLowerCase() + ",";
                }
            }
        }
        fieldSql = fieldSql.substring(0, fieldSql.length() - 1);
        String insertSql = "insert into " + tableName + " (" + fieldSql + ") select  " + fieldSql + " from ( " + sql + ") dw";
        return insertSql;
    }


}
