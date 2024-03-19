package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.entity.MetadataEntityAuditLogPO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_audit_log】的数据库操作Service
* @createDate 2024-03-14 11:44:11
*/
public interface IMetadataEntityAuditLog extends IService<MetadataEntityAuditLogPO> {

    ResultEnum setMetadataAuditLog(Object object, Integer entityId, MetadataAuditOperationTypeEnum operationType, String rdbmsType);
}
