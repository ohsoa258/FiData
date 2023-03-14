package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.dataaccess.service.IBuildOverlaySqlPreview;
import com.fisk.dataaccess.service.factory.BuildSqlFactory;
import com.fisk.datamodel.dto.TableStructDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Component
@Slf4j
public class BuildSqlServerSqlImpl implements IBuildOverlaySqlPreview, InitializingBean {
    @Override
    public Object buildStgToOdsSql(DataSourceDTO dataSourceDTO, OverLoadCodeDTO dataModel, TableAccessPO tableAccessPO, AppRegistrationPO appRegistrationPO) {
        log.info("获取ods接入参数{}", JSON.toJSONString(dataModel));

        String targetTableName = dataModel.config.processorConfig.targetTableName;
        List<String> stgAndTableName = getStgAndTableName(targetTableName, appRegistrationPO);

        // 获取字段
        List<TableStructDTO> odsFieldList = getSqlServerFieldInfo(dataSourceDTO, targetTableName, appRegistrationPO);

        // 1、业务主键类型
        if (dataModel.config.targetDsConfig.syncMode == syncModeTypeEnum.INCREMENT_MERGE.getValue()){
            return getMergeSql(odsFieldList, dataModel, stgAndTableName, appRegistrationPO);
        }

        // 2、追加、全量
        // 处理架构
        String odsSql = "";
        String schema = "dbo";
        if (appRegistrationPO.whetherSchema){
            schema = appRegistrationPO.appAbbreviation;
        }
        StringBuilder odsInsertSql = new StringBuilder("INSERT INTO " + schema + "." + stgAndTableName.get(1) + "(");

        // 拼接ods需要插入哪些字段
        for (TableStructDTO item : odsFieldList){
            odsInsertSql.append(item.fieldName).append(",");
        }
        odsInsertSql.append(") SELECT ");

        // 拼接stg需要查询的字段及字段转换
        for (TableStructDTO item : odsFieldList){
            odsInsertSql.append(fieldTypeTransform(item)).append(",");
        }

        // 拼接stg查询字段
        odsInsertSql.append(" FROM ").append(schema).append(".").append(stgAndTableName.get(0));

        // 3、业务时间拼接条件
        if (dataModel.config.targetDsConfig.syncMode == syncModeTypeEnum.TIME_INCREMENT.getValue()){
            TableBusinessDTO tbDto = dataModel.config.businessDTO;
            String whereStr = previewCoverCondition(tbDto, dataSourceDTO);
            odsInsertSql.append(" ");
            odsInsertSql.append(whereStr);
        }
        odsSql = odsInsertSql.toString();
        odsSql = odsSql.replace(",)", ")");
        odsSql = odsSql.replace(", FROM", " FROM");
        log.info("stgToOds预览SQL语句，{}", JSON.toJSONString(odsSql));
        return odsSql;
    }

