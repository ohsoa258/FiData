package com.fisk.dataservice.dto.datasource;

import com.fisk.common.enums.dataservice.InteractiveTypeEnum;
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
public class ChartQueryObject {

    /**
     * 数据源连接id
     */
    @NotNull
    public Integer id;
    @NotNull
    public String tableName;
    public List<ColumnDetails> columnDetails;
    public List<ChartQueryFilter> queryFilters;
    public InteractiveTypeEnum interactiveType;
    public PaginationQuery pagination;
}
