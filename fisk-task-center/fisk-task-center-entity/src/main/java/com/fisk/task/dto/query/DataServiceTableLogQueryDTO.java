package com.fisk.task.dto.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;

/**
 * @author dick
 * @version 1.0
 * @description 查询数据服务表服务日志查询DTO
 * @date 2023/1/10 15:06
 */
@Data
public class DataServiceTableLogQueryDTO {

    /**
     * 表类型
     */
    @ApiModelProperty(value = "表类型")
    public int tableType;

    /**
     * 表ID，逗号分割
     */
    @ApiModelProperty(value = "表ID，逗号分割")
    public String tableIds;

    /**
     * 表信息集合
     */
    @ApiModelProperty(value = "表信息集合")
    public HashMap<Long,String> tableList;

    /**
     * keyword
     */
    @ApiModelProperty(value = "keyword")
    public String keyword;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码，从第一页开始")
    public Integer current;

    /**
     * 页数
     */
    @ApiModelProperty(value = "页数")
    public Integer size;

}
