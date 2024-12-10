package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-11-29 09:58:14
 */
@TableName("tb_datacheck_server_api_config")
@Data
public class DatacheckServerApiConfigPO extends BasePO {

    @ApiModelProperty(value = "appId")
    private Integer appId;
    @ApiModelProperty(value = "校验规则id")
    private Integer checkRuleId;
    @ApiModelProperty(value = "api编码")
    private String apiCode;
    @ApiModelProperty(value = "api描述")
    private String apiDesc;
    @ApiModelProperty(value = "1启用、0禁用")
    private Integer apiState;
}
