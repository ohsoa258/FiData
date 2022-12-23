package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 报告扩展信息-系统用户VO
 * @date 2022/12/23 16:16
 */
@Data
public class QualityReportExt_UserVO {
    /**
     * 用户Id
     */
    @ApiModelProperty(value = "用户Id")
    public Long id;

    /**
     * 用户邮箱
     */
    @ApiModelProperty(value = "用户邮箱")
    public String email;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    public String userAccount;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    public String username;

    /**
     * 是否有效
     */
    @ApiModelProperty(value = "是否有效")
    public boolean valid;
}
