package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogUpdateDTO;
import com.fisk.mdm.dto.attributelog.AttributeRollbackDTO;
import com.fisk.mdm.entity.AttributeLogPO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.map.AttributeLogMap;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.mapper.AttributeLogMapper;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.service.AttributeGroupService;
import com.fisk.mdm.service.AttributeLogService;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
    AttributeMapper attributeMapper;
    @Resource
    UserClient userClient;
    @Resource
    AttributeLogService attributeLogService;
    @Resource
    AttributeGroupService groupService;

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
                .eq(AttributeLogPO::getAttributeId,attributeId)
                .orderByDesc(AttributeLogPO::getCreateTime);
        List<AttributeLogPO> list = logMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)){

            // 转换dto
            List<AttributeLogDTO> dtoList = AttributeLogMap.INSTANCES.logPoToDto(list);

            // 获取属性组
            dtoList.stream().filter(e -> e.getId() != null).forEach(e -> {
                List<AttributeGroupDTO> attributeGroupList = groupService.getDataByAttributeId(e.getAttributeId());
                e.setAttributeGroupList(attributeGroupList);
            });

            // 获取创建人、修改人
            ReplenishUserInfo.replenishUserName(dtoList, userClient, UserFieldEnum.USER_ACCOUNT);

            dtoList.stream().map(e -> {
                e.setDataTypeEnDisplay(DataTypeEnum.getValue(e.getDataType()).name());
                return e;
            }).collect(Collectors.toList());

            return dtoList;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum rollbackData(AttributeRollbackDTO dto) {
        AttributePO attributePo = attributeMapper.selectById(dto.getAttributeId());
        AttributeStatusEnum status = attributePo.getStatus();
        if (!status.equals(AttributeStatusEnum.SUBMITTED)){
            // 1.回滚需要回滚的日志表数据
            AttributeLogPO attributeLogPo = logMapper.selectById(dto.getId());
            AttributePO attribute = AttributeMap.INSTANCES.poToLogPo(attributeLogPo);
            attribute.setId(dto.getAttributeId());
            int res = attributeMapper.updateById(attribute);

            // 2.记录回滚前的最新数据
            AttributeLogSaveDTO dto1 = AttributeMap.INSTANCES.poToLogDto(attributePo);
            dto1.setAttributeId(dto.getAttributeId());
            attributeLogService.saveAttributeLog(dto1);
            return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }else {
            return ResultEnum.POSTULATES_NOT_ROLLBACK;
        }
    }

    @Override
    public ResultEnum updateAttributeLog(AttributeLogUpdateDTO dto) {
        AttributeLogPO attributeLogPo = logMapper.selectById(dto.getId());
        if (attributeLogPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = logMapper.updateById(AttributeLogMap.INSTANCES.dtoToUpdatePo(dto));
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
