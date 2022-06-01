package com.fisk.common.core.enums.mdm;

/**
 * @author JianWenYang
 */
public enum OperatorEnum {
    /**
     * 不为NULL
     */
    NOT_NULL("IS NOT NULL", "不为NULL"),
    IS_NULL("IS NULL", "为NULL"),
    EQUAL("=", "等于"),
    NOT_EQUAL("<>", "不等于"),
    LESS_THAN("<", "小于"),
    LESS_THAN_EQUAL("<=", "小于或等于"),
    GREATER_THAN(">", "大于"),
    GREATER_THAN_EQUAL(">=", "大于或等于"),
    REGEX("REGEX", "包含模式"),
    NOT_REGEX("NOT REGEX", "不包含模式"),
    MATCH("MATCH", "匹配"),
    NOT_MATCH("NOT MATCH", "不匹配"),
    LIKE("LIKE", "类似于"),
    NOT_LIKE("NOT LIKE", "不类似于"),
    TOP_LIKE("TOP LIKE", "开头为"),
    OTHER("OTHER", "其他");

    private final String name;
    private final String value;

    OperatorEnum(String value, String name) {
        this.name = name;
        this.value = value;
    }

    public static OperatorEnum getValue(String value) {
        OperatorEnum[] operatorEnums = values();
        for (OperatorEnum operatorEnum : operatorEnums) {
            String queryValue = operatorEnum.getValue();
            if (queryValue.equals(value)) {
                return operatorEnum;
            }
        }
        return OperatorEnum.OTHER;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
