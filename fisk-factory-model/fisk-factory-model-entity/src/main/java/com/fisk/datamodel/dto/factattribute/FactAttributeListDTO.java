package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeListDTO extends FactAttributeDTO {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    public long id;
    /**
     * 关联维度表名称
     */
    @ApiModelProperty(value = "关联维度表名称")
    public String dimensionCnName;

    /**
     * 源表名
     */
    @ApiModelProperty(value = "源表名")
    public String tableSource;
    /**
     * 源表字段
     */
    @ApiModelProperty(value = "源表字段")
    public String tableSourceField;
    /**
     * 维度关联维度名称
     */
    @ApiModelProperty(value = "维度关联维度名称")
    public String associationAttributeName;

}
