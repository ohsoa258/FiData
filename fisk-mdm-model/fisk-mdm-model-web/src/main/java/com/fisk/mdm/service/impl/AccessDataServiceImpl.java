package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.common.service.dbBEBuild.datamodel.BuildDataModelHelper;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.dbBEBuild.datamodel.dto.RelationDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datamodel.enums.DataBaseTypeEnum;
import com.fisk.datamodel.enums.RelateTableTypeEnum;
import com.fisk.mdm.dto.access.*;
import com.fisk.mdm.dto.accessmodel.AccessPublishDataDTO;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.entity.*;
import com.fisk.mdm.enums.PublicStatusEnum;
import com.fisk.mdm.enums.SystemVariableTypeEnum;
import com.fisk.mdm.map.*;
import com.fisk.mdm.mapper.AccessDataMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.mapper.TableHistoryMapper;
import com.fisk.mdm.service.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import com.fisk.task.dto.accessmdm.AccessMdmPublishFieldDTO;
import com.fisk.task.dto.accessmdm.AccessMdmPublishTableDTO;
import com.fisk.task.dto.mdmconfig.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.generateMdmTableName;
import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.generateStgTableName;
import static java.util.stream.Collectors.groupingBy;

@Service("accessDataService")
@Slf4j
public class AccessDataServiceImpl extends ServiceImpl<AccessDataMapper, AccessDataPO> implements AccessDataService {

    @Value("${fiData-data-mdm-source}")
    private Integer targetDbId;
    @Resource
    private AccessDataMapper mapper;

    @Resource
    private AccessTransformationService transformationService;

    @Resource
    private ISystemVariables systemVariables;

    @Resource
    private AttributeService attributeService;

    @Resource
    private ISyncMode syncMode;

    @Resource
    private CustomScriptImpl customScript;

    @Resource
    private AccessDataServiceImpl accessDataService;

    @Resource
    private ModelMapper modelMapper;
    @Resource
    private EntityMapper entityMapper;
    @Resource
    private TableHistoryMapper tableHistoryMapper;
    @Resource
    private TableSourceRelationsService tableSourceRelationsService;
    @Resource
    private ITableHistory iTableHistory;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserClient userClient;
    @Resource
    private UserHelper userHelper;

    @Value("${pgsql-mdm.url}")
    private String pgJdbcStr;
    @Value("${pgsql-mdm.username}")
    private String pgUser;
    @Value("${pgsql-mdm.password}")
    private String pgPassword;

    @Value("${taskdb.url}")
    private String jdbcStr;
    @Value("${taskdb.username}")
    private String user;
    @Value("${taskdb.password}")
    private String password;

    private DataSourceTypeEnum dataSourceTypeEnum;

    @Override
    public AccessAttributeListDTO getAccessAttributeList(Integer moudleId, Integer entityId) {
        AccessAttributeListDTO data = new AccessAttributeListDTO();
        LambdaQueryWrapper<AccessDataPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AccessDataPO::getModelId, moudleId)
                .eq(AccessDataPO::getEntityId, entityId);
        AccessDataPO accessPo = mapper.selectOne(lambdaQueryWrapper);
        if (accessPo == null) {
            AccessDataPO accessDataPo = new AccessDataPO();
            accessDataPo.setModelId(moudleId);
            accessDataPo.setEntityId(entityId);
            this.save(accessDataPo);
            data.accessId = (int) accessDataPo.getId();
            return data;
        }
        data.accessId = (int) accessPo.getId();
        //获取sql脚本
        data.sqlScript = accessPo.getExtractionSql();
        data.dataSourceId = accessPo.getSouceSystemId();
        data.domainUpdateSql = accessPo.domainUpdateSql;
        data.versionId = accessPo.versionId;
        //获取表字段详情
        data.attributeInfoDTOS = attributeService.listPublishedAttribute(entityId);

