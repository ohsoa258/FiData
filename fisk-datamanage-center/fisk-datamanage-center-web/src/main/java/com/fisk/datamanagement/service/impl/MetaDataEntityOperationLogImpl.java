package com.fisk.datamanagement.service.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.entity.MetaDataEntityOperationLogPO;
import com.fisk.datamanagement.map.MetaDataEntityOperationLogMap;
import com.fisk.datamanagement.mapper.MetaDataEntityOperationLogMapper;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:59
 * @description
 */
@Service
@Slf4j
public class MetaDataEntityOperationLogImpl implements IMetaDataEntityOperationLog {

    @Resource
    private MetaDataEntityOperationLogMapper logMapper;

    @Resource
    UserClient userClient;

    @Override
    public void addOperationLog(MetaDataEntityOperationLogDTO logDTO) {
        try {
            MetaDataEntityOperationLogPO entityOperationLogPO = MetaDataEntityOperationLogMap.INSTANCES.logDtoToPo(logDTO);
            int flag = logMapper.insert(entityOperationLogPO);
            if (flag > 0) { //判断日志记录是否成功
                log.info("日志记录成功");
            } else {
                log.info("日志记录失败");
            }
        } catch (Exception e) {
            log.info("日志记录异常:{}", e);
        }
    }

    @Override
    public List<MetaDataEntityOperationLogDTO> selectLogList(Integer entityId, Integer typeId) {
        List<MetaDataEntityOperationLogDTO> dtos = new ArrayList<>();
        List<MetaDataEntityOperationLogPO> operationLogPOS = logMapper.selectOperationLog(entityId, typeId);
        for (MetaDataEntityOperationLogPO po : operationLogPOS) {
            MetaDataEntityOperationLogDTO dto = new MetaDataEntityOperationLogDTO();
            dto.setId(po.getId());
            dto.setMetadataEntityId(po.getMetadataEntityId());
            dto.setOperationType(po.getOperationType());
            dto.setBeforeChange(po.getBeforeChange());
            dto.setAfterChange(po.getAfterChange());

            //id替换为名称
            if (po.getOwner() != null) {
                if (isInteger(po.getOwner())) {
                    ResultEntity<UserDTO> resultEntity = userClient.getUserV2(Integer.parseInt(po.getOwner()));
                    if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
                        dto.setCreateUser(resultEntity.getData().getUsername());
                    }else {
                        dto.setCreateUser(po.getOwner());
                    }
                } else {
                    dto.setCreateUser(po.getOwner());
                }
            } else {
                dto.setCreateUser(po.getOwner());
            }

            dto.setCreateTime(po.getCreateTime());
            dto.setDelFlag(po.getDelFlag());
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 判断是否是整数
     *
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
