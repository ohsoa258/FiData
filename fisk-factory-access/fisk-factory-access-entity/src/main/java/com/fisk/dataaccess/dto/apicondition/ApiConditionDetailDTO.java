package com.fisk.dataaccess.dto.apicondition;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiConditionDetailDTO {

    /**
     * 项
     */
    @ApiModelProperty(value = "项")
    public String item;

    /**
     * 说明
     */
    @ApiModelProperty(value = "说明")
    public String instructions;

    /**
     * 实例
     */
    @ApiModelProperty(value = "实例")
    public String instance;

}
