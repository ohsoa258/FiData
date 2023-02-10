package com.fisk.datamanagement.dto.glossary;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
@TableName("tb_glossary_library")
public class GlossaryLibraryDTO {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    public String id;

    @TableField(value = "pid")
    public String pid;

    @TableField(value = "name")
    public String name;

    @TableField(value = "short_description")
    public String shortDescription;

    @TableField(value = "long_description")
    public String longDescription;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    public LocalDateTime createTime;

    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    public String updateUser;

    @TableLogic
    public int delFlag;
}
