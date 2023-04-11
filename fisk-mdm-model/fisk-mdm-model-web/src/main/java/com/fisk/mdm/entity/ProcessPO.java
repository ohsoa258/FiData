package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程表基础对象
 *
 * @author gy
 */
@Data
public class ProcessPO {

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

    public int delFlag;

}
