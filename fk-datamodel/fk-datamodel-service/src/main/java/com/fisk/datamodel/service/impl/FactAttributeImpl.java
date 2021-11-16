package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.datamodel.dto.fact.FactAttributeDetailDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDropDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IFactAttribute;
import net.bytebuddy.implementation.bytecode.Throw;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class FactAttributeImpl
        extends ServiceImpl<FactAttributeMapper,FactAttributePO>
        implements IFactAttribute {

    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeMapper mapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    DimensionAttributeMapper attributeMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;
    @Resource
    DataAccessClient client;

    @Override
    public List<FactAttributeListDTO> getFactAttributeList(int factId)
    {
        return mapper.getFactAttributeList(factId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addFactAttribute(int factId,boolean isPublish, List<FactAttributeDTO> dto) {
        //删除维度字段属性
        List<Integer> ids=(List)dto.stream().filter(e->e.id!=0)
                .map(FactAttributeDTO::getId)
                .collect(Collectors.toList());
        if (ids!=null && ids.size()>0)
        {
            QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.notIn("id",ids).lambda().eq(FactAttributePO::getFactId,factId);
            List<FactAttributePO> list=mapper.selectList(queryWrapper);
            if (list!=null && list.size()>0)
            {
                if (!this.remove(queryWrapper))
                {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
        }
        //添加事实字段
        List<FactAttributePO> poList=FactAttributeMap.INSTANCES.addDtoToPoList(dto);
        poList.stream().map(e->e.factId=factId).collect(Collectors.toList());
        if (!this.saveOrUpdateBatch(poList))
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
    public ResultEnum deleteFactAttribute(List<Integer> ids)
    {

        return mapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactAttributeUpdateDTO getFactAttributeDetail(int factAttributeId)
    {
        return FactAttributeMap.INSTANCES.poDetailToDto(mapper.selectById(factAttributeId));
    }

    @Override
    public ResultEnum updateFactAttribute(FactAttributeUpdateDTO dto)
    {
        FactAttributePO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,po.factId)
                .eq(FactAttributePO::getFactFieldEnName,dto.factFieldEnName);
        FactAttributePO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.factFieldCnName=dto.factFieldCnName;
        po.factFieldDes=dto.factFieldDes;
        po.factFieldLength=dto.factFieldLength;
        po.factFieldEnName=dto.factFieldEnName;
        po.factFieldType=dto.factFieldType;
        return mapper.updateById(po)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ModelMetaDataDTO getFactMetaData(int id)
    {
        ModelMetaDataDTO data=new ModelMetaDataDTO();
        FactPO po=factMapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        data.tableName =po.factTableEnName;
        data.id=po.id;

        //查找业务域id
        BusinessProcessPO businessProcessPO=businessProcessMapper.selectById(po.businessProcessId);
        if (businessProcessPO==null)
        {
            return data;
        }
        data.appId=businessProcessPO.businessId;

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
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,id);
        List<ModelAttributeMetaDataDTO> dtoList=new ArrayList<>();
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        for (FactAttributePO item:list) {
            ModelAttributeMetaDataDTO dto =new ModelAttributeMetaDataDTO();
            dto.attributeType = item.attributeType;
            dto.fieldEnName = item.factFieldEnName;
            dto.fieldLength = item.factFieldLength;
            dto.fieldType = item.factFieldType;
            dto.fieldCnName = item.factFieldCnName;
            dto.sourceFieldId=item.tableSourceFieldId;
            dto.fieldId=String.valueOf(item.id);
            dtoList.add(dto);
        }
        data.dto=dtoList;
        return data;
    }

    @Override
    public List<FactAttributeDropDTO> GetFactAttributeData(int id)
    {
        List<FactAttributeDropDTO> data=new ArrayList<>();
        FactPO po=factMapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,id);
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        for (FactAttributePO item:list) {
            FactAttributeDropDTO dto = new FactAttributeDropDTO();
            dto.id = item.id;
            dto.factFieldEnName = item.factFieldEnName;
            data.add(dto);
        }
        return data;
    }

    @Override
    public List<FieldNameDTO> getFactAttributeSourceId(int id)
    {
        FactPO factPO=factMapper.selectById(id);
        if (factPO==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "事实表不存在");
        }
        ResultEntity<Object> data=client.getTableFieldId(factPO.tableSourceId);
        if (ResultEnum.SUCCESS.equals(data.code))
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, "获取数据接入表数据失败");
        }
        List<FieldNameDTO> list= JSON.parseArray(JSON.toJSONString(data.data), FieldNameDTO.class);
        System.out.println(list);
        if (list ==null || list.size()==0)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据接入表数据为空");
        }
        //获取维度表存在字段来源id
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("table_source_field_id").lambda()
                .eq(FactAttributePO::getFactId,id);
        List<Integer> ids=(List)mapper.selectObjs(queryWrapper).stream().collect(Collectors.toList());
        //过滤已添加来源表id
        return list.stream().filter(e -> !ids.contains((int)e.getId())).collect(Collectors.toList());
    }

    @Override
    public FactAttributeDetailDTO getFactAttributeDataList(int factId)
    {
        FactAttributeDetailDTO data=new FactAttributeDetailDTO();
        FactPO po=factMapper.selectById(factId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        data.sqlScript=po.sqlScript;
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,factId);
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        data.attributeDTO= FactAttributeMap.INSTANCES.poListsToDtoList(list);
        return data;
    }

}
