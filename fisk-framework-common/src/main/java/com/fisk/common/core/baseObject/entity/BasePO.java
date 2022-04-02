package com.fisk.common.core.baseObject.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表基础对象
 *
 * @author gy
 */
@Data
public class BasePO {

    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    public LocalDateTime createTime;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;

    @TableLogic
    public int delFlag;

}
