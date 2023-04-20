package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API今天调用情况前20条VO
 * @date 2023/4/20 17:20
 */
@Data
public class AtvTopCallApiAnalyseVO {
    /**
     * api标识
     */
    @ApiModelProperty(value = "api标识")
    public String apiCode;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    public String apiDesc;

    /**
     * 调用次数
     */
    @ApiModelProperty(value = "调用次数")
    public Integer totalCount;
}
