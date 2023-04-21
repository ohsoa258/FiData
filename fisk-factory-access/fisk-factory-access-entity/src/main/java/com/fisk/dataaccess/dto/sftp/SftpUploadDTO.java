package com.fisk.dataaccess.dto.sftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SftpUploadDTO {

    @ApiModelProperty(value = "上传路径")
    public String uploadPath;

}
