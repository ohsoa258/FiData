package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:35
 * @description 日志记录表实体类
 */
@Data
@TableName("tb_metadata_entity_operation_log")
public class MetaDataEntityOperationLogPO {
    @TableId(value = "id", type = IdType.AUTO)
    private long id;
    private String metadataEntityId;
    private String operationType;   //操作类型
    private String beforeChange;    //操作前
    private String afterChange;     //操作后
    private String createUser;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private int delFlag;

}
