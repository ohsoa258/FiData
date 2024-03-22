package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * 
 * @author JinXingWang
 * @TableName tb_metadata_entity_audit_atrribute_change
 */
@TableName(value ="tb_metadata_entity_audit_atrribute_change")
@Data
public class MetadataEntityAuditAttributeChangePO extends BasePO implements Serializable {

    /**
     * 
     */
    private Integer auditId;

    /**
     * 
     */
    private String attribute;

    /**
     * 
     */
    private String beforeValue;

    /**
     * 
     */
    private String afterValue;

}