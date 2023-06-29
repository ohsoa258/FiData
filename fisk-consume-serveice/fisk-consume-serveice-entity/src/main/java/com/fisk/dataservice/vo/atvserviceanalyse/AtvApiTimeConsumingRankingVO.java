package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API当天调用时长TOP20：接口调用耗时排名
 * @date 2023/6/19 11:52
 */
@Data
public class AtvApiTimeConsumingRankingVO {
    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * 耗时/SECOND
     */
    @ApiModelProperty(value = "耗时/SECOND")
    public String timeConsuming;
}
