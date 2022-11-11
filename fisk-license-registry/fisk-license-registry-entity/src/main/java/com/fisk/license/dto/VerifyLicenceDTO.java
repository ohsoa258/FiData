package com.fisk.license.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 许可证DTO
 * @date 2022/11/9 14:07
 */
@Data
public class VerifyLicenceDTO {
    /**
     * 相对路径地址
     */
    @NotNull()
    @ApiModelProperty(value = "相对路径地址 例：/home/index")
    public String relativePathUrl;
}
