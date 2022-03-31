package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_table_business")
@EqualsAndHashCode(callSuper = true)
public class TableBusinessPO extends BasePO {

    /**
     * tb_sync_mode(id)
     */
    public int syncId;

    /**
     * 模式(1:普通模式  2:高级模式)
     */
    public int otherLogic;

    /**
     * 1:每年  2:每月  3:每天(传汉字)
     */
    public String businessTimeFlag;

    /**
     * 具体日期
     */
    public int businessDate;

    /**
     * 业务时间覆盖字段
     */
    public String businessTimeField;

    /**
     * >,>=,=,<=,<(传符号)
     */
    public String businessOperator;

    /**
     * 业务覆盖范围
     */
    public int businessRange;

    /**
     * 业务覆盖单位,(默认传Year,Month,Day,Hour)
     */
    public String rangeDateUnit;

    /**
     * 其他逻辑  >,>=,=,<=,<(传符号)
     */
    public String businessOperatorStandby;

    /**
     * 其他逻辑  业务覆盖范围
     */
    public int businessRangeStandby;

    /**
     * 其他逻辑  业务覆盖单位,Year,Month,Day,Hour
     */
    public String rangeDateUnitStandby;

}
