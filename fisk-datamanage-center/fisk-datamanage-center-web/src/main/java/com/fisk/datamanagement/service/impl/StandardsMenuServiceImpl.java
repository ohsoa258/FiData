package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamanagement.dto.standards.StandardsTreeDTO;
import com.fisk.datamanagement.entity.StandardsMenuPO;
import com.fisk.datamanagement.entity.StandardsPO;
import com.fisk.datamanagement.map.StandardsMenuMap;
import com.fisk.datamanagement.mapper.StandardsMenuMapper;
import com.fisk.datamanagement.service.StandardsMenuService;
import com.fisk.datamanagement.service.StandardsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("standardsMenuService")
public class StandardsMenuServiceImpl extends ServiceImpl<StandardsMenuMapper, StandardsMenuPO> implements StandardsMenuService {

    @Resource
    StandardsService standardsService;

    @Override
    public List<StandardsTreeDTO> getStandardsTree() {
        LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StandardsMenuPO::getType, 1);
        List<StandardsMenuPO> standardsMenus = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(standardsMenus)) {
            return new ArrayList<>();
        }
        List<StandardsTreeDTO> allList = standardsMenus.stream().map(i -> {
            StandardsTreeDTO standardsTreeDTO = new StandardsTreeDTO();
            standardsTreeDTO.setId((int) i.getId());
            standardsTreeDTO.setPid(i.getPid());
            standardsTreeDTO.setName(i.getName());
            standardsTreeDTO.setType(i.getType());
            standardsTreeDTO.setSort(i.getSort());
            standardsTreeDTO.setCreateTime(i.getCreateTime());
            return standardsTreeDTO;
        }).collect(Collectors.toList());
        List<StandardsTreeDTO> parentList = allList.stream().filter(item -> item.getPid() == null || item.getPid() == 0).collect(Collectors.toList());
        if (parentList.size() > 1) {
            parentList.sort(Comparator.comparing(StandardsTreeDTO::getCreateTime).reversed());
        }
        // 递归处理子集
        standardsTree(allList, parentList);
        return parentList;
    }

    private void standardsTree(List<StandardsTreeDTO> allList, List<StandardsTreeDTO> parentList) {
        Map<Integer, List<StandardsTreeDTO>> childrenMap = new HashMap<>();
        for (StandardsTreeDTO dto : allList) {
            int parentId = dto.getPid() != null ? dto.getPid() : 0;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dto);
        }
        for (StandardsTreeDTO parent : parentList) {
            List<StandardsTreeDTO> children = childrenMap.get(parent.getId());
            if (children != null) {
                children.sort(Comparator.comparing(StandardsTreeDTO::getCreateTime).reversed());
                parent.setChildren(children);
                standardsTree(allList, children);
            }
        }
    }

    @Override
    public ResultEnum addorUpdateStandardsMenu(StandardsMenuDTO standardsMenuDTO) {
        LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StandardsMenuPO::getPid, standardsMenuDTO.getPid());
        queryWrapper.orderByDesc(StandardsMenuPO::getSort);
        queryWrapper.last("LIMIT 1");
        StandardsMenuPO tragetMenu = getOne(queryWrapper);
        if (tragetMenu == null) {
            standardsMenuDTO.setSort(1);
        } else {
            standardsMenuDTO.setSort(tragetMenu.getSort() + 1);
        }
        if (standardsMenuDTO.getName() == null || standardsMenuDTO.getType() == null) {
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        StandardsMenuPO standardsMenuPO = StandardsMenuMap.INSTANCES.dtoToPo(standardsMenuDTO);
        if (standardsMenuDTO.getId() == null || standardsMenuDTO.getId() == 0) {
            save(standardsMenuPO);
        } else {
            updateById(standardsMenuPO);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delStandardsMenu(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<StandardsMenuPO> StandardsMenus = listByIds(ids);
        List<StandardsMenuPO> StandardsDatas = StandardsMenus.stream().filter(i -> i.getType() == 2).collect(Collectors.toList());
        List<Integer> StandardsDataIds = StandardsDatas.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
        removeByIds(ids);
        if (!CollectionUtils.isEmpty(StandardsDataIds)) {
            standardsService.delStandards(StandardsDataIds);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 查看数据标准树形标签--非懒加载
     *
     * @return
     */
    @Override
    public List<StandardsTreeDTO> getStandardsAllTree() {
        //获取数据元menu
        List<StandardsMenuPO> standardsMenus = this.list();
        if (CollectionUtils.isEmpty(standardsMenus)) {
            return new ArrayList<>();
        }

        //获取数据元详情的所有id
        LambdaQueryWrapper<StandardsPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(StandardsPO::getId, StandardsPO::getMenuId);
        List<StandardsPO> list = standardsService.list(wrapper);

        List<StandardsTreeDTO> allList = standardsMenus.stream().map(i -> {
            StandardsTreeDTO standardsTreeDTO = new StandardsTreeDTO();
            standardsTreeDTO.setId((int) i.getId());
            standardsTreeDTO.setPid(i.getPid());
            standardsTreeDTO.setName(i.getName());
            standardsTreeDTO.setType(i.getType());
            standardsTreeDTO.setSort(i.getSort());
            standardsTreeDTO.setCreateTime(i.getCreateTime());
            return standardsTreeDTO;
        }).collect(Collectors.toList());
        List<StandardsTreeDTO> parentList = allList.stream().filter(item -> item.getPid() == null || item.getPid() == 0).collect(Collectors.toList());
        if (parentList.size() > 1) {
            parentList.sort(Comparator.comparing(StandardsTreeDTO::getCreateTime).reversed());
        }
        // 递归处理子集
        standardsTreeForAll(allList, parentList, list);
        return parentList;
    }

    /**
     * 获取所有数据元标准menu-只要id和name
     * @return
     */
    @Override
    public List<StandardsMenuDTO> getStandardMenus() {
        LambdaQueryWrapper<StandardsMenuPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(StandardsMenuPO::getId,StandardsMenuPO::getName);
        List<StandardsMenuPO> list = list(wrapper);
        return StandardsMenuMap.INSTANCES.posToDtos(list);
    }

    private void standardsTreeForAll(List<StandardsTreeDTO> allList, List<StandardsTreeDTO> parentList, List<StandardsPO> list) {
        Map<Integer, List<StandardsTreeDTO>> childrenMap = new HashMap<>();
        for (StandardsTreeDTO dto : allList) {
            int parentId = dto.getPid() != null ? dto.getPid() : 0;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dto);
        }
        List<StandardsTreeDTO> removeParentList = new ArrayList<>();;
        for (StandardsTreeDTO parent : parentList) {
            List<StandardsTreeDTO> children = childrenMap.get(parent.getId());
            if (children != null) {
                children.sort(Comparator.comparing(StandardsTreeDTO::getCreateTime).reversed());
                parent.setChildren(children);
                standardsTreeForAll(allList, children, list);
            } else {
                //类型:1:目录 2:数据
                if (parent.getType() == 2) {
                    boolean ifExists = list.stream()
                            .anyMatch(po -> Objects.equals(po.getMenuId(), parent.getId()));
                    if (!ifExists) {
                        removeParentList.add(parent);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(removeParentList)){
            parentList.removeAll(removeParentList);
        }
    }

}
