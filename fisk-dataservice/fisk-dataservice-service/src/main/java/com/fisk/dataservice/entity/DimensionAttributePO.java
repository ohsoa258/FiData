package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 10:35
 */
@Data
@TableName("tb_dimension_attribute")
public class DimensionAttributePO extends BasePO {

    /**
     * 维度id
     */
    private Integer dimensionId;
    /**
     * 表来源字段id
     */
    private Integer tableSourceFieldId;
    /**
     * 维度表中文字段名称
     */
    private String dimensionFieldCnName;
    /**
     * 维度表英文字段名称
     */
    private String dimensionFieldEnName;
    /**
     * 维度表字段类型
     */
    private String dimensionFieldType;
    /**
     * 维度表字段长度
     */
    private Integer dimensionFieldLength;
    /**
     * 维度表字段描述
     */
    private String dimensionFieldDes;
    /**
     * 属性类型：0、业务主键,1、关联维度,2、属性
     */
    private Integer attributeType;
    /**
     * 关联维度字段表id
     */
    private Integer associateDimensionId;
}
