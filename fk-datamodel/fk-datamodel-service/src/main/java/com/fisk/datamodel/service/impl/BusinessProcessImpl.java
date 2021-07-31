package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessAssociationDTO;
import com.fisk.datamodel.entity.BusinessProcessPO;
import com.fisk.datamodel.map.BusinessProcessMap;
import com.fisk.datamodel.mapper.BusinessProcessMapper;
import com.fisk.datamodel.service.IBusinessProcess;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class BusinessProcessImpl implements IBusinessProcess {

    @Resource
    BusinessProcessMapper mapper;
    @Resource
    UserHelper userHelper;

    @Override
    public IPage<BusinessProcessDTO> getBusinessProcessList(QueryDTO dto)
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        if (dto.id !=0)
        {
            queryWrapper.lambda().eq(BusinessProcessPO::getBusinessId,dto.id);
        }
        Page<BusinessProcessPO> data=new Page<>(dto.getPage(),dto.getSize());
        return BusinessProcessMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum addBusinessProcess(BusinessProcessDTO dto)
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessProcessPO::getBusinessProcessCnName,dto.businessProcessCnName);
        BusinessProcessPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        BusinessProcessPO model=BusinessProcessMap.INSTANCES.dtoToPo(dto);
        model.createUser=userInfo.id.toString();
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public BusinessProcessAssociationDTO getBusinessProcessDetail(int id)
    {
        return mapper.getBusinessProcessDetail(id);
    }

    @Override
    public ResultEnum updateBusinessProcess(BusinessProcessDTO dto)
    {
        BusinessProcessPO model=mapper.selectById(dto.id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessProcessPO::getBusinessProcessCnName,dto.businessProcessCnName);
        BusinessProcessPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model=BusinessProcessMap.INSTANCES.dtoToPo(dto);
        model.updateUser=userInfo.id.toString();
        return mapper.updateById(model)>0?ResultEnum.SUCCESS: ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteBusinessProcess(int id)
    {
        BusinessProcessPO model=mapper.selectById(id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.updateUser=userInfo.id.toString();
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

}
