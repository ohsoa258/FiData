package com.fisk.mdm.service.impl;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.dataaccess.dto.access.OverlayCodePreviewAccessDTO;
import com.fisk.dataaccess.dto.factorycodepreviewdto.AccessOverlayCodePreviewDTO;
import com.fisk.dataaccess.dto.factorycodepreviewdto.AccessPublishFieldDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.mdm.dto.accessmodel.AccessPublishDataDTO;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.mdm.entity.*;
import com.fisk.mdm.enums.PublicStatusEnum;
import com.fisk.mdm.dto.access.*;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.map.AccessTransformationMap;
import com.fisk.mdm.map.SyncModeMap;
import com.fisk.mdm.mapper.AccessDataMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.mapper.ModelMapper;
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

@Service("accessDataService")
@Slf4j
public class AccessDataServiceImpl extends ServiceImpl<AccessDataMapper, AccessDataPO> implements AccessDataService {

    @Value("${fiData-data-ods-source}")
    private Integer targetDbId;
    @Resource
    AccessDataMapper mapper;

    @Resource
    AccessTransformationService transformationService;

    @Resource
    ISystemVariables systemVariables;
    
    @Resource
    AttributeService attributeService;

    @Resource
    ISyncMode syncMode;

    @Resource
    CustomScriptImpl customScript;

    @Resource
    private AccessDataServiceImpl accessDataService;

    @Resource
    ModelMapper modelMapper;
    @Resource
    EntityMapper entityMapper;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    UserClient userClient;

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
        lambdaQueryWrapper.eq(AccessDataPO::getModelId,moudleId)
                .eq(AccessDataPO::getEntityId,entityId);
        AccessDataPO accessPo = mapper.selectOne(lambdaQueryWrapper);
        if (accessPo == null) {
            AccessDataPO accessDataPo = new AccessDataPO();
            accessDataPo.setModelId(moudleId);
            accessDataPo.setEntityId(entityId);
            this.save(accessDataPo);
            data.accessId = (int)accessDataPo.getId();
            return data;
        }
        data.accessId = (int)accessPo.getId();
        //获取sql脚本
        data.sqlScript = accessPo.getExtractionSql();
        data.dataSourceId = accessPo.getSouceSystemId();
        data.attributeDTOList = getAccessAttributeField((int)accessPo.id,accessPo.getEntityId());

