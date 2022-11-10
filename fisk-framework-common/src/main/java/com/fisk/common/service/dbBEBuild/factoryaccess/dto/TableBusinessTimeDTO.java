package com.fisk.common.service.dbBEBuild.factoryaccess.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableBusinessTimeDTO {
    /**
     * 模式(1:普通模式  2:高级模式)
     */
    public Integer otherLogic;
    /**
     * 1:每年  2:每月  3:每天(传汉字)
     */
    public String businessTimeFlag;
    /**
     * 具体日期
     */
    public Long businessDate;
    /**
     * 业务时间覆盖字段
     */
    public String businessTimeField;
    /**
     * >,>=,=,<=,<(传符号)
     */
    public String businessOperator;
    /**
     * 业务覆盖单位,Year,Month,Day,Hour
     */
    public String rangeDateUnit;
    /**
     * 业务覆盖范围
     */
    public Long businessRange;
    /**
     * 其他逻辑  >,>=,=,<=,<(传符号)
     */
    public String businessOperatorStandby;
    /**
     * 其他逻辑  业务覆盖范围
     */
    public Long businessRangeStandby;
    /**
     * 其他逻辑  业务覆盖单位,Year,Month,Day,Hour
     */
    public String rangeDateUnitStandby;

}
