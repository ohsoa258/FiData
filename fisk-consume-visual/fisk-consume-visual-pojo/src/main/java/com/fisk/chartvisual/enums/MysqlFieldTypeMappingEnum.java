package com.fisk.chartvisual.enums;

import java.util.Arrays;

/**
 * @author WangYan
 * @date 2022/3/9 15:22
 */
public enum MysqlFieldTypeMappingEnum {

    /**
     * MYSQL 字段类型转换
     */
    // 数字
    INT("int", AnalysisFieldTypeEnum.NUMBER),
    BIGINT("bigint", AnalysisFieldTypeEnum.NUMBER),
    TINYINT("tinyint", AnalysisFieldTypeEnum.NUMBER),

    // 文本
    NVARCHAR("varchar", AnalysisFieldTypeEnum.TEXT),
    BINARY("binary", AnalysisFieldTypeEnum.TEXT),
    BIT("bit", AnalysisFieldTypeEnum.TEXT),
    BLOB("blob", AnalysisFieldTypeEnum.TEXT),
    CHAR("char", AnalysisFieldTypeEnum.TEXT),
    TINYBLOB("tinyblob", AnalysisFieldTypeEnum.TEXT),
    TINYTEXT("tinytext", AnalysisFieldTypeEnum.TEXT),
    VARBINARY("varbinary", AnalysisFieldTypeEnum.TEXT),
    LONGBLOB("longblob", AnalysisFieldTypeEnum.TEXT),
    YEAR("year", AnalysisFieldTypeEnum.TEXT),

    // 时间
    DATE("date", AnalysisFieldTypeEnum.DATE),
    TIMESTAMP("timestamp", AnalysisFieldTypeEnum.DATE),
    DATETIME("datetime", AnalysisFieldTypeEnum.DATE);

    MysqlFieldTypeMappingEnum(String source, AnalysisFieldTypeEnum target) {
        this.source = source;
        this.target = target;
    }

    public static String getTargetTypeBySourceType(String type) throws Exception {
        MysqlFieldTypeMappingEnum fieldType = Arrays.stream(MysqlFieldTypeMappingEnum.values()).filter(e -> e.source.equals(type)).findFirst().orElse(null);
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
