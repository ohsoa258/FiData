package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description api信息，含订阅信息
 * @date 2022/1/19 19:05
 */
@Data
public class ApiSeverSubVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "appId")
    private Integer appId;
    @ApiModelProperty(value = "校验规则id")
    private Integer checkRuleId;
    @ApiModelProperty(value = "规则名称")
    private String ruleName;
    @ApiModelProperty(value = "api编码")
    private String apiCode;
    @ApiModelProperty(value = "api描述")
    private String apiDesc;
    @ApiModelProperty(value = "1启用、0禁用")
    private Integer apiState;

    @ApiModelProperty(value = "字段集合")
    private List<ApiFieldServerVO> fieldList;

}