        //获取增量配置信息
        QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
        syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId, accessPo.id);
        SyncModePO syncModePo = syncMode.getOne(syncModePoQueryWrapper);
        if (syncModePo == null) {
            return data;
        }
        data.syncModeDTO = SyncModeMap.INSTANCES.poToDto(syncModePo);

        //自定义脚本
        CustomScriptQueryDTO queryDto = new CustomScriptQueryDTO();
        queryDto.tableId = (int)accessPo.getId();
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
    public ResultEnum updateAccessSql(AccessSqlDTO dto) {
        AccessDataPO model = mapper.selectById(dto.getId());
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.setExtractionSql(dto.getSqlScript());
        model.setSouceSystemId( dto.getDataSourceId());
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
        if (!org.springframework.util.CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.accessId, dto.deltaTimes);
        }

        //添加增量配置
        SyncModePO syncModePo = SyncModeMap.INSTANCES.dtoToPo(dto.syncModeDTO);
        boolean syncMode = this.syncMode.saveOrUpdate(syncModePo);
        boolean tableBusiness = true;
        if (!syncMode || !tableBusiness) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //自定义脚本
        customScript.addOrUpdateCustomScript(dto.customScriptList);

        //删除字段属性
        QueryWrapper<AccessTransformationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_id", dto.accessId);
        boolean remove = transformationService.remove(queryWrapper);
        if (!remove) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
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
        accessDataPO.isPublish = PublicStatusEnum.UN_PUBLIC.getValue();
        if (mapper.updateById(accessDataPO) == 0) {
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        //是否发布
        if (dto.isPublish) {
            accessDataService.assembleModel(dto,accessDataPO);

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
        accessDataPO.setIsPublish(dto.status);
        int i = mapper.updateById(accessDataPO);
        if(i==0 || dto.status != PublicStatusEnum.PUBLIC_SUCCESS.getValue()) {
            log.info("access表更改状态失败!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum assembleModel(AccessAttributeAddDTO dto,AccessDataPO accessPo) {
        try {
            ModelPO modelPo = modelMapper.selectById((long)accessPo.getModelId());
            if (modelPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }

            EntityPO entityPo = entityMapper.selectById((long)accessPo.getEntityId());
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
            accessMdmPublishTableDTO.coverScript =accessPo.getLoadingSql();
            accessPo.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
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
            List<SyncModePO> syncModePoList=syncMode.list();
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
            List<AttributeInfoDTO> attributeInfoDTOS = attributeService.listPublishedAttribute((int)entityPo.getId());
            List<AccessMdmPublishFieldDTO> attributes = new ArrayList<>();
            if (!CollectionUtils.isEmpty(attributeInfoDTOS)) {
                Map<Integer, AttributeInfoDTO> attributeInfoDTOMap = attributeInfoDTOS.stream()
                        .collect(Collectors.toMap(AttributeInfoDTO::getId, i -> i));
                if (!CollectionUtils.isEmpty(list)) {
                    attributes = list.stream().map(i -> {
                        AttributeInfoDTO attributeInfoDTO = attributeInfoDTOMap.get(i.getAttributeId());
                        AccessMdmPublishFieldDTO accessMdmPublishFieldDTO = new AccessMdmPublishFieldDTO();
                        accessMdmPublishFieldDTO.setEntityId((int)entityPo.getId());
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
        }catch (Exception ex){
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
        AccessDataPO accessDataPO = this.query().eq("entity_id", entityId).eq("model_id",modelId).eq("del_flag", 1).one();
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
        if (accessDataPO.getAccessType() == null){
            targetDsConfig.syncMode = 3;
        }else {
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
        List<AccessAttributeDTO> list = this.getAccessAttributeField((int)accessDataPO.getId(), accessDataPO.getEntityId());
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

//    @Override
//    public Object mdmOverlayCodePreview(OverlayCodePreviewAccessDTO dto) {
//
//        // 查询表数据
//        //从tb_table_access表获取物理表信息
//        TableAccessPO tableAccessPO = tableAccessMapper.selectById(dto.id);
//        //获取不到，抛出异常
//        if (Objects.isNull(tableAccessPO)) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，表信息不存在");
//        }
//
//        // 查询app应用信息
//        AppRegistrationPO appRegistrationPO = appRegistrationMapper.selectById(tableAccessPO.appId);
//        //获取不到应用信息，则抛出异常
//        if (Objects.isNull(appRegistrationPO)) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，应用不存在");
//        }
//        //获取应用下的数据源信息
//        DataSourceDTO dataSourceDTO = getTargetDbInfo(appRegistrationPO.getTargetDbId());
//
//        // 处理不同架构下的表名称
//        String targetTableName = "";
//
//        /*appRegistrationPO.whetherSchema
//         * 是否将应用简称作为schema使用
//         * 否：0  false
//         * 是：1  true
//         */
//        if (appRegistrationPO.whetherSchema) {
//            targetTableName = tableAccessPO.tableName;
//        } else {
//            targetTableName = "ods_" + appRegistrationPO.getAppAbbreviation() + "_" + tableAccessPO;
//        }
//
//        List<String> stgAndTableName = getStgAndTableName(targetTableName, appRegistrationPO);
//
//        //临时表
//        String stgTableName = "";
//        //目标表
//        String odsTableName = "";
//        for (int i = 0; i < 2; i++) {
//            if (i == 0) {
//                stgTableName = stgAndTableName.get(i);
//            } else {
//                odsTableName = stgAndTableName.get(i);
//            }
//        }
//
//        //List<ModelPublishFieldDTO> ==> List<AccessPublishFieldDTO>
//        List<TableFieldsDTO> dtoList = dto.modelPublishFieldDTOList;
//
//        //新建集合预装载转换后的字段数据
//        List<AccessPublishFieldDTO> accessList = new ArrayList<>();
//        //遍历==>手动转换，属性不多，并未使用mapStruct
//        for (TableFieldsDTO m : dtoList) {
//            AccessPublishFieldDTO a = new AccessPublishFieldDTO();
//            a.sourceFieldName = m.sourceFieldName;
//            a.fieldLength = Math.toIntExact(m.fieldLength);
//            a.fieldType = m.fieldType;
//            a.isBusinessKey = m.isPrimarykey;
//            accessList.add(a);
//        }
//
//        //新建AccessOverlayCodePreviewDTO对象，参数赋值
//        AccessOverlayCodePreviewDTO previewDTO = new AccessOverlayCodePreviewDTO();
//        //业务时间覆盖所需的业务逻辑
//        previewDTO.tableBusiness = dto.tableBusiness;
//        //物理表id
//        previewDTO.id = dto.id;
//        //同步方式
//        previewDTO.syncMode = dto.syncMode;
//        //字段集合
//        previewDTO.modelPublishFieldDTOList = accessList;
//
//        //调用方法，获取sql语句
//        String finalSql = codePreviewBySyncMode(stgTableName, odsTableName, previewDTO);
//
//        //返回最终拼接好的sql
//        return finalSql;
//    }



}
