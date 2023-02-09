package com.fisk.common.core.utils;

/**
 * @author JianWenYang
 */
public class StringBuildUtils {

    /**
     * 获取维度关联key名
     *
     * @param tableName
     * @return
     */
    public static String dimensionKeyName(String tableName) {
        return tableName.replace("dim_", "") + "key";
    }

}
