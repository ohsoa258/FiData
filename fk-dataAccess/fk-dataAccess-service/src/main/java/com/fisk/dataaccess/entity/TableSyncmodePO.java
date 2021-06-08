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

    /**
     * 1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     */
    public int syncMode;

    /**
     * 自定义删除条件：定义每次同步的时候删除我们已有的数据条件
     */
    public String customDeleteCondition;

    /**
     * 自定义插入条件：定义删除之后获取插入条件的数据进行插入
     */
    public String customInsertCondition;

}
