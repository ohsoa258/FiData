package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_syncmode")
public class TableSyncmodePO extends BasePO {

    public int syncMode;
    public String customDeleteCondition;
    public String customInsertCondition;

}
