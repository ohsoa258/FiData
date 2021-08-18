package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 10:28
 */
@Data
@TableName("tb_dimension")
public class DimensionPO extends BasePO {

    /**
     * 业务域id
     */
    private Integer businessId;
    /**
     * 维度名称
     */
    private String dimensionCnName;
    /**
     * 维度英文名称
     */
    private String dimensionEnName;
    /**
     * 维度逻辑表名称
     */
    private String dimensionTabName;
    /**
     * 维度描述
     */
    private String dimensionDesc;
    /**
     * 是否共享
     */
    private int share;
}
