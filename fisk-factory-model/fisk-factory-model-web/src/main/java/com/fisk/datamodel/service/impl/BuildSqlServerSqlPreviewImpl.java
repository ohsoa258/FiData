package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.datamodel.dto.businessarea.OverlayCodePreviewDTO;
import com.fisk.datamodel.service.IBuildOverlaySqlPreview;
import com.fisk.datamodel.service.strategy.BuildSqlStrategy;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@Component
@Slf4j
public class BuildSqlServerSqlPreviewImpl implements IBuildOverlaySqlPreview, InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        BuildSqlStrategy.register(DataSourceTypeEnum.SQLSERVER.getName().toUpperCase(), this);
    }

    @Override
    public Object buildStgToOdsSql(DataSourceDTO data, OverlayCodePreviewDTO dto, OverLoadCodeDTO dataModel) {
        log.info("获取ods接入参数{}", JSON.toJSONString(dto));

        String targetTableName = dataModel.config.processorConfig.targetTableName;
        List<String> stgAndTableName = getStgAndTableName(targetTableName);
        log.info("stgAntTableName集合{}", JSON.toJSONString(stgAndTableName));
        StringBuilder odsInsertSql = new StringBuilder("INSERT INTO " + targetTableName + "(");

        // 拼接ods需要插入的字段并去除重复的系统字段
        List<ModelPublishFieldDTO> odsFieldList = dataModel.config.modelPublishFieldDTOList;
        odsFieldList = odsFieldList.stream().filter(item -> !item.sourceFieldName.equals("fi_createtime")
                && !item.sourceFieldName.equals("fi_updatetime") && !item.sourceFieldName.equals("fidata_batch_code")).collect(Collectors.toList());

        // 业务主键模式
        if (dto.syncMode == syncModeTypeEnum.INCREMENT_MERGE.getValue()){
            return getMergeSql(odsFieldList, dto, stgAndTableName, targetTableName);
        }

        String tableKey = stgAndTableName.get(2);
        odsInsertSql.append(tableKey);
        odsInsertSql.append(",");
        for (ModelPublishFieldDTO field : odsFieldList){
            if (StringUtils.isEmpty(field.sourceFieldName)){
                continue;
            }
            odsInsertSql.append(field.sourceFieldName).append(",");
        }
        // 拼接ods需要插入的系统字段
        odsInsertSql.append("fi_createtime, ")
                .append("fi_updatetime, ")
                .append("fidata_batch_code, ");
        odsInsertSql.append(") SELECT ");

        // 拼接stg查询字段及字段转换
        odsInsertSql.append(tableKey);
        odsInsertSql.append(",");
        for (ModelPublishFieldDTO item : odsFieldList){
            if (StringUtils.isEmpty(item.sourceFieldName)){
                continue;
            }
            odsInsertSql.append(fieldTypeTransform(item));
            odsInsertSql.append(",");
        }
        odsInsertSql.append("fi_createtime, ")
                .append("fi_updatetime, ")
                .append("fidata_batch_code, ");

        // 拼接条件
        odsInsertSql.append("FROM ");
        odsInsertSql.append(stgAndTableName.get(0));
        // 业务时间覆盖拼接参数
        if (dataModel.config.targetDsConfig.syncMode == syncModeTypeEnum.TIME_INCREMENT.getValue()){
            TableBusinessDTO tbDto = dataModel.config.businessDTO;
            String whereStr = previewCoverCondition(tbDto, data);
            odsInsertSql.append(" ");
            odsInsertSql.append(whereStr);
        }

        // 替换拼接后的多余符号
        String sql = odsInsertSql.toString();
        sql = sql.replace(", )", ")");
        sql = sql.replace(", FROM", " FROM");
        sql += ";";

        // 判断有无更新语句
        if (!StringUtils.isEmpty(dto.getUpdateSql())){
            sql += dto.updateSql;
        }
        log.info("非业务主键sql预览：{}", JSON.toJSONString(sql));

        return sql;

    }

    private Object getMergeSql(List<ModelPublishFieldDTO> odsFieldList, OverlayCodePreviewDTO dto, List<String> stgAndTableName, String targetTableName) {
        String stgName = stgAndTableName.get(0);
        // 处理架构
        String mergeSql = "";
        // 1、拼接stg表
        mergeSql = "MERGE INTO " + targetTableName + " AS T USING " + stgName + " AS S ON( ";

        // 2、拼接主键关联条件
        // 拼接tableKey
        String tableKey = stgAndTableName.get(2);
        StringBuilder pkBuilder = new StringBuilder();
        String pkSql = "";
        List<String> collect = dto.modelPublishFieldDTOList.stream().filter(e -> e.isPrimaryKey == 1).map(e -> e.sourceFieldName).collect(Collectors.toList());
        for (String key : collect){
            pkBuilder.append(" AND T.");
            pkBuilder.append(key);
            pkBuilder.append("= S.");
            pkBuilder.append(key);
        }
        pkBuilder.append(")");
        pkSql = pkBuilder.toString();
        // 去掉前面的and
        pkSql = pkSql.substring(5);
        pkSql = pkSql.replace(",)", ")");
        // 组合
        mergeSql += pkSql;

        // 3、拼接更新语句
        StringBuilder upBuilder = new StringBuilder(" WHEN MATCHED THEN UPDATE SET ");
        String upSql = "";
        for (ModelPublishFieldDTO item : odsFieldList){
            if (StringUtils.isEmpty(item.sourceFieldName)){
                continue;
            }
            upBuilder.append("T.");
            upBuilder.append(item.sourceFieldName);
            // 类型转换
            upBuilder.append(fieldTypeTransformKey(item));
            upBuilder.append(",");
        }
        upBuilder.append("T.fi_createtime = S.fi_createtime, ")
                .append("T.fi_updatetime = S.fi_updatetime, ")
                .append("T.fi_version = S.fi_version, ")
                .append("T.fidata_batch_code = S.fidata_batch_code, ")
                .append("T.").append(tableKey).append(" = ").append("S.").append(tableKey).append(",");
        // 去除尾部的,符号
        upSql = upBuilder.toString();
        upSql = upSql.substring(0, upSql.length() - 1);
        // 组合
        mergeSql += upSql;

        // 4、拼接插入语句
        StringBuilder insBuilder = new StringBuilder(" WHEN NOT MATCHED THEN INSERT (");
        String insSql = "";
        for (ModelPublishFieldDTO item : odsFieldList){
            if (StringUtils.isEmpty(item.sourceFieldName)){
                continue;
            }
            insBuilder.append(item.sourceFieldName);
            insBuilder.append(",");
        }
        insBuilder.append("fi_createtime, ")
                .append("fi_updatetime, ")
                .append("fi_version, ")
                .append("fidata_batch_code, ")
                .append(tableKey).append(",");
        insBuilder.append(") VALUES( ");
        for (ModelPublishFieldDTO item : odsFieldList){
            // 类型转换
            if (StringUtils.isEmpty(item.sourceFieldName)){
                continue;
            }
            String str = fieldTypeTransformKey(item);
            insBuilder.append(str.replace(" = ", ""));
            insBuilder.append(",");
        }
        insBuilder.append("S.fi_createtime, ")
                .append("S.fi_updatetime, ")
                .append("S.fi_version, ")
                .append("S.fidata_batch_code, ")
                .append("S.").append(tableKey).append(",");
        insBuilder.append(")");
        // 去除尾部的,符号
        insSql = insBuilder.toString();
        insSql = insSql.replace(",)", ")");
        // 组合
        mergeSql += insSql;
        mergeSql += ";";

        log.info("业务主键sql{}", mergeSql);
        return mergeSql;
    }

    private String fieldTypeTransformKey(ModelPublishFieldDTO item) {
        log.info("字段名称-类型：{}-{}", item.sourceFieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.toUpperCase().contains("DATE") || item.fieldType.toUpperCase().contains("TIME")){
            fieldInfo = " = DATEADD(minute, cast(left(S." + item.sourceFieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.equalsIgnoreCase("nvarchar") && !item.fieldType.equalsIgnoreCase("varchar")){
            fieldInfo = " = CAST(S." + item.sourceFieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = " = S." + item.sourceFieldName;
        }
        return fieldInfo;
    }

    private List<String> getStgAndTableName(String tableName){
        log.info("获取stgAndTableName参数：{}", tableName);
        String stgTableName = "";
        String odsTableName = "";
        String tableKey = "";
        List<String> tableNames = new ArrayList<>();
        String[] split = null;
        if (tableName.contains("ods_")) {
            split = tableName.split("ods_");
        } else if (tableName.contains("help_")) {
            split = tableName.split("help_");
        } else if (tableName.contains("dim_")) {
            split = tableName.split("dim_");
        } else{
            split = tableName.split("fact_");
        }
        stgTableName = "stg_" + tableName;
        odsTableName = tableName;
        tableKey = split[1]+"key";
        tableNames.add(stgTableName);
        tableNames.add(odsTableName);
        tableNames.add(tableKey);
        log.info("getStgTableName的表名称{}", tableName);
        return tableNames;
    }

    private String fieldTypeTransform(ModelPublishFieldDTO item) {
        log.info("字段名称-类型：{}-{}", item.sourceFieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.toUpperCase().contains("DATE") || item.fieldType.toUpperCase().contains("TIME")){
            fieldInfo = "DATEADD(minute, cast(left(" + item.sourceFieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.equalsIgnoreCase("nvarchar") && !item.fieldType.equalsIgnoreCase("varchar")){
            fieldInfo = "CAST(" + item.sourceFieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = item.sourceFieldName;
        }
        return fieldInfo;
    }

    private String previewCoverCondition(TableBusinessDTO dto, DataSourceDTO dataSource) {
        log.info("拼接条件{}", JSON.toJSONString(dto));
        //数据库时间
        Integer businessDate = 0;

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(dataSource.conType);

        //查询数据库时间sql
        String timeSql = command.buildQueryTimeSql(BusinessTimeEnum.getValue(dto.businessTimeFlag));
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        Connection connection = commonDbHelper.connection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword, dataSource.conType);
        businessDate = Integer.parseInt(AbstractCommonDbHelper.executeTotalSql(timeSql, connection, "tmp"));

        StringBuilder str = new StringBuilder();
        str.append("where ");
        str.append(dto.businessTimeField + " ");

        //普通模式
        if (dto.otherLogic == 1 || businessDate < dto.businessDate) {
            str.append(dto.businessOperator + " ");
            str.append("DATEADD");
            str.append("(");
            str.append(dto.rangeDateUnit);
            str.append(",");
            str.append(dto.businessRange);
            str.append(",GETDATE());");
            return str.toString();
        }
        //高级模式
        str.append(dto.businessOperatorStandby);
        str.append("DATEADD");
        str.append("(");
        str.append(dto.rangeDateUnitStandby);
        str.append(",");
        str.append(dto.businessRangeStandby);
        str.append(",GETDATE());");
        log.info("预览业务时间覆盖,where条件:{}", str);
        return str.toString();
    }
}
