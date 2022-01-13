package com.fisk.dataservice.dto.datasource;

import com.fisk.common.enums.dataservice.AggregationTypeEnum;
import com.fisk.common.enums.dataservice.ColumnTypeEnum;
import com.fisk.common.enums.dataservice.TableOrderEnum;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
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
