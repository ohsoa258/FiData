package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.RoleInfoDTO;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.entity.RoleInfoPO;
import com.fisk.system.mapper.RoleInfoMapper;
import org.springframework.stereotype.Service;
import com.fisk.system.service.IRoleInfoService;
import com.fisk.system.map.RoleInfoMap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class RoleInfoImpl implements IRoleInfoService{

    @Resource
    UserHelper userHelper;
    @Resource
    RoleInfoMapper mapper;

    @Override
    public List<RoleInfoDTO> listRoleData()
    {
        List<RoleInfoDTO> result;
        QueryWrapper<RoleInfoPO> queryWrapper = new QueryWrapper<>();
        result =RoleInfoMap.INSTANCES.poToDtos(mapper.selectList(queryWrapper));
        return  result;
    }

    @Override
    public ResultEnum addRole(RoleInfoDTO dto){

        QueryWrapper<RoleInfoPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RoleInfoPO::getRoleName, dto.roleName);

        RoleInfoPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        RoleInfoPO po= RoleInfoMap.INSTANCES.dtoToPo(dto);
        po.createUser = userInfo.id.toString();
        return mapper.insert(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteRole(int id)
    {
        RoleInfoPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.updateUser=userInfo.id.toString();
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public RoleInfoDTO getRoleById(int id) {
        RoleInfoDTO po =RoleInfoMap.INSTANCES.poToDto(mapper.selectById(id));
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }

    @Override
    public ResultEnum updateRole(RoleInfoDTO dto)
    {
        /*判断是否存在*/
        RoleInfoPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        /*判断名称是否重复*/
        QueryWrapper<RoleInfoPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RoleInfoPO::getRoleName, dto.roleName);
        RoleInfoPO data = mapper.selectOne(queryWrapper);
        if (data != null && data.id != dto.id) {
            return ResultEnum.NAME_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.roleDesc=dto.roleDesc;
        model.roleName=dto.roleName;
        model.updateUser=userInfo.id.toString();
        int res=0;
        res=mapper.updateById(model);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

}
