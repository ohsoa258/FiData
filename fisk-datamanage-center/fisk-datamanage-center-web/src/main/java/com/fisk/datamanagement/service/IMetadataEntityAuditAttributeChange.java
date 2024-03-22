package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditAttributeChangeVO;
import com.fisk.datamanagement.entity.MetadataEntityAuditAttributeChangePO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_audit_atrribute_change】的数据库操作Service
* @createDate 2024-03-14 11:48:08
*/
public interface IMetadataEntityAuditAttributeChange extends IService<MetadataEntityAuditAttributeChangePO> {
    List<MetadataEntityAuditAttributeChangeVO> getAttributeChange(Integer auditId);
}
