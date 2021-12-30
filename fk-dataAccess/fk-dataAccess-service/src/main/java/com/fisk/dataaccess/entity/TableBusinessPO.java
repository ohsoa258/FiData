package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_table_business2")
@EqualsAndHashCode(callSuper = true)
public class TableBusinessPO extends BasePO {

    /**
     * tb_table_access(id)
     */
    public long accessId;

    /**
     * 模式(1:普通模式  2:高级模式)
     */
    public Integer otherLogic;

    /**
     * 1:每年  2:每月  3:每天
     */
    public long businessTimeFlag;

    /**
     * 具体日期
     */
    public long businessDate;

    /**
     * 业务时间覆盖字段
     */
    public String businessTimeField;

    /**
     * 1:大于  2:小于  3:等于  4:大于等于  5:小于等于
     */
    public long businessOperator;

    /**
     * 业务覆盖范围
     */
    public long businessRange;

    /**
     * 业务覆盖单位
     */
    public long rangeDateUnit;

    /**
     * 其他逻辑  1:大于  2:小于  3:等于  4:大于等于  5:小于等于
     */
    public long businessOperatorStandby;

    /**
     * 其他逻辑  业务覆盖范围
     */
    public long businessRangeStandby;

    /**
     * 其他逻辑  业务覆盖单位
     */
    public long rangeDateUnitStandby;
}
