package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.0
 * @description 操作表返回信息
 * @date 2022/1/10 17:42
 */
@Data
public class OperateMsgDTO {

    @ApiModelProperty(value = "操作信息")
    public String operateBehaveMsg;
}
