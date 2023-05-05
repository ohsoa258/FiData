package com.fisk.mdm.dto.attributeGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/26 10:02
 * @Version 1.0
 */
@Data
public class AttributeInfoDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "展示名称")
    private String displayName;
    @ApiModelProperty(value = "排序")
    private String desc;
    @ApiModelProperty(value = "数据类型")
    private String dataType;
    @ApiModelProperty(value = "数据类型长度")
    private Integer dataTypeLength;
    @ApiModelProperty(value = "数据类型小数的长度")
    private Integer dataTypeDecimalLength;
    @ApiModelProperty(value = "存在组")
    private Integer existsGroup;
    /**
     * 关联实体的名称
     */
    @ApiModelProperty(value = "关联实体的名称")
    private String domainName;
    @ApiModelProperty(value = "类型")
    private String type;
    /**
     * 实体id,实体名称
     */
    @ApiModelProperty(value = "实体id,实体名称")
    private Integer entityId;
    @ApiModelProperty(value = "实体名称")
    private String entityName;
    @ApiModelProperty(value = "实体展示名称")
    private String entityDisplayName;
}
