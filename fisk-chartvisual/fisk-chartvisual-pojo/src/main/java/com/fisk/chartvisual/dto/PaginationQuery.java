package com.fisk.chartvisual.dto;

import com.fisk.common.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.enums.chartvisual.TableOrderEnum;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class PaginationQuery {
    /**
     * 是否开启分页
     */
    public boolean enablePage;
    /**
     * 是否开启排序
     */
    public boolean enableOrder;
    public Integer pageNum;
    public int pageSize = 10;
    public String orderColumn;
    public TableOrderEnum ascType = TableOrderEnum.DESC;
    public ColumnTypeEnum orderType;
    public AggregationTypeEnum aggregationType;
}
