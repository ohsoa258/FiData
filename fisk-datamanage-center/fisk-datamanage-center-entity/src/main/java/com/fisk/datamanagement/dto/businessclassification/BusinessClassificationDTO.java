package com.fisk.datamanagement.dto.businessclassification;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
@TableName("tb_business_classification")
public class BusinessClassificationDTO{

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    public String id;

    @TableField(value = "pid")
    public String pid;

    @TableField(value = "name")
    public String name;

    @TableField(value = "description")
    public String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    public LocalDateTime createTime;

    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    public String updateUser;

    @TableLogic
    public int delFlag;
}
