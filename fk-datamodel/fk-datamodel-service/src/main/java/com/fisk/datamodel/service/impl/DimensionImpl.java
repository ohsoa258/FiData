package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAssociationDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionSourceDTO;
import com.fisk.datamodel.entity.DataAreaPO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.ProjectInfoPO;
import com.fisk.datamodel.map.DimensionMap;
import com.fisk.datamodel.mapper.DataAreaMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.ProjectInfoMapper;
import com.fisk.datamodel.service.IDimension;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DimensionImpl implements IDimension {

    @Resource
    DataAreaMapper dataAreaMapper;
    @Resource
    DimensionMapper mapper;
    @Resource
    ProjectInfoMapper projectInfoMapper;

    @Override
    public List<DimensionSourceDTO> getDimensionList()
    {
        List<DimensionSourceDTO> list=new ArrayList<>();
        //获取项目下所有业务域
        QueryWrapper<ProjectInfoPO> projectInfo=new QueryWrapper<>();
        projectInfo.select("businessid");
        List<Object> ids=projectInfoMapper.selectObjs(projectInfo).stream().distinct().collect(Collectors.toList());
        if (ids ==null || ids.size()==0)
        {
            return list;
        }
        //获取数据源
        QueryWrapper<DataAreaPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("business_id",ids.toArray());
        List<DataAreaPO> dataArea=dataAreaMapper.selectList(queryWrapper).stream().sorted(Comparator.comparing(DataAreaPO::getCreateTime)).collect(Collectors.toList());
        //降序
        Collections.reverse(dataArea);
        //获取维度表
        QueryWrapper<DimensionPO> dimensionList=new QueryWrapper<>();
        List<DimensionPO> dimensionPo=mapper.selectList(dimensionList).stream().sorted(Comparator.comparing(DimensionPO::getCreateTime)).collect(Collectors.toList());
        //降序
        Collections.reverse(dimensionPo);
        for (DataAreaPO item:dataArea)
        {
            DimensionSourceDTO dto=new DimensionSourceDTO();
            dto.id=item.id;
            dto.dimensionCnName=item.dataName;
            list.add(dto);
        }
        return list;
    }

    @Override
    public ResultEnum addDimension(DimensionDTO dto){

        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getBusinessId,dto.businessId)
                            .eq(DimensionPO::getDimensionCnName,dto.dimensionCnName)
                            .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        DimensionPO model= DimensionMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionAssociationDTO getDimension(int id)
    {
        return mapper.getDimension(id);
    }

    @Override
    public ResultEnum updateDimension(DimensionDTO dto)
    {
        DimensionPO model=mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getBusinessId,dto.businessId)
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName)
                .eq(DimensionPO::getDimensionCnName,dto.dimensionCnName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=model.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        model= DimensionMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDimension(int id)
    {
        DimensionPO model=mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public IPage<DimensionDTO> getDimensionList(QueryDTO dto)
    {
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        if (dto.id !=0)
        {
            queryWrapper.lambda().eq(DimensionPO::getBusinessId,dto.id)
                                 .or()
                                 .eq(DimensionPO::getShare,true);
        }
        Page<DimensionPO> data=new Page<>(dto.getPage(),dto.getSize());
        return DimensionMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }


}
