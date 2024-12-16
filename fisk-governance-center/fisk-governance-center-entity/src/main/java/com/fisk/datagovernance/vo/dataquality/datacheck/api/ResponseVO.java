package com.fisk.datagovernance.vo.dataquality.datacheck.api;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据集
 * @date 2022/1/18 11:15
 */
@Data
public class ResponseVO {

    /**
     * 返回结果集
     */
    @ApiModelProperty(value = "返回结果集")
    public JSONArray data;
}
