package com.fisk.dataservice.vo.app;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description 应用 DTO
 * @date 2022/1/6 14:51
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
     * 应用负责人
     */
    @ApiModelProperty(value = "应用负责人")
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
}
