package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamanagement.dto.standards.StandardsTreeDTO;
import com.fisk.datamanagement.entity.StandardsMenuPO;
import com.fisk.datamanagement.map.StandardsMenuMap;
import com.fisk.datamanagement.mapper.StandardsMenuMapper;
import com.fisk.datamanagement.service.StandardsMenuService;
import com.fisk.datamanagement.service.StandardsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service("standardsMenuService")
public class StandardsMenuServiceImpl extends ServiceImpl<StandardsMenuMapper, StandardsMenuPO> implements StandardsMenuService {

    @Resource
    StandardsService standardsService;
    @Override
    public List<StandardsTreeDTO> getStandardsTree() {
        List<StandardsMenuPO> standardsMenus = this.list();
        if (CollectionUtils.isEmpty(standardsMenus)){
            return new ArrayList<>();
        }
        List<StandardsTreeDTO> allList = standardsMenus.stream().map(i -> {
            StandardsTreeDTO standardsTreeDTO = new StandardsTreeDTO();
            standardsTreeDTO.setId((int)i.getId());
            standardsTreeDTO.setPid(i.getPid());
            standardsTreeDTO.setName(i.getName());
            standardsTreeDTO.setType(i.getType());
            standardsTreeDTO.setSort(i.getSort());
            standardsTreeDTO.setCreateTime(i.getCreateTime());
            return standardsTreeDTO;
        }).collect(Collectors.toList());
        List<StandardsTreeDTO> parentList = allList.stream().filter(item -> item.getPid() == null || item.getPid() == 0).collect(Collectors.toList());
        if (parentList.size() > 1){
            parentList.sort(Comparator.comparing(StandardsTreeDTO::getCreateTime).reversed());
        }
        // 递归处理子集
        standardsTree(allList, parentList);
        return parentList;
    }

    private void standardsTree(List<StandardsTreeDTO> allList,List<StandardsTreeDTO> parentList){
        // 遍历父级
        for (StandardsTreeDTO parent : parentList){
            // 子集容器
            List<StandardsTreeDTO> children = new ArrayList<>();
            for (StandardsTreeDTO sub : allList){
                if (parent.getId().equals(sub.getPid())){
                    children.add(sub);
                }
                // 递归处理
                standardsTree(allList, children);
                children.sort(Comparator.comparing(StandardsTreeDTO::getCreateTime).reversed());
            }
            // 加入父级
            parent.setChildren(children);
        }
    }

    @Override
    public ResultEnum addorUpdateStandardsMenu(StandardsMenuDTO standardsMenuDTO) {
        LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StandardsMenuPO::getPid,standardsMenuDTO.getPid());
        queryWrapper.orderByDesc(StandardsMenuPO::getSort);
        queryWrapper.last("LIMIT 1");
        StandardsMenuPO tragetMenu = getOne(queryWrapper);
        if (tragetMenu == null){
            standardsMenuDTO.setSort(1);
        }else {
            standardsMenuDTO.setSort(tragetMenu.getSort()+1);
        }
        if (standardsMenuDTO.getName() == null || standardsMenuDTO.getType() == null){
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        StandardsMenuPO standardsMenuPO = StandardsMenuMap.INSTANCES.dtoToPo(standardsMenuDTO);
        if (standardsMenuDTO.getId() == null || standardsMenuDTO.getId() == 0){
            save(standardsMenuPO);
        }else {
            updateById(standardsMenuPO);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delStandardsMenu(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<StandardsMenuPO> StandardsMenus = listByIds(ids);
        List<StandardsMenuPO> StandardsDatas = StandardsMenus.stream().filter(i -> i.getType() == 2).collect(Collectors.toList());
        List<Integer> StandardsDataIds = StandardsDatas.stream().map(i->(int)i.getId()).collect(Collectors.toList());
        removeByIds(ids);
        if (!CollectionUtils.isEmpty(StandardsDataIds)){
            standardsService.delStandards(StandardsDataIds);
        }
        return ResultEnum.SUCCESS;
    }
}
