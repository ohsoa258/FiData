package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @TableName tb_system_version
 */
@Data
public class SystemVersionDTO {

    /**
     * 版本id
     */
    @ApiModelProperty(value = "版本id")
    private Integer id;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号")
    private String version;

    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    private String publishTime;

}
