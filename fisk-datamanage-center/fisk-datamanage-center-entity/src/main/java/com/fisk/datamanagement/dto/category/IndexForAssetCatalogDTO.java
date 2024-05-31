package com.fisk.datamanagement.dto.category;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IndexForAssetCatalogDTO {

    /**
     * 指标id
     */
    @ApiModelProperty(value = "指标id")
    private Long indexId;

    /**
     * 指标名称
     */
    @ApiModelProperty(value = "指标名称")
    private String indexName;

    /**
     * 指标汇总
     */
    @ApiModelProperty(value = "指标汇总")
    private Integer indexCount;

    /**
     * 指标的元数据汇总
     */
    @ApiModelProperty(value = "指标的元数据汇总")
    private Integer indexdMetaCount;

}
