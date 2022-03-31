package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_table_history")
@EqualsAndHashCode(callSuper = true)
public class TableHistoryPO extends BasePO {
    /**
     * 发布表id
     */
    public int tableId;
    /**
     * 发布表类型 0：维度表 1：事实表。。。
     */
    public int tableType;
    /**
     * 发布备注
     */
    public String  remark;
}
