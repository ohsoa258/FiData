package com.fisk.common.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class TableNameGenerateUtils {

    private static String ods = "ods_";
    private static String stg = "stg_";

    /**
     * 生成ods表名
     *
     * @param tableName
     * @param appAbbreviation 应用简称
     * @param whetherSchema   是否是架构名
     * @return
     */
    public static String buildOdsTableName(String tableName, String appAbbreviation, Boolean whetherSchema) {
        StringBuilder str = new StringBuilder();
        if (whetherSchema) {
            str.append(appAbbreviation);
            str.append(".");
        } else {
            str.append(ods);
            str.append(appAbbreviation);
            str.append("_");
        }
        str.append(tableName);
        return str.toString();
    }

    /**
     * 生成stg表名
     *
     * @param tableName
     * @param appAbbreviation
     * @param whetherSchema
     * @return
     */
    public static String buildStgTableName(String tableName, String appAbbreviation, Boolean whetherSchema) {
        StringBuilder str = new StringBuilder();
        if (whetherSchema) {
            str.append(appAbbreviation);
            str.append(".");
            str.append(stg);
        } else {
            str.append(stg);
            str.append(appAbbreviation);
            str.append("_");
        }
        str.append(tableName);
        return str.toString();
    }

    /**
     * 生成ods表名(不加ods或stg前缀)
     *
     * @param tableName
     * @param appAbbreviation
     * @param whetherSchema
     * @return
     */
    public static String buildTableName(String tableName, String appAbbreviation, Boolean whetherSchema) {
        if (whetherSchema) {
            return appAbbreviation + "." + tableName;
        }
        return appAbbreviation + "_" + tableName;
    }

    public static List<String> getStgAndTableName(String tableName) {
        String stgTableName = "";
        String odsTableName = "";
        String tableKey = "";
        List<String> tableNames = new ArrayList<>();
        log.info("getStgTableName的表名称{}", tableName);
        if (tableName.contains(".")) {
            String[] split = tableName.split("\\.");
            stgTableName = split[0] + ".stg_" + split[1];
            odsTableName = tableName;
            tableKey = split[0] + "_" + split[1] + "key";
            tableNames.add(stgTableName);
            tableNames.add(odsTableName);
            tableNames.add(tableKey);
        } else {
            stgTableName = "stg_" + tableName;
            odsTableName = "ods_" + tableName;
            tableKey = tableName + "key";
            tableNames.add(stgTableName);
            tableNames.add(odsTableName);
            tableNames.add(tableKey);
        }
        return tableNames;
    }

}
