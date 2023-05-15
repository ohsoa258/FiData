package com.fisk.mdm.entity;

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
    public Integer tableId;
    /**
     * 发布表类型 0：实时api 1：非实时表
     */
    public Integer tableType;
    /**
     * 发布备注
     */
    public String  remark;

    /**
     *关联发布日志标识
     */
    public String subRunId;
}
