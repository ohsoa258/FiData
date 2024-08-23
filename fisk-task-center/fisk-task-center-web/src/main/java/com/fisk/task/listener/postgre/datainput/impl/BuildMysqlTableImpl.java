package com.fisk.task.listener.postgre.datainput.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class BuildMysqlTableImpl implements IbuildTable {

    @Resource
    TaskPgTableStructureMapper structureMapper;

    @Override
    public List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        log.info("保存版本号方法执行成功");
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS fi_tableName ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder pksql = new StringBuilder();
        StringBuilder stgSql = new StringBuilder("CREATE TABLE fi_tableName (fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        List<String> pkFields = new ArrayList<>();
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if (l.getFieldDes() == null) {
                l.setFieldDes("No field description is set");
            }
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
                stgSql.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append(l.fieldName).append(" datetime ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            } else if (l.fieldType.contains("BIT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            } else {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append("(").append(l.fieldLength).append(") ").append(" COMMENT '").append(l.getFieldDes()).append("' ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSql.append(l.fieldName).append(" TEXT,");
            }

            //是否主键
            if (l.isPrimarykey == 1) {
                pkFields.add(l.getFieldName());
                sqlFileds.append("not null ,");
            } else {
                sqlFileds.append(",");
            }

        });

        // MYSQL单一主键或复合主键均支持
        if (!CollectionUtils.isEmpty(pkFields)) {
            pksql.append(", PRIMARY KEY (");
            for (String pkField : pkFields) {
                pksql.append(pkField)
                        .append(",");

            }
            pksql.deleteCharAt(pksql.lastIndexOf(","));
            pksql.append(")");
        } else {
            pksql.append(" ");
        }


        stgSql.append("fi_updatetime DATETIME,fi_version varchar(50),fi_enableflag varchar(50)," + "fi_error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3',").append(buildPhysicalTableDTO.appAbbreviation).append("_").append(buildPhysicalTableDTO.tableName).append("key").append(" varchar(50) ");
        sqlFileds.insert(0, "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        sqlFileds.append("fi_updatetime DATETIME,fi_version varchar(50),fidata_batch_code varchar(50),")
                .append(buildPhysicalTableDTO.appAbbreviation)
                .append("_")
                .append(buildPhysicalTableDTO.tableName)
                .append("key")
                .append(" varchar(50)  DEFAULT (UUID())")
                .append(pksql);

        //补上字段括号
        sqlFileds.append(")");
        stgSql.append(")");
        sql.append(sqlFileds);
        //目标表建表语句
        String stg_sql1 = "";
        //临时stg表建表语句
        String stg_sql2 = "";
        stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;

        List<String> sqlList = new ArrayList<>();
        sqlList.add(stg_sql1);
        sqlList.add(stg_sql2);
        return sqlList;
    }

    @Override
    public String queryTableNum(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        return null;
    }

    @Override
    public String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName, BuildNifiFlowDTO buildNifiFlow) {
        return null;
    }

    @Override
    public String prepareCallSql() {
        return null;
    }

    @Override
    public String prepareCallSqlForDoris(String version, int type) {
        //新版本字段个数
        int newFieldCount = 0;
        //上个版本字段个数
        int oldFieldCount = 0;

        //新表名
        String newTblName = "";
        //原表名
        String oldTblName = "";

        //表唯一id
        int tblId = 0;
        //上一个版本该表的版本号
        String oldVersion = "";

        //修改表结构的sql
        StringBuilder sql = new StringBuilder();

        //查询新版本表详情
        LambdaQueryWrapper<TaskPgTableStructurePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskPgTableStructurePO::getVersion, version)
                .eq(TaskPgTableStructurePO::getTableType, type)
                .eq(TaskPgTableStructurePO::getValidVersion, 1);
        List<TaskPgTableStructurePO> newVersionTbl = structureMapper.selectList(wrapper);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(newVersionTbl)) {
            log.error("数仓建模-发布表修改表结构失败，tb_task_pg_table_structure表无字段！");
            throw new FkException(ResultEnum.DATA_MODEL_FIELD_NOT_EXISTS, "数仓建模-发布表修改表结构失败，tb_task_pg_table_structure表无字段！");
        }
        tblId = Integer.parseInt(newVersionTbl.get(0).getTableId());

        //查询该表拥有的所有版本 并筛选出上一个版本号
        QueryWrapper<TaskPgTableStructurePO> wrapper1 = new QueryWrapper<>();
        wrapper1.select(" DISTINCT version")
                .lambda()
                .eq(TaskPgTableStructurePO::getTableId, tblId)
                .eq(TaskPgTableStructurePO::getTableType, type)
                .eq(TaskPgTableStructurePO::getValidVersion, 1)
                .orderByDesc(TaskPgTableStructurePO::getVersion);
        List<TaskPgTableStructurePO> versions = structureMapper.selectList(wrapper1);
        for (TaskPgTableStructurePO structurePO : versions) {
            if (!Objects.equals(structurePO.getVersion(), version)) {
                oldVersion = structurePO.getVersion();
                break;
            }
        }

        //查询上一个版本号的信息
        LambdaQueryWrapper<TaskPgTableStructurePO> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(TaskPgTableStructurePO::getVersion, oldVersion)
                .eq(TaskPgTableStructurePO::getTableType, type)
                .eq(TaskPgTableStructurePO::getValidVersion, 1)
                .eq(TaskPgTableStructurePO::getTableId, tblId);
        List<TaskPgTableStructurePO> oldVersionTbl = structureMapper.selectList(wrapper2);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(oldVersionTbl)) {
            log.info("此数仓表是新发布的不存在历史结构信息...");
            return "";
        }

        //是否修改表名
        oldTblName = oldVersionTbl.get(0).getTableName();
        newTblName = newVersionTbl.get(0).getTableName();

        //找出属性改变的字段，生成sql
        //doris字段名不支持修改
        for (TaskPgTableStructurePO newPo : newVersionTbl) {
            TaskPgTableStructurePO field = null;
            //将新版本字段和老版本里面相同字段id的字段对比
            Optional<TaskPgTableStructurePO> first = oldVersionTbl.stream().filter(dto ->
                    Objects.equals(dto.getFieldId(), newPo.fieldId)).findFirst();
            if (first.isPresent()) {
                field = first.get();
            }

            //如果不存在则是新增
            if (field == null) {
                //如果新增的是主键
                if (newPo.isPrimaryKey()) {
                    sql.append("ALTER TABLE `")
                            .append(oldTblName)
                            .append("` ADD COLUMN `")
                            .append(newPo.fieldName)
                            .append("` ")
                            .append(newPo.fieldType)
                            .append(" KEY; ");
                } else {
                    //ALTER TABLE example_db.my_table
                    //ADD COLUMN new_col INT
                    sql.append("ALTER TABLE `")
                            .append(oldTblName)
                            .append("` ADD COLUMN `")
                            .append(newPo.fieldName)
                            .append("` ")
                            .append(newPo.fieldType);

                    if (StringUtils.isNotEmpty(newPo.fieldDes)) {
                        sql.append(" COMMENT ")
                                .append("'")
                                .append(newPo.fieldDes)
                                .append("'");
                    }
                    sql.append("; ");
                }
            } else {
                //如果存在，但属性有变化则是修改
                //如果字段类型不同 则修改字段类型
                if (!Objects.equals(newPo.fieldType, field.fieldType)) {
                    //如果修改的是主键
                    if (newPo.isPrimaryKey()) {
                        //ALTER TABLE example_db.my_table
                        //MODIFY COLUMN col1 BIGINT KEY DEFAULT "1" AFTER col2;
                        sql.append(" ALTER TABLE `")
                                .append(oldTblName)
                                .append("` MODIFY COLUMN `")
                                .append(field.fieldName)
                                .append("` ")
                                .append(newPo.fieldType)
                                .append(" KEY; ");
                    } else {
                        sql.append("ALTER TABLE `")
                                .append(oldTblName)
                                .append("` MODIFY COLUMN `")
                                .append(field.fieldName)
                                .append("` ")
                                .append(newPo.fieldType)
                                .append("; ");
                    }
                }

                //如果字段名称不同 则修改字段名称
                if (!Objects.equals(newPo.fieldName, field.fieldName)) {
                    sql.append(" ALTER TABLE `")
                            .append(oldTblName)
                            .append("` CHANGE COLUMN `")
                            .append(field.fieldName)
                            .append("` `")
                            .append(newPo.fieldName)
                            .append("`; ");
                }
            }
        }

        //新版本字段个数
        newFieldCount = newVersionTbl.size();
        //旧版本字段个数
        oldFieldCount = oldVersionTbl.size();

        //if true 则存在需要删除的字段
        boolean addOrDel = oldFieldCount > newFieldCount;
        if (addOrDel) {
            //找出需要删除的字段，生成sql
            for (TaskPgTableStructurePO oldPo : oldVersionTbl) {
                TaskPgTableStructurePO field = null;
                //将老版本字段和新版本里面相同字段id的字段对比
                Optional<TaskPgTableStructurePO> first = newVersionTbl.stream().filter(dto ->
                        Objects.equals(dto.getFieldId(), oldPo.fieldId)).findFirst();
                if (first.isPresent()) {
                    field = first.get();
                }
                if (field != null) {
                    continue;
                }

                //删除字段
                //ALTER TABLE example_db.my_table
                //DROP COLUMN col2
                sql.append("ALTER TABLE `")
                        .append(oldTblName)
                        .append("` DROP COLUMN `")
                        .append(oldPo.fieldName)
                        .append("` ; ");

            }
        }

        //如果表名不相同，则修改表名
        if (!Objects.equals(oldTblName, newTblName)) {
            //ALTER TABLE table1 RENAME table2;
            String oldTableKeyName = oldTblName.substring(oldTblName.indexOf("_") + 1) + "key";
            String newTableKeyName = newTblName.substring(newTblName.indexOf("_") + 1) + "key";

            //改表名的同时，修改系统key字段名
            sql.append(" ALTER TABLE `")
                    .append(oldTblName)
                    .append("` CHANGE COLUMN `")
                    .append(oldTableKeyName)
                    .append("` `")
                    .append(newTableKeyName)
                    .append("` varchar(50); ");
            sql.append("ALTER TABLE `")
                    .append(oldTblName)
                    .append("` RENAME `")
                    .append(newTblName)
                    .append("`; ");
        }
        return sql.toString();
    }

    @Override
    public String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        List<String> stgAndTableName = getStgAndTableName(config.processorConfig.targetTableName);
        if (config.processorConfig.targetTableName.contains("\\.")) {
        }
        String querySql = "";
        if (Objects.equals(dto.type, OlapTableEnum.WIDETABLE) || Objects.equals(dto.type, OlapTableEnum.KPI)) {
//            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, 0 as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType;";
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
//                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + stgAndTableName.get(1) + " where  fidata_batch_code='${fidata_batch_code}'";
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, 0 as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType;";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, 0 as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType;";
            }

        }
        return querySql;
    }

    @Override
    public String queryMdmNumbersField(BuildMdmNifiFlowDTO dto, AccessMdmConfigDTO config, String groupId) {
        return null;
    }

    @Override
    public String delMdmField(BuildMdmNifiFlowDTO dto, AccessMdmConfigDTO config, String groupId) {
        return null;
    }

    @Override
    public List<String> getStgAndTableName(String tableName) {
        return null;
    }

    @Override
    public List<String> buildDwStgAndOdsTable(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public String queryNumbersFieldForTableServer(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        String querySql = "";
        String targetTableName = config.processorConfig.targetTableName;
        if (targetTableName.indexOf(".") > 0) {
            //去掉.前面的部分
            targetTableName = targetTableName.substring(targetTableName.indexOf('.') + 1);
        }
        querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') AS end_time," +
                "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + targetTableName;

        return querySql;
    }

    @Override
    public String getTotalSql(String sql, SynchronousTypeEnum synchronousTypeEnum) {
        return null;
    }

    @Override
    public void fieldFormatModification(DataAccessConfigDTO dto) {

    }

    @Override
    public String getEsqlAutoCommit() {
        return null;
    }

    @Override
    public List<String> buildDorisDimTables(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisDimTablesWithoutSystemFields(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisFactTables(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisFactTablesWithoutSystemFields(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisaAggregateTables(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }
}
