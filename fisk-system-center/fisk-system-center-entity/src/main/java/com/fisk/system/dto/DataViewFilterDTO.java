package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/4 15:03
 */
@Data
public class DataViewFilterDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "数据视图")
    private Integer dataviewId;
    @ApiModelProperty(value = "字段")
    private String field;
    @ApiModelProperty(value = "操作者")
    private String operator;
    @ApiModelProperty(value = "结果")
    private String result;
}
