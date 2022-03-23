package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum PortComponentEnum implements BaseEnum {

    APP_INPUT_PORT_COMPONENT(0, "应用层input_port组件"),
    APP_OUTPUT_PORT_COMPONENT(1, "应用层output_port组件"),
    TASK_INPUT_PORT_COMPONENT(2, "任务层input_port组件"),
    TASK_OUTPUT_PORT_COMPONENT(3, "任务层output_port组件"),
    COMPONENT_INPUT_PORT_COMPONENT(4, "组件层input_port组件"),
    COMPONENT_OUTPUT_PORT_COMPONENT(5, "组件层output_port组件"),

    APP_INPUT_PORT_CONNECTION(6, "应用层input_port连接"),
    APP_OUTPUT_PORT_CONNECTION(7, "应用层output_port连接"),
    TASK_INPUT_PORT_CONNECTION(8, "任务层input_port连接"),
    TASK_OUTPUT_PORT_CONNECTION(9, "任务层output_port连接"),
    COMPONENT_INPUT_PORT_CONNECTION(10, "组件层input_port连接"),
    COMPONENT_OUTPUT_PORT_CONNECTION(11, "组件层output_port连接");


    PortComponentEnum(int value, String name) {
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
