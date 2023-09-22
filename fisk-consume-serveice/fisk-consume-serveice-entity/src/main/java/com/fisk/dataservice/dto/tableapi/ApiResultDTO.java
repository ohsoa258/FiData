package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Data
public class ApiResultDTO {

    @ApiModelProperty(value = "true:成功 flase:失败")
    private Boolean flag;

    @ApiModelProperty(value = "返回结果")
    private String msg;
}
