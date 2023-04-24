package com.fisk.system.dto;

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
     *角查询字段名称
     */
    @ApiModelProperty(value = "角查询字段名称")
    public String name;
    /**
     * 查询角色id
     */
    @ApiModelProperty(value = "查询角色id")
    public int roleId;

}
