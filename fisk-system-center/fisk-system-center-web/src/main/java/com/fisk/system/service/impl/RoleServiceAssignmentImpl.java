package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.dto.IconDTO;
import com.fisk.system.dto.LoginServiceDTO;
import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.dto.SaveRoleServiceAssignmentDTO;
import com.fisk.system.entity.RoleServiceAssignmentPO;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.entity.ServiceRegistryPO;
import com.fisk.system.mapper.RoleServiceAssignmentMapper;
import com.fisk.system.mapper.RoleUserAssignmentMapper;
import com.fisk.system.mapper.ServiceRegistryMapper;
import com.fisk.system.service.IRoleServiceAssignmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class RoleServiceAssignmentImpl
        extends ServiceImpl<RoleServiceAssignmentMapper, RoleServiceAssignmentPO>
        implements IRoleServiceAssignmentService {

    @Resource
    RoleServiceAssignmentMapper serviceMapper;
    @Resource
    RoleUserAssignmentMapper roleUserMapper;
    @Resource
    ServiceRegistryMapper serviceRegistryMapper;
    @Resource
    UserHelper userHelper;

    @Resource
    ServiceRegistryImpl serviceRegistry;

    @Override
    public List<RoleServiceAssignmentDTO> getRoleServiceList(int roleId) {
        return serviceMapper.getRoleServiceAssignmentDto(roleId);
    }

    @Override
    public ResultEnum addRoleServiceAssignment(SaveRoleServiceAssignmentDTO dto)
    {
        /*查询当前角色所有用户*/
        QueryWrapper<RoleServiceAssignmentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleServiceAssignmentPO::getRoleId,dto.id);
        List<RoleServiceAssignmentPO>  dataList;
        dataList=serviceMapper.selectList(queryWrapper);
        if ( dataList !=null && dataList.size()>0)
        {
            boolean res=this.remove(queryWrapper);
            if (res == false) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        if (dto.list==null)
        {
            return ResultEnum.SUCCESS;
        }
        List<RoleServiceAssignmentPO> list=new ArrayList<>();
        for (RoleServiceAssignmentDTO item:dto.list) {
            RoleServiceAssignmentPO model=new RoleServiceAssignmentPO();
            model.roleId=dto.id;
            model.serviceId=item.serviceId;
            model.switchAdd = item.switchAdd;
            model.switchUpdate = item.switchUpdate;
            model.switchDelete = item.switchDelete;
            list.add(model);
        }
        return this.saveBatch(list)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<LoginServiceDTO> getServiceList() {

        /*获取登录信息*/
        UserInfo userInfo = userHelper.getLoginUserInfo();
        if (userInfo == null) {
            throw new FkException(ResultEnum.USER_NON_EXISTENT);
        }

        /*查询当前用户下所有角色*/
        QueryWrapper<RoleUserAssignmentPO> roleData = new QueryWrapper<>();
        roleData.select("role_id").lambda().eq(RoleUserAssignmentPO::getUserId, userInfo.id);
        List<Object> idList = roleUserMapper
                .selectObjs(roleData)
                .stream()
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        }

        /*查询角色下所有服务*/
        QueryWrapper<RoleServiceAssignmentPO> serviceData = new QueryWrapper<>();
        serviceData.in("role_id", idList.toArray());
        List<RoleServiceAssignmentPO> roleServiceAssignmentPOS = serviceMapper
                .selectList(serviceData);
        Map<Integer, Map<String, Integer>> servicePermissions = new HashMap<>();
        roleServiceAssignmentPOS.stream().forEach(i->{
            if (i.switchAdd == 1){
                servicePermissions.computeIfAbsent(i.getServiceId(),k->new HashMap<>()).put("switchAdd",1);
            }
            if (i.switchUpdate == 1){
                servicePermissions.computeIfAbsent(i.getServiceId(),k->new HashMap<>()).put("switchUpdate",1);
            }
            if (i.switchDelete == 1){
                servicePermissions.computeIfAbsent(i.getServiceId(),k->new HashMap<>()).put("switchDelete",1);
            }
        });
        List<Object> serviceIds = roleServiceAssignmentPOS
                .stream()
                .map(RoleServiceAssignmentPO::getServiceId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(serviceIds)) {
            return new ArrayList<>();
        }

        /*根据服务id集合获取服务列表*/
        QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", serviceIds.toArray());
        List<ServiceRegistryPO> list = serviceRegistryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Long> collect = list.stream().map(e -> e.getId()).collect(Collectors.toList());


        return buildMenu(collect,servicePermissions);
    }

    @Override
    public List<LoginServiceDTO> getAllMenuList() {
        /*根据服务id集合获取服务列表*/
        QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ServiceRegistryPO::getDelFlag, 1);
        List<ServiceRegistryPO> list = serviceRegistryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Long> collect = list.stream().map(e -> e.getId()).collect(Collectors.toList());
        return buildMenu(collect);
    }

    public List<LoginServiceDTO> buildMenu(List<Long> collect) {
        QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
        List<ServiceRegistryPO> list = serviceRegistryMapper.selectList(queryWrapper);
        //查询所有父节点,并根据序号排序
        String code = "1";
        List<ServiceRegistryPO> listParent = list.stream()
                .sorted(Comparator.comparing(ServiceRegistryPO::getSequenceNo))
                .filter(e -> code.equals(e.getParentServeCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(listParent)) {
            return new ArrayList<>();
        }
        List<LoginServiceDTO> data = new ArrayList<>();
        for (ServiceRegistryPO po : listParent) {
            LoginServiceDTO dto = new LoginServiceDTO();

            dto.id=po.id;
            dto.name = po.serveUrl;
            dto.component = "Layout";
            dto.serveCode = po.getServeCode();
            IconDTO icon = new IconDTO();
            icon.title = po.serveCnName;
            icon.noCache = false;
            icon.icon = po.icon;
            dto.meta = icon;
            dto.description = po.description;
            dto.sequenceNo = po.sequenceNo;

            if (collect.contains(po.id)) {
                dto.authority = true;
            }
            dto.path = "/" + po.serveUrl;
            dto.children = new ArrayList<>();
            dto.children.add(buildChildTree(dto, list, collect));
            data.add(dto);
        }

        return data;
    }

    public List<LoginServiceDTO> buildMenu(List<Long> collect,Map<Integer, Map<String, Integer>> servicePermissions) {
        QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
        List<ServiceRegistryPO> list = serviceRegistryMapper.selectList(queryWrapper);
        //查询所有父节点,并根据序号排序
        String code = "1";
        List<ServiceRegistryPO> listParent = list.stream()
                .sorted(Comparator.comparing(ServiceRegistryPO::getSequenceNo))
                .filter(e -> code.equals(e.getParentServeCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(listParent)) {
            return new ArrayList<>();
        }
        List<LoginServiceDTO> data = new ArrayList<>();
        for (ServiceRegistryPO po : listParent) {
            LoginServiceDTO dto = new LoginServiceDTO();

            dto.id=po.id;
            dto.name = po.serveUrl;
            dto.component = "Layout";
            dto.serveCode = po.getServeCode();
            IconDTO icon = new IconDTO();
            icon.title = po.serveCnName;
            icon.noCache = false;
            icon.icon = po.icon;
            dto.meta = icon;
            dto.description = po.description;
            dto.sequenceNo = po.sequenceNo;

            if (collect.contains(po.id)) {
                dto.authority = true;
            }
            dto.path = "/" + po.serveUrl;
            dto.children = new ArrayList<>();
            dto.children.add(buildChildTree(dto, list, collect,servicePermissions));
            data.add(dto);
        }

        return data;
    }

    public LoginServiceDTO buildChildTree(LoginServiceDTO pNode,
                                          List<ServiceRegistryPO> poList,
                                          List<Long> collect) {
        List<LoginServiceDTO> list = new ArrayList<>();
        for (ServiceRegistryPO item : poList) {
            if (item.getParentServeCode().equals(pNode.getServeCode())) {

                LoginServiceDTO obj = new LoginServiceDTO();
                obj.id= item.id;
                obj.name = item.serveUrl;
                obj.component = item.serveUrl;
                IconDTO iconChildren = new IconDTO();
                iconChildren.title = item.serveCnName;
                iconChildren.noCache = false;
                iconChildren.icon = item.icon;
                obj.meta = iconChildren;
                obj.serveCode = item.getServeCode();
                obj.description = item.description;
                obj.sequenceNo = item.sequenceNo;
                obj.path = "/" + item.serveUrl;
                if (collect.contains(item.id)) {
                    obj.authority = true;
                }

                List<LoginServiceDTO> child = new ArrayList<>();
                obj.children = child;
                list.add(buildChildTree(obj, poList, collect));
            }
        }
        pNode.children = list;
        return pNode;
    }
    public LoginServiceDTO buildChildTree(LoginServiceDTO pNode,
                                          List<ServiceRegistryPO> poList,
                                          List<Long> collect,
                                          Map<Integer, Map<String, Integer>> servicePermissions) {
        List<LoginServiceDTO> list = new ArrayList<>();
        for (ServiceRegistryPO item : poList) {
            if (item.getParentServeCode().equals(pNode.getServeCode())) {

                LoginServiceDTO obj = new LoginServiceDTO();
                obj.id= item.id;
                obj.name = item.serveUrl;
                obj.component = item.serveUrl;
                IconDTO iconChildren = new IconDTO();
                iconChildren.title = item.serveCnName;
                iconChildren.noCache = false;
                iconChildren.icon = item.icon;
                obj.meta = iconChildren;
                obj.serveCode = item.getServeCode();
                obj.description = item.description;
                obj.sequenceNo = item.sequenceNo;
                obj.path = "/" + item.serveUrl;
                if (collect.contains(item.id)) {
                    obj.authority = true;
                }
                //处理添加修改删除权限
                Map<String, Integer> permissions = servicePermissions.get((int) item.id);
                if (permissions!= null){
                    Integer switchAdd = permissions.get("switchAdd");
                    Integer switchUpdate = permissions.get("switchUpdate");
                    Integer switchDelete = permissions.get("switchDelete");
                    if (switchAdd != null){
                        obj.setSwitchAdd(switchAdd);
                    }
                    if (switchUpdate != null){
                        obj.setSwitchUpdate(switchUpdate);
                    }
                    if (switchDelete != null){
                        obj.setSwitchDelete(switchDelete);
                    }
                }
                List<LoginServiceDTO> child = new ArrayList<>();
                obj.children = child;
                list.add(buildChildTree(obj, poList, collect,servicePermissions));
            }
        }
        pNode.children = list;
        return pNode;
    }


}
