package com.fisk.chartvisual.enums;

/**
 * @author WangYan
 * @date 2022/3/9 14:56
 * 目标字段类型
 */
public enum AnalysisFieldTypeEnum {
    /**
     * 分析字段类型
     */
    NUMBER("数值"),
    TEXT("文本"),
    DATE("时间");

    AnalysisFieldTypeEnum(String text) {
        this.text = text;
    }

    private String text;

    public String getText() {
        return text;
    }
}
