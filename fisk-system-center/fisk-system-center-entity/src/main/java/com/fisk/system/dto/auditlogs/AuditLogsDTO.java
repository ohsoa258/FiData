package com.fisk.system.dto.auditlogs;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName tb_audit_logs
 */
@TableName(value = "tb_audit_logs")
@Data
public class AuditLogsDTO implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 服务类型
     */
    @ApiModelProperty(value = "id")
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

    /**
     * 参数对象
     */
    @ApiModelProperty(value = "参数对象")
    private String paramMap;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}