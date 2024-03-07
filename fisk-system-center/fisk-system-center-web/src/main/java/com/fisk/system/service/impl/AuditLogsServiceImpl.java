package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.system.dto.auditlogs.AuditLogsDTO;
import com.fisk.system.dto.auditlogs.AuditLogsPageDTO;
import com.fisk.system.entity.AuditLogsPO;
import com.fisk.system.map.AuditLogsMap;
import com.fisk.system.mapper.AuditLogsMapper;
import com.fisk.system.service.IAuditLogsService;
import org.springframework.stereotype.Service;

/**
 * @author 56263
 * @description 针对表【tb_audit_logs】的数据库操作Service实现
 * @createDate 2024-03-06 11:43:58
 */
@Service
public class AuditLogsServiceImpl extends ServiceImpl<AuditLogsMapper, AuditLogsPO>
        implements IAuditLogsService {

    /**
     * 保存一条操作记录
     *
     * @param dto
     * @return
     */
    @Override
    public Object saveAuditLog(AuditLogsDTO dto) {
        AuditLogsPO auditLogsPO = AuditLogsMap.INSTANCES.dtoToPo(dto);
        return save(auditLogsPO);
    }

    /**
     * 分页查询操作记录
     *
     * @param dto
     * @return
     */
    @Override
    public Object pageFilterAudits(AuditLogsPageDTO dto) {
        Page<AuditLogsPO> auditLogsPOPage = new Page<>(dto.getCurrent(),dto.getSize());
        return page(auditLogsPOPage);
    }
}




