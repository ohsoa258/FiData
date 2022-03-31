package com.fisk.dataaccess.dto.ftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.0
 * @description
 * @date 2021/12/30 14:26
 */
@Data
public class ExcelPropertyDTO {

    @ApiModelProperty(value = "文件名or文件夹名", required = true)
    public String fileName;
    @ApiModelProperty(value = "文件全路径名称or文件夹全路径名称", required = true)
    public String fileFullName;
}
