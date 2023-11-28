package com.fisk.datamanagement.dto.standards;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-21
 * @Description:
 */
@Data
public class QueryResultDTO {
    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;

    @ApiModelProperty(value = "页索引")
    public int pageIndex;

    @ApiModelProperty(value = "页大小")
    public int pageSize;

    @ApiModelProperty(value = "总数")
    public int total = 0;

    @ApiModelProperty(value = "sql")
    public String sql;
}
