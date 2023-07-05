package com.fisk.datamanagement.dto.businessclassification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class FirstBusinessClassificationSummaryDto {
    @ApiModelProperty(value = "id")
    public String id;

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "pid")
    public String pid;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "元数据汇总")
    public long metaEntitySummary;

    @ApiModelProperty(value = "业务系统汇总")
    public long systemBusinessSummary;
}