        //获取增量配置信息
        QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
        syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId, accessPo.id);
        SyncModePO syncModePo = syncMode.getOne(syncModePoQueryWrapper);
        if (syncModePo == null) {
            return data;
        }
        data.syncModeDTO = SyncModeMap.INSTANCES.poToDto(syncModePo);

        LambdaQueryWrapper<TableSourceRelationsPO> queryRelations = new LambdaQueryWrapper<>();
        queryRelations.eq(TableSourceRelationsPO::getAccessId,accessPo.id);
        List<TableSourceRelationsPO> tableSourceRelations = tableSourceRelationsService.list(queryRelations);
        List<RelationDTO> tableSourceRelationsDTOS = TableSourceRelationsMap.INSTANCES.poListToDtoList(tableSourceRelations);
        data.setTableSourceRelationsDTO(tableSourceRelationsDTOS);
        //自定义脚本
        CustomScriptQueryDTO queryDto = new CustomScriptQueryDTO();
        queryDto.tableId = (int) accessPo.getId();
        queryDto.execType = 1;
        data.customScriptList = customScript.listCustomScript(queryDto);
        queryDto.execType = 2;
        data.customScriptList.addAll(customScript.listCustomScript(queryDto));

        // 系统变量
        data.deltaTimes = systemVariables.getSystemVariable(data.accessId);

        data.execSql = accessPo.getLoadingSql();

        return data;
    }

    @Override
    public Object getAccessDefaultSql(Integer moudleId, Integer entityId) {
        LambdaQueryWrapper<AccessDataPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AccessDataPO::getModelId, moudleId)
                .eq(AccessDataPO::getEntityId, entityId);
        AccessDataPO accessPo = mapper.selectOne(lambdaQueryWrapper);
        List<AccessAttributeDTO> accessAttributeField = getAccessAttributeField((int) accessPo.id, accessPo.getEntityId());
        OverlayCodePreviewAccessDTO dto = new OverlayCodePreviewAccessDTO();
        dto.AccessMdmPublishFieldDTOList = accessAttributeField;
        dto.syncMode = accessPo.getAccessType();
        dto.id = (int) accessPo.id;
        return mdmOverlayCodePreview(dto);
    }

    @Override
    public ResultEnum updateAccessSql(AccessSqlDTO dto) {
        AccessDataPO model = mapper.selectById(dto.getId());
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.setExtractionSql(dto.getSqlScript());
        model.setSouceSystemId(dto.getDataSourceId());
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addOrUpdateAccessAttribute(AccessAttributeAddDTO dto) {
        //判断是否存在
        AccessDataPO accessDataPO = mapper.selectById(dto.accessId);
        if (accessDataPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //系统变量
        if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.accessId, dto.deltaTimes);
        }

        //添加增量配置
        SyncModePO syncModePo = SyncModeMap.INSTANCES.dtoToPo(dto.syncModeDTO);
        boolean syncMode = this.syncMode.saveOrUpdate(syncModePo);
        boolean tableBusiness = true;
        if (!syncMode || !tableBusiness) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //更新表基于域表源关系
        if (CollectionUtils.isNotEmpty(dto.tableSourceRelationsDTO)){
            LambdaQueryWrapper<TableSourceRelationsPO> queryRelations = new LambdaQueryWrapper<>();
            queryRelations.eq(TableSourceRelationsPO::getAccessId,dto.accessId);
            tableSourceRelationsService.remove(queryRelations);
            List<TableSourceRelationsPO> tableSourceRelationsPOS = TableSourceRelationsMap.INSTANCES.dtoListToPoList(dto.tableSourceRelationsDTO);
            tableSourceRelationsPOS = tableSourceRelationsPOS.stream().map(i->{
                i.setAccessId(dto.accessId);
                return i;
            }).collect(Collectors.toList());
            tableSourceRelationsService.saveBatch(tableSourceRelationsPOS);
        }
        //自定义脚本
        customScript.addOrUpdateCustomScript(dto.customScriptList);

        //删除字段属性
        QueryWrapper<AccessTransformationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_id", dto.accessId);
        transformationService.remove(queryWrapper);
        //添加或修改字段
        List<AccessTransformationPO> poList = AccessTransformationMap.INSTANCES.dtoListToPoList(dto.list);
        poList.stream().map(e -> {
            e.setAccessId(dto.accessId);
            return e;
        }).collect(Collectors.toList());
        boolean result = transformationService.saveOrUpdateBatch(poList);
        if (!result) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //修改发布状态
        accessDataPO.publish = PublicStatusEnum.UN_PUBLIC.getValue();
        accessDataPO.setLoadingSql(dto.coverScript);
        accessDataPO.setVersionId(dto.versionId);
        accessDataPO.setDomainUpdateSql(dto.domainUpdateSql);
        UserInfo userInfo = userHelper.getLoginUserInfo();
        dto.setUserId(userInfo.id);
        if (mapper.updateById(accessDataPO) == 0) {
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        //是否发布
        if (dto.publish) {
            accessDataService.assembleModel(dto, accessDataPO);

        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public void updateAccessPublishState(AccessPublishStatusDTO dto) {
        //判断是否存在
        AccessDataPO accessDataPO = mapper.selectById(dto.getId());
        if (accessDataPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        accessDataPO.setPublish(dto.publish);
        accessDataPO.setPublishErrorMsg(dto.getPublishErrorMsg());
        int i = mapper.updateById(accessDataPO);
        if (i == 0 || dto.publish != PublicStatusEnum.PUBLIC_SUCCESS.getValue()) {
            log.info("access表更改状态失败!");
        }
        if (!StringUtils.isEmpty(dto.subRunId)) {
            tableHistoryMapper.updateSubRunId(dto.tableHistoryId, dto.subRunId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum assembleModel(AccessAttributeAddDTO dto, AccessDataPO accessPo) {
        //发布历史
        Long tableHistoryId = 0L;
        if (!CollectionUtils.isEmpty(dto.tableHistorys)) {
            log.info("开始记录发布日志");
            for (TableHistoryDTO tableHistory : dto.tableHistorys) {
                if (tableHistory.tableId.longValue() == accessPo.getId()) {
                    log.info("记录发布日志的表id:{}", accessPo.getId());
                    List<TableHistoryDTO> list = new ArrayList<>();
                    list.add(tableHistory);
                    tableHistoryId = iTableHistory.addTableHistory(list);
                }
            }
        }
        try {
            ModelPO modelPo = modelMapper.selectById((long) accessPo.getModelId());
            if (modelPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }

            EntityPO entityPo = entityMapper.selectById((long) accessPo.getEntityId());
            if (entityPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            AccessPublishDataDTO data = new AccessPublishDataDTO();

            data.setAccessId(dto.accessId);
            data.setModelId(modelPo.getId());
            data.setEntityId(entityPo.getId());
            data.setEntityName(entityPo.getName());
            data.setModelName(modelPo.getName());
            data.setOpenTransmission(dto.openTransmission);

            //表详情
            AccessMdmPublishTableDTO accessMdmPublishTableDTO = new AccessMdmPublishTableDTO();
            accessMdmPublishTableDTO.setTableHistoryId(tableHistoryId);
            accessMdmPublishTableDTO.setDomainUpdateSql(dto.domainUpdateSql);
            accessMdmPublishTableDTO.coverScript = dto.getCoverScript();
            accessMdmPublishTableDTO.setUserId(dto.userId);
            accessMdmPublishTableDTO.setVersionId(dto.versionId);
            accessPo.publish = PublicStatusEnum.PUBLIC_ING.getValue();
            if (!accessDataService.updateById(accessPo)) {
                throw new FkException(ResultEnum.PUBLISH_FAILURE);
            }
            //拼接数据
            DataTranDTO dtDto = new DataTranDTO();
            dtDto.tableName = entityPo.getTableName();
            dtDto.querySql = accessPo.getExtractionSql();
            ResultEntity<Map<String, String>> converMap = publishTaskClient.converSql(dtDto);
            Map<String, String> data1 = converMap.data;
            accessMdmPublishTableDTO.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
            accessMdmPublishTableDTO.sqlScript = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            accessMdmPublishTableDTO.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());
            accessMdmPublishTableDTO.setDataSourceDbId(accessPo.getSouceSystemId());
            accessMdmPublishTableDTO.setTableId(entityPo.getId());
            accessMdmPublishTableDTO.setTableName(entityPo.getTableName());
            accessMdmPublishTableDTO.setUserId(dto.userId);
            accessMdmPublishTableDTO.setVersionId(dto.getVersionId());
            // 设置目标库id
            accessMdmPublishTableDTO.setTargetDbId(targetDbId);
            //获取自定义脚本
            CustomScriptQueryDTO customScriptDto = new CustomScriptQueryDTO();
            customScriptDto.tableId = Integer.parseInt(String.valueOf(accessPo.id));
            customScriptDto.execType = 1;
            String beforeCustomScript = customScript.getBatchScript(customScriptDto);
            if (!StringUtils.isEmpty(beforeCustomScript)) {
                accessMdmPublishTableDTO.customScript = beforeCustomScript;
            }
            customScriptDto.execType = 2;

            //自定义脚本
            String batchScript = customScript.getBatchScript(customScriptDto);
            if (!StringUtils.isEmpty(batchScript)) {
                accessMdmPublishTableDTO.customScriptAfter = batchScript;
            }
            //获取表增量配置信息
            List<SyncModePO> syncModePoList = syncMode.list();
            //获取维度表同步方式
            Optional<SyncModePO> first = syncModePoList.stream().filter(e -> e.syncTableId == accessPo.id).findFirst();
            if (first.isPresent()) {
                accessMdmPublishTableDTO.synMode = first.get().syncMode;
                accessMdmPublishTableDTO.maxRowsPerFlowFile = first.get().maxRowsPerFlowFile;
                accessMdmPublishTableDTO.fetchSize = first.get().fetchSize;
            } else {
                accessMdmPublishTableDTO.synMode = dto.syncModeDTO.syncMode;
            }
            //获取表字段详情
            QueryWrapper<AccessTransformationPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(AccessTransformationPO::getAccessId, dto.accessId);
            List<AccessTransformationPO> list = transformationService.list(queryWrapper);
            List<AttributeInfoDTO> attributeInfoDTOS = attributeService.listPublishedAttribute((int) entityPo.getId());
            List<AccessMdmPublishFieldDTO> attributes = new ArrayList<>();
            if (!CollectionUtils.isEmpty(attributeInfoDTOS)) {
                Map<Integer, AttributeInfoDTO> attributeInfoDTOMap = attributeInfoDTOS.stream()
                        .collect(Collectors.toMap(AttributeInfoDTO::getId, i -> i));
                if (!CollectionUtils.isEmpty(list)) {
                    attributes = list.stream().map(i -> {
                        AttributeInfoDTO attributeInfoDTO = attributeInfoDTOMap.get(i.getAttributeId());
                        AccessMdmPublishFieldDTO accessMdmPublishFieldDTO = new AccessMdmPublishFieldDTO();
                        accessMdmPublishFieldDTO.setEntityId((int) entityPo.getId());
                        accessMdmPublishFieldDTO.setIsPrimaryKey(i.getBusinessKey());
                        accessMdmPublishFieldDTO.setFieldName(attributeInfoDTO.getName());
                        accessMdmPublishFieldDTO.setSourceFieldName(i.getSourceFieldName());
                        return accessMdmPublishFieldDTO;
                    }).collect(Collectors.toList());
                }
            }
            accessMdmPublishTableDTO.setFieldList(attributes);
            data.setAccess(accessMdmPublishTableDTO);
            //发送消息
            log.info(JSON.toJSONString(data));
            publishTaskClient.publishBuildMdmTableTask(data);
        } catch (Exception ex) {
            log.error("batchPublishDimensionFolder ex:", ex);
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }


    public void getDbType(Integer dbId) {
        ResultEntity<DataSourceDTO> fiDataDataSourceById = userClient.getFiDataDataSourceById(dbId);
        if (fiDataDataSourceById.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        dataSourceTypeEnum = fiDataDataSourceById.data.conType;
    }

    @Override
    public List<AccessAttributeDTO> getAccessAttributeField(int accessId, int entityId) {
        //获取表字段详情
        QueryWrapper<AccessTransformationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccessTransformationPO::getAccessId, accessId);
        List<AccessTransformationPO> list = transformationService.list(queryWrapper);
        List<AttributeInfoDTO> attributeInfoDTOS = attributeService.listPublishedAttribute(entityId);
        List<AccessAttributeDTO> attributes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(attributeInfoDTOS)) {
            Map<Integer, AttributeInfoDTO> attributeInfoDTOMap = attributeInfoDTOS.stream()
                    .collect(Collectors.toMap(AttributeInfoDTO::getId, i -> i));
            List<AccessAttributeDTO> accessAttributeDTOS = AccessTransformationMap.INSTANCES.poListToDtoList(list);
            if (!CollectionUtils.isEmpty(accessAttributeDTOS)) {
                attributes = accessAttributeDTOS.stream().map(i -> {
                    AttributeInfoDTO attributeInfoDTO = attributeInfoDTOMap.get(i.getAttributeId());
                    if (attributeInfoDTO != null) {
                        i.setFieldName(attributeInfoDTO.getName());
                        i.setMdmFieldName(attributeInfoDTO.getColumnName());
                        i.setDataType(attributeInfoDTO.getDataType());
                        i.setDataTypeEnDisplay(attributeInfoDTO.getDataTypeEnDisplay());
                        i.setDataTypeDecimalLength(attributeInfoDTO.getDataTypeDecimalLength());
                        i.setDataTypeLength(attributeInfoDTO.getDataTypeLength());
                    }
                    return i;
                }).collect(Collectors.toList());
            }
        }
        return attributes;
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_CONFIG)
    @Override
    public ResultEntity<AccessMdmConfigDTO> dataAccessConfig(long entityId, long modelId) {
        // 增量
        int syncModel = 4;
        AccessMdmConfigDTO dto = new AccessMdmConfigDTO();
        // app组配置
        GroupConfig groupConfig = new GroupConfig();
        //任务组配置
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        // 数据源jdbc配置
        DataSourceConfig sourceDsConfig = new DataSourceConfig();
        // 目标源jdbc连接
        DataSourceConfig targetDsConfig = new DataSourceConfig();
        // 增量配置库源jdbc连接
        DataSourceConfig cfgDsConfig = new DataSourceConfig();
        // 表及表sql
        ProcessorConfig processorConfig = new ProcessorConfig();
        // 1.app组配置
        // select * from tb_app_registration where id=id and del_flag=1;
        LambdaQueryWrapper<ModelPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelPO::getId, modelId).eq(ModelPO::getDelFlag, 1);
        ModelPO modelPO = modelMapper.selectOne(queryWrapper);
        if (modelPO == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        groupConfig.setAppName(modelPO.getName());
        groupConfig.setAppDetails(modelPO.getDesc());
        //3.数据源jdbc配置
        AccessDataPO accessDataPO = this.query().eq("entity_id", entityId).eq("model_id", modelId).eq("del_flag", 1).one();
        if (accessDataPO == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(accessDataPO.getSouceSystemId());
        if (fiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        sourceDsConfig.setJdbcStr(fiDataDataSource.data.getConStr());
        switch (fiDataDataSource.data.conType) {
            case SQLSERVER:
                sourceDsConfig.setType(DriverTypeEnum.SQLSERVER);
                break;
            case MYSQL:
                sourceDsConfig.setType(DriverTypeEnum.MYSQL);
                break;
            case POSTGRESQL:
                sourceDsConfig.setType(DriverTypeEnum.POSTGRESQL);
                break;
            case ORACLE:
                sourceDsConfig.setType(DriverTypeEnum.ORACLE);
                break;
//            case FTP:
//            case SFTP:
//                break;
            default:
                break;
        }

        sourceDsConfig.setUser(fiDataDataSource.data.getConAccount());
        sourceDsConfig.setPassword(fiDataDataSource.data.getConPassword());
        // 5.表及表sql
        LambdaQueryWrapper<EntityPO> entityQueryWrapper = new LambdaQueryWrapper<>();
        entityQueryWrapper.eq(EntityPO::getId, accessDataPO.getEntityId()).eq(EntityPO::getModelId, modelId).eq(EntityPO::getDelFlag, 1);
        EntityPO entityPO = entityMapper.selectOne(entityQueryWrapper);
        if (entityPO == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        // 新增同步方式
        if (accessDataPO.getAccessType() == null) {
            targetDsConfig.syncMode = 3;
        } else {
            targetDsConfig.syncMode = accessDataPO.getAccessType();
        }
        // 2.任务组配置
        taskGroupConfig.setAppName(entityPO.getTableName());
        taskGroupConfig.setAppDetails(entityPO.getDesc());
        // 增量配置库源jdbc连接
        cfgDsConfig.setType(DriverTypeEnum.MYSQL);
        cfgDsConfig.setJdbcStr(jdbcStr);
        cfgDsConfig.setUser(user);
        cfgDsConfig.setPassword(password);
        // nifi流程需要物理表字段
        List<AccessAttributeDTO> list = this.getAccessAttributeField((int) accessDataPO.getId(), accessDataPO.getEntityId());
        String businessKeyAppend = "";
        if (list != null && !list.isEmpty()) {
            targetDsConfig.tableFieldsList = list;
            // 封装业务主键
            businessKeyAppend = list.stream().filter(e -> e.getBusinessKey() == 1).map(e -> e.fieldName + ",").collect(Collectors.joining());
        }

        if (businessKeyAppend.length() > 0) {
            dto.businessKeyAppend = businessKeyAppend.substring(0, businessKeyAppend.length() - 1);
        }
        dto.groupConfig = groupConfig;
        dto.taskGroupConfig = taskGroupConfig;
        dto.sourceDsConfig = sourceDsConfig;
        dto.targetDsConfig = targetDsConfig;
        dto.processorConfig = processorConfig;
        dto.cfgDsConfig = cfgDsConfig;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    @Override
    public List<ChannelDataDTO> getTableId() {

        List<AccessDataPO> accessDataPOS = this.list(Wrappers.<AccessDataPO>lambdaQuery()
                .eq(AccessDataPO::getPublish, 1));
        List<Integer> modelIds = accessDataPOS.stream().map(AccessDataPO::getModelId).collect(Collectors.toList());
        List<ModelPO> modelPOS = modelMapper.selectList(Wrappers.<ModelPO>lambdaQuery()
                .select(ModelPO::getId, ModelPO::getName).in(ModelPO::getId, modelIds)
                .orderByDesc(ModelPO::getCreateTime));
        List<ChannelDataDTO> channelDataDTOS = ModelMap.INSTANCES.listPoToChannelDataDto(modelPOS);
        channelDataDTOS = channelDataDTOS.stream().map(i -> {
            i.setType("主数据表任务");
            return i;
        }).collect(Collectors.toList());

        channelDataDTOS = channelDataDTOS.stream().map(dto -> {
            List<AccessDataPO> list1 = this.list(Wrappers.<AccessDataPO>lambdaQuery()
                    .eq(AccessDataPO::getModelId, dto.getId())
                    .eq(AccessDataPO::getPublish, 1));
            if (CollectionUtils.isEmpty(list1)){
                return dto;
            }
            List<Integer> entityId = list1.stream().map(AccessDataPO::getEntityId).collect(Collectors.toList());
            List<EntityPO> entityPOS = entityMapper.selectList(Wrappers.<EntityPO>lambdaQuery().in(EntityPO::getId, entityId));
            Map<Integer, EntityPO> entityPOMap = entityPOS.stream().collect(Collectors.toMap(i->(int)i.getId(), i -> i));
            List<ChannelDataChildDTO> channelDataChildDTOS = new ArrayList<>();
            for (AccessDataPO accessDataPO : list1) {
                EntityPO entityPO = entityPOMap.get(accessDataPO.getEntityId());
                if(entityPO !=null){
                    ChannelDataChildDTO channelDataChildDTO = new ChannelDataChildDTO();
                    channelDataChildDTO.setId(accessDataPO.getEntityId());
                    channelDataChildDTO.setTableName(entityPO.getTableName());
                    channelDataChildDTOS.add(channelDataChildDTO);
                }
            }
            dto.setList(channelDataChildDTOS);
            return dto;
        }).collect(Collectors.toList());

        return channelDataDTOS;
    }

    @Override
    public List<TableHistoryDTO> getTableHistoryList(Integer tableId) {
        QueryWrapper<TableHistoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(TableHistoryPO::getTableId,tableId);
        return TableHistoryMap.INSTANCES.poListToDtoList(tableHistoryMapper.selectList(queryWrapper));
    }

    @Override
    public List<EntityTableDTO> getEntityTable(long modelId) {
        LambdaQueryWrapper<EntityPO> queryEntity = new LambdaQueryWrapper<>();
        queryEntity.eq(EntityPO::getModelId,modelId);
        List<EntityPO> entityPOS = entityMapper.selectList(queryEntity);
        //查询表名列表
        List<EntityTableDTO> entityTableListDTO = entityPOS.stream().map(i -> {
            EntityTableDTO entityTableDTO = new EntityTableDTO();
            entityTableDTO.setId((int) i.getId());
            entityTableDTO.setTableName(i.getTableName());
            return entityTableDTO;
        }).collect(Collectors.toList());
        List<Long> entityIds = entityPOS.stream().map(BasePO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<AttributePO> queryAttribute = new LambdaQueryWrapper<>();
        queryAttribute.in(AttributePO::getEntityId,entityIds);
        List<AttributePO> attributePOList = attributeService.list(queryAttribute);
        Map<Integer, List<AttributePO>> attributePOMap = attributePOList.stream().collect(groupingBy(AttributePO::getEntityId));
        //表名关联字段返回列表
        return entityTableListDTO.stream().map(i->{
            List<AttributePO> attributePO = attributePOMap.get(i.getId());
            List<AttributeInfoDTO> attributeInfoDTOS = AttributeMap.INSTANCES.poToVoList(attributePO);
            i.setFields(attributeInfoDTOS);
            return i;
        }).collect(Collectors.toList());
    }

    @Override
    public EntityTableDTO getEntityStgTable(long entityId, long modelId) {
        EntityPO entityPO = entityMapper.selectById(entityId);
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ModelPO modelPO = modelMapper.selectById(modelId);
        if (modelPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String stgTableName = generateStgTableName(modelPO.getName(), entityPO.getName());
        EntityTableDTO entityTableDTO = new EntityTableDTO();
        entityTableDTO.setId((int)entityId);
        entityTableDTO.setTableName(stgTableName);
        LambdaQueryWrapper<AttributePO> queryAttribute = new LambdaQueryWrapper<>();
        queryAttribute.eq(AttributePO::getEntityId,entityId);
        List<AttributePO> attributePOList = attributeService.list(queryAttribute);
        List<AttributeInfoDTO> attributeInfoDTOS = AttributeMap.INSTANCES.poToVoList(attributePOList);
        entityTableDTO.setFields(attributeInfoDTOS);
        return entityTableDTO;
    }

//    @Override
//    public Object buildDomainUpdateScript(List<TableSourceRelationsDTO> dto) {
//        if (org.springframework.util.CollectionUtils.isEmpty(dto)) {
//            return "";
//        }
//        //获取连接类型
//        DataSourceTypeEnum conType = getTargetDbInfo(targetDbId).conType;
//
//        //获取对应连接类型的IBuildMdmScript
//        IBuildMdmScript iBuildMdmScript = MdmScriptHelper.getDomainScriptHelperByConType(conType);
//
//        return iBuildMdmScript.buildMdmScript(dto);
//    }

    @Override
    public Object buildDomainUpdateScript(List<RelationDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        Map<Integer, List<RelationDTO>> map = list.stream().collect(groupingBy(RelationDTO::getTargetEntityId));
        StringBuilder updateScriptSql = new StringBuilder();
        for (Integer targetEntityId : map.keySet()) {
            // 查询待更新域字段
            String updateColumn;
            List<RelationDTO> relationDTOS = map.get(targetEntityId);
            if(CollectionUtils.isEmpty(relationDTOS)){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //通过源实体id获取源实体所有字段并取出待更新域字段
            Integer sourceEntityId = relationDTOS.get(0).getSourceEntityId();
            List<AttributePO> sourceAttribute = attributeService.getAttributeByEntityId(sourceEntityId);
            List<Integer> domainIdList = sourceAttribute.stream().filter(i -> i.getDomainId() != null).map(i -> i.getDomainId()).collect(Collectors.toList());
            final List<AttributePO> targetAttribute = attributeService.getAttributeByEntityId(targetEntityId);
            List<AttributePO> collect = targetAttribute.stream().filter(i -> domainIdList.contains((int)i.getId())).collect(Collectors.toList());
            Integer domainId = 0;
            if (CollectionUtils.isEmpty(collect)){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }else {
                domainId = (int)collect.get(0).getId();
                Integer finalDomainId = domainId;
                Optional<AttributePO> first = sourceAttribute.stream().filter(i -> {
                    if (i.getDomainId() != null && i.getDomainId().equals(finalDomainId)){
                            return true;
                    }
                    return false;
                }).findFirst();
                if (first.isPresent()){
                    AttributePO attributePO = first.get();
                    updateColumn = attributePO.getName();
                }else {
                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
                }
            }

            //开始拼接sql
            StringBuilder str = new StringBuilder();
            String stgTable = "\""+relationDTOS.get(0).sourceTable+"\"";
            String targetTable = "\""+relationDTOS.get(0).targetTable+"\"";
            str.append("UPDATE ")
                .append(stgTable)
                .append(" SET \"")
                .append(updateColumn)
                .append("\" = (select \"fidata_id\" FROM ")
                .append(targetTable)
                .append(" WHERE ")
                .append(targetTable)
                .append(".\"fidata_del_flag\"= '1' ");

            //处理关联关系
            StringBuilder and = new StringBuilder();
            for (RelationDTO item : map.get(targetEntityId)) {
                and.append("AND ")
                        .append(stgTable)
                        .append(".\"")
                        .append(item.getSourceColumn())
                        .append("\" = ")
                        .append(targetTable)
                        .append(".\"column_")
                        .append(item.getTargetColumn())
                        .append("\" ");
            }
            str.append(and)
                    .append(" AND ")
                    .append(stgTable)
                    .append(".\"fidata_version_id\"=")
                    .append(targetTable)
                    .append(".\"fidata_version_id\")")
                    .append(" WHERE ")
                    .append(stgTable)
                    .append(".\"fidata_batch_code\"=")
                    .append("'${fidata_batch_code}' AND ")
                    .append(stgTable)
                    .append(".\"fidata_flow_batch_code\"=")
                    .append("'${fragment.index}' AND ")
                    .append(stgTable)
                    .append(".\"fidata_version_id\"=")
                    .append("'${fidata_version_id}';");
            updateScriptSql.append(str);
        }
        return updateScriptSql;
    }

    /**
     * 获取数据源信息
     *
     * @param id
     * @return
     */
    private DataSourceDTO getTargetDbInfo(Integer id) {
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try {
            dataSourceConfig = userClient.getFiDataDataSourceById(id);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            if (Objects.isNull(dataSourceConfig.data)) {
                throw new FkException(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS);
            }
        } catch (Exception e) {
            log.error("调用userClient服务获取数据源失败,", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return dataSourceConfig.data;
    }

    @Override
    public Object mdmOverlayCodePreview(OverlayCodePreviewAccessDTO dto) {

        // 查询表数据
        //从tb_table_access表获取物理表信息
        AccessDataPO accessDataPO = accessDataService.getById(dto.id);
        //获取不到，抛出异常
        if (Objects.isNull(accessDataPO)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，表信息不存在");
        }
        EntityPO entityPO = entityMapper.selectById(accessDataPO.getEntityId());
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityPO.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        // 1.查询属性配置信息
        List<AccessAttributeDTO> attributeList = dto.AccessMdmPublishFieldDTOList;
        if (CollectionUtils.isEmpty(attributeList)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        attributeList = attributeList.stream().map(i -> {
            i.setMdmFieldName("column_" + i.getFieldName());
            return i;
        }).collect(Collectors.toList());
        // 获取表名称
        String stgTableName = generateStgTableName(modelPO.getName(), entityPO.getName());
        String mdmTableName = generateMdmTableName(modelPO.getName(), entityPO.getName());

        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(targetDbId);
        if (fiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        String finalSql = "";
        switch (fiDataDataSource.data.conType) {
            case SQLSERVER:
                break;
            case MYSQL:
                break;
            case POSTGRESQL:
                finalSql = this.buildMerge(attributeList, stgTableName, mdmTableName, dto.versionId);
                break;
            case ORACLE:
                break;
            default:
                break;
        }
        return finalSql;
    }

    String buildMerge(List<AccessAttributeDTO> attributeList,String souceTableName,String targetTableName,Integer versionId) {

        List<String> sourceNames = new ArrayList<>();
        //处理类型转换
        for (AccessAttributeDTO accessAttributeDTO : attributeList) {
            String sourceName;
            switch (accessAttributeDTO.getDataType()) {
                case "文件":
                case "经纬度坐标":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "数值":
                case "域字段":
                    sourceName =  "CAST(NULLIF(\""+accessAttributeDTO.getFieldName()+"\",\'\'\'\') AS int4) AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "时间":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "日期":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "日期时间":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'\'YYYY-MM-DD HH24:MI:SS\'\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "浮点型":
                    sourceName = "CAST(NULLIF(\""+accessAttributeDTO.getFieldName()+"\",\'\'\'\') AS DECIMAL("+accessAttributeDTO.getDataTypeLength()+","+accessAttributeDTO.getDataTypeDecimalLength()+")) AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "布尔型":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "货币":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "POI":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "文本":
                default:
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
            }
            sourceNames.add(sourceName);
        }


        //获取stg表列名
        List<String> sourceColumnNames = attributeList.stream()
                .map(i->"\""+i.getFieldName() +"\"")
                .collect(Collectors.toList());
        //获取mdm表列名
        List<String> targetColumnNames = attributeList.stream()
                .map(i->"\""+i.getMdmFieldName()+"\"")
                .collect(Collectors.toList());
        //获取主键
        List<AccessAttributeDTO> businessKeys = attributeList.stream().filter(i -> i.getBusinessKey() == 1).collect(Collectors.toList());
        //如果code是主键就正常同步如果code非主键则添加code并自动生成uuid
        List<AccessAttributeDTO> code = businessKeys.stream().filter(i -> i.getFieldName().equals("code")).collect(Collectors.toList());
        boolean flag = false;
        if (CollectionUtils.isEmpty(code)){
            flag = true;
        }
        StringBuilder str = new StringBuilder();
        //调用存储过程并拼接同步sql
        str.append("call \"public\".\"sync_mdm_table\"('DECLARE\nsource_row RECORD;\n");
        str.append("BEGIN\n");
        str.append("    FOR source_row IN SELECT\n");
        str.append("    fidata_version_id,\n");
        str.append("    fidata_create_time,\n");
        str.append("    fidata_create_user,\n");
        str.append("    fidata_del_flag,\n");
        str.append("    fidata_batch_code,\n");
        if(flag){
            str.append("    \"code\",\n");
        }
        str.append("    "+org.apache.commons.lang.StringUtils.join(sourceNames, ",\n    ")+"\n");

        str.append("  FROM\n");
        str.append("    \""+souceTableName+"\"\n");
        str.append("  WHERE\n");
        str.append("    fidata_batch_code = ''${fidata_batch_code}''\n");
        str.append("    AND fidata_flow_batch_code = ''${fragment.index}''\n");
        str.append("    AND fidata_version_id = ''"+versionId+"''\n");
        str.append("  LOOP\n");
        str.append("    BEGIN\n");
        str.append("        UPDATE \""+targetTableName+"\"\n");
        str.append("        SET\n");
        str.append("        fidata_version_id = source_row.fidata_version_id,\n");
        str.append("        fidata_create_time = source_row.fidata_create_time,\n");
        str.append("        fidata_create_user =  source_row.fidata_create_user,\n");
        str.append("        fidata_update_time = DATE_TRUNC(''second'', CURRENT_TIMESTAMP),\n");
        str.append("        fidata_update_user = source_row.fidata_create_user,\n");
        str.append("        fidata_del_flag = source_row.fidata_del_flag,\n");
        str.append("        fidata_batch_code = source_row.fidata_batch_code,\n");
        if(flag){
            str.append("        column_code = uuid_generate_v4(),\n");
        }
        str.append(attributeList.stream().map(i->
                "       \""+i.getMdmFieldName()+"\" = source_row.\""+i.getFieldName()+"\""
        ).collect(Collectors.joining(",\n")));
        str.append("\n      WHERE\n");
        str.append("        fidata_version_id = ''"+versionId+"''\n");
        str.append(businessKeys.stream().map(i->
                "        AND \""+i.getMdmFieldName()+"\" = source_row.\""+i.getFieldName()+"\"").collect(Collectors.joining(",\n")));
        str.append(";\n");
        str.append("      IF\n");
        str.append("        NOT FOUND THEN\n");

        str.append("          insert into \"" + targetTableName+"\" (");


        str.append("fidata_version_id, fidata_create_time, fidata_create_user, fidata_del_flag, fidata_batch_code,");
        if(flag){
            str.append("\"column_code\",");
        }
        str.append(org.apache.commons.lang.StringUtils.join(targetColumnNames, ","));
        str.append(" )\n");
        str.append("        VALUES\n");
        str.append("          (\n");
        String columnNames = sourceColumnNames.stream().map(i ->
                "           source_row." + i).collect(Collectors.joining(",\n"));
        str.append("            source_row.fidata_version_id,\n");
        str.append("            source_row.fidata_create_time,\n");
        str.append("            source_row.fidata_create_user,\n");
        str.append("            source_row.fidata_del_flag,\n");
        str.append("            source_row.fidata_batch_code,\n");
        if(flag){
            str.append("            uuid_generate_v4(),\n");
        }
        str.append(columnNames);
        str.append("\n          );\n");
        str.append("      END IF;\n");
        str.append("      EXCEPTION\n");
        str.append("      WHEN unique_violation THEN\n");
        String collect = businessKeys.stream().map(i -> {
            StringBuilder str1 = new StringBuilder();
            str1.append("%");
            return str1;
        }).collect(Collectors.joining(","));
        if(flag){
            collect+=",%";
        }
        str.append("      RAISE NOTICE''Duplicate key value ("+collect+") found. Skipping.'',");
        if(flag){
            str.append("source_row.\"code\",");
        }
        str.append(businessKeys.stream().map(i ->
                "source_row.\"" + i.getFieldName()+"\"").collect(Collectors.joining(",")));
        str.append(";\n    END;\n");
        str.append("  END LOOP;')\n");
        return str.toString();

    }


    /**
     * SQL拼接关联表
     *
     * @param relations
     * @return
     */
    public String appendRelateTable(List<TableSourceRelationsDTO> relations) {
        ResultEntity<List<DataSourceDTO>> all = userClient.getAll();
        if (all.getCode() != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(all.data)) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        List<DataSourceDTO> dataSourceDTOS = all.data.stream().filter(i -> i.getSourceType() == 1 && i.getSourceBusinessType() == SourceBusinessTypeEnum.MDM).collect(Collectors.toList());

        if (dataSourceDTOS.get(0).conType.getValue() == DataBaseTypeEnum.MYSQL.getValue()) {
            List<TableSourceRelationsDTO> fullJoin = relations.stream().filter(e -> RelateTableTypeEnum.FULL_JOIN.getName().equals(e.joinType)).collect(Collectors.toList());
            if (!org.springframework.util.CollectionUtils.isEmpty(fullJoin)) {
                throw new FkException(ResultEnum.NOT_SUPPORT_FULL_JOIN);
            }
        }
        IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dataSourceDTOS.get(0).conType);
        return command.buildAppendRelationTable(relations);
    }
}
