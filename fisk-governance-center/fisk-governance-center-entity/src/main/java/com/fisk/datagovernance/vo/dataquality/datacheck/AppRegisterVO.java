package com.fisk.datagovernance.vo.dataquality.datacheck;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wangjian
 * @version v1.0
 * @description 应用 vo
 * @date 2024/10/14 14:51
 */
@Data
public class AppRegisterVO
{
    /**
     * Id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;

    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    public String appDesc;

    /**
     * 应用申请人
     */
    @ApiModelProperty(value = "应用申请人")
    public String appPrincipal;

    /**
     * 应用账号
     */
    @ApiModelProperty(value = "应用账号")
    public String appAccount;

    /**
     * 密码/加密
     */
    @ApiModelProperty(value = "密码/加密")
    public String appPassword;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 所有应用下服务总数
     */
    @ApiModelProperty(value = "所有应用下服务总数")
    public int totalCount;

    /**
     * 单个应用下服务个数
     */
    @ApiModelProperty(value = "单个应用下服务个数")
    public int  itemCount;
}
