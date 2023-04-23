package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/31 15:07
 * @Version 1.0
 */
@Data
public class ViwGroupCheckDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "别名")
    private String aliasName;
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
    @ApiModelProperty(value = "数据类型小数长度")
    private Integer dataTypeDecimalLength;

    @ApiModelProperty(value = "域名实体id")
    private Integer domainEntityId;
    @ApiModelProperty(value = "域名名称")
    private String domainName;
    @ApiModelProperty(value = "映射类型")
    private String mapType;

    /**
     * 实体id,实体名称
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;
    @ApiModelProperty(value = "实体名称")
    private String entityName;
    @ApiModelProperty(value = "实体展示名称")
    private String entityDisplayName;
}
