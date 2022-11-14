package com.fisk.dataaccess.dto.ftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 复制ftp文件
 * @date 2022/11/14 15:13
 */
@Data
public class CopyFtpFileDTO {

    @ApiModelProperty(value = "参数key")
    @NotNull()
    public String keyStr;

}
