package com.fisk.common.core.utils;

/**
 * @author JianWenYang
 */
public class TableNameGenerateUtils {

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
        str.append("ods_");
        str.append(appAbbreviation);
        if (whetherSchema) {
            str.append(".");
        } else {
            str.append("_");
        }
        str.append(tableName);
        return str.toString();
    }

}
