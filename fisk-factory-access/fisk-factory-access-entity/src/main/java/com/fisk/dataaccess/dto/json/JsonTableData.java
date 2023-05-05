package com.fisk.dataaccess.dto.json;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author gy
 * @version 1.0
 * @description 表数据
 * @date 2022/1/20 14:28
 */
@Data
@Builder
public class JsonTableData {

    @ApiModelProperty(value = "表")
    public String table;

    @ApiModelProperty(value = "数据")
    public JSONArray data;
}
