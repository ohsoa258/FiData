package com.fisk.common.enums;

/**
 * MDC类型
 * @author gy
 */
public enum TraceTypeEnum implements BaseEnum {

    /**
     * 查询字段类型
     */
    CHARTVISUAL_QUERY(0, "ChartVisual_Query"),
    CHARTVISUAL_CONNECTION(1, "ChartVisual_Connection"),
    CHARTVISUAL_SERVICE(2, "ChartVisual_Service"),
    UNKNOWN(-1, "UNKNOWN");

    TraceTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
