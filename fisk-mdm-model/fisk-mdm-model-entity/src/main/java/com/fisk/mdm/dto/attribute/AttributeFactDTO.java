package com.fisk.mdm.dto.attribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/21 11:13
 * @Version 1.0
 * 属性事实表
 */
@Data
public class AttributeFactDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;
    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    private Integer dataType;
    /**
     * 数据类型长度
     */
    @ApiModelProperty(value = "数据类型长度")
    private Integer dataTypeLength;
    /**
     * 数据类型小数点长度
     */
    @ApiModelProperty(value = "数据类型小数点长度")
    private Integer dataTypeDecimalLength;
    /**
     * 是否必填
     */
    @ApiModelProperty(value = "是否必填")
    private Integer enableRequired;
    /**
     * 属性id
     */
    @ApiModelProperty(value = "属性id")
    private Integer attribute_id;
}
