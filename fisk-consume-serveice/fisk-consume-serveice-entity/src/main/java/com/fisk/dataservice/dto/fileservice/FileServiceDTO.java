package com.fisk.dataservice.dto.fileservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FileServiceDTO {

    public long id;

    @ApiModelProperty(value = "文件名称", required = true)
    public String name;

    @ApiModelProperty(value = "显示名称", required = true)
    public String displayName;

    @ApiModelProperty(value = "描述")
    public String describe;

    @ApiModelProperty(value = "sql脚本", required = true)
    public String sqlScript;

    @ApiModelProperty(value = "存储文件源id", required = true)
    public Integer targetSourceId;

    @ApiModelProperty(value = "文件存储路径", required = true)
    public String storagePath;

}
