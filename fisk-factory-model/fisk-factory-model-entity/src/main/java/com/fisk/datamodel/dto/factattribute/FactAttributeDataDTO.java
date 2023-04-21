package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDataDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 事实字段中文名称
     */
    @ApiModelProperty(value = "事实字段中文名称")
    public String factFieldCnName;
    /**
     * 事实字段英文名称
     */
    @ApiModelProperty(value = "事实字段英文名称")
    public String factFieldEnName;
    /**
     * 事实字段描述
     */
    @ApiModelProperty(value = "事实字段描述")
    public String factFieldDes;
    /**
     * 关联维度表id
     */
    @ApiModelProperty(value = "关联维度表id")
    public int associateDimensionId;
    /**
     * 关联维度表名称
     */
    @ApiModelProperty(value = "关联维度表名称")
    public String associateDimensionName;
    /**
     * 关联维度表字段id
     */
    @ApiModelProperty(value = "关联维度表字段id")
    public int associateDimensionFieldId;
    /**
     * 关联维度表字段名称
     */
    @ApiModelProperty(value = "关联维度表字段名称")
    public String associateDimensionFieldName;

}
