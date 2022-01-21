package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
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
            BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPO==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //获取业务过程下所有事实
            QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("id",dto.factIds);
            List<FactPO> factPOList=factMapper.selectList(queryWrapper);
            if (factPOList==null || factPOList.size()==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"维度表为空");
            }
            //更改发布状态
            for (FactPO item:factPOList)
            {
                item.isPublish= PublicStatusEnum.PUBLIC_ING.getValue();
                if (factMapper.updateById(item)==0)
                {
                    throw new FkException(ResultEnum.PUBLISH_FAILURE);
                }
            }
            //获取事实字段数据
            QueryWrapper<FactAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
            //获取事实id集合
            List<Integer> factIds=(List) factMapper.selectObjs(queryWrapper.select("id"));
            List<FactAttributePO> factAttributePOList=factAttributeMapper
                    .selectList(attributePOQueryWrapper.in("fact_id",factIds));
            //遍历取值
            ModelPublishDataDTO data=new ModelPublishDataDTO();
            data.businessAreaId=businessAreaPO.getId();
            data.businessAreaName=businessAreaPO.getBusinessName();
            data.userId=userHelper.getLoginUserInfo().id;
            List<ModelPublishTableDTO> factList=new ArrayList<>();
            //发布历史添加数据
            addTableHistory(dto);
            for (FactPO item:factPOList)
            {
                ModelPublishTableDTO pushDto=new ModelPublishTableDTO();
                pushDto.tableId=Integer.parseInt(String.valueOf(item.id));
                pushDto.tableName=item.factTabName;
                pushDto.createType=CreateTypeEnum.CREATE_FACT.getValue();
                ResultEntity<Map<String, String>> converMap = dataAccessClient.converSql(pushDto.tableName, item.sqlScript, null);
                Map<String, String> data1 = converMap.data;
                pushDto.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
                pushDto.sqlScript = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
                pushDto.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());
                pushDto.synMode=1;
                //获取该维度下所有维度字段
                List<ModelPublishFieldDTO> fieldList=new ArrayList<>();
                List<FactAttributePO> attributePOList=factAttributePOList.stream().filter(e->e.factId==item.id).collect(Collectors.toList());
                for (FactAttributePO attributePO:attributePOList)
                {
                    ModelPublishFieldDTO fieldDTO=new ModelPublishFieldDTO();
                    fieldDTO.fieldId=attributePO.id;
                    fieldDTO.fieldEnName=attributePO.factFieldEnName;
                    fieldDTO.fieldType=attributePO.factFieldType;
                    fieldDTO.fieldLength=attributePO.factFieldLength;
                    fieldDTO.attributeType=attributePO.attributeType;
                    fieldDTO.sourceFieldName=attributePO.sourceFieldName;
                    fieldDTO.associateDimensionId=attributePO.associateDimensionId;
                    fieldDTO.associateDimensionFieldId=attributePO.associateDimensionFieldId;
                    //判断是否关联维度
                    if (attributePO.associateDimensionId !=0 && attributePO.associateDimensionFieldId !=0 )
                    {
                        DimensionPO dimensionPO=dimensionMapper.selectById(attributePO.associateDimensionId);
                        fieldDTO.associateDimensionName=dimensionPO==null?"":dimensionPO.dimensionTabName;
                        fieldDTO.associateDimensionSqlScript=dimensionPO==null?"":dimensionPO.sqlScript;
                        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(attributePO.associateDimensionFieldId);
                        fieldDTO.associateDimensionFieldName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
                    }
                    fieldList.add(fieldDTO);
                }
                pushDto.fieldList=fieldList;
                factList.add(pushDto);
                data.dimensionList=factList;
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
        QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
        factPOQueryWrapper.in("business_process_id",businessProcessIds);
        List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
        for (BusinessProcessListDTO item:dtoList)
        {
            //获取业务过程下所有事实表
            item.factList= FactMap.INSTANCES.poListToDtoList(factPOList.stream()
                    .filter(e->e.businessProcessId==item.id)
                    .sorted(Comparator.comparing(FactPO::getCreateTime))
                    .collect(Collectors.toList()));
            Collections.reverse(item.factList);
            if (item.factList.size()==0)
            {
                continue;
            }
            //查询业务过程下事实表id集合
            List<Integer> factIds=(List)factMapper.selectObjs(factPOQueryWrapper.select("id"));
            if (factIds==null || factIds.size()==0)
            {
                continue;
            }
            //根据事实表id集合,获取事实字段列表以及指标列表
            QueryWrapper<FactAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
            attributePOQueryWrapper.in("fact_id",factIds);
            List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(attributePOQueryWrapper);
            //根据事实表id集合,获取指标列表
            QueryWrapper<IndicatorsPO> indicatorsPOQueryWrapper=new QueryWrapper<>();
            indicatorsPOQueryWrapper.in("fact_id",factIds);
            List<IndicatorsPO> indicatorsPOList=indicatorsMapper.selectList(indicatorsPOQueryWrapper);
            //获取每个事实表下字段列表、指标列表
            for (FactDataDTO fact:item.factList)
            {
                List<FactAttributePO> attributePOS=factAttributePOList.stream()
                        .filter(e->e.factId==fact.id).collect(Collectors.toList());
                if (attributePOS!=null && attributePOS.size()>0)
                {
                    fact.attributeList=FactAttributeMap.INSTANCES.poListToDtoList(attributePOS);
                    //循环获取关联维度表相关数据
                    for (FactAttributeDataDTO attributeItem:fact.attributeList)
                    {
                        if (attributeItem.associateDimensionFieldId !=0)
                        {
                            DimensionPO dimensionPO=dimensionMapper.selectById(attributeItem.associateDimensionId);
                            attributeItem.associateDimensionName=dimensionPO==null?"":dimensionPO.dimensionTabName;
                            DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(attributeItem.associateDimensionFieldId);
                            attributeItem.associateDimensionFieldName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
                        }
                    }
                    Collections.reverse(fact.attributeList);
                }
                List<IndicatorsPO> indicatorsPOS=indicatorsPOList.stream()
                        .filter(e->e.factId==fact.id).collect(Collectors.toList());
                if (indicatorsPOS!=null && indicatorsPOS.size()>0)
                {
                    fact.indicatorsList= AtomicIndicatorsMap.INSTANCES.poListToDtoList(indicatorsPOS);
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
            BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPO==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("business_process_id",dto.businessProcessIds);
            List<FactPO> factPOList=factMapper.selectList(queryWrapper);
            if (factPOList==null || factPOList.size()==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"事实表为空");
            }
            List<DimensionAttributeAddDTO> list=new ArrayList<>();
            DimensionAttributeAddListDTO dimensionAttributeAddListDTO = new DimensionAttributeAddListDTO();
            for (FactPO item:factPOList)
            {
                DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
                pushDto.dimensionId=Integer.parseInt(String.valueOf(item.id));;
                pushDto.dimensionName=item.factTableEnName;
                pushDto.businessAreaName=businessAreaPO.getBusinessName();
                pushDto.createType= CreateTypeEnum.CREATE_FACT.getValue();
                pushDto.userId=userHelper.getLoginUserInfo().id;
                list.add(pushDto);
            }
            dimensionAttributeAddListDTO.dimensionAttributeAddDTOS=list;
            dimensionAttributeAddListDTO.userId=userHelper.getLoginUserInfo().id;
            //发送消息
            //publishTaskClient.publishBuildAtlasDorisTableTask(dimensionAttributeAddListDTO);
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
        BusinessProcessPO businessProcessPO=businessProcessMapper.selectById(po.businessProcessId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "业务过程不存在");
        }
        dto.businessAreaId=businessProcessPO.businessId;
        return  dto;
    }

}
