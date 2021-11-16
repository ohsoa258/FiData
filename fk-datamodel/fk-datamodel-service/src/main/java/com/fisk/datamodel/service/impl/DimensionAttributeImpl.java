package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.map.DimensionMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DimensionAttributeImpl
        extends ServiceImpl<DimensionAttributeMapper, DimensionAttributePO>
        implements IDimensionAttribute {

    @Resource
    DimensionMapper mapper;
    @Resource
    DimensionAttributeMapper attributeMapper;
    @Resource
    DataAccessClient client;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionImpl dimensionImpl;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addOrUpdateDimensionAttribute(int dimensionId,boolean isPublish,List<DimensionAttributeDTO> dto)
    {
        //删除维度字段属性
        List<Integer> ids=(List)dto.stream().filter(e->e.id!=0)
                .map(DimensionAttributeDTO::getId)
                .collect(Collectors.toList());
        if (ids!=null && ids.size()>0)
        {
            QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.notIn("id",ids).lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
            List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
            if (list!=null && list.size()>0)
            {
                boolean flat=this.remove(queryWrapper);
                if (!flat)
                {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
        }
        //添加或修改维度字段
        List<DimensionAttributePO> poList=DimensionAttributeMap.INSTANCES.dtoListToPoList(dto);
        poList.stream().map(e->e.dimensionId=dimensionId).collect(Collectors.toList());
        boolean result=this.saveOrUpdateBatch(poList);
        if (!result)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //是否发布
        if (isPublish)
        {
            ////return dimensionImpl.dimensionPublish(dimensionId);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteDimensionAttribute(List<Integer> ids)
    {
        //判断字段是否与其他表有关联
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("associate_dimension_field_id",ids);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (list.size()>0)
        {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        //判断字段是否与事实表有关联
        QueryWrapper<FactAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.in("associate_dimension_field_id",ids);
        List<FactAttributePO> poList=factAttributeMapper.selectList(queryWrapper1);
        if (poList.size()>0)
        {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        DimensionAttributePO po=attributeMapper.selectById(ids.get(0));
        return attributeMapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionAttributeUpdateDTO getDimensionAttribute(int id)
    {
        return DimensionAttributeMap.INSTANCES.poToDetailDto(attributeMapper.selectById(id));
    }

    @Override
    public DimensionAttributeListDTO getDimensionAttributeList(int dimensionId)
    {
        DimensionAttributeListDTO data=new DimensionAttributeListDTO();
        DimensionPO dimensionPO=mapper.selectById(dimensionId);
        if (dimensionPO==null)
        {
            return data;
        }
        data.sqlScript=dimensionPO.sqlScript;
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        data.attributeDTOList=DimensionAttributeMap.INSTANCES.poListToDtoList(list);
        return data;
    }

    @Override
    public ResultEnum updateDimensionAttribute(DimensionAttributeUpdateDTO dto)
    {
        DimensionAttributePO po=attributeMapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,po.dimensionId)
                .eq(DimensionAttributePO::getDimensionFieldEnName,dto.dimensionFieldEnName);
        DimensionAttributePO model=attributeMapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.dimensionFieldCnName=dto.dimensionFieldCnName;
        po.dimensionFieldDes=dto.dimensionFieldDes;
        po.dimensionFieldLength=dto.dimensionFieldLength;
        po.dimensionFieldEnName=dto.dimensionFieldEnName;
        po.dimensionFieldType=dto.dimensionFieldType;
        ////po=DimensionAttributeMap.INSTANCES.updateDtoToPo(dto);
        return attributeMapper.updateById(po)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }




    @Override
    public List<ModelMetaDataDTO> getDimensionMetaDataList(int businessAreaId)
    {
        List<ModelMetaDataDTO> list=new ArrayList<>();
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getBusinessId,businessAreaId);
        List<DimensionPO> poList=mapper.selectList(queryWrapper);
        if (poList==null || poList.size()==0)
        {
            return list;
        }
        for (DimensionPO item:poList)
        {
            ModelMetaDataDTO dto=getDimensionMetaData((int)item.id);
            if (dto==null)
            {
                break;
            }
            list.add(dto);
        }
        return list;
    }

    @Override
    public ModelMetaDataDTO getDimensionMetaData(int id)
    {
        ModelMetaDataDTO data=new ModelMetaDataDTO();
        /*DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        data.tableName =po.dimensionTabName;
        data.id=po.id;
        data.appId=po.businessId;
        //获取注册表相关数据
        ResultEntity<AppRegistrationDTO> appAbbreviation = client.getData(po.appId);
        if (appAbbreviation.code==ResultEnum.SUCCESS.getCode() || appAbbreviation.data !=null)
        {
            data.appbAbreviation=appAbbreviation.data.appAbbreviation;
        }
        //获取来源表相关数据
        ResultEntity<TableAccessDTO> tableAccess = client.getTableAccess(po.tableSourceId);
        if (tableAccess.code==ResultEnum.SUCCESS.getCode() || tableAccess.data !=null)
        {
            data.sourceTableName=tableAccess.data.tableName;
        }
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,id);
        List<ModelAttributeMetaDataDTO> dtoList=new ArrayList<>();
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list))
        {
            return data;
        }
        for (DimensionAttributePO item:list) {
            ModelAttributeMetaDataDTO dto = new ModelAttributeMetaDataDTO();
            dto.sourceFieldId=item.tableSourceFieldId;
            dto.attributeType = item.attributeType;
            dto.fieldEnName = item.dimensionFieldEnName;
            dto.fieldLength = item.dimensionFieldLength;
            dto.fieldType = item.dimensionFieldType;
            dto.fieldId= String.valueOf(item.id);
            dtoList.add(dto);
        }
        data.dto=dtoList;*/
        return data;
    }

    @Override
    public List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id)
    {
        QueryWrapper <DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionAttributePO::getDimensionId,id);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        return DimensionAttributeMap.INSTANCES.poToNameListDTO(list);
    }

    @Override
    public List<FieldNameDTO> getDimensionAttributeSourceId(int id)
    {
       /* //查询维度表
        DimensionPO dimensionPO=mapper.selectById(id);
        if (dimensionPO==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "维度表不存在");
        }
        ResultEntity<Object> data = client.getTableFieldId(dimensionPO.tableSourceId);
        if (ResultEnum.SUCCESS.equals(data.code))
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, "获取数据接入表数据失败");
        }
        List<FieldNameDTO> list=JSON.parseArray(JSON.toJSONString(data.data), FieldNameDTO.class);
        System.out.println(list);
        if (list ==null || list.size()==0)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据接入表数据为空");
        }
        //获取维度表存在字段来源id
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("table_source_field_id").lambda()
                .eq(DimensionAttributePO::getDimensionId,id);
        List<Integer> ids=(List)attributeMapper.selectObjs(queryWrapper).stream().collect(Collectors.toList());
        //过滤已添加来源表id
        list = list.stream().filter(e -> !ids.contains((int)e.getId())).collect(Collectors.toList());
        return list;*/
        return null;
    }

}
