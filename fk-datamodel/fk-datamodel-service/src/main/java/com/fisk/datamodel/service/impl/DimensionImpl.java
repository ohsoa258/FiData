package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAssociationDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.DimensionFolderMap;
import com.fisk.datamodel.map.DimensionMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class DimensionImpl implements IDimension {

    @Resource
    DimensionMapper mapper;
    @Resource
    UserHelper userHelper;
    @Resource
    BusinessAreaMapper businessAreaMapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;

    @Override
    public ResultEnum addDimension(DimensionDTO dto)
    {

        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getBusinessId,dto.businessId)
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.BUSINESS_AREA_EXIST;
        }
        dto.isPublish= PublicStatusEnum.UN_PUBLIC.getValue();
        DimensionPO model= DimensionMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=model.id)
        {
            return ResultEnum.BUSINESS_AREA_EXIST;
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
        //判断维度表是否存在关联
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("associate_dimension_id",id)
                .lambda().eq(DimensionAttributePO::getAttributeType, DimensionAttributeEnum.ASSOCIATED_DIMENSION);
        List<DimensionAttributePO> poList=dimensionAttributeMapper.selectList(queryWrapper);
        if (poList.size()>0)
        {
            return ResultEnum.TABLE_ASSOCIATED;
        }
        //判断维度表是否与事实表有关联
        QueryWrapper<FactAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.in("associate_dimension_id",id)
                .lambda().eq(FactAttributePO::getAttributeType, FactAttributeEnum.ASSOCIATED_DIMENSION);
        List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(queryWrapper1);
        if (factAttributePOList.size()>0)
        {
            return ResultEnum.TABLE_ASSOCIATED;
        }

        //判断是否发布
        if (model.isPublish!=PublicStatusEnum.UN_PUBLIC.getValue())
        {
            //删除组合对象
            DataModelVO vo=new DataModelVO();
            vo.businessId= String.valueOf(model.businessId);
            vo.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
            vo.delBusiness=false;
            DataModelTableVO tableVO=new DataModelTableVO();
            tableVO.type= OlapTableEnum.DIMENSION;
            List<Long> ids=new ArrayList<>();
            ids.add(Long.valueOf(id));
            tableVO.ids=ids;
            vo.dimensionIdList=tableVO;
        }

        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionDTO getDimension(int id)
    {
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DimensionMap.INSTANCES.poToDto(po);
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

    @Override
    public ResultEnum dimensionPublish(int id)
    {
        try{
            DimensionPO po=mapper.selectById(id);
            if (po==null)
            {
                return ResultEnum.DATA_NOTEXISTS;
            }
            BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(po.businessId);
            if (businessAreaPO==null)
            {
                return ResultEnum.DATA_NOTEXISTS;
            }
            DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
            pushDto.dimensionId=id;
            pushDto.dimensionName=po.dimensionTabName;
            pushDto.businessAreaName=businessAreaPO.getBusinessName();
            pushDto.createType= CreateTypeEnum.CREATE_DIMENSION.getValue();
            pushDto.userId=userHelper.getLoginUserInfo().id;
            //发送消息
            //publishTaskClient.publishBuildAtlasDorisTableTask(pushDto);
        }
        catch (Exception ex){
            log.error(ex.getMessage());
            return ResultEnum.PUBLISH_FAILURE;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public void updatePublishStatus(int id,int isSuccess)
    {
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            log.info(id+":数据不存在");
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        po.isPublish=isSuccess;
        int flat=mapper.updateById(po);
        String msg=flat>0?"发布成功":"发布失败";
        log.info(po.dimensionTabName+":"+msg);
    }

}
