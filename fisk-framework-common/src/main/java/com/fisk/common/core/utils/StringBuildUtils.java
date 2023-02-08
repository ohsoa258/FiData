package com.fisk.common.core.utils;

/**
 * @author JianWenYang
 */
public class StringBuildUtils {

    public static String dimensionKeyName(String tableName) {
        return tableName.replace("dim_", "") + "key";
    }

}
