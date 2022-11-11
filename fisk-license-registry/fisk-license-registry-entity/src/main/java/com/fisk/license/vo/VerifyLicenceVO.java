package com.fisk.license.vo;

import com.fisk.license.enums.LicenceStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 许可证VO
 * @date 2022/11/9 14:10
 */
@Data
public class VerifyLicenceVO {
    /**
     * 许可证状态
     */
    @ApiModelProperty(value = "许可证状态")
    public LicenceStateEnum licenceState;

    /**
     * 许可证过期时间
     */
    @ApiModelProperty(value = "许可证过期时间")
    public String licenceExpireTime;
}
