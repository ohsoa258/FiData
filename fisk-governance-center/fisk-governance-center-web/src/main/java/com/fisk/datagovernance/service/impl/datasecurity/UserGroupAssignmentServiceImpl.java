package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.usergroupassignment.AddUserGroupAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupAssignmentPO;
import com.fisk.datagovernance.mapper.datasecurity.UserGroupAssignmentMapper;
import com.fisk.datagovernance.service.datasecurity.UserGroupAssignmentService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserGroupQueryDTO;
import com.fisk.system.dto.userinfo.UserPowerDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class UserGroupAssignmentServiceImpl
        extends ServiceImpl<UserGroupAssignmentMapper, UserGroupAssignmentPO>
        implements UserGroupAssignmentService {

    @Resource
    UserGroupAssignmentMapper mapper;
    @Resource
    UserClient client;

    @Override
    public Page<UserPowerDTO> getPageUserData(UserGroupQueryDTO dto)
    {
        ResultEntity<Page<UserPowerDTO>> result = client.userGroupQuery(dto);
        if (result.code != ResultEnum.SUCCESS.getCode())
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return result.data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveData(AddUserGroupAssignmentDTO dto)
    {
        ResultEnum resultEnum = deleteData(dto.userGroupId);
        if (resultEnum.getCode()!=ResultEnum.SUCCESS.getCode())
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<UserGroupAssignmentPO> poList=new ArrayList<>();
        for (Integer userId:dto.userIdList)
        {
            UserGroupAssignmentPO po=new UserGroupAssignmentPO();
            po.userGroupId=dto.userGroupId;
            po.userId=userId;
            poList.add(po);
        }
        if (!this.saveBatch(poList))
        {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<Integer> getSelectedUser(long userGroupId)
    {
        QueryWrapper<UserGroupAssignmentPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("user_id").lambda().eq(UserGroupAssignmentPO::getUserGroupId,userGroupId);
        List<Integer> idList=(List) mapper.selectObjs(queryWrapper);
        return idList;
    }

    public ResultEnum deleteData(long userGroupId)
    {
        QueryWrapper<UserGroupAssignmentPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(UserGroupAssignmentPO::getUserGroupId,userGroupId);
        List<UserGroupAssignmentPO> poList=mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList))
        {
            return ResultEnum.SUCCESS;
        }
        return this.remove(queryWrapper)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

}