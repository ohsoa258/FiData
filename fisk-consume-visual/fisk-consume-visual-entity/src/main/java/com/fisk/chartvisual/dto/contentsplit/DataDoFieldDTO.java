package com.fisk.chartvisual.dto.contentsplit;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import com.fisk.chartvisual.enums.FieldTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataDoFieldDTO {

    @ApiModelProperty(value = "字段id")
    private Integer fieldId;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "条件")
    private String where;

    @ApiModelProperty(value = "条件值")
    private String whereValue;

    @ApiModelProperty(value = "字段类型")
    private FieldTypeEnum fieldType;

    @ApiModelProperty(value = "表名")
    private String tableName;
    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public NodeTypeEnum dimension;
    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private String endTime;

    /**
     * 指定时间
     */
    @ApiModelProperty(value = "指定时间")
    private String[] specifiedTime;
}
