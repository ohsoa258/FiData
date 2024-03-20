package com.fisk.datamanagement.mapper;

import com.fisk.datamanagement.entity.MetadataEntityAuditLogPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_audit_log】的数据库操作Mapper
* @createDate 2024-03-14 11:44:11
* @Entity com.fisk.datamanagement.entity.MetadataEntityAuditLogPO
*/
public interface MetadataEntityAuditLogMapper extends BaseMapper<MetadataEntityAuditLogPO> {
    
    @Delete("truncate TABLE tb_metadata_entity_audit_log")
    int truncateTable();
}




