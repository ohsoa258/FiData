package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import lombok.Data;

import java.io.Serializable;

@TableName(value = "tb_metadata_entity_audit_log")
@Data
public class AuditLogWithEntityTypeAndDetailPO extends BasePO implements Serializable {


    /**
     * 元数据id
     */
    public Integer entityId;

    /**
     * 元数据父id
     */
    public Integer parentId;

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

    /**
     * 元数据类型id tb_metadata_entity name
     */
    public String name;

    /**
     * 审计明细id tb_metadata_entity_audit_atrribute_change audit_id
     */
    public Integer auditId;

    /**
     * tb_metadata_entity_audit_atrribute_change
     */
    public String attribute;

    /**
     * tb_metadata_entity_audit_atrribute_change
     */
    public String beforeValue;

    /**
     * tb_metadata_entity_audit_atrribute_change
     */
    public String afterValue;


//    /**
//     * 发现时间
//     */
//    public LocalDateTime time;


}