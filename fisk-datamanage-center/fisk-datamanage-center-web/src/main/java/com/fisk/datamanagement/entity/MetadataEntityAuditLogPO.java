package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.dbdatatype.OpenEdgeTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author JinXingWang
 * @TableName tb_metadata_entity_audit_log
 */
@TableName(value ="tb_metadata_entity_audit_log")
@Data
public class MetadataEntityAuditLogPO extends BasePO implements Serializable {


    /**
     * 元数据qualified_name
     */
    private Integer entityId;

    /**
     * 类型 1 新增 2 修改 3 删除
     */
    private MetadataAuditOperationTypeEnum operationType;




}