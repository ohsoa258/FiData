package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tb_email_user
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_email_user")
@Data
public class EmailUserPO extends BasePO implements Serializable {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱地址
     */
    private String emailAddress;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}