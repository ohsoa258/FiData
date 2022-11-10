package com.fisk.mdm.utils.mdmBEBuild;

/**
 * @Author WangYan
 * @Date 2022/5/19 10:30
 * @Version 1.0
 * 底层表名生成工具类
 */
public class TableNameGenerateUtils {

    /**
     * stg表
     * @param modelId
     * @param entityId
     * @return
     */
    public static String generateStgTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("stg_" + modelId + "_" + entityId);
        return str.toString();
    }

    /**
     * mdm表
     * @param modelId
     * @param entityId
     * @return
     */
    public static String generateMdmTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("mdm_" + modelId + "_" + entityId);
        return str.toString();
    }

    /**
     * 视图表
     * @param modelId
     * @param entityId
     * @return
     */
    public static String generateViwTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("viw_" + modelId + "_" + entityId);
        return str.toString();
    }

    /**
     * 自定义视图表
     * @param entityId
     * @param viewId
     * @return
     */
    public static String generateCustomizeViwTableName(Integer entityId,Integer viewId) {
        StringBuilder str = new StringBuilder();
        str.append("viw_" + entityId + "_" + viewId);
        return str.toString();
    }

    /**
     * 日志表
     * @param modelId
     * @param entityId
     * @return
     */
    public static String generateLogTableName(Integer modelId, Integer entityId) {
        StringBuilder str = new StringBuilder();
        str.append("log_" + modelId + "_" + entityId);
        return str.toString();
    }

    public static String generateDomainCode(String domainName) {
        return domainName + "_code";
    }

    public static String generateDomainName(String domainName) {
        return domainName + "_name";
    }

    public static String generateDomainCodeDisplayName(String domainDisplayName) {
        return domainDisplayName + "_编码";
    }

    public static String generateDomainNameDisplayName(String domainDisplayName) {
        return domainDisplayName + "_名称";
    }

}
