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
    public Long accessId;

    /**
     * 业务时间字段
     */
    public String businessTimeField;

    /**
     * 1:  取上一个月数据,覆盖上一个月数据
     * 2:  取当月数据,覆盖当月数据
     * 3:  当月
     * 4:  取上一年数据,覆盖上一年
     * 5:  取当年数据,覆盖当年
     */
    public Long businessFlag;

    /**
     * 当月具体多少号
     */
    public Long businessDay;

}
