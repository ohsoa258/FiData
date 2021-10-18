package com.fisk.taskfactory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Lock
 */
@Data
@TableName("tb_task_schedule")
public class TaskSchedulePO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    public int id;

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
    /**
     * 子节点id
     */
    public int jobId;
    /**
     * 父节点id
     */
    public int jobPid;
    /**
     * 同步方式
     */
    public String syncMode;
    /**
     * 表达式
     */
    public String expression;
    /**
     * 日志
     */
    public String msg;
    /**
     * tree标识
     */
    public int flag;
}
