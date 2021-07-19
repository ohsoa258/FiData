package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.dto.ServiceSourceDTO;
import com.fisk.system.entity.RoleServiceAssignmentPO;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.entity.ServiceRegistryPO;
import com.fisk.system.map.RoleServiceAssignmentMap;
import com.fisk.system.map.ServiceRegistryMap;
import com.fisk.system.mapper.RoleServiceAssignmentMapper;
import com.fisk.system.mapper.RoleUserAssignmentMapper;
import com.fisk.system.mapper.ServiceRegistryMapper;
import com.fisk.system.service.IRoleServiceAssignmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class RoleServiceAssignmentImpl
        extends ServiceImpl<RoleServiceAssignmentMapper, RoleServiceAssignmentPO>
        implements IRoleServiceAssignmentService
{
    @Resource
    RoleServiceAssignmentMapper serviceMapper;
    @Resource
    RoleUserAssignmentMapper roleUserMapper;
    @Resource
    ServiceRegistryMapper serviceRegistryMapper;
    @Resource
    UserHelper userHelper;

    @Override
    public List<RoleServiceAssignmentDTO> getRoleServiceList(int roleId)
    {
        QueryWrapper<RoleServiceAssignmentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleServiceAssignmentPO::getRoleId,roleId);
        return RoleServiceAssignmentMap.INSTANCES.poToDto(serviceMapper.selectList(queryWrapper));
    }

    @Override
    public ResultEnum addRoleServiceAssignment(AssignmentDTO dto)
    {
        /*获取登录信息*/
        UserInfo userInfo = userHelper.getLoginUserInfo();
        /*查询当前角色所有用户*/
        QueryWrapper<RoleServiceAssignmentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleServiceAssignmentPO::getRoleId,dto.id);
        List<RoleServiceAssignmentPO>  dataList;
        dataList=serviceMapper.selectList(queryWrapper);
        if (dataList.size()>0)
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
        for (Integer item:dto.list) {
            RoleServiceAssignmentPO model=new RoleServiceAssignmentPO();
            model.roleId=dto.id;
            model.serviceId=item;
            model.createUser=userInfo.id.toString();
            list.add(model);
        }
        return this.saveBatch(list)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<ServiceSourceDTO> getServiceList()
    {
        /*获取登录信息*/
        UserInfo userInfo = userHelper.getLoginUserInfo();
        if (userInfo==null)
        {
            return null;
        }
        List<ServiceSourceDTO> dataList=new ArrayList<>();
        /*查询当前用户下所有角色*/
        QueryWrapper<RoleUserAssignmentPO> roleData = new QueryWrapper<>();
        roleData.select("role_id").lambda().eq(RoleUserAssignmentPO::getUserId,userInfo.id);
        List<Object> idList = roleUserMapper.selectObjs(roleData).stream().distinct().collect(Collectors.toList());

        /*查询角色下所有服务*/
        QueryWrapper<RoleServiceAssignmentPO> serviceData = new QueryWrapper<>();
        serviceData.in("role_id",idList.toArray()).select("service_id");
        List<Object> serviceIds = serviceMapper.selectObjs(serviceData).stream().distinct().collect(Collectors.toList());

        /*根据服务id集合获取服务列表*/
        QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",serviceIds.toArray());
        List<ServiceRegistryPO> list = serviceRegistryMapper.selectList(queryWrapper);

        /*查询所有父节点*/
        String code="1";
        List<ServiceRegistryPO> listParent=list.stream().sorted(Comparator.comparing(ServiceRegistryPO::getSequenceNo)).filter(e->code.equals(e.getParentServeCode()))
                .collect(Collectors.toList());
        List<ServiceSourceDTO> dtoList = new ArrayList<>();

        for (ServiceRegistryPO po : listParent) {
            ServiceSourceDTO dto=RoleServiceAssignmentMap.INSTANCES.servicePoToDto(po);
            List<ServiceSourceDTO> data=new ArrayList<>();
            List<ServiceRegistryPO> listChild=list.stream().sorted(Comparator.comparing(ServiceRegistryPO::getSequenceNo)).filter(e->po.getServeCode().equals(e.getParentServeCode())).collect(Collectors.toList());
            /*查询所有子节点*/
            for (ServiceRegistryPO item : listChild)
            {
                ServiceSourceDTO obj=RoleServiceAssignmentMap.INSTANCES.servicePoToDto(item);
                data.add(obj);
            }
            dto.setDto(data);
            dtoList.add(dto);
        }
        return dtoList;
    }


}
