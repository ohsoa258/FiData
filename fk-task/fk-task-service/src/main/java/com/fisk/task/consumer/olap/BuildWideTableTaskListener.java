package com.fisk.task.consumer.olap;

import com.alibaba.fastjson.JSON;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableSourceFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableSourceTableConfigDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.service.doris.IDorisBuild;
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

    public void msg(String dataInfo, Acknowledgment acke) {
        try {
            WideTableFieldConfigTaskDTO wideTableFieldConfigDTO = JSON.parseObject(dataInfo, WideTableFieldConfigTaskDTO.class);
            String createTableSql = BuildWideTableSql(wideTableFieldConfigDTO);
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
            buildNifiFlowDTO.selectSql = wideTableFieldConfigDTO.sql;
            pc.publishBuildNifiFlowTask(buildNifiFlowDTO);
        } catch (Exception e) {
            log.error("宽表创建失败:" + e.getMessage());
        } finally {
            acke.acknowledge();
        }
    }


    public String BuildWideTableSql(WideTableFieldConfigTaskDTO wideTableFieldConfigDTO) {
        String tableName = "";
        String fistfield = wideTableFieldConfigDTO.entity.get(0).columnConfig.get(0).fieldName;
        String createTableSql = "DROP TABLE IF EXISTS " + tableName + "; create table " + tableName + " (";
        String field = "";
        List<WideTableSourceTableConfigDTO> entity = wideTableFieldConfigDTO.entity;
        for (WideTableSourceTableConfigDTO wideTableSourceTableConfigDTO : entity) {
            List<WideTableSourceFieldConfigDTO> columnConfig = wideTableSourceTableConfigDTO.columnConfig;
            for (WideTableSourceFieldConfigDTO wideTableSourceFieldConfigDTO : columnConfig) {
                field += "," + wideTableSourceFieldConfigDTO.fieldName + " " + wideTableSourceFieldConfigDTO.fieldType + "(" + wideTableSourceFieldConfigDTO.fieldLength + ")";
            }
        }
        createTableSql += field.substring(1) + ") ";
        createTableSql += "ENGINE=OLAP  duplicate KEY(`" + fistfield + "`) DISTRIBUTED BY HASH(`" + fistfield + "`) BUCKETS 10 PROPERTIES(\"replication_num\" = \"1\")";
        return createTableSql;
    }


}
