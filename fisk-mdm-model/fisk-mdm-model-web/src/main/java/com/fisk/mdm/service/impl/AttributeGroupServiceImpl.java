package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.attributeGroup.*;
import com.fisk.mdm.entity.AttributeGroupDetailsPO;
import com.fisk.mdm.entity.AttributeGroupPO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.AttributeGroupMap;
import com.fisk.mdm.mapper.AttributeGroupDetailsMapper;
import com.fisk.mdm.mapper.AttributeGroupMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeGroupService;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupDropDownVO;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;
import com.fisk.mdm.vo.attributeGroup.QueryAttributeGroupVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Resource
    AttributeGroupService attributeGroupService;
    @Resource
    UserClient userClient;
    @Resource
    AttributeService attributeService;
    @Resource
    EntityMapper entityMapper;

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
            List<AttributeGroupDetailsDTO> collect = detailsPoList.stream().map(e -> {
                AttributeVO data = attributeService.getById(e.getAttributeId()).getData();
                AttributeGroupDetailsDTO dto = AttributeGroupMap.INSTANCES.detailsPoToDto(e);
                if (data != null){
                    dto.setName(data.getName());
                    dto.setDisplayName(data.getDisplayName());
                    dto.setDesc(data.getDesc());
                    dto.setDataType(data.getDataType());
                    dto.setDataTypeLength(data.getDataTypeLength());
                    dto.setDataTypeDecimalLength(data.getDataTypeDecimalLength());
                }
                return dto;
            }).collect(Collectors.toList());
            attributeGroupVo.setGroupDetailsList(collect);
        }
        return attributeGroupVo;
    }

    @Override
    public List<AttributeGroupVO> getDataByModelId(Integer modelId) {
        QueryWrapper<AttributeGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupPO::getModelId,modelId);
        List<AttributeGroupPO> groupPoList = groupMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(groupPoList)){
            List<AttributeGroupVO> collect = groupPoList.stream().filter(e -> e.getId() != 0).map(e -> {
                AttributeGroupVO attributeGroupVo = attributeGroupService.getDataByGroupId((int) e.getId());
                return attributeGroupVo;
            }).collect(Collectors.toList());

            // 获取创建人、修改人
            ReplenishUserInfo.replenishUserName(collect, userClient, UserFieldEnum.USER_ACCOUNT);
            return collect;
        }

        return null;
    }

    @Override
    public ResultEnum updateData(AttributeGroupUpdateDTO dto) {
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
        int res1 = detailsMapper.delete(queryWrapper);
        if (res1 <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAttribute(AttributeGroupDetailsAddDTO dto) {

        // 删除属性组下的实体数据
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getGroupId,dto.getGroupId())
                .eq(AttributeGroupDetailsPO::getEntityId,dto.getEntityId());
        detailsMapper.delete(queryWrapper);

        // 新增属性
        AttributeGroupDetailsDTO detailsDto = new AttributeGroupDetailsDTO();
        detailsDto.setGroupId(dto.getGroupId());
        detailsDto.setEntityId(dto.getEntityId());
        dto.getAttributeId().stream().forEach(e -> {
            detailsDto.setAttributeId(e);
            AttributeGroupDetailsPO detailsPo1 = AttributeGroupMap.INSTANCES.detailsDtoToDto(detailsDto);
            int res = detailsMapper.insert(detailsPo1);
            if (res <= 0){
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        });

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteAttribute(AttributeGroupDetailsDTO dto) {
        boolean attributeGroup = this.isExistAttributeGroup(dto.getGroupId());
        if (attributeGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getGroupId,dto.getGroupId())
                .eq(AttributeGroupDetailsPO::getAttributeId,dto.getAttributeId());
        int res = detailsMapper.delete(queryWrapper);
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

    @Override
    public List<AttributeGroupDTO> getDataByAttributeId(Integer attributeId) {
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getAttributeId,attributeId);
        List<AttributeGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(detailsPoList)){
            List<AttributeGroupDTO> collect = detailsPoList.stream().map(e -> {
                AttributeGroupPO groupPo = groupMapper.selectById(e.getGroupId());
                AttributeGroupDTO groupDto = AttributeGroupMap.INSTANCES.poToDto(groupPo);
                return groupDto;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    @Override
    public List<QueryAttributeGroupVO> getDataGroupById(Integer id) {
        AttributeGroupPO attributeGroupPo = groupMapper.selectById(id);
        if (attributeGroupPo == null){
            return null;
        }

        // 查询属性组下的属性
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getGroupId,id);
        List<AttributeGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        List<QueryAttributeGroupVO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(detailsPoList)){
            // 根据实体id进行分组
            Map<Integer, List<AttributeGroupDetailsPO>> listMap = detailsPoList.stream().collect(Collectors.groupingBy(AttributeGroupDetailsPO::getEntityId));
            for (Integer key : listMap.keySet()) {
                // 拼接所需参数
                List<AttributeGroupDetailsPO> detailsList = listMap.get(key);
                EntityPO entityPo = entityMapper.selectById(key);
                List<AttributeGroupDetailsDTO> collect = detailsList.stream().map(e -> {
                    AttributeVO data = attributeService.getById(e.getAttributeId()).getData();
                    AttributeGroupDetailsDTO dto = AttributeGroupMap.INSTANCES.detailsPoToDto(e);
                    if (data != null){
                        dto.setType(ObjectTypeEnum.ATTRIBUTES.getName());
                        dto.setName(data.getName());
                        dto.setDisplayName(data.getDisplayName());
                        dto.setDesc(data.getDesc());
                        dto.setDataType(data.getDataType());
                        dto.setDataTypeLength(data.getDataTypeLength());
                        dto.setDataTypeDecimalLength(data.getDataTypeDecimalLength());
                    }
                    return dto;
                }).collect(Collectors.toList());

                // 组装参数
                QueryAttributeGroupVO attributeGroupVo = new QueryAttributeGroupVO();
                attributeGroupVo.setId(key);
                if (entityPo != null){
                    attributeGroupVo.setName(entityPo.getName());
                }
                attributeGroupVo.setType(ObjectTypeEnum.ENTITY.getName());
                attributeGroupVo.setDetailsDtoList(collect);
                list.add(attributeGroupVo);
            }
        }
        return list;
    }

    @Override
    public List<AttributeInfoDTO> getAttributeExists(AttributeInfoQueryDTO dto) {
        return groupMapper.getAttributeExists(dto.getGroupId(),dto.getEntityId());
    }

    /**
     * 判断属性组是否存在
     * @param id
     * @return
     */
    public boolean isExistAttributeGroup(Integer id) {
        AttributeGroupPO attributeGroupPo = groupMapper.selectById(id);
        if (attributeGroupPo == null) {
            return false;
        }

        return true;
    }

    /**
     * 根据模型id,获取属性组列表
     *
     * @param modelId
     * @return
     */
    public List<AttributeGroupDropDownVO> getAttributeGroupByModelId(Integer modelId) {
        QueryWrapper<AttributeGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(AttributeGroupPO::getModelId, modelId);
        List<AttributeGroupPO> list = groupMapper.selectList(queryWrapper);
        return AttributeGroupMap.INSTANCES.groupListPoToVoList(list);
    }

    /**
     * 根据属性组id/实体id,获得属性集合
     *
     * @param groupId
     * @param entityId
     * @return
     */
    public List<Integer> getAttributeGroupAttribute(Integer groupId, Integer entityId) {
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").select("attribute_id")
                .lambda()
                .eq(AttributeGroupDetailsPO::getGroupId, groupId)
                .eq(AttributeGroupDetailsPO::getEntityId, entityId);
        List<Integer> attributeIds = (List) detailsMapper.selectObjs(queryWrapper);
        return attributeIds;
    }

    /**
     * 根据属性组id,获取详情
     *
     * @param groupId
     * @return
     */
    public AttributeGroupDTO getAttributeGroup(Integer groupId) {
        AttributeGroupPO po = groupMapper.selectById(groupId);
        if (po == null) {
            return null;
        }
        return AttributeGroupMap.INSTANCES.poToDto(po);
    }

}
