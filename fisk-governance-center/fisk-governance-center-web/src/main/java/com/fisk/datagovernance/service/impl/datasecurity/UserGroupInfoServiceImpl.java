package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDropDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoPageDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoQueryDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupInfoPO;
import com.fisk.datagovernance.map.datasecurity.UserGroupInfoMap;
import com.fisk.datagovernance.mapper.datasecurity.UserGroupInfoMapper;
import com.fisk.datagovernance.service.datasecurity.UserGroupInfoService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDropDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 * @email jianwen@fisk.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class UserGroupInfoServiceImpl
        extends ServiceImpl<UserGroupInfoMapper, UserGroupInfoPO>
        implements UserGroupInfoService {

    @Resource
    UserGroupInfoMapper mapper;

    @Resource
    UserGroupAssignmentServiceImpl userGroupAssignmentService;
    @Resource
    UserClient client;

    @Override
    public IPage<UserGroupInfoPageDTO> listUserGroupInfos(UserGroupInfoQueryDTO dto)
    {
        QueryWrapper<UserGroupInfoPO> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotEmpty(dto.name))
        {
            queryWrapper.lambda().like(UserGroupInfoPO::getUserGroupName,dto.name);
        }
        Page<UserGroupInfoPO> data=new Page<>(dto.getPage(),dto.getSize());
        return UserGroupInfoMap.INSTANCES.poListToDtoList(mapper.selectPage(data, queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public UserGroupInfoDTO getData(long id)
    {
        UserGroupInfoPO po=mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return UserGroupInfoMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum saveData(UserGroupInfoDTO dto)
    {
        //判断名称是否已存在
        QueryWrapper<UserGroupInfoPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(UserGroupInfoPO::getUserGroupName,dto.userGroupName);
        UserGroupInfoPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return mapper.insert(UserGroupInfoMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateData(UserGroupInfoDTO dto)
    {
        UserGroupInfoPO po=mapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断名称是否重复
        QueryWrapper<UserGroupInfoPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(UserGroupInfoPO::getUserGroupName,dto.userGroupName);
        UserGroupInfoPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id!=dto.id)
        {
            return ResultEnum.NAME_EXISTS;
        }
        return mapper.updateById(UserGroupInfoMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteData(long id)
    {
        UserGroupInfoPO po=mapper.selectById(id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //删除用户组下用户
        ResultEnum resultEnum = userGroupAssignmentService.deleteData(id);
        if (resultEnum.getCode()!=ResultEnum.SUCCESS.getCode())
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<UserGroupInfoDropDTO> listUserGroupInfoDrops()
    {
        QueryWrapper<UserGroupInfoPO> queryWrapper=new QueryWrapper<>();
        return UserGroupInfoMap.INSTANCES.poToDtoDrop(mapper.selectList(queryWrapper.orderByDesc("create_time")));
    }

    @Override
    public List<UserDropDTO> listSystemUserDrops()
    {
        ResultEntity<List<UserDropDTO>> data = client.listUserDrops();
        if (data.code!=ResultEnum.SUCCESS.getCode())
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return data.data;
    }

}