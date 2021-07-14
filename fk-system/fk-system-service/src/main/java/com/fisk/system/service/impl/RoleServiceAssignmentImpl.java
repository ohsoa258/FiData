package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.entity.RoleServiceAssignmentPO;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.map.RoleServiceAssignmentMap;
import com.fisk.system.mapper.RoleServiceAssignmentMapper;
import com.fisk.system.mapper.RoleUserAssignmentMapper;
import com.fisk.system.service.IRoleServiceAssignmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    RoleUserAssignmentMapper userMapper;
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


}
