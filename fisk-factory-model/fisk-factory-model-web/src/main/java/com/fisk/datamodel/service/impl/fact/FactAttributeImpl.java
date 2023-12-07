package com.fisk.datamodel.service.impl.fact;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.datamodel.BuildDataModelHelper;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessPublishQueryDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSelectDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.fact.FactAttributeDetailDTO;
import com.fisk.datamodel.dto.factattribute.*;
import com.fisk.datamodel.dto.widetableconfig.WideTableAliasDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableQueryPageDTO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.entity.TableBusinessPO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.BusinessProcessPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.SyncModeMap;
import com.fisk.datamodel.map.TableBusinessMap;
import com.fisk.datamodel.map.dimension.DimensionAttributeMap;
import com.fisk.datamodel.map.dimension.DimensionMap;
import com.fisk.datamodel.map.fact.FactAttributeMap;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.BusinessProcessMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.mapper.fact.FactMapper;
import com.fisk.datamodel.service.IFactAttribute;
import com.fisk.datamodel.service.impl.CustomScriptImpl;
import com.fisk.datamodel.service.impl.SyncModeImpl;
import com.fisk.datamodel.service.impl.SystemVariablesImpl;
import com.fisk.datamodel.service.impl.TableBusinessImpl;
import com.fisk.datamodel.service.impl.widetable.WideTableImpl;
import com.fisk.datamodel.utils.mysql.DataSourceConfigUtil;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class FactAttributeImpl
        extends ServiceImpl<FactAttributeMapper, FactAttributePO>
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
    @Resource
    WideTableImpl wideTableImpl;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataSourceConfigUtil dataSourceConfigUtil;
    @Resource
    CustomScriptImpl customScript;
    @Resource
    SystemVariablesImpl systemVariables;

    @Override
    public List<FactAttributeListDTO> getFactAttributeList(int factId) {
        return mapper.getFactAttributeList(factId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addFactAttribute(FactAttributeAddDTO dto) {
        //判断是否存在
        FactPO factPo = factMapper.selectById(dto.factId);
        if (factPo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //系统变量
        if (!org.springframework.util.CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.factId, dto.deltaTimes, CreateTypeEnum.CREATE_FACT.getValue());
        }

        //添加增量配置
        SyncModePO syncModePo = SyncModeMap.INSTANCES.dtoToPo(dto.syncModeDTO);
        //dmp_datamodel_db 库： tb_sync_mode表
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
        List<Integer> ids = (List) dto.list.stream().filter(e -> e.id != 0)
                .map(FactAttributeDTO::getId)
                .collect(Collectors.toList());
        if (ids != null && ids.size() > 0) {
            QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.notIn("id", ids).lambda().eq(FactAttributePO::getFactId, dto.factId);
            List<FactAttributePO> list = mapper.selectList(queryWrapper);
            if (list != null && list.size() > 0) {
                if (!this.remove(queryWrapper)) {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
        }
        // TODO 添加事实字段(新增了config_details字段,用于存维度key的json连线配置信息)
        List<FactAttributePO> poList = FactAttributeMap.INSTANCES.addDtoToPoList(dto.list);
        //将 poList 中的每个元素的 factId 属性都设置为 dto.factId
        poList.stream().map(e -> e.factId = dto.factId).collect(Collectors.toList());
        boolean b = saveOrUpdateBatch(poList);
        if (!b) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //修改发布状态
        factPo.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
        factPo.dimensionKeyScript = dto.dimensionKeyScript;
        factPo.coverScript = dto.coverScript;
        factPo.dorisIfOpenStrictMode = dto.dorisIfOpenStrictMode;

        //2023-04-26李世纪新增，拼接删除temp表的脚本并存入数据库
        //获取临时表前缀
        String prefixTempName = factPo.prefixTempName;
        //获取表名称
        String dimensionTabName = factPo.factTabName;
        //拼接删除临时表的脚本
        factPo.deleteTempScript = "TRUNCATE TABLE " + prefixTempName +
                "_" +
                dimensionTabName +
                ";";

        if (factMapper.updateById(factPo) == 0) {
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        //是否发布
        if (dto.isPublish) {
            BusinessProcessPublishQueryDTO queryDTO = new BusinessProcessPublishQueryDTO();
            List<Integer> dimensionIds = new ArrayList<>();
            dimensionIds.add(dto.factId);

            queryDTO.factIds = dimensionIds;
            queryDTO.businessAreaId = factPo.businessId;
            queryDTO.remark = dto.remark;
            queryDTO.syncMode = dto.syncModeDTO.syncMode;
            queryDTO.openTransmission = dto.openTransmission;
            return businessProcess.batchPublishBusinessProcess(queryDTO);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteFactAttribute(List<Integer> ids) {
        return mapper.deleteBatchIds(ids) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactAttributeUpdateDTO getFactAttributeDetail(int factAttributeId) {
        return FactAttributeMap.INSTANCES.poDetailToDto(mapper.selectById(factAttributeId));
    }

    @Override
    public ResultEnum updateFactAttribute(FactAttributeUpdateDTO dto) {
        FactAttributePO po = mapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId, po.factId)
                .eq(FactAttributePO::getFactFieldEnName, dto.factFieldEnName);
        FactAttributePO model = mapper.selectOne(queryWrapper);
        if (model != null && model.id != dto.id) {
            return ResultEnum.DATA_EXISTS;
        }
        po.factFieldCnName = dto.factFieldCnName;
        po.factFieldDes = dto.factFieldDes;
        po.factFieldLength = dto.factFieldLength;
        po.factFieldEnName = dto.factFieldEnName;
        po.factFieldType = dto.factFieldType;

        //系统变量
        if (!org.springframework.util.CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.id, dto.deltaTimes, CreateTypeEnum.CREATE_FACT.getValue());
        }
        return mapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ModelMetaDataDTO getFactMetaData(int id) {
        ModelMetaDataDTO data = new ModelMetaDataDTO();
        FactPO po = factMapper.selectById(id);
        if (po == null) {
            return data;
        }
        data.tableName = po.factTabName;
        data.id = po.id;

        //查找业务域id
        BusinessProcessPO businessProcessPo = businessProcessMapper.selectById(po.businessProcessId);
        if (businessProcessPo == null) {
            return data;
        }
        data.appId = businessProcessPo.businessId;
        return data;
    }

    @Override
    public List<FactAttributeDropDTO> getFactAttributeData(FactAttributeDropQueryDTO dto) {
        List<FactAttributeDropDTO> data = new ArrayList<>();
        FactPO po = factMapper.selectById(dto.id);
        if (po == null) {
            return data;
        }
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("attribute_type", dto.type).lambda()
                .eq(FactAttributePO::getFactId, dto.id);
        List<FactAttributePO> list = mapper.selectList(queryWrapper);
        for (FactAttributePO item : list) {
            FactAttributeDropDTO dropDTO = new FactAttributeDropDTO();
            dropDTO.id = item.id;
            dropDTO.factFieldEnName = item.factFieldEnName;
            dropDTO.factFieldType = item.factFieldType;
            dropDTO.attributeType = item.attributeType;
            data.add(dropDTO);
        }
        return data;
    }

    @Override
    public List<FieldNameDTO> getFactAttributeSourceId(int id) {
        FactPO factPo = factMapper.selectById(id);
        if (factPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "事实表不存在");
        }
        return null;
    }

    @Override
    public FactAttributeDetailDTO getFactAttributeDataList(int factId) {
        FactAttributeDetailDTO data = new FactAttributeDetailDTO();
        FactPO po = factMapper.selectById(factId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        data.sqlScript = po.sqlScript;
        data.dataSourceId = po.dataSourceId;
        data.dimensionKeyScript = po.dimensionKeyScript;
        //doris是否开启严格模式
        data.dorisIfOpenStrictMode = po.dorisIfOpenStrictMode;
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId, factId);
        List<FactAttributePO> list = mapper.selectList(queryWrapper);
        data.attributeDTO = FactAttributeMap.INSTANCES.poListsToDtoList(list);
        //获取增量配置信息
        QueryWrapper<SyncModePO> syncModePoQueryWrapper = new QueryWrapper<>();
        syncModePoQueryWrapper.lambda().eq(SyncModePO::getSyncTableId, po.id)
                .eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_FACT);
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
        queryDto.tableId = factId;
        queryDto.type = 2;
        queryDto.execType = 1;
        data.customScriptList = customScript.listCustomScript(queryDto);

        queryDto.execType = 2;
        data.customScriptList.addAll(customScript.listCustomScript(queryDto));
        data.execSql = po.coverScript;

        data.deltaTimes = systemVariables.getSystemVariable(factId, CreateTypeEnum.CREATE_FACT.getValue());
        return data;
    }

    @Override
    public ResultEntity<List<ModelPublishFieldDTO>> selectAttributeList(Integer factId) {
        Map<String, Object> conditionHashMap = new HashMap<>();
        List<ModelPublishFieldDTO> fieldList = new ArrayList<>();
        conditionHashMap.put("fact_id", factId);
        conditionHashMap.put("del_flag", 1);
        List<FactAttributePO> factAttributePoList = mapper.selectByMap(conditionHashMap);
        for (FactAttributePO attributePo : factAttributePoList) {
            ModelPublishFieldDTO fieldDTO = new ModelPublishFieldDTO();
            fieldDTO.fieldId = attributePo.id;
            fieldDTO.fieldEnName = attributePo.factFieldEnName;
            fieldDTO.fieldType = attributePo.factFieldType;
            fieldDTO.fieldLength = attributePo.factFieldLength;
            fieldDTO.attributeType = attributePo.attributeType;

            fieldDTO.sourceFieldName = attributePo.sourceFieldName;
            if (attributePo.attributeType == 1) {
                fieldDTO.sourceFieldName = attributePo.factFieldEnName;
            }

            fieldDTO.associateDimensionId = attributePo.associateDimensionId;
            fieldDTO.associateDimensionFieldId = attributePo.associateDimensionFieldId;
            //判断是否关联维度
            if (attributePo.associateDimensionId != 0 && attributePo.associateDimensionFieldId != 0) {
                DimensionPO dimensionPo = dimensionMapper.selectById(attributePo.associateDimensionId);
                fieldDTO.associateDimensionName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
                fieldDTO.associateDimensionSqlScript = dimensionPo == null ? "" : dimensionPo.sqlScript;
                DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
                fieldDTO.associateDimensionFieldName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
            }
            fieldList.add(fieldDTO);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, fieldList);
    }

    @Override
    public List<FactAttributeUpdateDTO> getFactAttribute(int factId) {
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId, factId);
        List<FactAttributePO> list = mapper.selectList(queryWrapper);
        return FactAttributeMap.INSTANCES.poDetailToDtoList(list);
    }

    @Override
    public FactAttributeDTO getConfigDetailsByFactAttributeId(int id) {

        return FactAttributeMap.INSTANCES.poToDto(baseMapper.selectById(id));
    }

    @Override
    public ResultEnum addFactField(FactAttributeDTO dto) {

        // 根据事实id获取所有的事实字段
        List<String> factFieldEnNameList = this.query()
                .eq("fact_id", dto.factId)
                .select("fact_field_en_name")
                .list()
                .stream().map(e -> e.factFieldEnName).collect(Collectors.toList());
        // 判断字段唯一
        for (String s : factFieldEnNameList) {
            if (s.equalsIgnoreCase(dto.factFieldEnName)) {
                return ResultEnum.FACT_FIELD_EXIST;
            }
        }
        return this.save(FactAttributeMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionSelectDTO> getDimensionDetailByBusinessId(int id) {

        QueryWrapper<DimensionPO> queryWrapper1 = new QueryWrapper<>();
        // 根据业务域id查询的是不共享的
        queryWrapper1.lambda()
                .eq(DimensionPO::getBusinessId, id)
                .eq(DimensionPO::getShare, false)
                .eq(DimensionPO::getIsPublish, 1)
                .select(DimensionPO::getId, DimensionPO::getDimensionTabName, DimensionPO::getShare);
        List<DimensionPO> dimensionPoList = dimensionMapper.selectList(queryWrapper1);

        QueryWrapper<DimensionPO> queryWrapper2 = new QueryWrapper<>();

        queryWrapper2.lambda()
                .eq(DimensionPO::getShare, true)
                .eq(DimensionPO::getIsPublish, 1)
                .select(DimensionPO::getId, DimensionPO::getDimensionTabName, DimensionPO::getShare);
        dimensionPoList.addAll(dimensionMapper.selectList(queryWrapper2));

        List<DimensionSelectDTO> dimensionSelectDtoList = DimensionMap.INSTANCES.listPoToListSelectDto(dimensionPoList);
        for (DimensionSelectDTO dto : dimensionSelectDtoList) {

            QueryWrapper<DimensionAttributePO> queryWrapper3 = new QueryWrapper<>();
            queryWrapper3.lambda()
                    .eq(DimensionAttributePO::getDimensionId, dto.getId())
                    .select(DimensionAttributePO::getId, DimensionAttributePO::getDimensionFieldEnName);
            dto.setList(DimensionAttributeMap.INSTANCES.listPoToListSelectDto(dimensionAttributeMapper.selectList(queryWrapper3)));
        }

        return dimensionSelectDtoList;
    }

    @Override
    public WideTableQueryPageDTO executeFactTableSql(WideTableFieldConfigDTO dto) {
        if (CollectionUtils.isEmpty(dto.entity) || CollectionUtils.isEmpty(dto.relations)) {
            throw new FkException(ResultEnum.QUERY_CONDITION_NOTNULL);
        }
        StringBuilder appendSql = new StringBuilder();
        appendSql.append("SELECT ");
        //拼接查询字段
        WideTableAliasDTO wideTableAliasDTO = wideTableImpl.appendField(dto.entity);
        appendSql.append(wideTableAliasDTO.sql);
        //拼接关联表
        appendSql.append(wideTableImpl.appendRelateTable(dto.relations));
        // TODO 执行sql语句,封装结果集
        WideTableQueryPageDTO wideTableData = new WideTableQueryPageDTO();
        // 1.根据前端的来源sql,查询主表的前十条数据
        OdsQueryDTO queryDto = new OdsQueryDTO();
        queryDto.querySql = dto.factTableSourceSql;
        queryDto.pageIndex = 1;
        queryDto.pageSize = 10;
        queryDto.tableName = dto.factTableName;
        try {
            ResultEntity<OdsResultDTO> result = dataAccessClient.getTableAccessQueryList(queryDto);
            if (result.code == ResultEnum.SUCCESS.getCode()) {
                OdsResultDTO data = result.data;
                JSONArray dataArray = data.dataArray;
                JSONArray jsonArray = new JSONArray();
                JSONArray wideTableDataDataArray = new JSONArray();
                // 主表查询出来的结果集不为空
                if (dataArray != null) {

                    List<String> columnList = new ArrayList<>();

                    wideTableData.columnList = dataArray.getJSONObject(0).entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());

                    DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
                    IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dwSource.conType);

                    // SELECT * FROM 目标表名 WHERE
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject jsonObject = dataArray.getJSONObject(i);

                        String sql = command.buildSelectTable(dto.relations, dto.relations.get(0).targetTable, jsonObject);
                        WideTableQueryPageDTO queryPageDto = wideTableImpl.getWideTableData(sql, 10, "dw_");

                        columnList = queryPageDto.columnList;

                        for (TableSourceRelationsDTO relation : dto.relations) {
                            if (queryPageDto.dataArray != null) {
                                for (int j = 0; j < queryPageDto.dataArray.size(); j++) {
                                    JSONObject object = queryPageDto.dataArray.getJSONObject(j);

                                    JSONObject newJosn = new JSONObject();
                                    newJosn.put(relation.sourceColumn, object.getString(relation.targetColumn));
                                    if (queryPageDto.dataArray != null) {
                                        jsonArray.addAll(queryPageDto.dataArray);
                                    }
                                }
                            }
                        }
                    }
                    JSONObject jsonObject = new JSONObject();

                    for (int i = 0; i < dataArray.size(); i++) {
                        // 合并两个JsonObject
                        String note;
                        if (CollectionUtils.isEmpty(jsonArray)) {
                            StringBuffer str = new StringBuffer();
                            note = str.append(dataArray.getJSONObject(i).toString()).toString();
                            jsonObject = JSONObject.parseObject(note);
                        } else {
                            StringBuffer str = new StringBuffer();
                            note = str.append(dataArray.getJSONObject(i).toString()).append(jsonArray.getJSONObject(i).toString()).toString();
                        }
                        if (note.contains("}{")) {
                            note = note.replace("}{", ",");
                            jsonObject = JSONObject.parseObject(note);
                        }
                        wideTableDataDataArray.add(jsonObject);
                    }

                    wideTableData.columnList.addAll(columnList);
                    wideTableData.dataArray = wideTableDataDataArray;
                }

            }
        } catch (Exception e) {
            log.error("executeFactTableSql ex:{}", e);
        }

        if (StringUtils.isBlank(dto.factTableName)) {
            throw new FkException(ResultEnum.FACT_NAME_NOTNULL);
        }
        wideTableData.updateSqlScript = buildFactUpdateSql(dto.entity.get(0).tableId);
        dto.entity = wideTableAliasDTO.entity;
        wideTableData.configDTO = dto;
        return wideTableData;
    }

    @Override
    public ResultEnum editFactField(FactAttributeDTO dto) {

        // 根据事实id获取所有的事实字段
        List<String> factFieldEnNameList = this.query()
                .eq("fact_id", dto.factId)
                .select("fact_field_en_name")
                .list()
                .stream().map(e -> e.factFieldEnName).collect(Collectors.toList());
        // 判断字段唯一
        for (String s : factFieldEnNameList) {
            if (s.equalsIgnoreCase(dto.factFieldEnName)) {
                return ResultEnum.FACT_FIELD_EXIST;
            }
        }

        FactAttributePO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_EXISTS;
        }
        return this.updateById(FactAttributeMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    public String buildFactUpdateSql(int factId) {

        Map<String, String> configDetailsMap = this.query()
                .eq("fact_id", factId)
                .eq("attribute_type", 1)
                .select("config_details", "fact_field_en_name")
                .list()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.isNotBlank(e.configDetails))
                .collect(Collectors.toMap(FactAttributePO::getFactFieldEnName, FactAttributePO::getConfigDetails));

        if (CollectionUtils.isEmpty(configDetailsMap)) {
            return null;
        }

        return buildUpdateSql(configDetailsMap);
    }

    public String buildUpdateSql(Map<String, String> configDetailsMap) {

        StringBuilder str = new StringBuilder();

        for (Map.Entry<String, String> entry : configDetailsMap.entrySet()) {

            // 当前维度key关联关系
            List<TableSourceRelationsDTO> dto = JSON.parseArray(entry.getValue(), TableSourceRelationsDTO.class);
            List<TableSourceRelationsDTO> relations = dto;
            if (CollectionUtils.isEmpty(relations)) {
                continue;
            }
            // 先做单连线
            TableSourceRelationsDTO relationsDto = relations.get(0);
            str.append("update ");
            str.append(relationsDto.sourceTable);
            str.append(" set ");
            str.append(" = ");
            str.append(relationsDto.targetTable).append(".").append(entry.getKey());
            str.append(" from ");
            str.append(relationsDto.targetTable);
            str.append(" where ");
            str.append(relationsDto.sourceTable).append(".").append(relationsDto.sourceColumn);
            str.append(" = ");
            str.append(relationsDto.targetTable).append(".").append(relationsDto.targetColumn);
            str.append(";");
        }

        if (StringUtils.isEmpty(str.toString())) {
            return null;
        }
        return str.toString();
    }

}
