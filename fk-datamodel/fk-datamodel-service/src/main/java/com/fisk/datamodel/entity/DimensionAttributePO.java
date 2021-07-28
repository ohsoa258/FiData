package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_dimension_attribute")
@Data
@EqualsAndHashCode(callSuper = true)
public class DimensionAttributePO extends BasePO {
    /**
     * 维度表id
     */
    public int dimensionId;
    /**
     *表来源字段
     */
    public String tableSourceField;
    /**
     * 维度表中文字段名称
     */
    public String dimensionFieldCnName;
    /**
     * 维度表字段类型
     */
    public  String dimensionFieldType;
    /**
     * 维度表字段长度
     */
    public int dimensionFieldLength;
    /**
     * 维度表字段描述
     */
    public String dimensionFieldDes;
    /**
     * 维度表英文字段名称
     */
    public String dimensionFieldEnName;
    /**
     * 属性类型：1、业务主键,2、关联维度,3、属性
     */
    public int attributeType;
    /**
     * 关联维度表id
     */
    public int associate_dimension_id;

}
