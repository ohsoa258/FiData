package com.fisk.system.dto.auditlogs;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AuditLogQueryType {

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 服务类型
     */
    @ApiModelProperty(value = "服务类型")
    private Integer serviceType;

    /**
     * 请求方式
     */
    @ApiModelProperty(value = "请求方式")
    private String requestType;

    /**
     * 请求地址
     */
    @ApiModelProperty(value = "请求地址")
    private String requestAddr;

    /**
     * ip地址
     */
    @ApiModelProperty(value = "ip地址")
    private String ipAddr;

}
