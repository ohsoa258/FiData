package com.fisk.common.core.baseObject.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 表基础对象
 *
 * @author wangyan
 */
@Data
public class BaseSqlServerPO {

    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    public Timestamp createTime;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public Timestamp updateTime;

    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;

    @TableLogic
    public int delFlag;

}
