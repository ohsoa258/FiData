package com.fisk.datamanagement.mapper;

import com.fisk.datamanagement.entity.MetadataEntityAuditAttributeChangePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_audit_atrribute_change】的数据库操作Mapper
* @createDate 2024-03-14 11:48:08
* @Entity com.fisk.datamanagement.entity.MetadataEntityAuditAtrributeChangePO
*/
public interface MetadataEntityAuditAttributeChangeMapper extends BaseMapper<MetadataEntityAuditAttributeChangePO> {
    @Delete("truncate TABLE tb_metadata_entity_audit_atrribute_change")
    int truncateTable();
}




