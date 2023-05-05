package com.fisk.datamanagement.controller;

import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-09 11:48
 * @description 操作日志
 */
@Api(tags = {SwaggerConfig.OperateLog})
@RestController
@RequestMapping("/OperateLog")
public class OperateLogController {
    @Resource
    private IMetaDataEntityOperationLog iMetaDataEntityOperationLog;

    /**
     * 记录日志信息
     * @param dto
     * @return
     */
    @ApiOperation("记录日志信息")
    @RequestMapping("/addOperateLog")
    public void saveLog(@RequestBody MetaDataEntityOperationLogDTO dto){
         iMetaDataEntityOperationLog.addOperationLog(dto);
    }
}
