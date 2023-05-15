package com.fisk.datamodel.service.impl.dimension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.entity.TableBusinessPO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.SyncModeMap;
import com.fisk.datamodel.map.TableBusinessMap;
import com.fisk.datamodel.map.dimension.DimensionAttributeMap;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import com.fisk.datamodel.service.impl.CustomScriptImpl;
import com.fisk.datamodel.service.impl.SyncModeImpl;
import com.fisk.datamodel.service.impl.SystemVariablesImpl;
import com.fisk.datamodel.service.impl.TableBusinessImpl;
import com.fisk.datamodel.service.impl.fact.FactAttributeImpl;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionFolderImpl dimensionFolder;
    @Resource
    FactAttributeImpl factAttribute;
    @Resource
    SyncModeImpl syncMode;
    @Resource
    TableBusinessImpl tableBusiness;
    @Resource
    CustomScriptImpl customScript;
    @Resource
    SystemVariablesImpl systemVariables;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addOrUpdateDimensionAttribute(DimensionAttributeAddDTO dto) {
        //判断是否存在
        DimensionPO dimensionPo = mapper.selectById(dto.dimensionId);
        if (dimensionPo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //系统变量
        if (!org.springframework.util.CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.dimensionId, dto.deltaTimes, CreateTypeEnum.CREATE_DIMENSION.getValue());
        }

        //添加增量配置
        SyncModePO syncModePo = SyncModeMap.INSTANCES.dtoToPo(dto.syncModeDTO);
        boolean syncMode = this.syncMode.saveOrUpdate(syncModePo);
        boolean tableBusiness = true;
        if (dto.syncModeDTO.syncMode == SyncModeEnum.CUSTOM_OVERRIDE.getValue()) {
            QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId, dto.syncModeDTO.syncTableId)
                    .eq(SyncModePO::getTableType, dto.syncModeDTO.tableType);
            SyncModePO po = this.syncMode.getOne(syncModePoQueryWrapper);
            if (po == null) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            dto.syncModeDTO.syncTableBusinessDTO.syncId = (int) po.id;
            tableBusiness = this.tableBusiness.saveOrUpdate(TableBusinessMap.INSTANCES.dtoToPo(dto.syncModeDTO.syncTableBusinessDTO));
        }
        if (!syncMode || !tableBusiness) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //自定义脚本
        customScript.addOrUpdateCustomScript(dto.customScriptList);

        //删除维度字段属性
        List<Integer> ids = (List) dto.list.stream().filter(e -> e.id != 0).map(DimensionAttributeDTO::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ids)) {
            QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.notIn("id", ids).lambda().eq(DimensionAttributePO::getDimensionId, dto.dimensionId);
            List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(list)) {
                boolean flat = this.remove(queryWrapper);
                if (!flat) {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
        }
        //添加或修改维度字段
        List<DimensionAttributePO> poList = DimensionAttributeMap.INSTANCES.dtoListToPoList(dto.list);
        poList.stream().map(e -> e.dimensionId = dto.dimensionId).collect(Collectors.toList());
        boolean result = this.saveOrUpdateBatch(poList);
        if (!result) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        //修改发布状态
//        dimensionPo.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
//        if (dimensionPo.isDimDateTbl != null && dimensionPo.isDimDateTbl) {
//            dimensionPo.isPublish = PublicStatusEnum.PUBLIC_SUCCESS.getValue();
//        }

        //2023-04-26李世纪新增，拼接删除temp表的脚本并存入数据库
        //获取临时表前缀
        String prefixTempName = dimensionPo.prefixTempName;
        //获取表名称
        String dimensionTabName = dimensionPo.dimensionTabName;
        //拼接删除临时表的脚本
        StringBuilder delSql = new StringBuilder("TRUNCATE TABLE ");
        delSql.append(prefixTempName)
                .append("_")
                .append(dimensionTabName)
                .append(";");
        dimensionPo.deleteTempScript = String.valueOf(delSql);

        dimensionPo.dimensionKeyScript = dto.dimensionKeyScript;
        dimensionPo.coverScript = dto.coverScript;
        if (mapper.updateById(dimensionPo) == 0) {
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        //是否发布
        if (dto.isPublish) {
//        //2023-04-27 这里不能这样写，日期维度表也要走普通表相同的流程
//        if (dto.isPublish && !dimensionPo.isDimDateTbl || dimensionPo.isDimDateTbl == null) {
            DimensionFolderPublishQueryDTO queryDTO = new DimensionFolderPublishQueryDTO();
            List<Integer> dimensionIds = new ArrayList<>();
            dimensionIds.add(dto.dimensionId);
            queryDTO.dimensionIds = dimensionIds;
            queryDTO.businessAreaId = dimensionPo.businessId;
            queryDTO.remark = dto.remark;
            queryDTO.syncMode = dto.syncModeDTO.syncMode;
            queryDTO.openTransmission = dto.openTransmission;
            return dimensionFolder.batchPublishDimensionFolder(queryDTO);
        }
        return result ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<ModelPublishFieldDTO>> selectDimensionAttributeList(Integer dimensionId) {
        Map<String, Object> conditionHashMap = new HashMap<>();
        List<ModelPublishFieldDTO> fieldList = new ArrayList<>();
        conditionHashMap.put("dimension_id", dimensionId);
        conditionHashMap.put("del_flag", 1);
        List<DimensionAttributePO> dimensionAttributePoList = attributeMapper.selectByMap(conditionHashMap);
        for (DimensionAttributePO attributePo : dimensionAttributePoList) {
            ModelPublishFieldDTO fieldDTO = new ModelPublishFieldDTO();
            fieldDTO.fieldId = attributePo.id;
            fieldDTO.fieldEnName = attributePo.dimensionFieldEnName;
            fieldDTO.fieldType = attributePo.dimensionFieldType;
            fieldDTO.fieldLength = attributePo.dimensionFieldLength;
            fieldDTO.attributeType = attributePo.attributeType;
            fieldDTO.isPrimaryKey = attributePo.isPrimaryKey;

            if (attributePo.attributeType == 1) {
                fieldDTO.sourceFieldName = attributePo.dimensionFieldEnName;
            } else {
                fieldDTO.sourceFieldName = attributePo.sourceFieldName;
            }
            fieldDTO.associateDimensionId = attributePo.associateDimensionId;
            fieldDTO.associateDimensionFieldId = attributePo.associateDimensionFieldId;
            //判断是否关联维度
            if (attributePo.associateDimensionId != 0 && attributePo.associateDimensionFieldId != 0) {
                DimensionPO dimensionPo = mapper.selectById(attributePo.associateDimensionId);
                fieldDTO.associateDimensionName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
                fieldDTO.associateDimensionSqlScript = dimensionPo == null ? "" : dimensionPo.sqlScript;
                DimensionAttributePO dimensionAttributePo = attributeMapper.selectById(attributePo.associateDimensionFieldId);
                fieldDTO.associateDimensionFieldName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
            }
            fieldList.add(fieldDTO);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, fieldList);
    }

    @Override
    public ResultEnum deleteDimensionAttribute(List<Integer> ids) {
        //判断字段是否与其他表有关联
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("associate_dimension_field_id", ids);
        List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        //判断字段是否与事实表有关联
        QueryWrapper<FactAttributePO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.in("associate_dimension_field_id", ids);
        List<FactAttributePO> poList = factAttributeMapper.selectList(queryWrapper1);
        if (poList.size() > 0) {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        ////DimensionAttributePO po=attributeMapper.selectById(ids.get(0));
        return attributeMapper.deleteBatchIds(ids) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionAttributeUpdateDTO getDimensionAttribute(int id) {
        return DimensionAttributeMap.INSTANCES.poToDetailDto(attributeMapper.selectById(id));
    }

    @Override
    public DimensionAttributeListDTO getDimensionAttributeList(int dimensionId) {
        DimensionAttributeListDTO data = new DimensionAttributeListDTO();
        DimensionPO dimensionPo = mapper.selectById(dimensionId);
        if (dimensionPo == null) {
            return data;
        }
        //获取sql脚本
        data.sqlScript = dimensionPo.sqlScript;
        data.dataSourceId = dimensionPo.dataSourceId;
        data.dimensionKeyScript = dimensionPo.dimensionKeyScript;
        //获取表字段详情
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId, dimensionId);
        List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
        data.attributeDTOList = DimensionAttributeMap.INSTANCES.poListToDtoList(list);
        //获取增量配置信息
        QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
        syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId, dimensionPo.id)
                .eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_DIMENSION);
        SyncModePO syncModePo = syncMode.getOne(syncModePoQueryWrapper);
        if (syncModePo == null) {
            return data;
        }
        data.syncModeDTO = SyncModeMap.INSTANCES.poToDto(syncModePo);
        if (syncModePo.syncMode == SyncModeEnum.CUSTOM_OVERRIDE.getValue()) {
            QueryWrapper<TableBusinessPO> tableBusinessPoQueryWrapper = new QueryWrapper<>();
            tableBusinessPoQueryWrapper.lambda().eq(TableBusinessPO::getSyncId, syncModePo.id);
            TableBusinessPO tableBusinessPo = tableBusiness.getOne(tableBusinessPoQueryWrapper);
            if (tableBusinessPo == null) {
                return data;
            }
            data.syncModeDTO.syncTableBusinessDTO = TableBusinessMap.INSTANCES.poToDto(tableBusinessPo);
        }
        //自定义脚本
        CustomScriptQueryDTO queryDto = new CustomScriptQueryDTO();
        queryDto.tableId = dimensionId;
        queryDto.type = 1;
        queryDto.execType = 1;
        data.customScriptList = customScript.listCustomScript(queryDto);
        queryDto.execType = 2;
        data.customScriptList.addAll(customScript.listCustomScript(queryDto));

        // 系统变量
        data.deltaTimes = systemVariables.getSystemVariable(dimensionId, CreateTypeEnum.CREATE_DIMENSION.getValue());

        data.execSql = dimensionPo.coverScript;

        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateDimensionAttribute(DimensionAttributeUpdateDTO dto) {
        DimensionAttributePO po = attributeMapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId, po.dimensionId)
                .eq(DimensionAttributePO::getDimensionFieldEnName, dto.dimensionFieldEnName);
        DimensionAttributePO model = attributeMapper.selectOne(queryWrapper);
        if (model != null && model.id != dto.id) {
            return ResultEnum.DATA_EXISTS;
        }
        //更改维度表发布状态
        ////DimensionPO dimensionPO=mapper.selectById(po.dimensionId);
        ////dimensionPO.isPublish=PublicStatusEnum.UN_PUBLIC.getValue();
        //更改维度字段数据
        po.dimensionFieldCnName = dto.dimensionFieldCnName;
        po.dimensionFieldDes = dto.dimensionFieldDes;
        po.dimensionFieldLength = dto.dimensionFieldLength;
        po.dimensionFieldEnName = dto.dimensionFieldEnName;
        po.dimensionFieldType = dto.dimensionFieldType;
        ////po=DimensionAttributeMap.INSTANCES.updateDtoToPo(dto);

        //系统变量
        if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.id, dto.deltaTimes, CreateTypeEnum.CREATE_DIMENSION.getValue());
        }
        return attributeMapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id) {
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionAttributePO::getDimensionId, id);
        List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
        return DimensionAttributeMap.INSTANCES.poToNameListDTO(list);
    }

    @Override
    public List<ModelMetaDataDTO> getDimensionMetaDataList(List<Integer> factIds) {
        List<ModelMetaDataDTO> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(factIds)) {
            return list;
        }
        //根据事实表id查询所有字段
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("associate_dimension_id").in("fact_id", factIds)
                .lambda().ne(FactAttributePO::getAssociateDimensionId, 0);
        List<Integer> dimensionIds = (List) factAttributeMapper.selectObjs(queryWrapper);
        dimensionIds = dimensionIds.stream().distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionIds)) {
            for (Integer id : dimensionIds) {
                ModelMetaDataDTO dto = getDimensionMetaData(id);
                list.add(dto);
            }
        }
        return list;
    }

    @Override
    public ModelMetaDataDTO getDimensionMetaData(int id) {
        ModelMetaDataDTO data = new ModelMetaDataDTO();
        DimensionPO po = mapper.selectById(id);
        if (po == null) {
            return data;
        }
        data.tableName = po.dimensionTabName;
        data.id = po.id;
        data.appId = po.businessId;
        //获取注册表相关数据
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId, id);
        List<ModelAttributeMetaDataDTO> dtoList = new ArrayList<>();
        List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return data;
        }
        for (DimensionAttributePO item : list) {
            ModelAttributeMetaDataDTO dto = new ModelAttributeMetaDataDTO();
            dto.fieldEnName = item.dimensionFieldEnName;
            dto.fieldLength = item.dimensionFieldLength;
            dto.fieldType = item.dimensionFieldType;
            dto.fieldId = String.valueOf(item.id);
            dto.attributeType = 1;
            dtoList.add(dto);
            //判断维度是否关联维度
            if (item.associateDimensionId != 0 && item.associateDimensionFieldId != 0) {
                DimensionPO po1 = mapper.selectById(item.associateDimensionId);
                if (po1 != null) {
                    ModelAttributeMetaDataDTO dto1 = new ModelAttributeMetaDataDTO();
                    dto1.attributeType = 2;
                    dto1.associationTable = po1.dimensionTabName;
                    dtoList.add(dto1);
                }
            }
        }
        data.dto = dtoList;
        return data;
    }

    /**
     * 生成时间日期维度表数据
     *
     * @param list
     * @param dimensionId
     * @return
     */
    public ResultEnum addTimeTableAttribute(List<DimensionAttributeDTO> list, int dimensionId) {
        List<DimensionAttributePO> poList = DimensionAttributeMap.INSTANCES.dtoListToPoList(list);
        poList.stream().map(e -> e.dimensionId = dimensionId).collect(Collectors.toList());
        return this.saveOrUpdateBatch(poList) == true ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionAttributeUpdateDTO> getDimensionAttributeDataList(int dimensionId) {
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId, dimensionId);
        List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
        return DimensionAttributeMap.INSTANCES.poToDetailDtoList(list);
    }


    /**
     * 维度键update语句
     *
     * @param dimensionId
     * @return
     */
    public String buildDimensionUpdateSql(int dimensionId) {
        Map<String, String> configDetailsMap = this.query()
                .eq("dimension_id", dimensionId)
                .eq("attribute_type", 1)
                .select("config_details", "dimension_field_en_name")
                .list()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.isNotBlank(e.configDetails))
                .collect(Collectors.toMap(DimensionAttributePO::getDimensionFieldEnName, DimensionAttributePO::getConfigDetails));

        if (org.springframework.util.CollectionUtils.isEmpty(configDetailsMap)) {
            return null;
        }

        return factAttribute.buildUpdateSql(configDetailsMap);
    }

    @Override
    public ResultEnum addDimensionAttribute(DimensionAttributeDTO dto) {
        List<DimensionAttributePO> list = this.query().eq("dimension_id", dto.associateDimensionId)
                .eq("dimension_field_en_name", dto.dimensionFieldEnName).list();
        if (!CollectionUtils.isEmpty(list)) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }

        DimensionAttributePO dimensionAttributePO = DimensionAttributeMap.INSTANCES.dtoToPo(dto);
        dimensionAttributePO.dimensionId = dto.associateDimensionId;
        dimensionAttributePO.associateDimensionId = 0;

        boolean flat = this.save(dimensionAttributePO);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

}
