package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:41
 * @Version 1.0
 */
@Data
public class ViwGroupDetailsDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "组id")
    @NotNull
    private Integer groupId;
    @ApiModelProperty(value = "属性id")
    private Integer attributeId;
    @ApiModelProperty(value = "别名")
    private String aliasName;

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    /**
     * 域字段id
     */
    @ApiModelProperty(value = "域字段id")
    private Integer domainId;

    /**
     * 属性名称
     */
    @ApiModelProperty(value = "属性名称")
    private String name;

    /**
     * 展示名称
     */
    @ApiModelProperty(value = "展示名称")
    private String displayName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String desc;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    private String dataType;

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
}
