package com.fisk.task.listener.postgre.datainput.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class BuildDorisTableImpl implements IbuildTable {

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
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
                stgSql.append(l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append(l.fieldName + " datetime ");
            } else if (l.fieldType.contains("BIT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSql.append(l.fieldName + " varchar(4000),");
            }
            if (l.isPrimarykey == 1) {
                pksql.append("" + l.fieldName + ",");
                sqlFileds.append("not null ,");
            } else {
                sqlFileds.append(",");
            }

        });
        stgSql.append("fi_updatetime DATETIME,fi_version varchar(50),fi_enableflag varchar(50)," + "fi_error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3'," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50)");
        sqlFileds.insert(0, "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        sqlFileds.append("fi_updatetime DATETIME,fi_version varchar(50),fidata_batch_code varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50)");
        String havePk = pksql.toString();
        sqlFileds.append(") ENGINE=OLAP DISTRIBUTED BY HASH(fi_createtime) BUCKETS 10 PROPERTIES(\"replication_num\" =\"1\");");
        stgSql.append(") ENGINE=OLAP DISTRIBUTED BY HASH(fi_createtime) BUCKETS 10 PROPERTIES(\"replication_num\" =\"1\");");
        sql.append(sqlFileds);
        String stg_sql1 = "";
        String stg_sql2 = "";
        String odsTableName = "";
        if (buildPhysicalTableDTO.whetherSchema) {
            odsTableName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;
        } else {
            odsTableName = "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;
        }
        List<String> sqlList = new ArrayList<>();
        //alter table Date add constraint PK_Date primary key(ID)
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
        if (CollectionUtils.isEmpty(newVersionTbl)) {
            log.error("数仓建模-发布表修改表结构失败，tb_task_pg_table_structure表无字段！");
            throw new FkException(ResultEnum.DATA_MODEL_FIELD_NOT_EXISTS, "数仓建模-发布表修改表结构失败，tb_task_pg_table_structure表无字段！");
        }
        tblId = Integer.parseInt(newVersionTbl.get(0).getTableId());

        //查询该表拥有的所有版本 并筛选出上一个版本号
        QueryWrapper<TaskPgTableStructurePO> wrapper1 = new QueryWrapper<>();
        wrapper1.select(" DISTINCT version")
                .lambda()
                .eq(TaskPgTableStructurePO::getTableId, tblId)
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
        if (CollectionUtils.isEmpty(oldVersionTbl)) {
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
                            .append(newPo.fieldType)
                            .append("; ");
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
                            .append("` RENAME COLUMN `")
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
                Optional<TaskPgTableStructurePO> first = oldVersionTbl.stream().filter(dto ->
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
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + stgAndTableName.get(1) + " where  fidata_batch_code='${fidata_batch_code}'";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName + " where fidata_batch_code='${fidata_batch_code}'";
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
        return TableNameGenerateUtils.getStgAndTableName(tableName);
    }

    @Override
    public List<String> buildDwStgAndOdsTable(ModelPublishTableDTO modelPublishTableDTO) {
        List<String> sqlList = new ArrayList<>();
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        String tableName = modelPublishTableDTO.tableName;
        String tablePk = "";
        // 前缀问题
        tablePk = tableName.substring(tableName.indexOf("_") + 1) + "key";
        StringBuilder pksql = new StringBuilder("UNIQUE KEY ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:" + fieldList);
        fieldList.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
                stgSqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append(l.fieldEnName).append(" datetime, ");
            } else {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append("(").append(l.fieldLength).append("), ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSqlFileds.append("" + l.fieldEnName + " varchar(4000),");
            }
            if (l.isPrimaryKey == 1) {
                pksql.append("").append(l.fieldEnName).append(" ,");
            }

        });

        String sql1 = "CREATE TABLE " + modelPublishTableDTO.tableName + " ( ";
        //String associatedKey = associatedConditions(fieldList);
        String associatedKey = "";
        String sql2 = sqlFileds.toString() + associatedKey;
        sql2 += "fi_createtime DATETIME,fi_updatetime DATETIME";
        sql2 += ",fidata_batch_code varchar(50)";
        String sql3 = "";
        if (Objects.equals("", sql3)) {
            sql1 += sql2;
        } else {
            sql1 += sql2 + sql3;
        }
        sql1 += ") ";
        if (modelPublishTableDTO.synMode == 3) {
            String havePk = pksql.toString();
            if (havePk.length() != 14) {
                sql1 += havePk.substring(0, havePk.length() - 1) + ")";
            }
        }

        //创建表
        log.info("pg_dw建表语句" + sql1);
        //String stgTable = sql1.replaceFirst(tableName, "stg_" + tableName);
        String stgTable = "DROP TABLE IF EXISTS " + modelPublishTableDTO.prefixTempName + tableName + "; CREATE TABLE " + modelPublishTableDTO.prefixTempName + tableName + " (" + tablePk + " BIGINT IDENTITY(1,1) NOT NULL ," + stgSqlFileds.toString() + associatedKey + "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,fi_updatetime DATETIME,fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3');";
        //stgTable += "create index " + tableName + "enableflagsy on stg_" + tableName + " (fi_enableflag);";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        return sqlList;
    }

    @Override
    public String queryNumbersFieldForTableServer(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        String querySql = "";
        String targetTableName = config.processorConfig.targetTableName;
        if (targetTableName.indexOf(".") > 0) {
            //去掉.前面的部分
            targetTableName = targetTableName.substring(targetTableName.indexOf('.') + 1);
        }
        querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') AS end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + targetTableName;

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

    /**
     * 创建建模dim表 - doris主键模型
     *
     * @param modelPublishTableDTO modelPublishTableDTO
     * @return
     */
    @Override
    public List<String> buildDorisDimTables(ModelPublishTableDTO modelPublishTableDTO) {
        List<String> sqlList = new ArrayList<>();
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        String tableName = modelPublishTableDTO.tableName;
        String tablePk = "";
        // 前缀问题
        tablePk = tableName.substring(tableName.indexOf("_") + 1) + "key";
        StringBuilder pksql = new StringBuilder("UNIQUE KEY ( ");
        //主键字段
        StringBuilder pkName = new StringBuilder();
        //doris分区字段
        StringBuilder partitionName = new StringBuilder();
        //分区类型 RANGE 或 LIST
        StringBuilder partitionType = new StringBuilder();
        //分区具体值（分区个数和分区逻辑）
        StringBuilder partitionValues = new StringBuilder();
        //doris分桶字段
        StringBuilder distributedName = new StringBuilder();
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:" + fieldList);
        //主键模型 重新排序
        //主键放在前面
        fieldList.sort((o1, o2) -> {
            if (o1.isBusinessKey == 1 && o2.isBusinessKey == 0) {
                return -1; // o1排在o2前面
            } else if (o1.isBusinessKey == 0 && o2.isBusinessKey == 1) {
                return 1; // o2排在o1前面
            } else {
                return 0; // 保持原有顺序
            }
        });
        log.info("doris主键模型重新排序后的字段信息:" + fieldList);

        //获取doris分区类型 RANGE 或 LIST
        if (CollectionUtils.isNotEmpty(fieldList)) {
            if (fieldList.get(0).getDorisPartitionType() != null) {
                partitionType = new StringBuilder(fieldList.get(0).getDorisPartitionType());
            } else {
                partitionType = new StringBuilder("RANGE");
            }
        }

        fieldList.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
                stgSqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(",");
            } else if (l.fieldType.equalsIgnoreCase("DATE")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.equalsIgnoreCase("TIME")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(" datetime, ");
            } else {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append("(").append(l.fieldLength).append("), ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSqlFileds.append("`").append(l.fieldEnName).append("` varchar(4000),");
            }
            //主键字段
            if (l.isBusinessKey == 1) {
                pksql.append("`").append(l.fieldEnName).append("` ,");
                pkName.append("`").
                        append(l.fieldEnName)
                        .append("` ,");
            }
            //doris分区字段
            if (l.isPartitionKey == 1) {
                //分区字段sql
                partitionName.append("(`").
                        append(l.fieldEnName)
                        .append("`) ,");
                //分区值sql
                partitionValues.append(l.dorisPartitionValues);

            }
            //doris分桶字段
            if (l.isDistributedKey == 1) {
                //分桶字段sql
                distributedName.append("`").
                        append(l.fieldEnName)
                        .append("` ,");
            }

        });
        //删掉多余逗号
        pkName.deleteCharAt(pkName.lastIndexOf(","));
        if (partitionName.length() > 0) partitionName.deleteCharAt(pkName.lastIndexOf(","));
        if (distributedName.length() > 0) distributedName.deleteCharAt(pkName.lastIndexOf(","));

        String sql1 = "CREATE TABLE IF NOT EXISTS `" + modelPublishTableDTO.tableName + "` ( ";
        //String associatedKey = associatedConditions(fieldList);
        String associatedKey = "";
        String sql2 = sqlFileds + associatedKey;
        sql2 += ("`" + tablePk + "` varchar(50),fi_createtime DATETIME,fi_updatetime DATETIME");
        sql2 += ",fidata_batch_code varchar(50)";
        String sql3 = "";
        if (Objects.equals("", sql3)) {
            sql1 += sql2;
        } else {
            sql1 += sql2 + sql3;
        }
        sql1 += ") ";

        //doris分区列
        String partition = "";
        String distributed = "";
        //todo:如果前端没有选择分区列，则默认一个分区 如果选择了分区列则按分区列分区
        //doris建表语句中 没有指定分区列的话 默认就是一个分区
        if (partitionName.length() > 0) {
            partition = " PARTITION BY " + partitionType.toString() + partitionName.toString() + " (" + partitionValues + ")";
        }

        //todo：分桶列同理 如果前端选择了分桶列，则按前端选择的来 如果没有选择则按默认系统key分桶
        if (distributedName.length() > 0) {
            distributed = distributedName.toString();
        } else {
            distributed = String.valueOf(pkName);
        }

        // UNIQUE KEY
        String havePk = pksql.toString();
        if (havePk.length() != 14) {
            havePk = havePk.substring(0, havePk.length() - 1) + ")";
        }
        sql1 += havePk;

        sql1 += partition + " DISTRIBUTED BY HASH(" + distributed + ") BUCKETS 10 " +
                //副本数为1
                "PROPERTIES (" + "    \"replication_num\" = \"1\"" + ");";

        //创建表
        log.info("pg_dw建表语句" + sql1);
        String stgTable = "DROP TABLE IF EXISTS `" + modelPublishTableDTO.prefixTempName + tableName + "` FORCE; CREATE TABLE `" + modelPublishTableDTO.prefixTempName + tableName + "` ( " + stgSqlFileds + associatedKey + "`" + tablePk + "` varchar(50)," + "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,fi_updatetime DATETIME,fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3') " +
                havePk +
                //hash分桶
                " DISTRIBUTED BY HASH(" + pkName + ") BUCKETS 10 " +
                //副本数为1
                "PROPERTIES (" + "    \"replication_num\" = \"1\"" + ");";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        return sqlList;
    }

    /**
     * 创建建模fact表 - doris冗余模型
     *
     * @param modelPublishTableDTO modelPublishTableDTO
     * @return
     */
    @Override
    public List<String> buildDorisFactTables(ModelPublishTableDTO modelPublishTableDTO) {
        List<String> sqlList = new ArrayList<>();
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        String tableName = modelPublishTableDTO.tableName;
        String tablePk = "";
        // 前缀问题
        tablePk = tableName.substring(tableName.indexOf("_") + 1) + "key";
        StringBuilder pksql = new StringBuilder("DUPLICATE KEY ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:" + fieldList);
        fieldList.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
                stgSqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(",");
            } else if (l.fieldType.equalsIgnoreCase("DATE")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.equalsIgnoreCase("TIME")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append("`").append(l.fieldEnName).append("` datetime, ");
            } else {
                sqlFileds.append("`").append(l.fieldEnName).append("` ").append(l.fieldType.toLowerCase()).append("(").append(l.fieldLength).append("), ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSqlFileds.append("`").append(l.fieldEnName).append("` varchar(4000),");
            }
            if (l.isBusinessKey == 1) {
                pksql.append(l.fieldEnName).append(" ,");
            }

        });

        String sql1 = "CREATE TABLE  IF NOT EXISTS `" + modelPublishTableDTO.tableName + "` (";
        //String associatedKey = associatedConditions(fieldList);
        String associatedKey = "";
        String sql2 = sqlFileds + associatedKey;
        sql2 += ("`" + tablePk + "` varchar(50),fi_createtime DATETIME,fi_updatetime DATETIME");
        sql2 += ",fidata_batch_code varchar(50)";
        String sql3 = "";
        if (Objects.equals("", sql3)) {
            sql1 += sql2;
        } else {
            sql1 += sql2 + sql3;
        }
        sql1 += ") ";
        if (modelPublishTableDTO.synMode == 3
                || modelPublishTableDTO.synMode == 5) {
            String havePk = pksql.toString();
            if (havePk.length() != 14) {
                sql1 += havePk.substring(0, havePk.length() - 1) + ")";
            }
        }

        sql1 = sql1 + "DISTRIBUTED BY HASH(" + tablePk + ") BUCKETS 10 " +
                //副本数为1
                "PROPERTIES (" + "    \"replication_num\" = \"1\"" + ");";

        //创建表
        log.info("pg_dw建表语句" + sql1);
        String stgTable = "DROP TABLE IF EXISTS `" + modelPublishTableDTO.prefixTempName + tableName + "` FORCE; CREATE TABLE `" + modelPublishTableDTO.prefixTempName + tableName + "` (`" + tablePk + "` BIGINT," + stgSqlFileds + associatedKey + "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,fi_updatetime DATETIME,fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3') " +
                //hash分桶
                "DISTRIBUTED BY HASH(" + tablePk + ") BUCKETS 10 " +
                //副本数为1
                "PROPERTIES (" + "    \"replication_num\" = \"1\"" + ");";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        return sqlList;
    }
}
