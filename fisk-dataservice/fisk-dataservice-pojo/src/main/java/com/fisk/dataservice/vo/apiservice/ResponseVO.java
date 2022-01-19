package com.fisk.dataservice.vo.apiservice;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version v1.0
 * @description 数据集
 * @date 2022/1/18 11:15
 */
public class ResponseVO {

    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;
}
