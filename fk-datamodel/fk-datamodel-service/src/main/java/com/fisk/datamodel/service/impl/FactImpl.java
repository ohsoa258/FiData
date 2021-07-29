package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.fact.FactAssociationDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.map.FactMap;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IFact;
import javafx.scene.layout.RegionBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class FactImpl implements IFact {

    @Resource
    FactMapper mapper;
    @Resource
    UserHelper userHelper;

    @Override
    public IPage<FactDTO> getFactList(QueryDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        if (dto.businessId !=0)
        {
            queryWrapper.lambda().eq(FactPO::getBusinessId,dto.businessId);
        }
        Page<FactPO> data=new Page<>(dto.getPage(),dto.getSize());
        return FactMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum addFact(FactDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getFactName,dto.factName);
        FactPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        FactPO model=FactMap.INSTANCES.dtoToPo(dto);
        model.createUser=userInfo.id.toString();
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactAssociationDTO getFactDetail(int id)
    {
        return mapper.getFactDetail(id);
    }

    @Override
    public ResultEnum updateFact(FactDTO dto)
    {
        FactPO model=mapper.selectById(dto.id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getFactName,dto.factName);
        FactPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model=FactMap.INSTANCES.dtoToPo(dto);
        model.updateUser=userInfo.id.toString();
        return mapper.updateById(model)>0?ResultEnum.SUCCESS: ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteFact(int id)
    {
        FactPO model=mapper.selectById(id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.updateUser=userInfo.id.toString();
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

}
