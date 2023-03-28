package com.fisk.dataaccess.dto.sftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SftpPreviewQueryDTO {
    @ApiModelProperty(value = "每页显示条数")
    public int pageSize;
    @ApiModelProperty(value = "应用id", required = true)
    public long appId;
    @ApiModelProperty(value = "完整文件路径", required = true)
    public String fileFullName;

    @ApiModelProperty(value = "预览的起始行", required = true)
    public Integer startRow;

}
