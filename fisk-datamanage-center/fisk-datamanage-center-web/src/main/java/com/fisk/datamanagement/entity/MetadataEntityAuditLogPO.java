package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.dbdatatype.OpenEdgeTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    public Integer entityId;

    /**
     * 类型 1 新增 2 修改 3 删除
     */
    public MetadataAuditOperationTypeEnum operationType;

//    /**
//     * 发现时间
//     */
//    public LocalDateTime time;




}