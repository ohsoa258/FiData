package com.fisk.task.listener.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.utils.PostgreHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
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
                    ;
                    inputData.tableList.forEach((t) -> {
                        buildDelSqlStr.append("stg_" + t.tableName + ",ods_" + t.tableName + ", ");
                        atlasEntityId.add(t.tableAtlasId);
                        conditionHashMap.put("table_name", "stg_" + t.tableName);
                        taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                        conditionHashMap.put("table_name", "ods_" + t.tableName);
                        taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                    });
                    String delSqlStr = buildDelSqlStr.toString();
                    delSqlStr = delSqlStr.substring(0, delSqlStr.lastIndexOf(",")) + " ;";
                    PostgreHelper.postgreExecuteSql(delSqlStr.toLowerCase(), BusinessTypeEnum.DATAINPUT);
                    log.info("delsql:" + delSqlStr);
                    log.info("执行pg delete table 完成");
                } else {
                    inputData.tableList.forEach((t) -> {
                        buildDelSqlStr.append(t.tableName + ", ");
                        buildDelSqlStr.append("stg_" + t.tableName + ", ");
                        conditionHashMap.put("table_name", t.tableName);
                        taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                    });
                    String delSqlStr = buildDelSqlStr.toString();
                    delSqlStr = delSqlStr.substring(0, delSqlStr.lastIndexOf(",")) + " ;";
                    PostgreHelper.postgreExecuteSql(delSqlStr.toLowerCase(), BusinessTypeEnum.DATAMODEL);
                    doris.dorisBuildTable(delSqlStr);
                    log.info("delsql:" + delSqlStr);
                    log.info("执行pg delete table 完成");
                }
            }
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            acke.acknowledge();
        }

    }
}
