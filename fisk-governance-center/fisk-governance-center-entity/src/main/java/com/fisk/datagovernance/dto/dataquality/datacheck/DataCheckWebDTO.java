package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验DTO_同步前
 * @date 2022/5/16 20:44
 */
@Data
public class DataCheckWebDTO {
    /**
     * FiData平台数据源ID
     */
    @ApiModelProperty(value = "FiData平台数据源ID")
    @NotNull()
    public int fiDataDataSourceId;

    /**
     * key:表名称 value:验证的数据，json数组
     */
    @ApiModelProperty(value = "key：表id/表名称，value：验证的数据")
    @NotNull()
    public HashMap<String, JSONArray> body;
}
