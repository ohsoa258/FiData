package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_project_dimension_attribute")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDimensionAttributePO extends BasePO {
    /**
     * 维度表id
     */
    public int dimensionId;
    /**
     *表来源字段
     */
    public String tableSourceField;
    /**
     * 维度表字段名称
     */
    public String dimensionFieldName;
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

}
