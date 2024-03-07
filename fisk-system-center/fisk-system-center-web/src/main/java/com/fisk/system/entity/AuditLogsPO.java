package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @TableName tb_audit_logs
 */
@TableName(value ="tb_audit_logs")
@Data
public class AuditLogsPO extends BasePO implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 服务类型
     */
    @ApiModelProperty(value = "id")
    private Integer serviceType;

    /**
     * 请求方式
     */
    private String requestType;

    /**
     * 请求地址
     */
    private String requestAddr;

    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 参数对象
     */
    private String paramMap;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}