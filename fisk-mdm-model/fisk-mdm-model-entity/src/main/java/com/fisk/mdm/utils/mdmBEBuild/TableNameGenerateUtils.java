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

    /**
     * 复杂类型-文件表-文件名称
     *
     * @param fileColumnName
     * @param isCode
     * @return
     */
    public static String generateComplexTypeFileName(String fileColumnName, boolean isCode) {
        return isCode ? fileColumnName + "_name" : fileColumnName + "_文件名";
    }

    /**
     * 复杂类型-文件表-文件路径
     *
     * @param fileColumnPath
     * @param isCode
     * @return
     */
    public static String generateComplexTypeFilePath(String fileColumnPath, boolean isCode) {
        return isCode ? fileColumnPath + "_path" : fileColumnPath + "_文件路径";
    }

    /**
     * 复杂类型-地理表-经度
     *
     * @param lngColumnName
     * @param isCode
     * @return
     */
    public static String generateComplexTypeLng(String lngColumnName, boolean isCode) {
        return isCode ? lngColumnName + "_lng" : lngColumnName + "_经度";
    }

    /**
     * 复杂类型-地理表-维度
     *
     * @param latColumnName
     * @param isCode
     * @return
     */
    public static String generateComplexTypeLat(String latColumnName, boolean isCode) {
        return isCode ? latColumnName + "_lat" : latColumnName + "_维度";
    }

    /**
     * 复杂类型-地理表-地图类型
     *
     * @param mapTypeColumnName
     * @param isCode
     * @return
     */
    public static String generateComplexTypeMapType(String mapTypeColumnName, boolean isCode) {
        return isCode ? mapTypeColumnName + "_type" : mapTypeColumnName + "_类型";
    }


}
