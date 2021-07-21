package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_table_business")
@EqualsAndHashCode(callSuper = true)
public class TableBusinessPO extends BaseEntity {

    @TableId
    public long id;

    /**
     * tb_table_access(id)
     */
    public long accessId;

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
    public long businessFlag;

    /**
     * 当月具体多少号
     */
    public long businessDay;

}
