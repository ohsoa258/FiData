package com.fisk.common.mdc;

import com.fisk.common.enums.BaseEnum;

/**
 * MDC类型
 * @author gy
 */
public enum TraceTypeEnum implements BaseEnum {

    /**
     *
     */
    CHARTVISUAL_QUERY(0, "ChartVisual_Query"),
    CHARTVISUAL_CONNECTION(1, "ChartVisual_Connection"),
    CHARTVISUAL_SERVICE(2, "ChartVisual_Service"),
    CHARTVISUAL_SHUTDOWN(3, "ChartVisual_Shutdown"),
    CHARTVISUAL_START(4, "ChartVisual_Start"),
    TASK_MQ_PRODUCER_CONFIRM(4, "Task_MQ_Producer_Confirm"),
    TASK_WS_SEND_MESSAGE(4, "Task_WS_Send_Message"),
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
