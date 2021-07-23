package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.ProjectDimensionAssociationDTO;
import com.fisk.datamodel.dto.ProjectDimensionDTO;
import com.fisk.datamodel.dto.ProjectDimensionSourceDTO;
import com.fisk.datamodel.dto.ProjectInfoDropDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DataAreaPO;
import com.fisk.datamodel.entity.ProjectDimensionPO;
import com.fisk.datamodel.entity.ProjectInfoPO;
import com.fisk.datamodel.map.ProjectDimensionMap;
import com.fisk.datamodel.map.ProjectInfoMap;
import com.fisk.datamodel.mapper.DataAreaMapper;
import com.fisk.datamodel.mapper.ProjectDimensionMapper;
import com.fisk.datamodel.mapper.ProjectInfoMapper;
import com.fisk.datamodel.service.IProjectDimension;
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
public class ProjectDimensionImpl implements IProjectDimension {

    @Resource
    DataAreaMapper dataAreaMapper;
    @Resource
    ProjectDimensionMapper mapper;
    @Resource
    ProjectInfoMapper projectInfoMapper;
    @Resource
    UserHelper userHelper;

    @Override
    public List<ProjectDimensionSourceDTO> getDimensionList()
    {
        List<ProjectDimensionSourceDTO> list=new ArrayList<>();
        //获取项目下所有业务域
        QueryWrapper<ProjectInfoPO> projectInfo=new QueryWrapper<>();
        projectInfo.select("businessid");
        List<Object> ids=projectInfoMapper.selectObjs(projectInfo).stream().distinct().collect(Collectors.toList());
        if (ids.size()==0)
        {
            return list;
        }
        //获取数据源
        QueryWrapper<DataAreaPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("businessid",ids.toArray());
        List<DataAreaPO> dataArea=dataAreaMapper.selectList(queryWrapper).stream().sorted(Comparator.comparing(DataAreaPO::getCreateTime)).collect(Collectors.toList());
        //降序
        Collections.reverse(dataArea);
        ////ProjectDimensionMap.INSTANCES.poToDtoList(dataAreaMapper.selectList(queryWrapper));
        //获取维度表
        QueryWrapper<ProjectDimensionPO> dimensionList=new QueryWrapper<>();
        List<ProjectDimensionPO> dimensionPo=mapper.selectList(dimensionList).stream().sorted(Comparator.comparing(ProjectDimensionPO::getCreateTime)).collect(Collectors.toList());
        //降序
        Collections.reverse(dimensionPo);
        for (DataAreaPO item:dataArea)
        {
            ProjectDimensionSourceDTO dto=new ProjectDimensionSourceDTO();
            dto.id=item.id;
            dto.dimensionCnName=item.dataName;
            dto.data=ProjectDimensionMap.INSTANCES.listPoToListDto(
                    dimensionPo.stream().filter(e->e.getDataId()==item.id).collect(Collectors.toList())
            );
            list.add(dto);
        }
        return list;
    }

    @Override
    public ResultEnum addDimension(ProjectDimensionDTO dto){

        QueryWrapper<ProjectDimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ProjectDimensionPO::getBusinessId,dto.businessId)
                            .eq(ProjectDimensionPO::getDataId,dto.dataId)
                            .eq(ProjectDimensionPO::getDimensionCnName,dto.dimensionCnName)
                            .eq(ProjectDimensionPO::getDimensionTabName,dto.dimensionTabName);
        ProjectDimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        ProjectDimensionPO model= ProjectDimensionMap.INSTANCES.dtoToPo(dto);
        model.createUser=userInfo.id.toString();
        return mapper.insert(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ProjectDimensionAssociationDTO getDimension(int id)
    {
        return mapper.getDimension(id);
    }

    @Override
    public ResultEnum updateDimension(ProjectDimensionDTO dto)
    {
        ProjectDimensionPO model=mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<ProjectDimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ProjectDimensionPO::getBusinessId,dto.businessId)
                .eq(ProjectDimensionPO::getDataId,dto.dataId)
                .eq(ProjectDimensionPO::getDimensionTabName,dto.dimensionTabName)
                .eq(ProjectDimensionPO::getDimensionCnName,dto.dimensionCnName);
        ProjectDimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=model.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model= ProjectDimensionMap.INSTANCES.dtoToPo(dto);
        model.updateUser=userInfo.id.toString();
        return mapper.updateById(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDimension(int id)
    {
        ProjectDimensionPO model=mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.updateUser=userInfo.id.toString();
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ProjectDimensionAssociationDTO getRegionDetail(int id)
    {
        return mapper.getDimensionAssociation(id);
    }

    @Override
    public List<ProjectInfoDropDTO> getProjectDropList(int dataId)
    {
        List<ProjectInfoDropDTO> list=new ArrayList<>();
        DataAreaPO po=dataAreaMapper.selectById(dataId);
        if (po==null)
        {
            return list;
        }
        QueryWrapper<ProjectInfoPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ProjectInfoPO::getBusinessid,po.businessid);
        list= ProjectInfoMap.INSTANCES.poToDto(projectInfoMapper.selectList(queryWrapper));
        return list;
    }

}
