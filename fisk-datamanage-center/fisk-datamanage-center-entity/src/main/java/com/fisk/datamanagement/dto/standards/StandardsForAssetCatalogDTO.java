package com.fisk.datamanagement.dto.standards;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StandardsForAssetCatalogDTO {

    /**
     * 数据元菜单id
     */
    @ApiModelProperty(value = "数据元菜单id")
    private Long standardId;

    /**
     * 数据元菜单名称
     */
    @ApiModelProperty(value = "数据元菜单名称")
    private String standardName;

    /**
     * 数据元标准汇总
     */
    @ApiModelProperty(value = "数据元汇总")
    private Integer standardCount;

    /**
     * 元数据汇总
     */
    @ApiModelProperty(value = "元数据汇总")
    private Integer standardMetaCount;

}
