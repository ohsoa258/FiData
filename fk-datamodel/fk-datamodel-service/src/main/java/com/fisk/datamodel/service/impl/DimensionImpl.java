package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionDateAttributeDTO;
import com.fisk.datamodel.dto.dimension.DimensionQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.DimensionMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class DimensionImpl implements IDimension {

    @Resource
    DimensionMapper mapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionAttributeImpl dimensionAttributeImpl;

    @Override
    public ResultEnum addDimension(DimensionDTO dto)
    {

        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                //.eq(DimensionPO::getBusinessId,dto.businessId)
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DIMENSION_EXIST;
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
        queryWrapper.lambda()
                //.eq(DimensionPO::getBusinessId,dto.businessId)
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=model.id)
        {
            return ResultEnum.DIMENSION_EXIST;
        }
        model= DimensionMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteDimension(int id)
    {
        DimensionPO model=mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断维度表是否存在关联
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getAssociateDimensionId,id);
        List<DimensionAttributePO> poList=dimensionAttributeMapper.selectList(queryWrapper);
        if (poList.size()>0)
        {
            return ResultEnum.TABLE_ASSOCIATED;
        }
        //判断维度表是否与事实表有关联
        QueryWrapper<FactAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda().eq(FactAttributePO::getAssociateDimensionId,id);
        List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(queryWrapper1);
        if (factAttributePOList.size()>0)
        {
            return ResultEnum.TABLE_ASSOCIATED;
        }
        //删除维度字段数据
        QueryWrapper<DimensionAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
        attributePOQueryWrapper.select("id").lambda().eq(DimensionAttributePO::getDimensionId,id);
        List<Integer> dimensionAttributeIds=(List)dimensionAttributeMapper.selectObjs(attributePOQueryWrapper);
        if (!CollectionUtils.isEmpty(dimensionAttributeIds))
        {
            ResultEnum resultEnum = dimensionAttributeImpl.deleteDimensionAttribute(dimensionAttributeIds);
            if (ResultEnum.SUCCESS !=resultEnum)
            {
                throw new FkException(resultEnum);
            }
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
    public ResultEnum updateDimensionSql(DimensionSqlDTO dto)
    {
        DimensionPO model=mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript=dto.sqlScript;
        return mapper.updateById(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionMetaDTO>getDimensionNameList(DimensionQueryDTO dto)
    {
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionPO::getBusinessId,dto.businessAreaId);
        if (dto.dimensionId !=0)
        {
            queryWrapper.lambda().ne(DimensionPO::getId,dto.dimensionId);
        }
        List<DimensionPO> list=mapper.selectList(queryWrapper);
        return DimensionMap.INSTANCES.poToListNameDto(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateDimensionDateAttribute(DimensionDateAttributeDTO dto)
    {
        //根据业务域id,还原之前的维度表设置的日期维度
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId,dto.businessAreaId)
                .eq(DimensionPO::getIsDimDateTbl,true);
        List<DimensionPO> list=mapper.selectList(queryWrapper);
        //获取维度字段
        QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
        List<DimensionAttributePO> attributePOList=dimensionAttributeMapper.selectList(queryWrapper1);
        if (list!=null && list.size()>0)
        {
            for (DimensionPO item:list)
            {
                item.isDimDateTbl=false;
                if (mapper.updateById(item)==0)
                {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
                List<DimensionAttributePO> attributePOS=attributePOList.stream()
                        .filter(e->e.dimensionId==item.id).collect(Collectors.toList());
                if (attributePOS !=null && attributePOList.size()>0)
                {
                    for (DimensionAttributePO attributePO:attributePOS)
                    {
                        attributePO.isDimDateField=false;
                        if (dimensionAttributeMapper.updateById(attributePO)==0)
                        {
                            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                        }
                    }
                }
            }
        }

        DimensionPO dimensionPO=mapper.selectById(dto.dimensionId);
        if (dimensionPO ==null)
        {
            return ResultEnum.SUCCESS;
        }
        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(dto.dimensionAttributeId);
        if (dimensionAttributePO==null)
        {
            return ResultEnum.SUCCESS;
        }
        dimensionPO.isDimDateTbl=true;
        int flat=mapper.updateById(dimensionPO);
        if (flat==0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        dimensionAttributePO.isDimDateField=true;
        return dimensionAttributeMapper.updateById(dimensionAttributePO)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionDateAttributeDTO getDimensionDateAttribute(int businessId)
    {
        DimensionDateAttributeDTO data=new DimensionDateAttributeDTO();
        //查询设置时间维度表
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId,businessId)
                .eq(DimensionPO::getIsDimDateTbl,true);
        List<DimensionPO> dimensionPOList=mapper.selectList(queryWrapper);
        if (dimensionPOList ==null || dimensionPOList.size()==0)
        {
            return data;
        }
        data.dimensionId=dimensionPOList.get(0).id;
        //查询设置时间维度表字段
        QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda()
                .eq(DimensionAttributePO::getDimensionId,data.dimensionId)
                .eq(DimensionAttributePO::getIsDimDateField,true);
        List<DimensionAttributePO> dimensionAttributePOList=dimensionAttributeMapper.selectList(queryWrapper1);
        if (dimensionAttributePOList ==null || dimensionAttributePOList.size()==0)
        {
            return data;
        }
        data.dimensionAttributeId=dimensionAttributePOList.get(0).id;
        return data;
    }

    @Override
    public void updateDimensionPublishStatus(ModelPublishStatusDTO dto)
    {
        DimensionPO po=mapper.selectById(dto.id);
        if (po !=null)
        {
            //0:DW发布状态
            if (dto.type==0)
            {
                po.isPublish=dto.status;
            }else {
                po.dorisPublish=dto.status;
            }
            mapper.updateById(po);
        }
    }

}
