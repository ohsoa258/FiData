package com.fisk.mdm.service.impl;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.mdm.dto.accessmodel.AccessPublishDataDTO;
import com.fisk.mdm.entity.*;
import com.fisk.mdm.enums.PublicStatusEnum;
import com.fisk.mdm.enums.SyncModeEnum;
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
import com.fisk.task.dto.accessmodel.AccessMdmPublishFieldDTO;
import com.fisk.task.dto.accessmodel.AccessMdmPublishTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        //获取表字段详情
        QueryWrapper<AccessTransformationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccessTransformationPO::getAccessId, accessPo.getId());
        List<AccessTransformationPO> list = transformationService.list(queryWrapper);
        List<AttributeInfoDTO> attributeInfoDTOS = attributeService.listPublishedAttribute(entityId);
        data.attributeInfoDTOS = attributeInfoDTOS;
        if (!CollectionUtils.isEmpty(attributeInfoDTOS)){
            Map<Integer, AttributeInfoDTO> attributeInfoDTOMap = attributeInfoDTOS.stream()
                    .collect(Collectors.toMap(AttributeInfoDTO::getId, i -> i));
            List<AccessAttributeDTO> accessAttributeDTOS = AccessTransformationMap.INSTANCES.poListToDtoList(list);
            List<AccessAttributeDTO> attributes = new ArrayList<>();
            if (!CollectionUtils.isEmpty(accessAttributeDTOS)){
                attributes = accessAttributeDTOS.stream().map(i -> {
                    AttributeInfoDTO attributeInfoDTO = attributeInfoDTOMap.get(i.getAttributeId());
                    if (attributeInfoDTO != null) {
                        i.setFieldName(attributeInfoDTO.getName());
                    }
                    return i;
                }).collect(Collectors.toList());
            }
            data.attributeDTOList = attributes;
        }

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
        queryDto.type = 1;
        queryDto.execType = 1;
        data.customScriptList = customScript.listCustomScript(queryDto);
        queryDto.execType = 2;
        data.customScriptList.addAll(customScript.listCustomScript(queryDto));

        // 系统变量
        data.deltaTimes = systemVariables.getSystemVariable(moudleId);

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
        if (dto.syncModeDTO.syncMode == SyncModeEnum.CUSTOM_OVERRIDE.getValue()) {
            QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId, dto.syncModeDTO.syncTableId);
            SyncModePO po = this.syncMode.getOne(syncModePoQueryWrapper);
            if (po == null) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        if (!syncMode || !tableBusiness) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //自定义脚本
        customScript.addOrUpdateCustomScript(dto.customScriptList);

        //删除维度字段属性
        List<Integer> ids = dto.list.stream().filter(e -> e.id != 0).map(i->(int)i.getId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ids)) {
            QueryWrapper<AccessTransformationPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.notIn("id", ids).lambda().eq(AccessTransformationPO::getAccessId, dto.accessId);
            List<AccessTransformationPO> list = transformationService.list(queryWrapper);
            if (!CollectionUtils.isEmpty(list)) {
                boolean flat = transformationService.remove(queryWrapper);
                if (!flat) {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
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
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum assembleModel(AccessAttributeAddDTO dto,AccessDataPO accessPo) {
        try {
            ModelPO modelPo = modelMapper.selectById((long)accessPo.getModelId());
            if (modelPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            AccessPublishDataDTO data = new AccessPublishDataDTO();
            data.setModelId(modelPo.getId());
            data.setModelName(modelPo.getName());
            data.setOpenTransmission(dto.openTransmission);

            //表详情
            AccessMdmPublishTableDTO accessMdmPublishTableDTO = new AccessMdmPublishTableDTO();
            accessMdmPublishTableDTO.coverScript =accessPo.getLoadingSql();
            accessPo.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
            if (accessDataService.updateById(accessPo)) {
                throw new FkException(ResultEnum.PUBLISH_FAILURE);
            }
            //拼接数据
//            accessMdmPublishTableDTO.tableId = Integer.parseInt(String.valueOf(item.id));
//            accessMdmPublishTableDTO.tableName = convertName(item.dimensionTabName);
            accessMdmPublishTableDTO.createType = 4;
            DataTranDTO dtDto = new DataTranDTO();
            dtDto.tableName = accessMdmPublishTableDTO.tableName;
            dtDto.querySql = accessPo.getExtractionSql();
            ResultEntity<Map<String, String>> converMap = publishTaskClient.converSql(dtDto);
            Map<String, String> data1 = converMap.data;
            accessMdmPublishTableDTO.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
            accessMdmPublishTableDTO.sqlScript = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            accessMdmPublishTableDTO.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());
            accessMdmPublishTableDTO.setDataSourceDbId(accessPo.getSouceSystemId());

            // 设置目标库id
            accessMdmPublishTableDTO.setTargetDbId(targetDbId);
            //获取自定义脚本
            CustomScriptQueryDTO customScriptDto = new CustomScriptQueryDTO();
            customScriptDto.type = 1;
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
            queryWrapper.lambda().eq(AccessTransformationPO::getAccessId, accessPo.getId());
            List<AccessTransformationPO> list = transformationService.list(queryWrapper);
            List<AttributeInfoDTO> attributeInfoDTOS = attributeService.listPublishedAttribute(accessPo.getEntityId());
            if (!CollectionUtils.isEmpty(attributeInfoDTOS)){
                Map<Integer, AttributeInfoDTO> attributeInfoDTOMap = attributeInfoDTOS.stream()
                        .collect(Collectors.toMap(AttributeInfoDTO::getId, i -> i));
                List<AccessMdmPublishFieldDTO> attributes = new ArrayList<>();
                if (!CollectionUtils.isEmpty(list)){
                    attributes = list.stream().map(i -> {
                        AttributeInfoDTO attributeInfoDTO = attributeInfoDTOMap.get(i.getAttributeId());
                        AccessMdmPublishFieldDTO accessMdmPublishFieldDTO = new AccessMdmPublishFieldDTO();
                        accessMdmPublishFieldDTO.setEntityId(accessPo.getEntityId());
                        accessMdmPublishFieldDTO.setIsPrimaryKey(i.getBusinessKey());
                        accessMdmPublishFieldDTO.setFieldName(attributeInfoDTO.getName());
                        accessMdmPublishFieldDTO.setSourceFieldName(i.getSourceFieldName());
                        return accessMdmPublishFieldDTO;
                    }).collect(Collectors.toList());
                }
                accessMdmPublishTableDTO.setFieldList(attributes);
                //发送消息
                log.info(JSON.toJSONString(data));
                publishTaskClient.publishBuildMdmTableTask(data);
            }
        }catch (Exception ex){
            log.error("batchPublishDimensionFolder ex:", ex);
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }


    public void getDwDbType(Integer dbId) {
        ResultEntity<DataSourceDTO> fiDataDataSourceById = userClient.getFiDataDataSourceById(dbId);
        if (fiDataDataSourceById.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        dataSourceTypeEnum = fiDataDataSourceById.data.conType;
    }
}
