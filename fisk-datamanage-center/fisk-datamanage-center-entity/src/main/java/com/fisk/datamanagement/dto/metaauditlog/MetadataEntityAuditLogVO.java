package com.fisk.datamanagement.dto.metaauditlog;

import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JinXingWang
 */
public class MetadataEntityAuditLogVO {
    /**
     * 元数据qualified_name
     */
    public Integer entityId;

    /**
     * 类型 1 新增 2 修改 3 删除
     */
    public MetadataAuditOperationTypeEnum operationType;

    /**
     * 变更属性
     */
    public List<MetadataEntityAuditAttributeChangeVO> attribute;


    public String createTime;
}
