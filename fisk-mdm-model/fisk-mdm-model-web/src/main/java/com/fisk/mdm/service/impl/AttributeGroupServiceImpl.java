package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import com.fisk.mdm.dto.attributeGroup.UpdateAttributeGroupDTO;
import com.fisk.mdm.entity.AttributeGroupDetailsPO;
import com.fisk.mdm.entity.AttributeGroupPO;
import com.fisk.mdm.map.AttributeGroupMap;
import com.fisk.mdm.mapper.AttributeGroupDetailsMapper;
import com.fisk.mdm.mapper.AttributeGroupMapper;
import com.fisk.mdm.service.AttributeGroupService;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:35
 * @Version 1.0
 */
@Service
public class AttributeGroupServiceImpl implements AttributeGroupService {

    @Resource
    AttributeGroupMapper groupMapper;
    @Resource
    AttributeGroupDetailsMapper detailsMapper;

    @Override
    public AttributeGroupVO getDataByGroupId(Integer id) {
        AttributeGroupPO attributeGroupPo = groupMapper.selectById(id);
        if (attributeGroupPo == null){
            return null;
        }

        AttributeGroupVO attributeGroupVo = AttributeGroupMap.INSTANCES.groupPoToVo(attributeGroupPo);

        // 查询属性组字段信息
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getGroupId,id);
        List<AttributeGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(detailsPoList)){
            attributeGroupVo.setGroupDetailsList(AttributeGroupMap.INSTANCES.detailsPoToVoList(detailsPoList));
        }
        return attributeGroupVo;
    }

    @Override
    public ResultEnum updateData(UpdateAttributeGroupDTO dto) {
        AttributeGroupPO attributeGroupPo = AttributeGroupMap.INSTANCES.groupDtoToPo(dto);
        int res = groupMapper.updateById(attributeGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteGroupById(Integer id) {
        boolean attributeGroup = this.isExistAttributeGroup(id);
        if (attributeGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = groupMapper.deleteById(id);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除属性组下的数据
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getGroupId,id);
        int res1 = detailsMapper.deleteById(id);
        if (res1 <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addAttribute(AttributeGroupDetailsDTO dto) {
        boolean attributeGroup = this.isExistAttributeGroup(dto.getGroupId());
        if (attributeGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        AttributeGroupDetailsPO detailsPo = AttributeGroupMap.INSTANCES.detailsDtoToDto(dto);
        int res = detailsMapper.insert(detailsPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteAttribute(AttributeGroupDetailsDTO dto) {
        boolean attributeGroup = this.isExistAttributeGroup(dto.getGroupId());
        if (attributeGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = detailsMapper.deleteById(dto.getAttributeId());
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum addAttributeGroup(AttributeGroupDTO dto) {
        QueryWrapper<AttributeGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupPO::getModelId,dto.getModelId())
                .eq(AttributeGroupPO::getName,dto.getName())
                .last("limit 1");
        AttributeGroupPO groupPo = groupMapper.selectOne(queryWrapper);
        if (groupPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        AttributeGroupPO attributeGroupPo = AttributeGroupMap.INSTANCES.groupDtoToPo(dto);
        int res = groupMapper.insert(attributeGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 判断属性组是否存在
     * @param id
     * @return
     */
    public boolean isExistAttributeGroup(Integer id){
        AttributeGroupPO attributeGroupPo = groupMapper.selectById(id);
        if (attributeGroupPo == null){
            return false;
        }

        return true;
    }
}
