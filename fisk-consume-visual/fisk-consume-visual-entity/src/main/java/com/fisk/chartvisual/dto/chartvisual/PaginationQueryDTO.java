package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.FieldTypeEnum;
import com.fisk.common.core.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.core.enums.chartvisual.TableOrderEnum;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "是否开启分页")
    public boolean enablePage;
    /**
     * 是否开启排序
     */
    @ApiModelProperty(value = "是否开启排序")
    public boolean enableOrder;

    @ApiModelProperty(value = "页码数")
    public Integer pageNum;

    @ApiModelProperty(value = "页面大小")
    public int pageSize = 10;

    @ApiModelProperty(value = "目标列")
    public String orderColumn;

    @ApiModelProperty(value = "排序类型")
    public TableOrderEnum ascType = TableOrderEnum.DESC;

    @ApiModelProperty(value = "目标类型")
    public FieldTypeEnum orderType;

    @ApiModelProperty(value = "集合类型")
    public AggregationTypeEnum aggregationType;
}
