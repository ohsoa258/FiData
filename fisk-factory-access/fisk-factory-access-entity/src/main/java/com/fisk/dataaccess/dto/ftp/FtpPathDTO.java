package com.fisk.dataaccess.dto.ftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.0
 * @description
 * @date 2021/12/31 10:07
 */
@Data
public class FtpPathDTO {

    @ApiModelProperty(value = "应用id",required = true)
    public Long appId;

    @ApiModelProperty(value = "文件夹全路径",required = true)
    public String fullPath;
}
