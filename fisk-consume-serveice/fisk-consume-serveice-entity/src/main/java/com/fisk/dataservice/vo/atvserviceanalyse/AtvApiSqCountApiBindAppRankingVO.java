package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API申请次数TOP20：API所绑定的应用排名
 * @date 2023/6/19 11:53
 */
@Data
public class AtvApiSqCountApiBindAppRankingVO {
    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * API所绑定的应用的数量
     */
    @ApiModelProperty(value = "API所绑定的应用的数量")
    public String apiBindAppCount;
}
