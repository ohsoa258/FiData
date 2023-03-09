package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.entity.MetaDataEntityOperationLogPO;
import com.fisk.datamanagement.map.MetaDataEntityOperationLogMap;
import com.fisk.datamanagement.mapper.MetaDataEntityOperationLogMapper;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:59
 * @description
 */
@Service
public class MetaDataEntityOperationLogImpl implements IMetaDataEntityOperationLog {
    @Resource
    private MetaDataEntityOperationLogMapper logMapper;


    @Override
    public boolean addOperationLog(MetaDataEntityOperationLogDTO logDTO) {

        if(logDTO!=null){
            MetaDataEntityOperationLogPO entityOperationLogPO = MetaDataEntityOperationLogMap.INSTANCES.logDtoToPo(logDTO);
            int flag = logMapper.insert(entityOperationLogPO);
            if(flag>0){ //判断日志记录是否成功
                return true;
            }
        }
        return false;
    }

    @Override
    public List<MetaDataEntityOperationLogDTO> selectLogList(Integer entityId,Integer typeId) {

        List<MetaDataEntityOperationLogPO> operationLogPOS = logMapper.selectOperationLog(entityId,typeId);
        List<MetaDataEntityOperationLogDTO> logDTOS = MetaDataEntityOperationLogMap.INSTANCES.logPoToDto(operationLogPOS);
        return logDTOS;
    }
}
