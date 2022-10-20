package com.fisk.common.core.utils;

/**
 * @author JianWenYang
 */
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
     * 生成表名(不加ods或stg前缀)
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

}