    private Object getMergeSql(List<TableStructDTO> odsFieldList, OverLoadCodeDTO dataModel, List<String> stgAndTableName, AppRegistrationPO appRegistrationPO) {
        String targetTableName = dataModel.config.processorConfig.targetTableName;
        String stgName = stgAndTableName.get(0);
        // 处理架构
        String schema = "dbo";
        if(appRegistrationPO.whetherSchema){
            schema = appRegistrationPO.appAbbreviation;
        }
        String mergeSql = "";
        // 1、拼接stg表
        mergeSql = "MERGE INTO " + schema + "." + targetTableName + " AS T USING " + schema + "." + stgName + " AS S ON( ";

        // 2、拼接主键关联条件
        StringBuilder pkBuilder = new StringBuilder();
        String pkSql = "";
        List<String> collect = dataModel.config.modelPublishFieldDTOList.stream().filter(e -> e.isPrimaryKey == 1).map(e -> e.fieldEnName).collect(Collectors.toList());
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
        for (TableStructDTO item : odsFieldList){
            upBuilder.append("T.");
            upBuilder.append(item.fieldName);
            // 类型转换
            upBuilder.append(fieldTypeTransformKey(item));
            upBuilder.append(",");
        }
        // 去除尾部的,符号
        upSql = upBuilder.toString();
        upSql = upSql.substring(0, upSql.length() - 1);
        // 组合
        mergeSql += upSql;

        // 4、拼接插入语句
        StringBuilder insBuilder = new StringBuilder(" WHEN NOT MATCHED THEN INSERT (");
        String insSql = "";
        for (TableStructDTO item : odsFieldList){
            insBuilder.append(item.fieldName);
            insBuilder.append(",");
        }
        insBuilder.append(") VALUES( ");
        for (TableStructDTO item : odsFieldList){
            // 类型转换
            String str = fieldTypeTransformKey(item);
            insBuilder.append(str.replace(" = ", ""));
            insBuilder.append(",");
        }
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

    private String fieldTypeTransformKey(TableStructDTO item) {
        log.info("字段名称-类型：{}-{}", item.fieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.contains("date") || item.fieldType.contains("time") || item.fieldType.contains("time")){
            fieldInfo = " = DATEADD(minute, cast(left(S." + item.fieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.equals("nvarchar") && !item.fieldType.equals("varchar")){
            fieldInfo = " = CAST(S." + item.fieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = " = S." + item.fieldName;
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

    private String fieldTypeTransform(TableStructDTO item) {
        log.info("字段名称-类型：{}-{}", item.fieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.contains("date") || item.fieldType.contains("time")){
            fieldInfo = "DATEADD(minute, cast(left(" + item.fieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.equals("nvarchar") && !item.fieldType.equals("varchar")){
            fieldInfo = "CAST(" + item.fieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = item.fieldName;
        }
        return fieldInfo;
    }

    private List<TableStructDTO> getSqlServerFieldInfo(DataSourceDTO data, String tableName, AppRegistrationPO appRegistrationPO){
        if (appRegistrationPO.getWhetherSchema()){
            tableName = appRegistrationPO.getAppAbbreviation() + "." + tableName;
        }
        // 获取ods表字段信息
        String selOdsFieldSql = "SELECT name AS column_name,TYPE_NAME(system_type_id) AS column_type,ROW_NUMBER() OVER(ORDER BY system_type_id ) AS rid " +
                "FROM sys.columns WHERE object_id = OBJECT_ID('" + tableName + "')";
        List<TableStructDTO> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(data.conStr, data.conAccount, data.conPassword);
             Statement st = connection.createStatement();
             ResultSet res = st.executeQuery(selOdsFieldSql)){
            while (res.next()){
                TableStructDTO dto = new TableStructDTO();
                dto.setFieldName(res.getString("column_name"));
                dto.setFieldType(res.getString("column_type"));
                dto.setRid(res.getInt("rid"));
                list.add(dto);
            }
        }catch (Exception e){
            log.info("获取表字段数据错误");
            e.printStackTrace();
        }
        if (CollectionUtils.isEmpty(list)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "表字段信息获取失败");
        }
        log.info("字段数据{}", JSON.toJSONString(list));
        return list;
    }

    private List<String> getStgAndTableName(String tableName, AppRegistrationPO appRegistrationPO){
        log.info("获取stgAndTableName参数：{}", tableName);
        String stgTableName = "";
        String odsTableName = "";
        String tableKey = "";
        List<String> tableNames = new ArrayList<>();
        if (tableName.contains("ods_")) {
            String[] split = tableName.split("ods_");
            stgTableName = "stg_" + split[1];
            odsTableName = tableName;
            tableKey = split[1]+"key";
            tableNames.add(stgTableName);
            tableNames.add(odsTableName);
            tableNames.add(tableKey);
        } else {
            stgTableName = "stg_" + tableName;
            odsTableName = tableName;
            tableKey = appRegistrationPO.getAppAbbreviation() + "_" + tableName + "key";
            tableNames.add(stgTableName);
            tableNames.add(odsTableName);
            tableNames.add(tableKey);
        }
        log.info("getStgTableName的表名称{}", tableName);
        return tableNames;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BuildSqlFactory.register(DataSourceTypeEnum.SQLSERVER.getName().toUpperCase(), this);
    }
}

