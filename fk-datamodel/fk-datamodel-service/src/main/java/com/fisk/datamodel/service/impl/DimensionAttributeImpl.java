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
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public List<DimensionMetaDTO> getProjectDimensionTable()
    {
        List<DimensionMetaDTO> list=new ArrayList<>();
        //获取维度表
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        List<DimensionPO> data=mapper.selectList(queryWrapper);
        //获取维度字段表
        QueryWrapper<DimensionAttributePO> queryWrapper2=new QueryWrapper<>();
        List<DimensionAttributePO> list2=attributeMapper.selectList(queryWrapper2);
        for (DimensionPO po:data)
        {
            DimensionMetaDTO model=new DimensionMetaDTO();
            model.tableName=po.dimensionTabName;
            if (po.share)
            {
                model.tableName=model.tableName+"(共享)";
            }
            model.associateDimensionId=po.id;
            List<DimensionAttributePO> filter=list2.stream().filter(e->e.getDimensionId()==po.id && e.getAttributeType() ==DimensionAttributeEnum.BUSINESS_KEY.getValue()).collect(Collectors.toList());
            List<DimensionAttributeAssociationDTO> associationList=new ArrayList<>();
            for (DimensionAttributePO attribute:filter)
            {
                DimensionAttributeAssociationDTO dto=new DimensionAttributeAssociationDTO();
                dto.id=attribute.id;
                dto.dimensionFieldEnName=attribute.dimensionFieldEnName;
                associationList.add(dto);
            }
            model.field=associationList;
            list.add(model);
        }
        return list;
    }

    @Override
    public ResultEnum addDimensionAttribute(int dimensionId,List<DimensionAttributeDTO> dto)
    {
        //判断列名是否重复
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        boolean isExit=false;
        List<DimensionAttributePO> list=new ArrayList<>();
        for (DimensionAttributeDTO item:dto)
        {
            DimensionAttributePO po=attributeMapper.selectOne(queryWrapper.lambda()
                    .eq(DimensionAttributePO::getDimensionFieldEnName,item.dimensionFieldEnName)
                    .eq(DimensionAttributePO::getAttributeType,item.attributeType)
                    .eq(DimensionAttributePO::getTableSourceFieldId,item.tableSourceFieldId)
            );
            if (po !=null)
            {
                isExit=true;
                break;
            }
            DimensionAttributePO data= DimensionAttributeMap.INSTANCES.dtoToPo(item);
            data.dimensionId=dimensionId;
            if (item.attributeType==DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
            {
                //获取维度表id
                DimensionAttributePO dimensionAttributePO=attributeMapper.selectById(item.associateDimensionFieldId);
                if (dimensionAttributePO !=null)
                {
                    data.associateDimensionId=dimensionAttributePO.dimensionId;
                }
            }
            list.add(data);
        }
        if (isExit)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return this.saveBatch(list)?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDimensionAttribute(List<Integer> ids)
    {
        //判断字段是否与其他表有关联
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("associate_dimension_field_id",ids)
                .lambda().eq(DimensionAttributePO::getAttributeType,DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue());
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (list.size()>0)
        {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        //判断字段是否与事实表有关联
        QueryWrapper<FactAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.in("associate_dimension_field_id",ids)
                .lambda().eq(FactAttributePO::getAttributeType, FactAttributeEnum.ASSOCIATED_DIMENSION);
        List<FactAttributePO> poList=factAttributeMapper.selectList(queryWrapper1);
        if (poList.size()>0)
        {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        DimensionAttributePO po=attributeMapper.selectById(ids.get(0));
        return attributeMapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionAttributeListDTO> getDimensionAttributeList(int dimensionId)
    {
        return attributeMapper.getDimensionAttributeList(dimensionId);
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
        DimensionPO po=mapper.selectById(id);
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
            //判断是否为关联维度
            if (item.attributeType==DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
            {
                //获取维度关联维度表名称,用于创建关联key
                DimensionAttributePO attributePO=attributeMapper.selectById(item.associateDimensionFieldId);
                if (attributePO==null)
                {
                    break;
                }
                DimensionPO dimensionPO=mapper.selectById(attributePO.dimensionId);
                if (dimensionPO==null)
                {
                    break;
                }
                //维度关联表名称
                dto.associationTable=dimensionPO.dimensionTabName;
                //维度关联字段名称
                dto.associationField=attributePO.dimensionFieldEnName;
                //关联字段来源
                dto.sourceFieldId=attributePO.tableSourceFieldId;
                //获取关联维度与本表关联字段名称
                DimensionAttributePO dimensionAttributePO=attributeMapper.selectById(item.associateId);
                if (dimensionAttributePO==null)
                {
                    break;
                }
                //关联维度与本表字段关联名称
                dto.fieldEnName=dimensionAttributePO.dimensionFieldEnName;
                //关联维度与本表字段关联来源id
                dto.associationSourceFieldId=dimensionAttributePO.tableSourceFieldId;
            }
            dtoList.add(dto);
        }
        data.dto=dtoList;
        return data;
    }

    @Override
    public List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id)
    {
        List<DimensionAttributeAssociationDTO> data=new ArrayList<>();
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        QueryWrapper <DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,id)
                .ne(DimensionAttributePO::getAttributeType,DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue());
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list))
        {
            return data;
        }
        for (DimensionAttributePO item:list) {
            DimensionAttributeAssociationDTO dto = new DimensionAttributeAssociationDTO();
            dto.id = item.id;
            dto.dimensionFieldEnName = item.dimensionFieldEnName;
            data.add(dto);
        }
        return data;
    }

    @Override
    public List<FieldNameDTO> getDimensionAttributeSourceId(int id)
    {
        //查询维度表
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
                .eq(DimensionAttributePO::getDimensionId,id)
                .ne(DimensionAttributePO::getAttributeType,DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue());
        List<Integer> ids=(List)attributeMapper.selectObjs(queryWrapper).stream().collect(Collectors.toList());
        //过滤已添加来源表id
        list = list.stream().filter(e -> !ids.contains((int)e.getId())).collect(Collectors.toList());
        return list;
    }

}
