package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-03-01 14:37:30
 */
@TableName("tb_facttreelist_history")
@Data
public class FacttreelistHistoryPO{
    public String pid;

    private String historyId;

    public String businessNameId;

    public String businessName;

    public String factTabNameId;

    public String factTabName;

    public String factFieldEnNameId;

    public String factFieldEnName;

    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    public LocalDateTime createTime;

    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;

    @TableLogic
    public int delFlag;
}
