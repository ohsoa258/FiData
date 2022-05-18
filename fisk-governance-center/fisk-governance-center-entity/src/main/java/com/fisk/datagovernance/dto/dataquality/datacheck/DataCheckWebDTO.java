package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验_页面DTO
 * @date 2022/5/16 20:44
 */
@Data
public class DataCheckWebDTO {
    /**
     * 服务器IP
     */
    @ApiModelProperty(value = "服务器IP")
    @NotNull()
    public String ip;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    @NotNull()
    public String dbName;

    /**
     * key:表名称 value:验证的数据，json数组
     */
    @ApiModelProperty(value = "key:表名称 value:验证的数据，json数组")
    @NotNull()
    public HashMap<String, JSONArray> body;
}
