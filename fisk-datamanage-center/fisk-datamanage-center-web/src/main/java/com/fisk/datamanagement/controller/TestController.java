package com.fisk.datamanagement.controller;

import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 11:28
 * @description
 */
@RestController
public class TestController {
    @Resource
    private IMetaDataEntityOperationLog iMetaDataEntityOperationLog;

    @RequestMapping("/add")
    public boolean saveLog(MetaDataEntityOperationLogDTO dto){
        return iMetaDataEntityOperationLog.addOperationLog(dto);
    }
    @GetMapping("/select")
    public List<MetaDataEntityOperationLogDTO> select(Integer entityId,Integer typeId){
        return iMetaDataEntityOperationLog.selectLogList(entityId,typeId);
    }
}
