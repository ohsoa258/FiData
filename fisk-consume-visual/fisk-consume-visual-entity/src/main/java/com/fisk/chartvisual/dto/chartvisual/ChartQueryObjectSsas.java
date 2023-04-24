package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.GraphicTypeEnum;
import com.fisk.common.core.enums.chartvisual.InteractiveTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * SSAS查询数据条件
 * @author JinXingWang
 */
public class ChartQueryObjectSsas {
    /**
     * 数据源连接id
     */
    @ApiModelProperty(value = "id")
    @NotNull
    public Integer id;

    @ApiModelProperty(value = "专栏详细信息")
    public List<ColumnDetailsSsas> columnDetails;

    @ApiModelProperty(value = "问题过滤器")
    public List<ChartQueryFilter> queryFilters;

    @ApiModelProperty(value = "chartDrillDown")
    public ChartDrillDown chartDrillDown;

    @ApiModelProperty(value = "交互式类型")
    public InteractiveTypeEnum interactiveType;

    @ApiModelProperty(value = "图表类型")
    public GraphicTypeEnum graphicType;

    public void columnDetails(List<ColumnDetailsSsas> columnDetailsSsas) {
        this.columnDetails = columnDetailsSsas;
    }
}
