package com.fisk.mdm.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.enums.SyncModeEnum;
import com.fisk.mdm.dto.access.*;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.entity.AccessDataPO;
import com.fisk.mdm.entity.AccessTransformationPO;
import com.fisk.mdm.entity.SyncModePO;
import com.fisk.mdm.map.AccessTransformationMap;
import com.fisk.mdm.map.SyncModeMap;
import com.fisk.mdm.mapper.AccessDataMapper;
import com.fisk.mdm.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("accessDataService")
public class AccessDataServiceImpl extends ServiceImpl<AccessDataMapper, AccessDataPO> implements AccessDataService {

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
            mapper.insert(accessDataPo);
            return data;
        }
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
        //添加或修改维度字段
        List<AccessTransformationPO> poList = AccessTransformationMap.INSTANCES.dtoListToPoList(dto.list);
        poList.stream().map(e -> {
            e.setAccessId(dto.accessId);
            return e;
        }).collect(Collectors.toList());
        boolean result = transformationService.saveOrUpdateBatch(poList);
        if (!result) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return result ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
