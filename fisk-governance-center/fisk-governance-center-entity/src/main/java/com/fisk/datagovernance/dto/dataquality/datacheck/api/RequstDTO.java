package com.fisk.datagovernance.dto.dataquality.datacheck.api;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据请求DTO
 * @date 2022/1/18 10:03
 */
@Data
public class RequstDTO {
    /**
     * 请求参数
     */
    @ApiModelProperty(value = "请求参数")
    public JSONArray data;

    /**
     * API标识
     */
    @ApiModelProperty(value = "API标识")
    @NotNull()
    public String apiCode;
}
