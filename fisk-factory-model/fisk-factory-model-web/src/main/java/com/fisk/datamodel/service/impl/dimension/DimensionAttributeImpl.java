package com.fisk.datamodel.service.impl.dimension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.dataaccess.dto.tablefield.TableFieldDTO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.dto.factattribute.FieldsAssociatedMetricsOrMetaObjDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.entity.TableBusinessPO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionFolderPO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.SyncModeMap;
import com.fisk.datamodel.map.TableBusinessMap;
import com.fisk.datamodel.map.dimension.DimensionAttributeMap;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.service.IBusinessArea;
import com.fisk.datamodel.service.IDimensionAttribute;
import com.fisk.datamodel.service.impl.CustomScriptImpl;
import com.fisk.datamodel.service.impl.SyncModeImpl;
import com.fisk.datamodel.service.impl.SystemVariablesImpl;
import com.fisk.datamodel.service.impl.TableBusinessImpl;
import com.fisk.datamodel.service.impl.fact.FactAttributeImpl;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
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
    @Resource
    private IBusinessArea businessArea;
    @Resource
    private DimensionImpl dimension;
    @Resource
    private DataManageClient dataManageClient;

    @Resource
    private UserClient userClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addOrUpdateDimensionAttribute(DimensionAttributeAddDTO dto) {
        //判断是否存在
        DimensionPO dimensionPo = mapper.selectById(dto.dimensionId);
        if (dimensionPo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //查询sql保存改为最后一步才保存
        dimensionPo.setDataSourceId(dto.dataSourceId);
        dimensionPo.setSqlScript(dto.sqlScript);

        //获取公共域维度的文件夹id
        LambdaQueryWrapper<DimensionFolderPO> folderWrapper = new LambdaQueryWrapper<>();
        folderWrapper.eq(DimensionFolderPO::getDimensionFolderEnName, "Commondomaindimension");
        DimensionFolderPO folderPO = dimensionFolder.getOne(folderWrapper);
        long id = folderPO.getId();

        //如果发布的是公共域维度表  则校验是否是第一次发布所在的业务域
        if (dimensionPo.getDimensionFolderId() == id) {
            //获取此次发布所在的业务域
            String businessAreaName = dto.getBusinessAreaName();
            LambdaQueryWrapper<BusinessAreaPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BusinessAreaPO::getBusinessName, businessAreaName);
            BusinessAreaPO one = businessArea.getOne(wrapper);

            //如果不同则不允许此次发布
            if (dimensionPo.getBusinessId() != one.getId()) {
//                //获取初始业务域
//                LambdaQueryWrapper<BusinessAreaPO> wrapper1 = new LambdaQueryWrapper<>();
//                wrapper1.eq(BusinessAreaPO::getId, dimensionPo.getBusinessId());
//                BusinessAreaPO businessAreaPO = businessArea.getOne(wrapper1);
//                return ResultEnum.PUBLIC_DIM_PUBLISH_ERROR.getMsg() + " 初始业务域名称：" + businessAreaPO.getBusinessName();
                return ResultEnum.PUBLIC_DIM_PUBLISH_ERROR;
            }
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
        customScript.addOrUpdateCustomScript(dto.customScriptList,dto.dimensionId);

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
        String delSql = "TRUNCATE TABLE " + prefixTempName +
                "_" +
                dimensionTabName +
                ";";
        dimensionPo.deleteTempScript = String.valueOf(delSql);

        dimensionPo.dimensionKeyScript = dto.dimensionKeyScript;
        dimensionPo.coverScript = dto.coverScript;
        dimensionPo.dorisIfOpenStrictMode = dto.dorisIfOpenStrictMode;
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
            queryDTO.ifDropTargetTbl = dto.ifDropTargetTbl;
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

        //删除前先查询
        List<DimensionAttributePO> dimPos = listByIds(ids);
        //删除
        ResultEnum resultEnum = attributeMapper.deleteBatchIds(ids) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

        //创建固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(1);
        //异步
        executor.submit(() -> {
            log.info("异步任务开始执行");
            try {
                if (org.springframework.util.CollectionUtils.isEmpty(dimPos)) {
                    return;
                }
                //获取平台配置-dmp_dw的信息 已经在catch块儿中 无需判断是否获取到
                ResultEntity<DataSourceDTO> resultEntity = userClient.getFiDataDataSourceById(1);
                String conIp = resultEntity.getData().getConIp();
                String conDbname = resultEntity.getData().getConDbname();

                //获取限定名称
                List<String> qNames = new ArrayList<>();
                for (DimensionAttributePO po : dimPos) {
                    qNames.add(conIp + "_" + conDbname + "_" + 1 + "_" + po.getDimensionId() + "_" + po.id);
                }
                MetaDataDeleteAttributeDTO metaDataDeleteAttributeDTO = new MetaDataDeleteAttributeDTO();
                metaDataDeleteAttributeDTO.setQualifiedNames(qNames);
                //删除字段元数据
                dataManageClient.deleteFieldMetaData(metaDataDeleteAttributeDTO);
            } catch (Exception e) {
                log.error("数仓建模-维度表删除字段时-异步删除元数据任务执行出错：" + e);
            }
            log.info("异步任务执行结束");
        });

        return resultEnum;
    }

    @Override
    public DimensionAttributeUpdateDTO getDimensionAttribute(int id) {
        return DimensionAttributeMap.INSTANCES.poToDetailDto(attributeMapper.selectById(id));
    }

    @Override
    public List<DimensionAttributeDTO> getDimensionAttributeByIds(List<Integer> ids) {
        return DimensionAttributeMap.INSTANCES.poListToDtoList(attributeMapper.selectBatchIds(ids));
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
        data.dorisIfOpenStrictMode = dimensionPo.dorisIfOpenStrictMode;
        //获取表字段详情
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId, dimensionId);
        List<DimensionAttributePO> list = attributeMapper.selectList(queryWrapper);
        List<DimensionAttributeDTO> dimensionAttributeDTOS = DimensionAttributeMap.INSTANCES.poListToDtoList(list);

        try {

            //todo:维度表暂不展示指标粒度在字段页面
            /*
            2024 04 03 数仓贯标 字段回显新增贯标列 展示数仓的字段和哪些指标或数据元关联
            */
//            //1.1获取所有指标 -> 指标id 指标名称    维度表 = 指标粒度
//            List<BusinessTargetinfoDTO> dtos = dataManageClient.modelGetBusinessTargetInfoList();
//
//            //1.2获取数仓字段和指标所属表里所有关联关系  -> 字段id 指标id    维度表 = 指标粒度
//            List<BusinessExtendedfieldsDTO> businessExtendedfieldsDTOS = dataManageClient.modelGetMetricMapList();

            //2.1获取所有数据元 -> 数据元id 数据元名称
            List<StandardsDTO> standardsDTOS = dataManageClient.modelGetStandards();

            //2.2获取数仓字段和数据元关联表里所有关联关系
            List<StandardsMenuDTO> standardMenus = dataManageClient.getStandardMenus();
            Map<Integer, String> standardsMenuMap = standardMenus.stream()
                    .collect(Collectors.toMap(
                            StandardsMenuDTO::getId,
                            StandardsMenuDTO::getName
                    ));

            //2.3获取数仓字段和数据元关联表里所有关联关系
            List<StandardsBeCitedDTO> standardsBeCitedDTOS = dataManageClient.modelGetStandardsMap();

            for (DimensionAttributeDTO dimensionAttributeDTO : dimensionAttributeDTOS) {
                ArrayList<FieldsAssociatedMetricsOrMetaObjDTO> objDTOS = new ArrayList<>();
                //获取字段id
                long filedId = dimensionAttributeDTO.getId();

//                //不为空则说明该维度表字段关联的有指标标准
//                if (!CollectionUtils.isEmpty(businessExtendedfieldsDTOS)) {
//                    //循环指标关联关系
//                    for (BusinessExtendedfieldsDTO d : businessExtendedfieldsDTOS) {
//                        if (d.getAttributeid().equals(String.valueOf(filedId))) {
//                            //循环指标 获取指标名称
//                            for (BusinessTargetinfoDTO businessTargetinfoDTO : dtos) {
//                                if (d.getIndexid().equals(String.valueOf(businessTargetinfoDTO.getId()))) {
//                                    FieldsAssociatedMetricsOrMetaObjDTO dto = new FieldsAssociatedMetricsOrMetaObjDTO();
//                                    dto.setId(Math.toIntExact(businessTargetinfoDTO.getId()));
//                                    dto.setName(businessTargetinfoDTO.getIndicatorName());
//                                    //类型 0指标 1数据元
//                                    dto.setType(0);
//                                    objDTOS.add(dto);
//                                }
//                            }
//                        }
//                    }
//                }

                //不为空则说明该事实表字段关联的有数据元标准
                if (!CollectionUtils.isEmpty(standardsBeCitedDTOS)) {
                    //循环数据元标准关联关系
                    for (StandardsBeCitedDTO s : standardsBeCitedDTOS) {
                        //只获取维度表的关联关系
                        if (!TableBusinessTypeEnum.DW_DIMENSION.equals(s.getTableBusinessType())) {
                            continue;
                        }

                        if (s.getFieldId().equals(String.valueOf(filedId))) {
                            //循环数据元标准集合 获取数据元标准名称
                            for (StandardsDTO standardsDTO : standardsDTOS) {
                                if (s.getStandardsId().equals(standardsDTO.getId())) {
                                    FieldsAssociatedMetricsOrMetaObjDTO dto = new FieldsAssociatedMetricsOrMetaObjDTO();
                                    //获取到数据元标准id关联的数据元标准menuid
                                    int menuId = standardsDTO.getMenuId();
                                    //获取menuId对应的菜单名称
                                    String menuName = standardsMenuMap.get(menuId);
                                    dto.setId(menuId);
                                    dto.setName(menuName);
                                    //类型 0指标 1数据元
                                    dto.setType(1);
                                    objDTOS.add(dto);
                                }
                            }
                        }
                    }
                }
                dimensionAttributeDTO.setAssociatedDto(objDTOS);
            }
        } catch (Exception e) {
            log.error("==============================");
            log.error("获取数仓贯标失败..." + e);
            log.error("==============================");
        }

        data.attributeDTOList = dimensionAttributeDTOS;

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
        //数据分类
        po.dataClassification = dto.dataClassification;
        //数据分级
        po.dataLevel = dto.dataLevel;
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

    @Override
    public List<TableFieldDTO> searchColumn(String key) {
        return attributeMapper.searchColumn(key);
    }
}
