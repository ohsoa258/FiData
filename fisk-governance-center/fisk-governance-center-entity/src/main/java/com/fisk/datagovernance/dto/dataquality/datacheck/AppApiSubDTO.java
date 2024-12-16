package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 应用API DTO
 * @date 2022/1/6 14:51
 */
@Data
public class AppApiSubDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * 规则Id
     */
    @ApiModelProperty(value = "规则Id")
    @NotNull()
    public Integer checkRuleId;

    /**
     * API状态 1启用、0禁用
     */
    @ApiModelProperty(value = "apiState")
    @NotNull()
    public Integer apiState;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    public String apiDesc;

    /**
     * api入参字段
     */
    @ApiModelProperty(value = "api入参字段")
    public List<ApiFieldDTO> apiFieldDTOList;
}
