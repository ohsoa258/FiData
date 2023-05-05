package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import com.fisk.chartvisual.enums.FieldTypeEnum;
import com.fisk.common.core.enums.chartvisual.AggregationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/1/6 15:55
 */
@Data
public class FieldDataDTO {
    /**
     * 字段id
     */
    @ApiModelProperty(value = "字段id")
    public Integer fieldId;
    /**
     * 字段名字
     */
    @ApiModelProperty(value = "字段名字")
    public String columnName;
    @ApiModelProperty(value = "字段标签")
    public String columnLabel;

    /**
     * 字段表名
     */
    @ApiModelProperty(value = "字段表名")
    public String fieldTableName;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    @NotNull
    public FieldTypeEnum fieldType;

    /**
     * 值类型(值才用)
     */
    @ApiModelProperty(value = "值类型")
    public AggregationTypeEnum aggregationType;

    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public NodeTypeEnum dimension;
}
