package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.dataaccess.dto.access.OverlayCodePreviewAccessDTO;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.dataaccess.service.IBuildOverlaySqlPreview;
import com.fisk.dataaccess.service.factory.BuildSqlFactory;
import com.fisk.datamodel.dto.TableStructDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
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
    public Object buildStgToOdsSql(DataSourceDTO dataSourceDTO, OverlayCodePreviewAccessDTO dto, TableAccessPO tableAccessPO,
                                   AppRegistrationPO appRegistrationPO, String targetTableName){
        log.info("获取ods接入参数{}", JSON.toJSONString(dto));

        List<String> stgAndTableName = getStgAndTableName(targetTableName, appRegistrationPO);

        // 获取字段
        List<TableFieldsDTO> odsFieldList = dto.modelPublishFieldDTOList;

        // 1、业务主键类型
        if (dto.syncMode == syncModeTypeEnum.INCREMENT_MERGE.getValue()){
            return getMergeSql(odsFieldList, dto, stgAndTableName, appRegistrationPO, targetTableName);
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
        for (TableFieldsDTO item : odsFieldList){
            if (StringUtils.isEmpty(item.sourceFieldName)){
                continue;
            }
            odsInsertSql.append(item.sourceFieldName).append(",");
        }
        // 拼接tableKey
        String tableKey = appRegistrationPO.appAbbreviation + "_" + stgAndTableName.get(1) + "key";
        odsInsertSql.append("fi_createtime, ")
                .append("fi_updatetime, ")
                .append("fi_version, ")
                .append("fidata_batch_code, ")
                .append(tableKey).append(",")
                .append(") SELECT ");

        // 拼接stg需要查询的字段及字段转换
        for (TableFieldsDTO item : odsFieldList){
            if (StringUtils.isEmpty(item.sourceFieldName)){
                continue;
            }
            odsInsertSql.append(fieldTypeTransform(item)).append(",");
        }
        odsInsertSql.append("fi_createtime, ")
                .append("fi_updatetime, ")
                .append("fi_version, ")
                .append("fidata_batch_code, ")
                .append(tableKey).append(",");

        // 拼接stg查询字段
        odsInsertSql.append(" FROM ").append(schema).append(".").append(stgAndTableName.get(0));

        // 3、业务时间拼接条件
        if (dto.syncMode == syncModeTypeEnum.TIME_INCREMENT.getValue()){
            TableBusinessDTO tbDto = dto.tableBusiness;
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

    private Object getMergeSql(List<TableFieldsDTO> odsFieldList, OverlayCodePreviewAccessDTO dto, List<String> stgAndTableName, AppRegistrationPO appRegistrationPO, String targetTableName) {
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
        // 拼接tableKey
        String tableKey = appRegistrationPO.appAbbreviation + "_" + stgAndTableName.get(1) + "key";
        StringBuilder pkBuilder = new StringBuilder();
        String pkSql = "";
        List<String> collect = dto.modelPublishFieldDTOList.stream().filter(e -> e.isPrimarykey == 1).map(e -> e.sourceFieldName).collect(Collectors.toList());
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
        for (TableFieldsDTO item : odsFieldList){
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
        for (TableFieldsDTO item : odsFieldList){
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
        for (TableFieldsDTO item : odsFieldList){
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

    private String fieldTypeTransformKey(TableFieldsDTO item) {
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
            str.append(",GETDATE()) AND fi_enableflag='Y';");
            return str.toString();
        }
        //高级模式
        str.append(dto.businessOperatorStandby);
        str.append("DATEADD");
        str.append("(");
        str.append(dto.rangeDateUnitStandby);
        str.append(",");
        str.append(dto.businessRangeStandby);
        str.append(",GETDATE()) AND fi_enableflag='Y';");
        log.info("预览业务时间覆盖,where条件:{}", str);
        return str.toString();
    }

    private String fieldTypeTransform(TableFieldsDTO item) {
        log.info("字段名称-类型：{}-{}", item.sourceFieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.toUpperCase().contains("DATE") || item.fieldType.toUpperCase().contains("TIME")){
            fieldInfo = "DATEADD(minute, cast(left(" + item.sourceFieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.toUpperCase().equals("NVARCHAR") && !item.fieldType.equals("VARCHAR")){
            fieldInfo = "CAST(" + item.sourceFieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = item.sourceFieldName;
        }
        return fieldInfo;
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

