package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.RoleUserAssignmentDTO;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.map.RoleUserAssignmentMap;
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
        /*查询当前角色所有用户*/
        QueryWrapper<RoleUserAssignmentPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleUserAssignmentPO::getRoleId,dto.id);
        List<RoleUserAssignmentPO> dataList;
        dataList=mapper.selectList(queryWrapper);
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
        List<RoleUserAssignmentPO> list=new ArrayList<>();
        for (Integer item:dto.list) {
            RoleUserAssignmentPO model=new RoleUserAssignmentPO();
            model.roleId=dto.id;
            model.userId=item;
            list.add(model);
        }
        return this.saveBatch(list)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }
}
