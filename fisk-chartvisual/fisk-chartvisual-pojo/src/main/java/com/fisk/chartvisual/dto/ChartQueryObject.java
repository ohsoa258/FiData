package com.fisk.chartvisual.dto;

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
    @NotNull
    public Integer id;
    @NotNull
    public String tableName;
    public List<ColumnDetails> columnDetails;
}
