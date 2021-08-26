package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_fact")
@EqualsAndHashCode(callSuper = true)
public class FactPO extends BasePO {
    /**
     * 业务过程id
     */
    public int businessProcessId;
    /**
     * 来源表id
     */
    public int tableSourceId;
    /**
     * 事实表中文名称
     */
    public String factTableCnName;
    /**
     * 事实表英文名称
     */
    public String factTableEnName;
    /**
     * 事实表描述
     */
    public String factTableDesc;

}
