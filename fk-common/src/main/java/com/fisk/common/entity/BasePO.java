package com.fisk.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.constants.SqlConstants;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

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

    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    public String updateUser;

    @TableLogic
    public int delFlag;

}
