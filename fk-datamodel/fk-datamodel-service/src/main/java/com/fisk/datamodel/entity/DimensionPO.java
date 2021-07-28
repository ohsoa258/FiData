package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author JianWenYang
 */
@TableName("tb_dimension")
@Data
@EqualsAndHashCode(callSuper = true)
public class DimensionPO extends BasePO {

    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 维度名称
     */
    public String dimensionCnName;
    /**
     * 维度英文名称
     */
    public String dimensionEnName;
    /**
     * 维度逻辑表名称
     */
    public String dimensionTabName;
    /**
     * 维度描述
     */
    public String dimensionDesc;
    /**
     * 是否共享
     */
    public Boolean share;

}
