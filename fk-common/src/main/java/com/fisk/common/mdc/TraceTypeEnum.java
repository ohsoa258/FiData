package com.fisk.common.mdc;

import com.fisk.common.enums.BaseEnum;

/**
 * MDC类型
 *
 * @author gy
 */
public enum TraceTypeEnum implements BaseEnum {

    /**
     *
     */
    PROJECT_START(1, "Project_Start"),
    PROJECT_SHUTDOWN(2, "Project_Shutdown"),
    CHARTVISUAL_QUERY(1000, "ChartVisual_Query"),
    CHARTVISUAL_CONNECTION(1001, "ChartVisual_Connection"),
    CHARTVISUAL_SERVICE(1002, "ChartVisual_Service"),
    TASK_MQ_PRODUCER_CONFIRM(2001, "Task_MQ_Producer_Confirm"),
    TASK_WS_SEND_MESSAGE(2002, "Task_WS_Send_Message"),
    TASK_NIFI_ERROR(2003, "Task_Nifi_Error"),
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
