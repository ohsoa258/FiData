package com.fisk.datamodel.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class QueryDTO {
    /**
     *当前页数
     */
    @ApiModelProperty(value = "当前页数")
    public int page;
    /**
     *每页条数
     */
    @ApiModelProperty(value = "每页条数")
    public int size;
    /**
    *id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
