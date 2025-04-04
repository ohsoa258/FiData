package com.fisk.datamodel.service.impl.fact;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.datamodel.DataModelTblTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.*;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddListDTO;
import com.fisk.datamodel.dto.fact.FactDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.versionsql.VersionSqlDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.IndicatorsPO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.BusinessProcessPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.map.fact.BusinessProcessMap;
import com.fisk.datamodel.map.fact.FactAttributeMap;
import com.fisk.datamodel.map.fact.FactMap;
import com.fisk.datamodel.map.versionsql.VersionSqlMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.IndicatorsMapper;
import com.fisk.datamodel.mapper.SyncModeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.BusinessProcessMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.mapper.fact.FactMapper;
import com.fisk.datamodel.service.IBusinessProcess;
import com.fisk.datamodel.service.ITableVersionSqlService;
import com.fisk.datamodel.service.impl.BusinessAreaImpl;
import com.fisk.datamodel.service.impl.CustomScriptImpl;
import com.fisk.datamodel.service.impl.TableHistoryImpl;
import com.fisk.datamodel.service.impl.dimension.DimensionFolderImpl;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class BusinessProcessImpl
        extends ServiceImpl<BusinessProcessMapper, BusinessProcessPO>
        implements IBusinessProcess {

    @Value("${fiData-data-dw-source}")
    private Integer targetDbId;

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
    DimensionFolderImpl dimensionFolder;
    @Resource
    FactImpl factImpl;
    @Resource
    CustomScriptImpl customScript;
    @Resource
    SyncModeMapper syncModeMapper;
    @Resource
    private ITableVersionSqlService versionSql;

    @Value("${open-metadata}")
    private Boolean openMetadata;

    @Resource
    private DataManageClient dataManageClient;

    @Resource
    private BusinessAreaImpl businessAreaImpl;

    @Resource
    UserClient userClient;

    @Value("${fiData-data-dw-source}")
    private Integer dw;

    @Override
    public IPage<BusinessProcessDTO> getBusinessProcessList(QueryDTO dto) {
        QueryWrapper<BusinessProcessPO> queryWrapper = new QueryWrapper<>();
        if (dto.id != 0) {
            queryWrapper.lambda().eq(BusinessProcessPO::getBusinessId, dto.id);
        }
        Page<BusinessProcessPO> data = new Page<>(dto.getPage(), dto.getSize());
        return BusinessProcessMap.INSTANCES.pagePoToDto(mapper.selectPage(data, queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum addBusinessProcess(BusinessProcessDTO dto) {
        QueryWrapper<BusinessProcessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessProcessPO::getBusinessId, dto.businessId)
                .eq(BusinessProcessPO::getBusinessProcessCnName, dto.businessProcessEnName);
        BusinessProcessPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.DATA_EXISTS;
        }
        BusinessProcessPO model = BusinessProcessMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public BusinessProcessAssociationDTO getBusinessProcessDetail(int id) {
        return mapper.getBusinessProcessDetail(id);
    }

    @Override
    public ResultEnum updateBusinessProcess(BusinessProcessDTO dto) {
        BusinessProcessPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<BusinessProcessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessProcessPO::getBusinessId, dto.businessId)
                .eq(BusinessProcessPO::getBusinessProcessCnName, dto.businessProcessEnName);
        BusinessProcessPO po = mapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.DATA_EXISTS;
        }
        model = BusinessProcessMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteBusinessProcess(List<Integer> ids) {
        try {
            int flat = mapper.deleteBatchIds(ids);
            if (flat == 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id").in("business_process_id", ids);
            List<Integer> factIds = (List) factMapper.selectObjs(queryWrapper);
            if (CollectionUtils.isEmpty(factIds)) {
                return ResultEnum.SUCCESS;
            }
            for (Integer id : factIds) {
                ResultEnum resultEnum = factImpl.deleteFact(id);
                if (resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("delete businessProcess:" + e);
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum batchPublishBusinessProcess(BusinessProcessPublishQueryDTO dto) {
        BusinessAreaPO businessAreaPo = null;
        List<Integer> factIds;
        try {
            businessAreaPo = businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }

            dimensionFolder.getDwDbType(targetDbId);

            //获取业务过程下所有事实
            QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", dto.factIds);
            List<FactPO> factPoList = factMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(factPoList)) {
                throw new FkException(ResultEnum.PUBLISH_FAILURE, "事实表为空");
            }
            //更改发布状态
            for (FactPO item : factPoList) {
                item.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
                if (factMapper.updateById(item) == 0) {
                    throw new FkException(ResultEnum.PUBLISH_FAILURE);
                }
            }
            //获取事实字段数据
            QueryWrapper<FactAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
            //获取事实id集合
            factIds = (List) factMapper.selectObjs(queryWrapper.select("id"));
            List<FactAttributePO> factAttributePoList = factAttributeMapper
                    .selectList(attributePoQueryWrapper.in("fact_id", factIds));
            //遍历取值
            ModelPublishDataDTO data = new ModelPublishDataDTO();
            data.businessAreaId = businessAreaPo.getId();
            data.businessAreaName = businessAreaPo.getBusinessName();
            data.userId = userHelper.getLoginUserInfo().id;

            //获取表增量配置信息
            QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_FACT.getValue());
            List<SyncModePO> syncModePoList = syncModeMapper.selectList(syncModePoQueryWrapper);
            //发布历史添加数据
            addTableHistory(dto);

            /*
            // 批量查询数据接入应用id对应的目标源id集合——应用appId修改为dataSourceId后，该段代码暂时不用
            List<Integer> appIds = factPoList.stream().map(FactPO::getAppId).collect(Collectors.toList());
            List<AppRegistrationInfoDTO> targetDbIdList = new ArrayList<>();
            try {
                ResultEntity<List<AppRegistrationInfoDTO>> resultEntity = dataAccessClient.getBatchTargetDbIdByAppIds(appIds);
                targetDbIdList = resultEntity.data;
            }catch (Exception e){
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
             */

            for (FactPO item : factPoList) {
                //版本sql添加数据
                VersionSqlDTO versionSqlDTO = new VersionSqlDTO();
                versionSqlDTO.setTableId((int) item.getId());
                versionSqlDTO.setVersionDes(dto.remark);
                versionSqlDTO.setHistoricalSql(item.sqlScript);
                addVersionSql(versionSqlDTO);

                //拼接数据 待发布事实表的信息
                ModelPublishTableDTO pushDto = new ModelPublishTableDTO();
                //增量时间
                pushDto.deltaTimes = dto.deltaTimes;
                //表id
                pushDto.tableId = Integer.parseInt(String.valueOf(item.id));
                //表名
                pushDto.tableName = dimensionFolder.convertName(item.factTabName);
                //创建类型：事实表  1
                pushDto.createType = CreateTypeEnum.CREATE_FACT.getValue();

                //DataTranDTO dtDto用于拼接sql数据传输
                DataTranDTO dtDto = new DataTranDTO();
                //表名称
                dtDto.tableName = pushDto.tableName;
                //维度sql脚本
                dtDto.querySql = item.sqlScript;

                Map<String, String> converSql = publishTaskClient.converSql(dtDto).data;
//                远程调用   -->  拼接sql替换时间
                ResultEntity<Map<String, String>> converMap = publishTaskClient.converSql(dtDto);
                Map<String, String> data1 = converMap.data;
                pushDto.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
                // @start_time @end_time 想生效，这里不能用替换后的sql,而是使用原始sql,然后发布时在task模块替换@start_time 和 @end_time
                //pushDto.queryEndTime = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
                pushDto.sqlScript = item.sqlScript;
                pushDto.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());

                //获取维度键update语句  数仓建模，关联外键的sql在这里传递
                pushDto.factUpdateSql = item.dimensionKeyScript;

                /*
                // 关联建模数据来源id——应用appId修改为dataSourceId后，该段代码暂时不用
                AppRegistrationInfoDTO appDto = targetDbIdList.stream().filter(e -> e.getAppId() == item.appId).findFirst().orElse(null);
                if (appDto != null){
                    pushDto.dataSourceDbId = appDto.getTargetDbId();
                }
                 */

                pushDto.setDataSourceDbId(item.dataSourceId);

                // 设置临时表名称前缀
                pushDto.setPrefixTempName(item.prefixTempName + "_");

                // 关联目标dw库id
                pushDto.setTargetDbId(targetDbId);

                //设置覆盖的脚本
                pushDto.setCoverScript(item.coverScript);
                //设置删除临时表的脚本
                pushDto.setDeleteTempScript(item.deleteTempScript);

                //获取自定义脚本
                CustomScriptQueryDTO customScriptDto = new CustomScriptQueryDTO();
                //2 事实表
                customScriptDto.type = 2;
                customScriptDto.tableId = Integer.parseInt(String.valueOf(item.id));
                customScriptDto.execType = 1;

                String beforeCustomScript = customScript.getBatchScript(customScriptDto);
                if (!StringUtils.isEmpty(beforeCustomScript)) {
                    pushDto.customScript = beforeCustomScript;
                }

                customScriptDto.execType = 2;

                //获取最后的自定义脚本
                String batchScript = customScript.getBatchScript(customScriptDto);
                if (!StringUtils.isEmpty(batchScript)) {
                    pushDto.customScriptAfter = batchScript;
                }

                //获取事实表同步方式
                Optional<SyncModePO> first = syncModePoList
                        .stream()
                        .filter(e -> e.syncTableId == item.id)
                        .findFirst();
                if (first.isPresent()) {
                    pushDto.synMode = first.get().syncMode;
                    pushDto.maxRowsPerFlowFile = first.get().maxRowsPerFlowFile;
                    pushDto.fetchSize = first.get().fetchSize;
                } else {
                    pushDto.synMode = dto.syncMode;
                }
                //获取该维度下所有维度字段
                List<ModelPublishFieldDTO> fieldList = new ArrayList<>();
                List<FactAttributePO> attributePoList = factAttributePoList.stream().filter(e -> e.factId == item.id).collect(Collectors.toList());
                for (FactAttributePO attributePo : attributePoList) {
                    fieldList.add(pushField(attributePo));
                }

                // 维度键没有源字段,暂时将目标字段赋值给源字段
                fieldList.stream().filter(e -> e.attributeType == 1).forEachOrdered(e -> {
                    e.sourceFieldName = e.fieldEnName;
                    e.fieldLength = 200;
                });
                pushDto.fieldList = fieldList;

                List<ModelPublishTableDTO> factList = new ArrayList<>();
                factList.add(pushDto);
                data.dimensionList = factList;
                data.openTransmission = dto.openTransmission;
                //是否删除目标表
                data.ifDropTargetTbl = dto.ifDropTargetTbl;
                data.dimOrFact = DataModelTblTypeEnum.FACT;
                //发送消息,建表
                log.info("数据建模发布表任务json: " + JSON.toJSONString(data));
                publishTaskClient.publishBuildAtlasDorisTableTask(data);

            }
        } catch (FkException ex) {
            log.error(ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }

        try {
            //同步单表元数据
            if (openMetadata) {
                //同步元数据
                BusinessAreaPO finalBusinessAreaPo = businessAreaPo;
                new Thread(() -> {
                    List<MetaDataInstanceAttributeDTO> dataModelMetaData = businessAreaImpl.getDimensionMetaDataOfBatchTbl((int) finalBusinessAreaPo.getId(), factIds, DataModelTableTypeEnum.DW_FACT);
                    consumeMetaData(dataModelMetaData);
                }).start();
            }

        } catch (Exception e) {
            //同步元数据的报错不要抛异常，不要影响单表同步
            log.error("同步元数据失败...");
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 批量发布doris聚合模型表
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum batchPublishForDorisAggregateTbl(BusinessProcessPublishQueryDTO dto) {
        BusinessAreaPO businessAreaPo = null;
        List<Integer> factIds;
        try {
            businessAreaPo = businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }

            dimensionFolder.getDwDbType(targetDbId);

            //获取业务过程下所有事实
            QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", dto.factIds);
            List<FactPO> factPoList = factMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(factPoList)) {
                throw new FkException(ResultEnum.PUBLISH_FAILURE, "事实表为空");
            }
            //更改发布状态
            for (FactPO item : factPoList) {
                item.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
                if (factMapper.updateById(item) == 0) {
                    throw new FkException(ResultEnum.PUBLISH_FAILURE);
                }
            }
            //获取事实字段数据
            QueryWrapper<FactAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
            //获取事实id集合
            factIds = (List) factMapper.selectObjs(queryWrapper.select("id"));
            List<FactAttributePO> factAttributePoList = factAttributeMapper
                    .selectList(attributePoQueryWrapper.in("fact_id", factIds));
            //遍历取值
            ModelPublishDataDTO data = new ModelPublishDataDTO();
            data.businessAreaId = businessAreaPo.getId();
            data.businessAreaName = businessAreaPo.getBusinessName();
            data.userId = userHelper.getLoginUserInfo().id;

            //发布历史添加数据
            addTableHistory(dto);

            for (FactPO item : factPoList) {
                //拼接数据 待发布事实表的信息
                ModelPublishTableDTO pushDto = new ModelPublishTableDTO();
                //表id
                pushDto.tableId = Integer.parseInt(String.valueOf(item.id));
                //表名
                pushDto.tableName = dimensionFolder.convertName(item.factTabName);
                //创建类型：事实表  1
                pushDto.createType = CreateTypeEnum.CREATE_FACT.getValue();

                //DataTranDTO dtDto用于拼接sql数据传输
                DataTranDTO dtDto = new DataTranDTO();
                //表名称
                dtDto.tableName = pushDto.tableName;
                //维度sql脚本
                dtDto.querySql = item.sqlScript;

                pushDto.setDataSourceDbId(item.dataSourceId);

                // 设置临时表名称前缀
                pushDto.setPrefixTempName(item.prefixTempName + "_");

                // 关联目标dw库id
                pushDto.setTargetDbId(targetDbId);

                //获取该事实表下所有事实字段
                List<ModelPublishFieldDTO> fieldList = new ArrayList<>();
                List<FactAttributePO> attributePoList = factAttributePoList.stream().filter(e -> e.factId == item.id).collect(Collectors.toList());
                for (FactAttributePO attributePo : attributePoList) {
                    fieldList.add(pushField(attributePo));
                }

                // 维度键没有源字段,暂时将目标字段赋值给源字段
                fieldList.stream().filter(e -> e.attributeType == 1).forEachOrdered(e -> {
                    e.sourceFieldName = e.fieldEnName;
                    e.fieldLength = 200;
                });
                pushDto.fieldList = fieldList;

                List<ModelPublishTableDTO> factList = new ArrayList<>();
                factList.add(pushDto);
                data.dimensionList = factList;
                data.openTransmission = dto.openTransmission;
                data.dimOrFact = DataModelTblTypeEnum.FACT;
                //发送消息,建表
                log.info("数据建模发布表任务json: " + JSON.toJSONString(data));
                publishTaskClient.publishBuildDorisAggregateTbl(data);

            }
        } catch (FkException ex) {
            log.error("数据建模发布表任务失败：" + ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }

        try {
            //同步单表元数据
            if (openMetadata) {
                List<MetaDataInstanceAttributeDTO> dataModelMetaData = businessAreaImpl.getDimensionMetaDataOfBatchTbl((int) businessAreaPo.getId(), factIds, DataModelTableTypeEnum.DW_FACT);
                consumeMetaData(dataModelMetaData);
            }
        } catch (Exception e) {
            //同步元数据的报错不要抛异常，不要影响单表同步
            log.error("同步元数据失败...");
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public BusinessQueryDataParamDTO getBusinessQueryDataParam(Integer fieldId) {
        // 创建业务查询数据参数对象
        BusinessQueryDataParamDTO businessQueryDataParamDTO = new BusinessQueryDataParamDTO();
        // 通过字段ID获取事实属性信息
        FactAttributePO facttAttributePO = factAttribute.getById(fieldId);
        // 获取事实ID
        int factId = facttAttributePO.getFactId();
        // 构建查询条件，查询同一事实下的所有属性
        LambdaQueryWrapper<FactAttributePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FactAttributePO::getFactId, factId);
        // 执行查询，获取事实属性列表
        List<FactAttributePO> factAttributePOS = factAttributeMapper.selectList(queryWrapper);
        // 将事实属性列表转换为DTO列表
        List<FactAttributeDTO> factAttributeDTOS = FactAttributeMap.INSTANCES.poListsToDtoList(factAttributePOS);
        // 设置转换后的DTO列表到业务查询数据参数对象中
        businessQueryDataParamDTO.setFactAttributeDTOList(factAttributeDTOS);
        // 根据数据源ID调用远程服务获取数据源信息
        ResultEntity<DataSourceDTO> dataSource = userClient.getById(dw);
        // 校验数据源信息获取是否成功
        if (dataSource.getCode() != ResultEnum.SUCCESS.getCode() || dataSource.data == null) {
            // 如果获取失败，抛出异常
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        } else {
            // 如果获取成功，设置数据源IP到业务查询数据参数对象中
            DataSourceDTO data = dataSource.data;
            businessQueryDataParamDTO.setIp(data.conIp);
            businessQueryDataParamDTO.setDbName(data.conDbname);
        }
        // 返回业务查询数据参数对象
        return businessQueryDataParamDTO;
    }

    /**
     * 调用元数据
     *
     * @param list
     */
    private void consumeMetaData(List<MetaDataInstanceAttributeDTO> list) {
        log.info("数仓建模构建元数据实时同步数据对象开始.........:  参数为: {}", JSON.toJSONString(list));
        dataManageClient.consumeMetaData(list);
    }

    public ModelPublishFieldDTO pushField(FactAttributePO attributePo) {
        ModelPublishFieldDTO fieldDTO = new ModelPublishFieldDTO();
        fieldDTO.fieldId = attributePo.id;
        fieldDTO.fieldEnName = dimensionFolder.convertName(attributePo.factFieldEnName);
        fieldDTO.fieldType = attributePo.factFieldType;
        fieldDTO.fieldLength = attributePo.factFieldLength;
        fieldDTO.attributeType = attributePo.attributeType;
        fieldDTO.sourceFieldName = attributePo.sourceFieldName;
        fieldDTO.associateDimensionId = attributePo.associateDimensionId;
        fieldDTO.associateDimensionFieldId = attributePo.associateDimensionFieldId;
        //doris的话就是建表主键
        fieldDTO.isBusinessKey = attributePo.isBusinessKey;
        fieldDTO.isPrimaryKey = attributePo.isPrimaryKey;
        fieldDTO.isPartitionKey = attributePo.isPartitionKey;
        fieldDTO.isDistributedKey = attributePo.isDistributedKey;
        fieldDTO.dorisPartitionType = attributePo.dorisPartitionType;
        fieldDTO.dorisPartitionValues = attributePo.dorisPartitionValues;
        fieldDTO.isAggregateKey = attributePo.isAggregateKey;
        fieldDTO.aggregateType = attributePo.aggregateType;
        //判断是否关联维度
        if (attributePo.associateDimensionId != 0 && attributePo.associateDimensionFieldId != 0) {
            DimensionPO dimensionPo = dimensionMapper.selectById(attributePo.associateDimensionId);
            fieldDTO.associateDimensionName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
            fieldDTO.associateDimensionSqlScript = dimensionPo == null ? "" : dimensionPo.sqlScript;
            DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
            fieldDTO.associateDimensionFieldName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
        }
        return fieldDTO;
    }

    private void addTableHistory(BusinessProcessPublishQueryDTO dto) {
        List<TableHistoryDTO> list = new ArrayList<>();
        for (Integer id : dto.factIds) {
            TableHistoryDTO data = new TableHistoryDTO();
            data.remark = dto.remark;
            data.tableId = id;
            data.tableType = CreateTypeEnum.CREATE_FACT.getValue();
            data.openTransmission = dto.openTransmission;
            list.add(data);
        }
        tableHistory.addTableHistory(list);
    }

    /**
     * 添加表的版本sql
     *
     * @param dto
     */
    private void addVersionSql(VersionSqlDTO dto) {
        VersionSqlDTO data = new VersionSqlDTO();
        data.setVersionNumber(String.valueOf(Instant.now().toEpochMilli()));
        data.setHistoricalSql(dto.getHistoricalSql());
        data.setVersionDes(dto.getVersionDes());
        data.setTableId(dto.getTableId());
        data.setTableType(CreateTypeEnum.CREATE_FACT.getValue());
        versionSql.save(VersionSqlMap.INSTANCES.dtoToPo(data));
    }


    @Override
    public List<BusinessProcessListDTO> getBusinessProcessList(int businessAreaId) {
        BusinessAreaPO po = businessAreaMapper.selectById(businessAreaId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        List<BusinessProcessListDTO> dtoList = new ArrayList<>();
        QueryWrapper<BusinessProcessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda()
                .eq(BusinessProcessPO::getBusinessId, businessAreaId);
        List<BusinessProcessPO> list = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return dtoList;
        }
        dtoList = BusinessProcessMap.INSTANCES.poListToDtoList(list);
        //获取业务过程id集合
        List<Integer> businessProcessIds = (List) mapper.selectObjs(queryWrapper.select("id"));
        if (CollectionUtils.isEmpty(businessProcessIds)) {
            return dtoList;
        }
        //根据业务过程id集合,获取事实表数据
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        factPoQueryWrapper.in("business_process_id", businessProcessIds);
        List<FactPO> factPoList = factMapper.selectList(factPoQueryWrapper);
        for (BusinessProcessListDTO item : dtoList) {
            //获取业务过程下所有事实表
            item.factList = FactMap.INSTANCES.poListToDtoList(factPoList.stream()
                    .filter(e -> e.businessProcessId == item.id)
                    .sorted(Comparator.comparing(FactPO::getCreateTime))
                    .collect(Collectors.toList()));
            Collections.reverse(item.factList);
            if (CollectionUtils.isEmpty(item.factList)) {
                continue;
            }
            //查询业务过程下事实表id集合
            List<Integer> factIds = (List) factMapper.selectObjs(factPoQueryWrapper.select("id"));
            if (CollectionUtils.isEmpty(factIds)) {
                continue;
            }
            //根据事实表id集合,获取事实字段列表以及指标列表
            QueryWrapper<FactAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
            attributePoQueryWrapper.in("fact_id", factIds);
            List<FactAttributePO> factAttributePoList = factAttributeMapper.selectList(attributePoQueryWrapper);
            //根据事实表id集合,获取指标列表
            QueryWrapper<IndicatorsPO> indicatorsPoQueryWrapper = new QueryWrapper<>();
            indicatorsPoQueryWrapper.in("fact_id", factIds);
            List<IndicatorsPO> indicatorsPoList = indicatorsMapper.selectList(indicatorsPoQueryWrapper);
            //获取每个事实表下字段列表、指标列表
            for (FactDataDTO fact : item.factList) {
                List<FactAttributePO> attributePoList = factAttributePoList.stream()
                        .filter(e -> e.factId == fact.id).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(attributePoList)) {
                    fact.attributeList = FactAttributeMap.INSTANCES.poListToDtoList(attributePoList);
                    //循环获取关联维度表相关数据
                    for (FactAttributeDataDTO attributeItem : fact.attributeList) {
                        if (attributeItem.associateDimensionFieldId != 0) {
                            DimensionPO dimensionPo = dimensionMapper.selectById(attributeItem.associateDimensionId);
                            attributeItem.associateDimensionName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
                            DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributeItem.associateDimensionFieldId);
                            attributeItem.associateDimensionFieldName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
                        }
                    }
                    Collections.reverse(fact.attributeList);
                }
                List<IndicatorsPO> indicatorsPoStreamList = indicatorsPoList.stream()
                        .filter(e -> e.factId == fact.id).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(indicatorsPoStreamList)) {
                    fact.indicatorsList = AtomicIndicatorsMap.INSTANCES.poListToDtoList(indicatorsPoStreamList);
                    Collections.reverse(fact.indicatorsList);
                }
            }

        }
        return dtoList;
    }


    @Override
    public List<BusinessProcessDropDTO> getBusinessProcessDropList() {
        QueryWrapper<BusinessProcessPO> queryWrapper = new QueryWrapper<>();
        return BusinessProcessMap.INSTANCES.poToDropPo(mapper.selectList(queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum businessProcessPublish(BusinessProcessPublishDTO dto) {
        try {
            BusinessAreaPO businessAreaPo = businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("business_process_id", dto.businessProcessIds);
            List<FactPO> factPoList = factMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(factPoList)) {
                throw new FkException(ResultEnum.PUBLISH_FAILURE, "事实表为空");
            }
            List<DimensionAttributeAddDTO> list = new ArrayList<>();
            DimensionAttributeAddListDTO dimensionAttributeAddListDTO = new DimensionAttributeAddListDTO();
            for (FactPO item : factPoList) {
                DimensionAttributeAddDTO pushDto = new DimensionAttributeAddDTO();
                pushDto.dimensionId = Integer.parseInt(String.valueOf(item.id));
                ;
                pushDto.dimensionName = item.factTabName;
                pushDto.businessAreaName = businessAreaPo.getBusinessName();
                pushDto.createType = CreateTypeEnum.CREATE_FACT.getValue();
                pushDto.userId = userHelper.getLoginUserInfo().id;
                list.add(pushDto);
            }
            dimensionAttributeAddListDTO.dimensionAttributeAddDtoList = list;
            dimensionAttributeAddListDTO.userId = userHelper.getLoginUserInfo().id;
            //发送消息
            ////publishTaskClient.publishBuildAtlasDorisTableTask(dimensionAttributeAddListDTO);
        } catch (Exception ex) {
            log.error("业务过程发布失败,{}", ex);
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ModelMetaDataDTO> businessProcessPush(int businessProcessId) {
        List<ModelMetaDataDTO> list = new ArrayList<>();
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(FactPO::getBusinessProcessId, businessProcessId);
        List<Object> data = factMapper.selectObjs(queryWrapper);
        List<Integer> ids = (List) data;
        //循环获取事实表
        for (Integer id : ids) {
            //获取事实表先关字段
            ModelMetaDataDTO metaDataDTO = factAttribute.getFactMetaData(id);
            if (metaDataDTO == null) {
                break;
            }
            list.add(metaDataDTO);
        }
        return list;
    }

    @Override
    public BusinessAreaContentDTO getBusinessId(int factId) {
        BusinessAreaContentDTO dto = new BusinessAreaContentDTO();
        //查询事实表信息
        FactPO po = factMapper.selectById(factId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "事实表不存在");
        }
        dto.factTableName = po.factTabName;
        //查询业务过程id
        BusinessProcessPO businessProcessPo = businessProcessMapper.selectById(po.businessProcessId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "业务过程不存在");
        }
        dto.businessAreaId = businessProcessPo.businessId;
        return dto;
    }

}
