package com.fisk.datamanagement.controller;

import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-09 11:48
 * @description 操作日志
 */
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
    @RequestMapping("/addOperateLog")
    public void saveLog(MetaDataEntityOperationLogDTO dto){
         iMetaDataEntityOperationLog.addOperationLog(dto);
    }
}
