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
     * @param modelName
     * @param entityName
     * @return
     */
    public static String generateStgTableName(String modelName, String entityName) {
        StringBuilder str = new StringBuilder();
        str.append("stg_" + modelName + "_" + entityName);
        return str.toString();
    }

    /**
     * mdm表
     * @param modelName
     * @param entityName
     * @return
     */
    public static String generateMdmTableName(String modelName, String entityName) {
        StringBuilder str = new StringBuilder();
        str.append("mdm_" + modelName + "_" + entityName);
        return str.toString();
    }

    /**
     * 视图表
     *
     * @param modelName
     * @param entityName
     * @return
     */
    public static String generateViwTableName(String modelName, String entityName) {
        StringBuilder str = new StringBuilder();
        str.append("viw_" + modelName + "_" + entityName);
        return str.toString();
    }

    /**
     * 自定义视图表
     * @param entityName
     * @param viewName
     * @return
     */
    public static String generateCustomizeViwTableName(String entityName, String viewName) {
        StringBuilder str = new StringBuilder();
        str.append("viw_" + entityName + "_" + viewName);
        return str.toString();
    }

    /**
     * 日志表
     * @param modelName
     * @param entityName
     * @return
     */
    public static String generateLogTableName(String modelName, String entityName) {
        StringBuilder str = new StringBuilder();
        str.append("log_" + modelName + "_" + entityName);
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
