package com.fisk.dataservice.dto.datasource;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
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
