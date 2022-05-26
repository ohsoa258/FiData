package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessPublishQueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.fact.FactAttributeDetailDTO;
import com.fisk.datamodel.dto.factattribute.*;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.map.SyncModeMap;
import com.fisk.datamodel.map.TableBusinessMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IFactAttribute;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    BusinessProcessMapper businessProcessMapper;
    @Resource
    BusinessProcessImpl businessProcess;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    SyncModeImpl syncMode;
    @Resource
    TableBusinessImpl tableBusiness;

    @Override
    public List<FactAttributeListDTO> getFactAttributeList(int factId)
    {
        return mapper.getFactAttributeList(factId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addFactAttribute(FactAttributeAddDTO dto) {
        //判断是否存在
        FactPO factPo=factMapper.selectById(dto.factId);
        if (factPo==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //添加增量配置
        SyncModePO syncModePo = SyncModeMap.INSTANCES.dtoToPo(dto.syncModeDTO);
        boolean syncMode = this.syncMode.saveOrUpdate(syncModePo);
        boolean tableBusiness=true;
        if (dto.syncModeDTO.syncMode== SyncModeEnum.CUSTOM_OVERRIDE.getValue())
        {
            QueryWrapper<SyncModePO> syncModePoQueryWrapper=new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId,dto.syncModeDTO.syncTableId)
                    .eq(SyncModePO::getTableType,dto.syncModeDTO.tableType);
            SyncModePO po=this.syncMode.getOne(syncModePoQueryWrapper);
            if (po==null)
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            dto.syncModeDTO.syncTableBusinessDTO.syncId=(int)po.id;
            tableBusiness= this.tableBusiness.saveOrUpdate(TableBusinessMap.INSTANCES.dtoToPo(dto.syncModeDTO.syncTableBusinessDTO));
        }
        if (!syncMode || !tableBusiness)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //删除维度字段属性
        List<Integer> ids=(List)dto.list.stream().filter(e->e.id!=0)
                .map(FactAttributeDTO::getId)
                .collect(Collectors.toList());
        if (ids!=null && ids.size()>0)
        {
            QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.notIn("id",ids).lambda().eq(FactAttributePO::getFactId,dto.factId);
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
        List<FactAttributePO> poList=FactAttributeMap.INSTANCES.addDtoToPoList(dto.list);
        poList.stream().map(e->e.factId=dto.factId).collect(Collectors.toList());
        if (!this.saveOrUpdateBatch(poList))
        {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //是否发布
        if (dto.isPublish)
        {
            BusinessProcessPublishQueryDTO queryDTO=new BusinessProcessPublishQueryDTO();
            List<Integer> dimensionIds=new ArrayList<>();
            dimensionIds.add(dto.factId);
            //修改发布状态
            factPo.isPublish= PublicStatusEnum.PUBLIC_ING.getValue();
            if (factMapper.updateById(factPo)==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE);
            }
            queryDTO.factIds=dimensionIds;
            queryDTO.businessAreaId=factPo.businessId;
            queryDTO.remark=dto.remark;
            queryDTO.syncMode=dto.syncModeDTO.syncMode;
            queryDTO.openTransmission=dto.openTransmission;
            return businessProcess.batchPublishBusinessProcess(queryDTO);
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
        BusinessProcessPO businessProcessPo=businessProcessMapper.selectById(po.businessProcessId);
        if (businessProcessPo==null)
        {
            return data;
        }
        data.appId=businessProcessPo.businessId;
        return data;
    }

    @Override
    public List<FactAttributeDropDTO> getFactAttributeData(FactAttributeDropQueryDTO dto)
    {
        List<FactAttributeDropDTO> data=new ArrayList<>();
        FactPO po=factMapper.selectById(dto.id);
        if (po==null)
        {
            return data;
        }
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("attribute_type",dto.type).lambda()
                .eq(FactAttributePO::getFactId,dto.id);
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        for (FactAttributePO item:list) {
            FactAttributeDropDTO dropDTO = new FactAttributeDropDTO();
            dropDTO.id = item.id;
            dropDTO.factFieldEnName = item.factFieldEnName;
            dropDTO.factFieldType=item.factFieldType;
            dropDTO.attributeType=item.attributeType;
            data.add(dropDTO);
        }
        return data;
    }

    @Override
    public List<FieldNameDTO> getFactAttributeSourceId(int id)
    {
        FactPO factPo=factMapper.selectById(id);
        if (factPo==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "事实表不存在");
        }
        return null;
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
        //获取增量配置信息
        QueryWrapper<SyncModePO> syncModePoQueryWrapper=new QueryWrapper<>();
        syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId,po.id)
                .eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_FACT);
        SyncModePO syncModePo=syncMode.getOne(syncModePoQueryWrapper);
        if(syncModePo==null)
        {
            return data;
        }
        data.syncModeDTO=SyncModeMap.INSTANCES.poToDto(syncModePo);
        if (syncModePo.syncMode!= SyncModeEnum.CUSTOM_OVERRIDE.getValue())
        {
            return data;
        }
        QueryWrapper<TableBusinessPO> tableBusinessPoQueryWrapper=new QueryWrapper<>();
        tableBusinessPoQueryWrapper.lambda().eq(TableBusinessPO::getSyncId,syncModePo.id);
        TableBusinessPO tableBusinessPo=tableBusiness.getOne(tableBusinessPoQueryWrapper);
        if (tableBusinessPo==null)
        {
            return data;
        }
        data.syncModeDTO.syncTableBusinessDTO=TableBusinessMap.INSTANCES.poToDto(tableBusinessPo);
        return data;
    }

    @Override
    public ResultEntity<List<ModelPublishFieldDTO>> selectAttributeList(Integer factId){
        Map<String, Object> conditionHashMap = new HashMap<>();
        List<ModelPublishFieldDTO> fieldList=new ArrayList<>();
        conditionHashMap.put("fact_id",factId);
        conditionHashMap.put("del_flag",1);
        List<FactAttributePO> factAttributePoList = mapper.selectByMap(conditionHashMap);
        for (FactAttributePO attributePo:factAttributePoList)
        {
            ModelPublishFieldDTO fieldDTO=new ModelPublishFieldDTO();
            fieldDTO.fieldId=attributePo.id;
            fieldDTO.fieldEnName=attributePo.factFieldEnName;
            fieldDTO.fieldType=attributePo.factFieldType;
            fieldDTO.fieldLength=attributePo.factFieldLength;
            fieldDTO.attributeType=attributePo.attributeType;
            fieldDTO.sourceFieldName=attributePo.sourceFieldName;
            fieldDTO.associateDimensionId=attributePo.associateDimensionId;
            fieldDTO.associateDimensionFieldId=attributePo.associateDimensionFieldId;
            //判断是否关联维度
            if (attributePo.associateDimensionId !=0 && attributePo.associateDimensionFieldId !=0 )
            {
                DimensionPO dimensionPo=dimensionMapper.selectById(attributePo.associateDimensionId);
                fieldDTO.associateDimensionName=dimensionPo==null?"":dimensionPo.dimensionTabName;
                fieldDTO.associateDimensionSqlScript=dimensionPo==null?"":dimensionPo.sqlScript;
                DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
                fieldDTO.associateDimensionFieldName=dimensionAttributePo==null?"":dimensionAttributePo.dimensionFieldEnName;
            }
            fieldList.add(fieldDTO);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, fieldList);
    }

    @Override
    public List<FactAttributeUpdateDTO> getFactAttribute(int factId)
    {
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,factId);
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        return FactAttributeMap.INSTANCES.poDetailToDtoList(list);
    }

}
