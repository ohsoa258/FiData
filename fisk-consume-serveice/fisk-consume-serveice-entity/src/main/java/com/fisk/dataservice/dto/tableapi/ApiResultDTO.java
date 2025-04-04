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

    @ApiModelProperty(value = "本次消费数量")
    private Integer number;

    @ApiModelProperty(value = "本次同步时间")
    private String syncTime;
}
