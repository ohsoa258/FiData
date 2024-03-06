package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.system.dto.auditlogs.AuditLogsDTO;
import com.fisk.system.dto.auditlogs.AuditLogsPageDTO;
import com.fisk.system.entity.AuditLogsPO;

/**
 * @author 56263
 * @description 针对表【tb_audit_logs】的数据库操作Service
 * @createDate 2024-03-06 11:43:58
 */
public interface IAuditLogsService extends IService<AuditLogsPO> {

    /**
     * 保存一条操作记录
     *
     * @param dto
     * @return
     */
    Object saveAuditLog(AuditLogsDTO dto);

    /**
     * 分页查询操作记录
     *
     * @param dto
     * @return
     */
    Object pageFilterAudits(AuditLogsPageDTO dto);
}
