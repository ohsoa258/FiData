package com.fisk.license.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 修改许可证
 * @date 2022/11/28 15:52
 */
@Data
public class AuthorizeLicenceDTO {
    /**
     * 许可证
     */
    @ApiModelProperty(value = "许可证")
    public String licence;
}
