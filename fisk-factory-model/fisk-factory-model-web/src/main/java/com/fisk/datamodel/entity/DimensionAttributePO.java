package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
     * 属性类型：0：维度属性
     */
    public int attributeType;
    /**
     * 关联维度表id
     */
    public int associateDimensionId;
    /**
     * 关联维度字段表id
     */
    public int associateDimensionFieldId;
    /**
     * 是否业务主键 0:否 1:是
     */
    public int isPrimaryKey;
    /**
     *源表名称
     */
    public String sourceTableName;
    /**
     * 源字段名称
     */
    public String sourceFieldName;
    /**
     * 是否维度表字段
     */
    public Boolean isDimDateField;

}
