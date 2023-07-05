package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class FirstGlossaryCategorySummaryDto {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "pid")
    public Integer pid;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "简短的描述")
    public String shortDescription;

    @ApiModelProperty(value = "详细的描述")
    public String longDescription;

    @ApiModelProperty(value = "业务系统汇总数量")
    public long glossarySummary;

    @ApiModelProperty(value = "元数据汇总数量")
    public long metaDataSummary;
}
