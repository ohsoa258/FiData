package com.fisk.dataservice.dto.logs;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 查询日志DTO
 * @date 2022/3/7 12:14
 */
@Data
public class LogQueryBasicsDTO {
    /**
     * APP ID
     */
    @ApiModelProperty(value = "应用ID")
    public int appId;

    /**
     * 表服务ID
     */
    @ApiModelProperty(value = "表服务ID")
    public int tableServiceId;

    /**
     * 文件服务ID
     */
    @ApiModelProperty(value = "文件服务ID")
    public int fileServiceId;

    /**
     * keyword
     */
    @ApiModelProperty(value = "keyword")
    public String keyword;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码，从第一页开始")
    public int current;

    /**
     * 页数
     */
    @ApiModelProperty(value = "页数")
    public int size;
}
