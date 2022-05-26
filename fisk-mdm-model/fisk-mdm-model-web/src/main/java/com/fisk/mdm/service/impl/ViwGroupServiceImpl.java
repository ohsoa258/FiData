package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.entity.EntityQueryDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupQueryDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupUpdateDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDetailsDTO;
import com.fisk.mdm.entity.ViwGroupDetailsPO;
import com.fisk.mdm.entity.ViwGroupPO;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.ViwGroupMap;
import com.fisk.mdm.mapper.ViwGroupDetailsMapper;
import com.fisk.mdm.mapper.ViwGroupMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.ViwGroupService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.viwGroup.ViewGroupDropDownVO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:27
 * @Version 1.0
 */
@Service
public class ViwGroupServiceImpl implements ViwGroupService {

    @Resource
    ViwGroupMapper viwGroupMapper;
    @Resource
    ViwGroupDetailsMapper detailsMapper;
    @Resource
    ViwGroupService viwGroupService;
    @Resource
    EntityService entityService;
    @Resource
    AttributeService attributeService;

    @Override
    public ViwGroupVO getDataByGroupId(Integer id) {
        ViwGroupPO viwGroupPo = viwGroupMapper.selectById(id);
        if (viwGroupPo == null){
            return null;
        }

        ViwGroupVO viwGroupVo = ViwGroupMap.INSTANCES.groupPoToVo(viwGroupPo);

        // 查询视图组
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,id);
        List<ViwGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(detailsPoList)){
            viwGroupVo.setGroupDetailsList(ViwGroupMap.INSTANCES.detailsPoToDtoList(detailsPoList));
        }

        return viwGroupVo;
    }

    @Override
    public List<ViwGroupVO> getDataByEntityId(Integer entityId) {
        if (entityId == null){
            return null;
        }

        QueryWrapper<ViwGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupPO::getEntityId,entityId);
        List<ViwGroupPO> viwGroupPoList = viwGroupMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(viwGroupPoList)){
            List<ViwGroupVO> collect = viwGroupPoList.stream().filter(e -> e.getId() != 0).map(e -> {
                ViwGroupVO viwGroupVo = viwGroupService.getDataByGroupId((int) e.getId());
                return viwGroupVo;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    @Override
    public ResultEnum addViwGroup(ViwGroupDTO dto) {
        QueryWrapper<ViwGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupPO::getEntityId,dto.getEntityId())
                .eq(ViwGroupPO::getName,dto.getName())
                .last("limit 1");
        ViwGroupPO groupPo = viwGroupMapper.selectOne(queryWrapper);
        if (groupPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        ViwGroupPO viwGroupPo = ViwGroupMap.INSTANCES.groupDtoToPo(dto);
        int res = viwGroupMapper.insert(viwGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateData(ViwGroupUpdateDTO dto) {
        ViwGroupPO viwGroupPo = ViwGroupMap.INSTANCES.groupUpdateDtoToPo(dto);
        int res = viwGroupMapper.updateById(viwGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteGroupById(Integer id) {
        boolean existViwGroup = this.isExistViwGroup(id);
        if (existViwGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = viwGroupMapper.deleteById(id);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除组下的数据
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,id);
        int res1 = detailsMapper.delete(queryWrapper);
        if (res1 <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAttribute(ViwGroupDetailsDTO dtoList) {
        boolean existViwGroup = this.isExistViwGroup(dtoList.getGroupId());
        if (existViwGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 同一个属性组下面数据不能重复
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,dtoList.getGroupId())
                .eq(ViwGroupDetailsPO::getAliasName,dtoList.getAliasName())
                .eq(ViwGroupDetailsPO::getAttributeId,dtoList.getAttributeId())
                .last("limit 1");
        ViwGroupDetailsPO viwGroupDetailsPo = detailsMapper.selectOne(queryWrapper);
        if (viwGroupDetailsPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        ViwGroupDetailsPO detailsPo1 = ViwGroupMap.INSTANCES.detailsDtoToDto(dtoList);
        int res = detailsMapper.insert(detailsPo1);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public EntityQueryDTO getRelationByEntityId(ViwGroupQueryDTO dto) {
        // 查询出视图组中的属性
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,dto.getGroupId());
        List<ViwGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        // 视图组id集合
        List<Integer> attributeIds = detailsPoList.stream().filter(e -> e.getAttributeId() != null).map(e -> e.getAttributeId()).collect(Collectors.toList());

        // 查询出域字段关联的实体
        EntityQueryDTO attributeInfo = this.getAttributeInfo(dto.getEntityId(),attributeIds);
        return attributeInfo;
    }

    /**
     * 根据实体id获取属性,拼接成需要返回的参数
     * @param entityId
     * @param attributeIds
     * @return
     */
    public EntityQueryDTO getAttributeInfo(Integer entityId,List<Integer> attributeIds){
        EntityInfoVO entityInfoVo = entityService.getAttributeById(entityId);
        if (entityInfoVo == null){
            return null;
        }

        EntityQueryDTO dto = new EntityQueryDTO();
        dto.setId(entityInfoVo.getId());
        dto.setName(entityInfoVo.getName());
        dto.setType(ObjectTypeEnum.ENTITY.getName());

        // 属性信息
        List<AttributeInfoDTO> attributeList = entityInfoVo.getAttributeList();
        List<EntityQueryDTO> collect = attributeList.stream().filter(e -> e.getDomainId() == null).map(e -> {
            EntityQueryDTO dto1 = new EntityQueryDTO();
            dto1.setId(e.getId());
            dto1.setName(e.getName());
            dto1.setType(ObjectTypeEnum.ATTRIBUTES.getName());

            // 判断是否在视图组中存在
            if (attributeIds.contains(e.getId())){
                dto1.setIsCheck(1);
            }else {
                dto1.setIsCheck(0);
            }

            return dto1;
        }).collect(Collectors.toList());

        // 域字段递归
        List<EntityQueryDTO> doMainList = attributeList.stream().filter(e -> e.getDomainId() != null).map(e -> {
            AttributeVO data = attributeService.getById(e.getDomainId()).getData();
            EntityQueryDTO attributeInfo = this.getAttributeInfo(data.getEntityId(),attributeIds);
            return attributeInfo;
        }).collect(Collectors.toList());
        collect.addAll(doMainList);
        dto.setChildren(collect);

        return dto;
    }

    /**
     * 判断自定义视图组是否存在
     * @param id
     * @return
     */
    public boolean isExistViwGroup(Integer id) {
        ViwGroupPO viwGroupPo = viwGroupMapper.selectById(id);
        if (viwGroupPo == null) {
            return false;
        }

        return true;
    }

    /**
     * 根据实体id,获取自定义视图
     *
     * @param entityId
     * @return
     */
    public List<ViewGroupDropDownVO> getViewGroupByEntityId(Integer entityId) {
        QueryWrapper<ViwGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ViwGroupPO::getEntityId, entityId);
        List<ViwGroupPO> poList = viwGroupMapper.selectList(queryWrapper);
        return ViwGroupMap.INSTANCES.poListToDropDownVo(poList);
    }

}
