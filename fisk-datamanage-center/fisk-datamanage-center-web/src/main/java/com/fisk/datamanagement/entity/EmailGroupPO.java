package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tb_email_group
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_email_group")
@Data
public class EmailGroupPO extends BasePO implements Serializable {

    /**
     * 邮件组名称
     */
    private String groupName;

    /**
     * 组描述
     */
    private String groupDesc;

    /**
     * 组关联的邮件服务器id
     */
    private Integer emailServerId;

    /**
     * 组关联的邮件服务器名称
     */
    private String emailServerName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}