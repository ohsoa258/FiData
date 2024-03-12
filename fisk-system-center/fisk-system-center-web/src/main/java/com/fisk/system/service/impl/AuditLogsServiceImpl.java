package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.system.dto.auditlogs.AuditLogQueryType;
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
        Page<AuditLogsPO> auditLogsPOPage = new Page<>(dto.getCurrent(), dto.getSize());
        AuditLogQueryType queryType = dto.getQueryType();

        Page<AuditLogsPO> page = null;
        LambdaQueryWrapper<AuditLogsPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AuditLogsPO::getCreateTime);
        //如果查询条件不为空
        if (queryType != null) {

            if (queryType.getUsername() != null) {
                wrapper.eq(AuditLogsPO::getUsername, queryType.getUsername());
            }
            if (queryType.getServiceType() != null) {
                wrapper.eq(AuditLogsPO::getServiceType, queryType.getServiceType());
            }
            if (queryType.getRequestType() != null) {
                wrapper.eq(AuditLogsPO::getRequestType, queryType.getRequestType());
            }
            if (queryType.getRequestAddr() != null) {
                wrapper.like(AuditLogsPO::getRequestAddr, queryType.getRequestAddr());
            }
            if (queryType.getIpAddr() != null) {
                wrapper.eq(AuditLogsPO::getIpAddr, queryType.getIpAddr());
            }
        }
        page = page(auditLogsPOPage, wrapper);
        return page;
    }
}




