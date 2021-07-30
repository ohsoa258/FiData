package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_fact")
@Data
@EqualsAndHashCode(callSuper = true)
public class FactPO extends BasePO {
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 业务过程名称
     */
    public String factName;
    /**
     * 事实表名称
     */
    public String factTableName;
    /**
     * 业务过程描述
     */
    public String factDesc;

}
