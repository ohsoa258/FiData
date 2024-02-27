package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.api.ApiMenuDTO;
import com.fisk.dataservice.dto.api.ApiSortDTO;
import com.fisk.dataservice.dto.api.ApiTreeDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.entity.ApiMenuConfigPO;
import com.fisk.dataservice.map.ApiMenuConfigMap;
import com.fisk.dataservice.mapper.ApiMenuConfigMapper;
import com.fisk.dataservice.service.IApiMenuConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("apiMenuConfigService")
public class ApiMenuConfigServiceImpl extends ServiceImpl<ApiMenuConfigMapper, ApiMenuConfigPO> implements IApiMenuConfigService {

    @Resource
    ApiRegisterManageImpl apiRegisterManage;

    @Override
    public List<ApiTreeDTO> getApiTree(Integer serverType) {
        LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiMenuConfigPO::getType,1);
        queryWrapper.eq(ApiMenuConfigPO::getServerType, serverType);
        List<ApiMenuConfigPO> apiMenus = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(apiMenus)){
            return new ArrayList<>();
        }
        List<ApiTreeDTO> allList = apiMenus.stream().map(i -> {
            ApiTreeDTO apiTreeDTO = new ApiTreeDTO();
            apiTreeDTO.setId((int)i.getId());
            apiTreeDTO.setPid(i.getPid());
            apiTreeDTO.setName(i.getName());
            apiTreeDTO.setType(i.getType());
            apiTreeDTO.setSort(i.getSort());
            apiTreeDTO.setCreateTime(i.getCreateTime());
            return apiTreeDTO;
        }).collect(Collectors.toList());
        List<ApiTreeDTO> parentList = allList.stream().filter(item -> item.getPid() == null || item.getPid() == 0).collect(Collectors.toList());
        if (parentList.size() > 1){
            parentList.sort(Comparator.comparing(ApiTreeDTO::getCreateTime).reversed());
        }
        // 递归处理子集
        standardsTree(allList, parentList);
        return parentList;
    }

    private void standardsTree(List<ApiTreeDTO> allList, List<ApiTreeDTO> parentList) {
        Map<Integer, List<ApiTreeDTO>> childrenMap = new HashMap<>();
        for (ApiTreeDTO dto : allList) {
            int parentId = dto.getPid() != null ? dto.getPid() : 0;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dto);
        }
        for (ApiTreeDTO parent : parentList) {
            List<ApiTreeDTO> children = childrenMap.get(parent.getId());
            if (children != null) {
                children.sort(Comparator.comparing(ApiTreeDTO::getCreateTime).reversed());
                parent.setChildren(children);
                standardsTree(allList, children);
            }
        }
    }

    @Override
    public ResultEnum addorUpdateApiMenu(ApiMenuDTO dto) {
        LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiMenuConfigPO::getPid,dto.getPid());
        queryWrapper.orderByDesc(ApiMenuConfigPO::getSort);
        queryWrapper.last("LIMIT 1");
        ApiMenuConfigPO tragetMenu = getOne(queryWrapper);
        if (tragetMenu == null){
            dto.setSort(1);
        }else {
            dto.setSort(tragetMenu.getSort()+1);
        }
        if (dto.getName() == null || dto.getType() == null){
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        ApiMenuConfigPO standardsMenuPO = ApiMenuConfigMap.INSTANCES.dtoToPo(dto);
        if (dto.getId() == null || dto.getId() == 0){
            save(standardsMenuPO);
        }else {
            updateById(standardsMenuPO);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delApiMenu(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<ApiMenuConfigPO> ApiMenus = listByIds(ids);
        List<ApiMenuConfigPO> ApiDatas = ApiMenus.stream().filter(i -> i.getType() == 2).collect(Collectors.toList());
        List<Integer> ApiDataIds = ApiDatas.stream().map(i->(int)i.getId()).collect(Collectors.toList());
        removeByIds(ids);
        if (!CollectionUtils.isEmpty(ApiDataIds)){
            for (Integer apiDataId : ApiDataIds) {
                apiRegisterManage.deleteData(apiDataId);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum apiSort(ApiSortDTO dto) {
        Integer targetId = dto.getTargetId();
        ApiMenuConfigPO apiMenuConfigPO = this.getById(dto.getMenuId());
        ApiMenuConfigPO tragetMenuPO = this.getById(targetId);
        //判断是否跨级移动
        if (dto.getCrossLevel()){
            if (targetId == null || targetId == 0){
                Integer pid = apiMenuConfigPO.getPid();
                Integer sort = apiMenuConfigPO.getSort();
                //查询目标菜单当前排序值之后的所有数据 并修改往后排序1位
                LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ApiMenuConfigPO::getPid,dto.getPid());
                List<ApiMenuConfigPO> all = this.list(queryWrapper);
                if (!CollectionUtils.isEmpty(all)){
                    List<ApiMenuConfigPO> menus = new ArrayList<>();
                    for (ApiMenuConfigPO menuPO : all) {
                        menuPO.setSort(menuPO.getSort()+1);
                        menus.add(menuPO);
                    }
                    this.updateBatchById(menus);
                }
                //将当前插入该位置的菜单放入合适的sort值
                apiMenuConfigPO.setPid(dto.getPid());
                apiMenuConfigPO.setSort(1);
                this.updateById(apiMenuConfigPO);

                if (apiMenuConfigPO.getType() == 2){
                    //更新api关联的菜单id
                    LambdaQueryWrapper<ApiConfigPO> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(ApiConfigPO::getApiMenuId,dto.getMenuId());
                    ApiConfigPO apiConfigPO = apiRegisterManage.getOne(queryWrapper1);
                    apiConfigPO.setMenuId(dto.getPid());
                    apiRegisterManage.updateById(apiConfigPO);
                }


                //查询源菜单当前排序值之后的所有数据 并修改往前排序1位
                LambdaQueryWrapper<ApiMenuConfigPO> selectMenus = new LambdaQueryWrapper<>();
                selectMenus.eq(ApiMenuConfigPO::getPid,pid);
                selectMenus.gt(ApiMenuConfigPO::getSort,sort);
                List<ApiMenuConfigPO> lastMenus = this.list(selectMenus);
                List<ApiMenuConfigPO> menus = new ArrayList<>();
                for (ApiMenuConfigPO menuPO : lastMenus) {
                    menuPO.setSort(menuPO.getSort()-1);
                    menus.add(menuPO);
                }
                if (!CollectionUtils.isEmpty(menus)){
                    this.updateBatchById(menus);
                }
            }else {
                Integer pid = apiMenuConfigPO.getPid();
                Integer sort = apiMenuConfigPO.getSort();
                //查询目标菜单目录并修改排序
                LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ApiMenuConfigPO::getPid,dto.getPid());
                //查询目标菜单当前排序值之后的所有数据 并修改往后排序1位
                queryWrapper.ge(ApiMenuConfigPO::getSort,tragetMenuPO.getSort()+1);
                List<ApiMenuConfigPO> lastMenus = this.list(queryWrapper);
                List<ApiMenuConfigPO> menus = new ArrayList<>();
                for (ApiMenuConfigPO menuPO : lastMenus) {
                    menuPO.setSort(menuPO.getSort()+1);
                    menus.add(menuPO);
                }
                if (!CollectionUtils.isEmpty(menus)){
                    this.updateBatchById(menus);
                }
                //将当前插入该位置的菜单放入合适的sort值
                apiMenuConfigPO.setPid(dto.getPid());
                apiMenuConfigPO.setSort(tragetMenuPO.getSort()+1);
                this.updateById(apiMenuConfigPO);

                if (apiMenuConfigPO.getType() == 2){
                    //更新api关联的菜单id
                    LambdaQueryWrapper<ApiConfigPO> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(ApiConfigPO::getApiMenuId,dto.getMenuId());
                    ApiConfigPO apiConfigPO = apiRegisterManage.getOne(queryWrapper1);
                    apiConfigPO.setMenuId(dto.getPid());
                    apiRegisterManage.updateById(apiConfigPO);
                }


                //查询源菜单当前排序值之后的所有数据 并修改往前排序1位
                LambdaQueryWrapper<ApiMenuConfigPO> selectMenus = new LambdaQueryWrapper<>();
                selectMenus.eq(ApiMenuConfigPO::getPid,pid);
                selectMenus.gt(ApiMenuConfigPO::getSort,sort);
                List<ApiMenuConfigPO> Menus = this.list(selectMenus);
                menus = new ArrayList<>();
                for (ApiMenuConfigPO menuPO : Menus) {
                    menuPO.setSort(menuPO.getSort()-1);
                    menus.add(menuPO);
                }
                if (!CollectionUtils.isEmpty(menus)){
                    this.updateBatchById(menus);
                }
            }
        }else {
            if (targetId == null || targetId == 0){
                //同级移动若放在第一位将同级其他目录sort往后排一位并插入排序为1
                LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ApiMenuConfigPO::getPid, apiMenuConfigPO.getPid());
                queryWrapper.lt(ApiMenuConfigPO::getSort, apiMenuConfigPO.getSort());
                List<ApiMenuConfigPO> list = this.list(queryWrapper);
                List<ApiMenuConfigPO> menus = new ArrayList<>();
                for (ApiMenuConfigPO menuPO : list) {
                    menuPO.setSort(menuPO.getSort()+1);
                    menus.add(menuPO);
                }
                if (!CollectionUtils.isEmpty(menus)){
                    this.updateBatchById(menus);
                }
                apiMenuConfigPO.setSort(1);
                this.updateById(apiMenuConfigPO);
            }else {
                if (tragetMenuPO.getSort()> apiMenuConfigPO.getSort()){
                    //同级移动若目标要放在在当前排序之后则查到这之间的目录修改 sort值-1
                    LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ApiMenuConfigPO::getPid, apiMenuConfigPO.getPid());
                    queryWrapper.gt(ApiMenuConfigPO::getSort, apiMenuConfigPO.getSort());
                    queryWrapper.le(ApiMenuConfigPO::getSort,tragetMenuPO.getSort());
                    List<ApiMenuConfigPO> list = this.list(queryWrapper);
                    List<ApiMenuConfigPO> menus = new ArrayList<>();
                    for (ApiMenuConfigPO menuPO : list) {
                        menuPO.setSort(menuPO.getSort()-1);
                        menus.add(menuPO);
                    }
                    if (!CollectionUtils.isEmpty(menus)){
                        this.updateBatchById(menus);
                    }
                    apiMenuConfigPO.setSort(tragetMenuPO.getSort());
                    this.updateById(apiMenuConfigPO);
                }else if (tragetMenuPO.getSort()< apiMenuConfigPO.getSort()){
                    //同级移动若目标要放在当前排序之前则查到这之间的目录修改合适的sort值+1
                    LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ApiMenuConfigPO::getPid, apiMenuConfigPO.getPid());
                    queryWrapper.gt(ApiMenuConfigPO::getSort,tragetMenuPO.getSort());
                    queryWrapper.lt(ApiMenuConfigPO::getSort, apiMenuConfigPO.getSort());
                    List<ApiMenuConfigPO> list = this.list(queryWrapper);
                    List<ApiMenuConfigPO> menus = new ArrayList<>();
                    for (ApiMenuConfigPO menuPO : list) {
                        menuPO.setSort(menuPO.getSort()+1);
                        menus.add(menuPO);
                    }
                    if (!CollectionUtils.isEmpty(menus)){
                        this.updateBatchById(menus);
                    }
                    apiMenuConfigPO.setSort(tragetMenuPO.getSort()+1);
                    this.updateById(apiMenuConfigPO);
                }
            }
        }
        return ResultEnum.SUCCESS;
    }
}
