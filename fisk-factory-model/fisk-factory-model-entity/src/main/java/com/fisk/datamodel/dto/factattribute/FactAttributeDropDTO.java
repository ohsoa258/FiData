package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDropDTO {
    @ApiModelProperty(value = "id")
    public long id;
    @ApiModelProperty(value = "事实字段英文名")
    public String factFieldEnName;
    @ApiModelProperty(value = "事实字段类型")
    public String factFieldType;

    /**
     * 0:退化维度，1:维度建，2:度量字段
     */
    @ApiModelProperty(value = "0:退化维度，1:维度建，2:度量字段")
    public int attributeType;
}
