package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Data
public class ApiConfigDTO {

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", required = true)
    public long id;

    @ApiModelProperty(value = "应用id", required = true)
    public Long appId;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称", required = true)
    public String apiName;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述", required = true)
    public String apiDes;

}
