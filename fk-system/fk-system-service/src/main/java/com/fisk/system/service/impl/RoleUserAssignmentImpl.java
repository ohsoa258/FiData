package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.dto.RoleUserAssignmentDTO;
import com.fisk.system.entity.RoleServiceAssignmentPO;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.map.RoleServiceAssignmentMap;
import com.fisk.system.map.RoleUserAssignmentMap;
import com.fisk.system.mapper.RoleServiceAssignmentMapper;
import com.fisk.system.mapper.RoleUserAssignmentMapper;
import com.fisk.system.service.IRoleUserAssignmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class RoleUserAssignmentImpl
        extends ServiceImpl<RoleUserAssignmentMapper, RoleUserAssignmentPO>
        implements IRoleUserAssignmentService {

    @Resource
    RoleUserAssignmentMapper mapper;
    @Resource
    UserHelper userHelper;

    @Override
    public List<RoleUserAssignmentDTO> getRoleUserList(int roleId)
    {
        QueryWrapper<RoleUserAssignmentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleUserAssignmentPO::getRoleId,roleId);
        return RoleUserAssignmentMap.INSTANCES.poToDto(mapper.selectList(queryWrapper));
    }

    @Override
    public ResultEnum addRoleUserAssignment(AssignmentDTO dto)
    {
        /*获取登录信息*/
        UserInfo userInfo = userHelper.getLoginUserInfo();
        /*查询当前角色所有用户*/
        QueryWrapper<RoleUserAssignmentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleUserAssignmentPO::getRoleId,dto.id);
        List<RoleUserAssignmentPO> dataList;
        dataList=mapper.selectList(queryWrapper);
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
        List<RoleUserAssignmentPO> list=new ArrayList<>();
        for (Integer item:dto.list) {
            RoleUserAssignmentPO model=new RoleUserAssignmentPO();
            model.roleId=dto.id;
            model.userId=item;
            model.createUser=userInfo.id.toString();
            list.add(model);
        }
        return this.saveBatch(list)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }



}
