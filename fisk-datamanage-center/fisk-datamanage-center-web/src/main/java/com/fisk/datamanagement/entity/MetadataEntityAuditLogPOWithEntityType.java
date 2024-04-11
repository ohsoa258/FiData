package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import lombok.Data;

import java.io.Serializable;

@TableName(value = "tb_metadata_entity_audit_log")
@Data
public class MetadataEntityAuditLogPOWithEntityType extends BasePO implements Serializable {


    /**
     * 元数据qualified_name
     */
    public Integer entityId;

    /**
     * 类型
     * ALL 全部
     * ADD 新增
     * EDIT 修改
     * DELETE 删除
     */
    public MetadataAuditOperationTypeEnum operationType;

    /**
     * 元数据类型id tb_metadata_entity type_id
     */
    public Integer typeId;


//    /**
//     * 发现时间
//     */
//    public LocalDateTime time;


}