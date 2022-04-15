package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeQueryDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.model.ModelVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author WangYan
 * @date 2022/4/5 14:49
 */
@Service
public class AttributeServiceImpl extends ServiceImpl<AttributeMapper, AttributePO> implements AttributeService {

    @Resource
    EventLogService logService;

    @Override
    public ResultEntity<AttributeVO> getById(Integer id) {
        AttributeVO attributeVO = AttributeMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if(Objects.isNull(attributeVO)){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, attributeVO);
    }

    @Override
    public ResultEnum addData(AttributeDTO attributeDTO) {
        //判断名称是否存在
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",attributeDTO.getName());
        if(baseMapper.selectOne(wrapper) != null){
            return ResultEnum.NAME_EXISTS;
        }

        //添加数据
        AttributePO attributePO = AttributeMap.INSTANCES.dtoToPo(attributeDTO);
        attributePO.setStatus(AttributeStatusEnum.INSERT);
        if (baseMapper.insert(attributePO) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 记录日志
        String desc = "新增一个属性,id:" + attributePO.getId();
        if (logService.saveEventLog((int)attributePO.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.SAVE,desc) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //创建成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(AttributeUpdateDTO attributeUpdateDTO) {
        AttributePO attributePO = baseMapper.selectById(attributeUpdateDTO.getId());

        //判断数据是否存在
        if (attributePO == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",attributeUpdateDTO.getName());
        if(baseMapper.selectOne(wrapper) != null){
            return ResultEnum.NAME_EXISTS;
        }

        //把DTO转化到查询出来的PO上
        attributePO = AttributeMap.INSTANCES.updateDtoToPo(attributeUpdateDTO);

        //修改数据
        attributePO.setStatus(AttributeStatusEnum.UPDATE);
        if (baseMapper.updateById(attributePO) <= 0) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "修改一个属性,id:" + attributeUpdateDTO.getId();
        if (logService.saveEventLog((int)attributePO.getId(),ObjectTypeEnum.ATTRIBUTES,EventTypeEnum.UPDATE,desc) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //添加成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteDataById(Integer id) {
        //判断数据是否存在
        if (baseMapper.selectById(id) == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //删除数据
        if (baseMapper.deleteById(id) <= 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 记录日志
        String desc = "删除一个属性,id:" + id;

        if (logService.saveEventLog(id,ObjectTypeEnum.ATTRIBUTES,EventTypeEnum.DELETE,desc) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //删除成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public Page<AttributeVO> getAll(AttributeQueryDTO query) {

        Page<AttributeVO> all = baseMapper.getAll(query.page, query);


        return all;
    }
}
