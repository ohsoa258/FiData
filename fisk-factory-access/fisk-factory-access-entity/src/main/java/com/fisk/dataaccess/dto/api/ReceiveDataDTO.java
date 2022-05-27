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
    @ApiModelProperty(value = "当前实时api的主键", required = true)
    public Long apiCode;
    @ApiModelProperty(value = "本次同步的数据", required = true)
    public String pushData;
    @ApiModelProperty(value = "true: 系统内部调用; false: 第三方调用", required = true)
    public boolean flag;
    @ApiModelProperty(value = "true: 勾选(发布之后,按照配置调用一次api,本次同步的数据为前端页面测试示例);false: 不勾选(本次推送的数据为正式数据)")
    public boolean executeConfigFlag;
}
