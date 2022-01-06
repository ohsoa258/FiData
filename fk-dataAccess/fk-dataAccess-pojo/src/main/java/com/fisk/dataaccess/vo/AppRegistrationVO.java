package com.fisk.dataaccess.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 *
 * 应用注册分页对象
 */
@Data
public class AppRegistrationVO{

    /**
     * id
     */
    @ApiModelProperty(value = "主键")
    public long id;
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;
    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    public String appDes;
    /**
     * 应用类型
     */
    @ApiModelProperty(value = "应用类型(0:实时应用  1:非实时应用)")
    public int appType;
    /**
     * 应用负责人
     */
    @ApiModelProperty(value = "应用负责人")
    public String appPrincipal;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", required = true)
    public LocalDateTime createTime;
    @ApiModelProperty(value = "创建时间", required = true)
    public String driveType;
}
