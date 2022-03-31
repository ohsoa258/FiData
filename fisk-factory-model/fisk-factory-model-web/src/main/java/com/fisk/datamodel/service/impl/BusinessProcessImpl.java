package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.*;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddListDTO;
import com.fisk.datamodel.dto.fact.FactDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.map.BusinessProcessMap;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.map.FactMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IBusinessProcess;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class BusinessProcessImpl
        extends ServiceImpl<BusinessProcessMapper,BusinessProcessPO>
        implements IBusinessProcess {

    @Resource
    BusinessProcessMapper mapper;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeImpl factAttribute;
    @Resource
    BusinessAreaMapper businessAreaMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    TableHistoryImpl tableHistory;
    @Resource
    FactImpl factImpl;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    SyncModeMapper syncModeMapper;

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
        queryWrapper.lambda()
                .eq(BusinessProcessPO::getBusinessId,dto.businessId)
                .eq(BusinessProcessPO::getBusinessProcessCnName,dto.businessProcessEnName);
        BusinessProcessPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        BusinessProcessPO model=BusinessProcessMap.INSTANCES.dtoToPo(dto);
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
        queryWrapper.lambda()
                .eq(BusinessProcessPO::getBusinessId,dto.businessId)
                .eq(BusinessProcessPO::getBusinessProcessCnName,dto.businessProcessEnName);
        BusinessProcessPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        model=BusinessProcessMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model)>0?ResultEnum.SUCCESS: ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteBusinessProcess(List<Integer> ids)
    {
        try {
            int flat=mapper.deleteBatchIds(ids);
            if (flat==0)
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("id").in("business_process_id",ids);
            List<Integer> factIds=(List)factMapper.selectObjs(queryWrapper);
            if (factIds ==null || factIds.size()==0)
            {
                return ResultEnum.SUCCESS;
            }
            for (Integer id:factIds)
            {
                ResultEnum resultEnum = factImpl.deleteFact(id);
                if (resultEnum.getCode()!=ResultEnum.SUCCESS.getCode())
                {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("delete businessProcess:"+e);
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum batchPublishBusinessProcess(BusinessProcessPublishQueryDTO dto)
    {
        try {
            BusinessAreaPO businessAreaPo=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //获取业务过程下所有事实
            QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("id",dto.factIds);
            List<FactPO> factPoList=factMapper.selectList(queryWrapper);
            if (factPoList==null || factPoList.size()==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"事实表为空");
            }
            //更改发布状态
            for (FactPO item:factPoList)
            {
                item.isPublish= PublicStatusEnum.PUBLIC_ING.getValue();
                if (factMapper.updateById(item)==0)
                {
                    throw new FkException(ResultEnum.PUBLISH_FAILURE);
                }
            }
            //获取事实字段数据
            QueryWrapper<FactAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
            //获取事实id集合
            List<Integer> factIds=(List) factMapper.selectObjs(queryWrapper.select("id"));
            List<FactAttributePO> factAttributePoList=factAttributeMapper
                    .selectList(attributePoQueryWrapper.in("fact_id",factIds));
            //遍历取值
            ModelPublishDataDTO data=new ModelPublishDataDTO();
            data.businessAreaId=businessAreaPo.getId();
            data.businessAreaName=businessAreaPo.getBusinessName();
            data.userId=userHelper.getLoginUserInfo().id;
            List<ModelPublishTableDTO> factList=new ArrayList<>();
            //获取表增量配置信息
            QueryWrapper<SyncModePO> syncModePoQueryWrapper=new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_FACT.getValue());
            List<SyncModePO> syncModePoList=syncModeMapper.selectList(syncModePoQueryWrapper);
            //发布历史添加数据
            addTableHistory(dto);
            for (FactPO item:factPoList)
            {
                ModelPublishTableDTO pushDto=new ModelPublishTableDTO();
                pushDto.tableId=Integer.parseInt(String.valueOf(item.id));
                pushDto.tableName=item.factTabName;
                pushDto.createType=CreateTypeEnum.CREATE_FACT.getValue();
                ResultEntity<Map<String, String>> converMap = publishTaskClient.converSql(pushDto.tableName, item.sqlScript, "");
                Map<String, String> data1 = converMap.data;
                pushDto.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
                pushDto.sqlScript = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
                pushDto.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());
                //获取事实表同步方式
                if (dto.syncMode !=0)
                {
                    pushDto.synMode=dto.syncMode;
                }
                else {
                    Optional<SyncModePO> first = syncModePoList.stream().filter(e -> e.syncTableId == item.id).findFirst();
                    if (first.isPresent())
                    {
                        pushDto.synMode=first.get().syncMode;
                    }
                }
                //获取该维度下所有维度字段
                List<ModelPublishFieldDTO> fieldList=new ArrayList<>();
                List<FactAttributePO> attributePoList=factAttributePoList.stream().filter(e->e.factId==item.id).collect(Collectors.toList());
                for (FactAttributePO attributePo:attributePoList)
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
                pushDto.fieldList=fieldList;
                factList.add(pushDto);
                data.dimensionList=factList;
                data.openTransmission=dto.openTransmission;
                //发送消息
                publishTaskClient.publishBuildAtlasDorisTableTask(data);
            }
        } catch (FkException ex) {
            log.error(ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }

    private void addTableHistory(BusinessProcessPublishQueryDTO dto)
    {
        List<TableHistoryDTO> list=new ArrayList<>();
        for (Integer id:dto.factIds)
        {
            TableHistoryDTO data=new TableHistoryDTO();
            data.remark=dto.remark;
            data.tableId=id;
            data.tableType=CreateTypeEnum.CREATE_FACT.getValue();
            list.add(data);
        }
        tableHistory.addTableHistory(list);
    }


    @Override
    public List<BusinessProcessListDTO> getBusinessProcessList(int businessAreaId)
    {
        BusinessAreaPO po=businessAreaMapper.selectById(businessAreaId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        List<BusinessProcessListDTO> dtoList=new ArrayList<>();
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda()
                .eq(BusinessProcessPO::getBusinessId,businessAreaId);
        List<BusinessProcessPO> list=mapper.selectList(queryWrapper);
        if (list==null || list.size()==0)
        {
            return dtoList;
        }
        dtoList=BusinessProcessMap.INSTANCES.poListToDtoList(list);
        //获取业务过程id集合
        List<Integer> businessProcessIds=(List)mapper.selectObjs(queryWrapper.select("id"));
        if (businessProcessIds==null || businessProcessIds.size()==0)
        {
            return dtoList;
        }
        //根据业务过程id集合,获取事实表数据
        QueryWrapper<FactPO> factPoQueryWrapper=new QueryWrapper<>();
        factPoQueryWrapper.in("business_process_id",businessProcessIds);
        List<FactPO> factPoList=factMapper.selectList(factPoQueryWrapper);
        for (BusinessProcessListDTO item:dtoList)
        {
            //获取业务过程下所有事实表
            item.factList= FactMap.INSTANCES.poListToDtoList(factPoList.stream()
                    .filter(e->e.businessProcessId==item.id)
                    .sorted(Comparator.comparing(FactPO::getCreateTime))
                    .collect(Collectors.toList()));
            Collections.reverse(item.factList);
            if (item.factList.size()==0)
            {
                continue;
            }
            //查询业务过程下事实表id集合
            List<Integer> factIds=(List)factMapper.selectObjs(factPoQueryWrapper.select("id"));
            if (factIds==null || factIds.size()==0)
            {
                continue;
            }
            //根据事实表id集合,获取事实字段列表以及指标列表
            QueryWrapper<FactAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
            attributePoQueryWrapper.in("fact_id",factIds);
            List<FactAttributePO> factAttributePoList=factAttributeMapper.selectList(attributePoQueryWrapper);
            //根据事实表id集合,获取指标列表
            QueryWrapper<IndicatorsPO> indicatorsPoQueryWrapper=new QueryWrapper<>();
            indicatorsPoQueryWrapper.in("fact_id",factIds);
            List<IndicatorsPO> indicatorsPoList=indicatorsMapper.selectList(indicatorsPoQueryWrapper);
            //获取每个事实表下字段列表、指标列表
            for (FactDataDTO fact:item.factList)
            {
                List<FactAttributePO> attributePoList=factAttributePoList.stream()
                        .filter(e->e.factId==fact.id).collect(Collectors.toList());
                if (attributePoList!=null && attributePoList.size()>0)
                {
                    fact.attributeList=FactAttributeMap.INSTANCES.poListToDtoList(attributePoList);
                    //循环获取关联维度表相关数据
                    for (FactAttributeDataDTO attributeItem:fact.attributeList)
                    {
                        if (attributeItem.associateDimensionFieldId !=0)
                        {
                            DimensionPO dimensionPo=dimensionMapper.selectById(attributeItem.associateDimensionId);
                            attributeItem.associateDimensionName=dimensionPo==null?"":dimensionPo.dimensionTabName;
                            DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(attributeItem.associateDimensionFieldId);
                            attributeItem.associateDimensionFieldName=dimensionAttributePo==null?"":dimensionAttributePo.dimensionFieldEnName;
                        }
                    }
                    Collections.reverse(fact.attributeList);
                }
                List<IndicatorsPO> indicatorsPoStreamList=indicatorsPoList.stream()
                        .filter(e->e.factId==fact.id).collect(Collectors.toList());
                if (indicatorsPoStreamList!=null && indicatorsPoStreamList.size()>0)
                {
                    fact.indicatorsList= AtomicIndicatorsMap.INSTANCES.poListToDtoList(indicatorsPoStreamList);
                    Collections.reverse(fact.indicatorsList);
                }
            }

        }
        return dtoList;
    }






    @Override
    public List<BusinessProcessDropDTO> getBusinessProcessDropList()
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        return BusinessProcessMap.INSTANCES.poToDropPo(mapper.selectList(queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum businessProcessPublish(BusinessProcessPublishDTO dto)
    {
        try{
            BusinessAreaPO businessAreaPo=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("business_process_id",dto.businessProcessIds);
            List<FactPO> factPoList=factMapper.selectList(queryWrapper);
            if (factPoList==null || factPoList.size()==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"事实表为空");
            }
            List<DimensionAttributeAddDTO> list=new ArrayList<>();
            DimensionAttributeAddListDTO dimensionAttributeAddListDTO = new DimensionAttributeAddListDTO();
            for (FactPO item:factPoList)
            {
                DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
                pushDto.dimensionId=Integer.parseInt(String.valueOf(item.id));;
                pushDto.dimensionName=item.factTableEnName;
                pushDto.businessAreaName=businessAreaPo.getBusinessName();
                pushDto.createType= CreateTypeEnum.CREATE_FACT.getValue();
                pushDto.userId=userHelper.getLoginUserInfo().id;
                list.add(pushDto);
            }
            dimensionAttributeAddListDTO.dimensionAttributeAddDtoList=list;
            dimensionAttributeAddListDTO.userId=userHelper.getLoginUserInfo().id;
            //发送消息
            ////publishTaskClient.publishBuildAtlasDorisTableTask(dimensionAttributeAddListDTO);
        }
        catch (Exception ex){
            log.error(ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ModelMetaDataDTO> businessProcessPush(int businessProcessId)
    {
        List<ModelMetaDataDTO> list=new ArrayList<>();
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(FactPO::getBusinessProcessId,businessProcessId);
        List<Object> data=factMapper.selectObjs(queryWrapper);
        List<Integer> ids = (List)data;
        //循环获取事实表
        for (Integer id:ids)
        {
            //获取事实表先关字段
            ModelMetaDataDTO metaDataDTO= factAttribute.getFactMetaData(id);
            if (metaDataDTO==null)
            {
                break;
            }
            list.add(metaDataDTO);
        }
        return list;
    }

    @Override
    public BusinessAreaContentDTO getBusinessId(int factId)
    {
        BusinessAreaContentDTO dto=new BusinessAreaContentDTO();
        //查询事实表信息
        FactPO po=factMapper.selectById(factId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "事实表不存在");
        }
        dto.factTableName=po.factTableEnName;
        //查询业务过程id
        BusinessProcessPO businessProcessPo=businessProcessMapper.selectById(po.businessProcessId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "业务过程不存在");
        }
        dto.businessAreaId=businessProcessPo.businessId;
        return  dto;
    }

}
