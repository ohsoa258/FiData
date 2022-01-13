package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.FieldTypeEnum;
import com.fisk.common.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.enums.chartvisual.TableOrderEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/1/13 15:17
 * 分页对象
 */
@Data
public class PaginationQueryDTO {
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
    public FieldTypeEnum orderType;
    public AggregationTypeEnum aggregationType;
}
