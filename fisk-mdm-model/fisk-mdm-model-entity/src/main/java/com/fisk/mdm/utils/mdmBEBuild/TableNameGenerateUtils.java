package com.fisk.mdm.utils.mdmBEBuild;

/**
 * @Author WangYan
 * @Date 2022/5/19 10:30
 * @Version 1.0
 * 底层表名生成工具类
 */
public class TableNameGenerateUtils {

    public static String generateStgTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("stg_" + modelId + "_" + entityId);
        return str.toString();
    }

    public static String generateMdmTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("mdm_" + modelId + "_" + entityId);
        return str.toString();
    }

    public static String generateViwTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("viw_" + modelId + "_" + entityId);
        return str.toString();
    }
}
