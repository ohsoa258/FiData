package com.fisk.common.core.utils.Dto.sftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.0
 * @description ftp文件树
 * @date 2021/12/30 10:27
 */
@Data
public class ExcelTreeDTO {
    @ApiModelProperty(value = "excel文件")
    public List<ExcelPropertyDTO> fileList;
    @ApiModelProperty(value = "excel文件夹")
    public List<ExcelPropertyDTO> directoryList;
}
