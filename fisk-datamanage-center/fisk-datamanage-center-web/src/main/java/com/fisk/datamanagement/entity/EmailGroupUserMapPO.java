package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * 
 * @TableName tb_email_group_user_map
 */
@TableName(value ="tb_email_group_user_map")
@Data
public class EmailGroupUserMapPO extends BasePO implements Serializable {
    /**
     * 
     */
    private Integer groupId;

    /**
     * 
     */
    private Integer userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}