package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum DispatchLogEnum implements BaseEnum {

    /*
     * 管道开始
     * */
    pipelstart(1, "管道开始"),

    /*
     *管道结束
     * */
    pipelend(2, "管道结束"),
    /*
     *管道状态
     * */
    pipelstate(3, "管道状态"),
    /*
     *job开始
     * */
    jobstart(4, "job开始"),
    /*
     *job结束
     * */
    jobend(5, "job结束"),
    /*
     *job状态
     * */
    jobstate(6, "job状态"),
    /*
     *task开始
     * */
    taskstart(7, "task开始"),
    /*
     *task结束
     * */
    taskend(8, "task结束"),
    /*
     *task状态
     * */
    taskstate(9, "task状态"),
    /*
     *task同步条数
     * */
    taskcount(10, "task同步条数"),
    /*
     *task同步日志备注
     * */
    taskcomment(11, "task同步日志备注"),
    /*
     *阶段--开始
     * */
    stagestart(12, "阶段--开始"),
    /*
     *阶段--转换
     * */
    stagetransition(13, "阶段--转换"),
    /*
     *阶段--插入
     * */
    stageinsert(14, "阶段--插入"),
    /*
     *阶段--状态
     * */
    stagestate(15, "阶段--状态");

    DispatchLogEnum(int value, String name) {
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

    public static DispatchLogEnum getValue(String name) {
        DispatchLogEnum[] carTypeEnums = values();
        for (DispatchLogEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static DispatchLogEnum getName(int value) {
        DispatchLogEnum[] carTypeEnums = values();
        for (DispatchLogEnum carTypeEnum : carTypeEnums) {
            if (carTypeEnum.value == value) {
                return carTypeEnum;
            }
        }
        return null;
    }
}
