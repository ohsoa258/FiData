package com.fisk.chartvisual.enums;

import java.util.Arrays;

/**
 * @author WangYan
 * @date 2022/3/9 15:22
 */
public enum SqlServerFieldTypeMappingEnum {

    /**
     * SQLServer 字段类型转换
     */
    // 数字
    INT("int", AnalysisFieldTypeEnum.NUMBER),
    BIGINT("bigint", AnalysisFieldTypeEnum.NUMBER),
    TINYINT("tinyint", AnalysisFieldTypeEnum.NUMBER),

    // 文本
    NVARCHAR("nvarchar", AnalysisFieldTypeEnum.TEXT),
    VARCHAR_MAX("varchar(max)", AnalysisFieldTypeEnum.TEXT),
    BINARY("binary", AnalysisFieldTypeEnum.TEXT),
    CHAR("char", AnalysisFieldTypeEnum.TEXT),
    VARBINARY("varbinary", AnalysisFieldTypeEnum.TEXT),
    VARCHAR("varchar", AnalysisFieldTypeEnum.TEXT),
    varchar_max("varchar(max)", AnalysisFieldTypeEnum.TEXT),
    XML("xml", AnalysisFieldTypeEnum.TEXT),
    UNDEFINED("(USER-DEFINED)", AnalysisFieldTypeEnum.TEXT),
    COMPUTED("(COMPUTED)", AnalysisFieldTypeEnum.TEXT),

    // 时间
    DATE("date", AnalysisFieldTypeEnum.DATE),
    DATETIME2("datetime2", AnalysisFieldTypeEnum.DATE),
    DATETIME("datetime", AnalysisFieldTypeEnum.DATE);

    SqlServerFieldTypeMappingEnum(String source, AnalysisFieldTypeEnum target) {
        this.source = source;
        this.target = target;
    }

    public static String getTargetTypeBySourceType(String type) throws Exception {
        SqlServerFieldTypeMappingEnum fieldType = Arrays.stream(SqlServerFieldTypeMappingEnum.values()).filter(e -> e.source.equals(type)).findFirst().orElse(null);
        if (fieldType == null) {
            throw new Exception("无法识别类型: " + type);
        }
        return fieldType.target.getText();
    }

    private final String source;
    private final AnalysisFieldTypeEnum target;

    public String getSource() {
        return source;
    }

    public AnalysisFieldTypeEnum getTarget() {
        return target;
    }
}
