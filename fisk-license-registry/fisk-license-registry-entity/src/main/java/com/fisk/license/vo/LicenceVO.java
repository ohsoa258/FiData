package com.fisk.license.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description Licence
 * @date 2022/11/10 15:45
 */
@Data
public class LicenceVO {
    /**
     * 许可证
     */
    @ApiModelProperty(value = "许可证")
    public String licence;

    /**
     * 许可证过期时间
     */
    @ApiModelProperty(value = "许可证过期时间")
    public String licenceExpireTime;
}
