package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import com.fisk.chartvisual.enums.DragElemTypeEnum;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author JinXingWang
 */
public class ColumnDetailsSsas {
    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "唯一名称")
    public String uniqueName;

    @ApiModelProperty(value = "维度类型")
    public NodeTypeEnum dimensionType;

    @ApiModelProperty(value = "拖动数据类型")
    public DragElemTypeEnum dragElemType;
}
