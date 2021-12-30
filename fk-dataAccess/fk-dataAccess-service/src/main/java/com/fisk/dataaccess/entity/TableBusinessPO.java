package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_table_business")
@EqualsAndHashCode(callSuper = true)
public class TableBusinessPO extends BasePO {

    /**
     * tb_table_access(id)
     */
    public long accessId;

    /**
     * 开关(0:false  1:true)
     */
    public Boolean otherLogic;

    /**
     * 1:每年  2:每月  3:每天
     */
    public long businessFlag;

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
    public long businessExtent;

    /**
     * 业务覆盖单位
     */
    public long extentDateUnit;

    /**
     * 其他逻辑  1:大于  2:小于  3:等于  4:大于等于  5:小于等于
     */
    public long businessOperatorStandby;

    /**
     * 其他逻辑  业务覆盖范围
     */
    public long businessExtentStandby;

    /**
     * 其他逻辑  业务覆盖单位
     */
    public long extentDateUnitStandby;
}
