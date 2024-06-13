package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.auditlogs.AuditLogsDTO;
import com.fisk.system.dto.auditlogs.AuditLogsPageDTO;
import com.fisk.system.service.IAuditLogsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lsj
 * @version v1.0
 * @description 审计日志控制器
 * @date 2024年3月6日11点46分
 */
@Api(tags = {SwaggerConfig.AUDIT_LOGS})
@RestController
@RequestMapping("/auditLogs")
public class AuditLogsController {

    @Resource
    IAuditLogsService auditLogsService;

    /**
     * 保存一条操作记录
     *
     * @param dto
     * @return
     */
    @ApiOperation("保存一条操作记录")
    @PostMapping("/saveAuditLog")
    public ResultEntity<Object> saveAuditLog(@RequestBody AuditLogsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, auditLogsService.saveAuditLog(dto));
    }

    /**
     * 分页查询操作记录
     *
     * @param dto
     * @return
     */
    @ApiOperation("分页查询操作记录")
    @PostMapping("/pageFilterAudits")
    public ResultEntity<Object> pageFilterAudits(@RequestBody AuditLogsPageDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, auditLogsService.pageFilterAudits(dto));
    }

}
