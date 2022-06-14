package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attributelog.AttributeLogDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
import com.fisk.mdm.entity.AttributeLogPO;
import com.fisk.mdm.map.AttributeLogMap;
import com.fisk.mdm.mapper.AttributeLogMapper;
import com.fisk.mdm.service.AttributeLogService;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/14 14:58
 * @Version 1.0
 */
@Service
public class AttributeLogServiceImpl implements AttributeLogService {

    @Resource
    AttributeLogMapper logMapper;
    @Resource
    UserClient userClient;

    @Override
    public ResultEnum saveAttributeLog(AttributeLogSaveDTO dto) {
        if (dto == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = logMapper.insert(AttributeLogMap.INSTANCES.dtoToPo(dto));
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataByAttributeId(Integer attributeId) {
        QueryWrapper<AttributeLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeLogPO::getAttributeId,attributeId)
                .last(" limit 1");
        AttributeLogPO attributeLogPo = logMapper.selectOne(queryWrapper);
        if (attributeLogPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<AttributeLogPO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda()
                .eq(AttributeLogPO::getAttributeId,attributeId);
        int res = logMapper.delete(queryWrapper1);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(Integer id) {
        AttributeLogPO attributeLogPo = logMapper.selectById(id);
        if (attributeLogPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = logMapper.deleteById(id);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<AttributeLogDTO> queryDataByAttributeId(Integer attributeId) {
        QueryWrapper<AttributeLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeLogPO::getAttributeId,attributeId);
        List<AttributeLogPO> list = logMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)){

            // 转换dto
            List<AttributeLogDTO> dtoList = AttributeLogMap.INSTANCES.logPoToDto(list);

            // 获取创建人、修改人
            ReplenishUserInfo.replenishUserName(dtoList, userClient, UserFieldEnum.USER_ACCOUNT);

            return dtoList;
        }
        return null;
    }
}
