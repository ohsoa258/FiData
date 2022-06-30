package com.fisk.task.listener.doris;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author:yhxu CreateTime: 2021年07月05日18:33:46
 * Description:
 */
@Component
@Slf4j
public class BuildDorisTaskListener {

    @Resource
    IDorisBuild doris;
    @Resource
    DataAccessClient dc;


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            log.info("执行Doris");
            log.info("dataInfo:" + dataInfo);
            AtlasEntityQueryDTO inpData = JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
            ResultEntity<AtlasEntityDbTableColumnDTO> queryRes = dc.getAtlasBuildTableAndColumn(Long.parseLong(inpData.dbId), Long.parseLong(inpData.appId));
            log.info("queryRes:" + JSON.toJSONString(queryRes));
            AtlasEntityDbTableColumnDTO dto = JSON.parseObject(JSON.toJSONString(queryRes.data), AtlasEntityDbTableColumnDTO.class);
            log.info("ae:" + JSON.toJSONString(dto));
            String tableName = dto.tableName;
            String stg_table = dto.appAbbreviation + "_stg_" + dto.tableName;
            String ods_table = dto.appAbbreviation + "_ods_" + dto.tableName;
            StringBuilder sql = new StringBuilder();
            boolean Duplicate = true;
            for (int i = 0; i < dto.columns.size(); i++) {
                if (dto.columns.get(i).isKey.equals("1")) {
                    Duplicate = false;
                }
            }
            if (Duplicate) {
                //冗余模型建表
                sql.append("CREATE TABLE tableName");
                sql.append("(");
                StringBuilder sqlFileds = new StringBuilder();
                StringBuilder sqlDuplicate = new StringBuilder("DUPLICATE KEY(");
                StringBuilder sqlSelectStrBuild = new StringBuilder();
                StringBuilder sqlDistributed = new StringBuilder("DISTRIBUTED BY HASH(");
                sqlDistributed.append("doris_custom_data_flag");
                dto.columns.forEach((l) -> {
                    sqlFileds.append(l.columnName + " " + l.dataType + " comment " + "'" + l.comment + "' ,");
                    sqlDuplicate.append(l.columnName + ",");
                    sqlSelectStrBuild.append(l.columnName + ",");
                });
                sqlFileds.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' ,");
                sqlDuplicate.append(")");
                sqlDistributed.append(") BUCKETS 10");
                String DuplicateStr = sqlDuplicate.toString();
                DuplicateStr = DuplicateStr.substring(0, DuplicateStr.lastIndexOf(",")) + ")";
                sql.append(sqlFileds.append(" doris_custom_data_flag varchar(2) DEFAULT \"1\" comment '系统字段，默认分桶'").toString());
                sql.append(")");
                sql.append(DuplicateStr);
                sql.append(sqlDistributed.toString());
                sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
                //聚合模型建表
//            sql.append("CREATE TABLE tableName");
//            sql.append("(");
//            StringBuilder sqlFileds = new StringBuilder();
//            StringBuilder sqlAggregate = new StringBuilder("AGGREGATE KEY(");
//            StringBuilder sqlSelectStrBuild = new StringBuilder();
//            StringBuilder sqlDistributed = new StringBuilder("DISTRIBUTED BY HASH(");
//            sqlDistributed.append("doris_custom_data_flag");
//            dto.columns.forEach((l) -> {
//                sqlFileds.append(l.columnName + " " + l.dataType + " comment " + "'" + l.comment + "' ,");
//                sqlAggregate.append(l.columnName + ",");
//                sqlSelectStrBuild.append(l.columnName + ",");
//            });
//            sqlFileds.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' ,");
//            sqlAggregate.append("fk_doris_increment_code ,doris_custom_data_flag ,");
//            sqlDistributed.append(") BUCKETS 10");
//            String aggregateStr = sqlAggregate.toString();
//            aggregateStr = aggregateStr.substring(0, aggregateStr.lastIndexOf(",")) + ")";
//            sql.append(sqlFileds.append(" doris_custom_data_flag varchar(2) DEFAULT \"1\" comment '系统字段，默认分桶'").toString());
//            sql.append(")");
//            sql.append(aggregateStr);
//            sql.append(sqlDistributed.toString());
//            sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
            } else {
                sql.append("CREATE TABLE tableName");
                sql.append("(");
                StringBuilder sqlFileds_build = new StringBuilder();
                StringBuilder sqlUnique_build = new StringBuilder("ENGINE=OLAP  UNIQUE KEY(");
                StringBuilder sqlDistributed_build = new StringBuilder("DISTRIBUTED BY HASH(");
                dto.columns.forEach((l) -> {
                    if (l.isKey.equals("1")) {
                        sqlUnique_build.append(l.columnName + ",");
                        sqlDistributed_build.append(l.columnName + ",");
                    }
                    sqlFileds_build.append(l.columnName + " " + l.dataType + " comment " + "'" + l.comment + "' ,");
                });
                sqlFileds_build.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' )");
                String sqlFileds = sqlFileds_build.toString();
                //sqlFileds = sqlFileds.substring(0, sqlFileds.lastIndexOf(",")) + ")";
                String sqlUnique = sqlUnique_build.toString();
                sqlUnique = sqlUnique.substring(0, sqlUnique.lastIndexOf(",")) + ")";
                String sqlDistributed = sqlDistributed_build.toString();
                sqlDistributed = sqlDistributed.substring(0, sqlDistributed.lastIndexOf(",")) + ") BUCKETS 10";
                sql.append(sqlFileds).append(sqlUnique).append(sqlDistributed).append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
            }
            String stg_sql = sql.toString().replace("tableName", stg_table);
            String ods_sql = sql.toString().replace("tableName", ods_table);
            doris.dorisBuildTable(stg_sql);
            doris.dorisBuildTable(ods_sql);
            log.info("【DORIS_STG】" + stg_sql);
            log.info("【DORIS_ODS】" + ods_sql);
            log.info("Doris执行结束");
            return resultEnum;
        } catch (Exception e) {
            resultEnum = ResultEnum.ERROR;
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return resultEnum;
        } finally {
            acke.acknowledge();
        }
    }

}
