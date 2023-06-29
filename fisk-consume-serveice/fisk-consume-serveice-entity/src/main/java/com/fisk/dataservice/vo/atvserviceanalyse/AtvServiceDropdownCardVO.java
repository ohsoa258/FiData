package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;

/**
 * @author dick
 * @version 1.0
 * @description 服务监控下拉框
 * @date 2023/6/19 15:28
 */
@Data
public class AtvServiceDropdownCardVO {
    /**
     * 接口类型
     */
    @ApiModelProperty(value = "接口类型")
    public HashMap<Integer, String> createApiTypeList;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public HashMap<Integer, String> appNameList;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public HashMap<Integer, String> apiNameList;
}
