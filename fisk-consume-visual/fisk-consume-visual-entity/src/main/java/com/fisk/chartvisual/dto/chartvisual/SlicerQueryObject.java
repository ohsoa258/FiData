package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author gy
 */
@Data
public class SlicerQueryObject {

    @ApiModelProperty(value = "Id")
    @NotNull
    public Integer id;
    @ApiModelProperty(value = "表名")
    @NotNull
    public String tableName;

    @ApiModelProperty(value = "专栏名")
    public String columnName;

    @ApiModelProperty(value = "类似值")
    public String likeValue;

    @ApiModelProperty(value = "问题过滤器")
    public List<ChartQueryFilter> queryFilters;

    @ApiModelProperty(value = "标记页码")
    public PaginationQuery pagination;
}
