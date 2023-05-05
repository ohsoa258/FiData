package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.common.core.enums.chartvisual.InteractiveTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author gy
 */
@Data
public class ChartQueryObject {

    /**
     * 数据源连接id
     */
    @ApiModelProperty(value = "id")
    @NotNull
    public Integer id;

    @ApiModelProperty(value = "表名")
    @NotNull
    public String tableName;

    @ApiModelProperty(value = "专栏详细信息")
    public List<ColumnDetails> columnDetails;

    @ApiModelProperty(value = "问题过滤器")
    public List<ChartQueryFilter> queryFilters;

    @ApiModelProperty(value = "交互式类型")
    public InteractiveTypeEnum interactiveType;

    @ApiModelProperty(value = "标记页数")
    public PaginationQuery pagination;
}
