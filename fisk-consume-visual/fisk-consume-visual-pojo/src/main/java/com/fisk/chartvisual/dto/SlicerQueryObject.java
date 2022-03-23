package com.fisk.chartvisual.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author gy
 */
@Data
public class SlicerQueryObject {
    @NotNull
    public Integer id;
    @NotNull
    public String tableName;
    public String columnName;
    public String likeValue;
    public List<ChartQueryFilter> queryFilters;
    public PaginationQuery pagination;
}
