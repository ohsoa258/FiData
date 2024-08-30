package com.fisk.common.core.enums.fidatadatasource;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/7/6 14:49
 */
public enum TableBusinessTypeEnum implements BaseEnum {

    NONE(0, "NONE"), //空
    DW_DIMENSION(1, "dw_dimension"), //dw维度表
    DW_FACT(2, "dw_fact"), //dw事实表
    DORIS_DIMENSION(3, "doris_dimension"), //doris维度表
    DORIS_FACT(4, "doris_fact"), //doris事实表
    WIDE_TABLE(5, "wide_table"), //宽表
    ENTITY_TABLR(6,"entity_table"),//主数据实体表

    DATA_SERVICE_TABLE(7,"data_service_table"),//数据分发表服务

    DATA_SERVICE_API(8,"data_service_api"),//数据分发api服务

    STANDARD_DATABASE(9,"standard_database"),
    PHYSICAL_TABLE(10,"physical_table"),//数接物理表
    ACCESS_API(11,"standard_database"),//数接api
    DORIS_CATALOG_TABLE(12,"doris_catalog_table"),//doris外部目录表

    ;//数据标准

    TableBusinessTypeEnum(int value, String name) {
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

    public static TableBusinessTypeEnum getEnum(int value){
        for (TableBusinessTypeEnum e:TableBusinessTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return TableBusinessTypeEnum.NONE;
    }
}
