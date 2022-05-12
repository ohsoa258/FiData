package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/2/16 16:46
 */
@Data
public class ReceiveDataDTO {
    @ApiModelProperty(value = "当前实时api的主键")
    public Long apiCode;
    @ApiModelProperty(value = "本次同步的数据")
    public String pushData;
}
