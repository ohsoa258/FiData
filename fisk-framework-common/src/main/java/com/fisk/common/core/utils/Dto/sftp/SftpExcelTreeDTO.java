package com.fisk.common.core.utils.Dto.sftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class SftpExcelTreeDTO {

    @ApiModelProperty(value = "文件名or文件夹名", required = true)
    public String fileName;

    @ApiModelProperty(value = "文件全路径名称or文件夹全路径名称", required = true)
    public String fileFullName;

    @ApiModelProperty(value = "是否是文件夹标识", required = true)
    public Boolean dirFlag;

    @ApiModelProperty(value = "子集集合")
    public List<SftpExcelTreeDTO> children = new ArrayList<>();
}