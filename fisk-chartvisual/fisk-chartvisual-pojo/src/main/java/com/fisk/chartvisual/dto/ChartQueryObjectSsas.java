package com.fisk.chartvisual.dto;

import com.fisk.common.enums.chartvisual.InteractiveTypeEnum;

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
    @NotNull
    public Integer id;
    public List<ColumnDetailsSsas> columnDetails;
    public List<ChartQueryFilter> queryFilters;
    public InteractiveTypeEnum interactiveType;
}
