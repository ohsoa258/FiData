package com.fisk.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName tb_sso_access_records
 */
@TableName(value = "tb_sso_access_records")
@Data
public class SsoAccessRecordsPO extends BasePO implements Serializable {

    /**
     * fidata用户id
     */
    private Long fiUid;

    /**
     * json格式存储的单点登录用户详情
     */
    private String ssoUserInfo;

    /**
     * 访问时间
     */
    private Date visitTime;

    /**
     * 退出时间
     */
    private Date existTime;

    /**
     * 访问角色：普通用户，管理员，超级管理员
     */
    private String roleInfo;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}