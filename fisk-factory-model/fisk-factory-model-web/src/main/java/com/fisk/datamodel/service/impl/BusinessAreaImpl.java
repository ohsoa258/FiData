package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.dto.GetConfigDTO;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.webindex.WebIndexDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.BusinessAreaMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IBusinessArea;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class BusinessAreaImpl
        extends ServiceImpl<BusinessAreaMapper,
        BusinessAreaPO> implements IBusinessArea {

    @Resource
    GenerateCondition generateCondition;
    @Resource
    GetMetadata getMetadata;
    @Resource
    UserHelper userHelper;
    @Resource
    BusinessAreaMapper mapper;
    @Resource
    GetConfigDTO getConfig;
    @Resource
    DimensionAttributeImpl dimensionAttribute;
    @Resource
    AtomicIndicatorsImpl atomicIndicators;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    DimensionImpl dimensionImpl;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactImpl factImpl;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    FactAttributeImpl factAttributeImpl;
    @Resource
    TableHistoryImpl tableHistory;
    @Resource
    DimensionFolderMapper dimensionFolderMapper;
    @Resource
    DimensionFolderImpl dimensionFolderImpl;
    @Resource
    BusinessProcessMapper businessProcessMapper;
    @Resource
    BusinessProcessImpl businessProcessImpl;
    @Resource
    WideTableImpl wideTable;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private WideTableImpl wideTableImpl;
    @Resource
    private RedisUtil redisUtil;
    @Value("${fidata.wide-table.guid}")
    private String wideTableGuid;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessAreaDTO businessAreaDTO) {
        //判断名称是否重复
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessAreaPO::getBusinessName, businessAreaDTO.businessName);
        BusinessAreaPO businessAreaPo = mapper.selectOne(queryWrapper);
        if (businessAreaPo != null) {
            return ResultEnum.BUSINESS_AREA_EXIST;
        }
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        boolean save = this.save(po);

        return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public BusinessAreaDTO getData(long id) {

        // select * from 表 where id=#{id} and del_flag=1
        BusinessAreaPO po = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();

        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }
        return BusinessAreaMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateBusinessArea(BusinessAreaDTO businessAreaDTO) {
        //根据id,判断是否存在
        long id = businessAreaDTO.getId();
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断名称是否重复
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessAreaPO::getBusinessName, businessAreaDTO.businessName);
        BusinessAreaPO businessAreaPo = mapper.selectOne(queryWrapper);
        if (businessAreaPo != null && businessAreaPo.id != businessAreaDTO.id) {
            return ResultEnum.BUSINESS_AREA_EXIST;
        }
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteBusinessArea(long id) {

        try {
            boolean result = false;
            // 1.非空判断
            BusinessAreaPO model = this.getById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            //判断该业务域下维度文件夹中的维度表是否被引用
            List<Long> idArray = checkIsAssociate(id);
            //删除业务域维度文件夹、维度
            idArray = idArray.stream().distinct().collect(Collectors.toList());

            //删除niFi流程--拼接参数
            DataModelVO vo = niFiDelTable(id);
            vo.dimensionIdList.ids.removeAll(idArray);

            PgsqlDelTableDTO dto = delDwDorisTable(idArray, id);

            if (!CollectionUtils.isEmpty(idArray)) {
                result = true;
                List<Integer> folder = new ArrayList<>();
                QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
                dimensionPoQueryWrapper
                        .notIn("id", idArray).lambda()
                        .eq(DimensionPO::getBusinessId, id);
                List<DimensionPO> dimensionPoList = dimensionMapper.selectList(dimensionPoQueryWrapper);
                if (!CollectionUtils.isEmpty(dimensionPoList)) {
                    //循环删除维度表数据
                    for (DimensionPO item : dimensionPoList) {
                        folder.add(item.dimensionFolderId);
                        if (dimensionMapper.deleteByIdWithFill(item) == 0) {
                            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                        }
                    }
                    //删除维度文件夹
                    QueryWrapper<DimensionFolderPO> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.select("id")
                            .notIn("id", folder.stream().distinct().collect(Collectors.toList()))
                            .lambda().eq(DimensionFolderPO::getBusinessId, id);
                    List<Integer> ids = (List) dimensionFolderMapper.selectObjs(queryWrapper1);
                    if (!CollectionUtils.isEmpty(ids)) {
                        if (dimensionFolderMapper.deleteBatchIds(ids) == 0) {
                            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                        }
                    }
                }
            } else {
                //删除所有维度文件夹
                QueryWrapper<DimensionFolderPO> folderPoQueryWrapper = new QueryWrapper<>();
                folderPoQueryWrapper.select("id").lambda().eq(DimensionFolderPO::getBusinessId, id);
                List<Integer> folderIds = (List) dimensionFolderMapper.selectObjs(folderPoQueryWrapper);
                if (!CollectionUtils.isEmpty(folderIds)) {
                    if (dimensionFolderMapper.deleteBatchIds(folderIds) == 0) {
                        throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                    }
                }
                //删除所有维度
                QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
                dimensionPoQueryWrapper.select("id").lambda().eq(DimensionPO::getBusinessId, id);
                List<Integer> ids = (List) dimensionMapper.selectObjs(dimensionPoQueryWrapper);
                if (!CollectionUtils.isEmpty(ids)) {
                    if (dimensionMapper.deleteBatchIds(ids) == 0) {
                        throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                    }
                }
            }
            //删除业务过程和事实表
            delBusinessProcessFact(id);
            //删除niFi流程
            ////publishTaskClient.deleteNifiFlow(vo);
            //拼接删除DW/Doris库中维度事实表
            publishTaskClient.publishBuildDeletePgsqlTableTask(dto);

            if (result) {
                return ResultEnum.BUSINESS_AREA_EXISTS_ASSOCIATED;
            }
            return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } catch (Exception e) {
            log.error("deleteBusinessArea:" + e);
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    /**
     * 检查维度表是否与其他业务域维度/事实是否有关联
     *
     * @param id
     * @return
     */
    private List<Long> checkIsAssociate(long id) {
        List<Long> dimensionIds = new ArrayList<>();
        List<Long> idArray = new ArrayList<>();
        QueryWrapper<DimensionFolderPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").lambda()
                .eq(DimensionFolderPO::getBusinessId, id);
        List<Long> dimensionFolderIds = (List) dimensionFolderMapper.selectObjs(queryWrapper);
        if (!CollectionUtils.isEmpty(dimensionFolderIds)) {
            //获取维度文件夹下维度表
            QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
            dimensionPoQueryWrapper.select("id").in("dimension_folder_id", dimensionFolderIds)
                    .lambda().eq(DimensionPO::getBusinessId, id);
            dimensionIds = (List) dimensionMapper.selectObjs(dimensionPoQueryWrapper);
            if (!CollectionUtils.isEmpty(dimensionIds)) {
                QueryWrapper<DimensionAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
                attributePoQueryWrapper.select("associate_dimension_id")
                        .in("associate_dimension_id", dimensionIds)
                        .notIn("dimension_id", dimensionIds);
                idArray.addAll((List) dimensionAttributeMapper.selectObjs(attributePoQueryWrapper));
            }
        }
        //查看事实表与共享维度是否存在关联
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        factPoQueryWrapper.select("id").lambda().ne(FactPO::getBusinessId, id);
        List<Long> factIds = (List) factMapper.selectObjs(factPoQueryWrapper);
        if (!CollectionUtils.isEmpty(factIds) && !CollectionUtils.isEmpty(dimensionIds)) {
            QueryWrapper<FactAttributePO> factAttributePoQueryWrapper = new QueryWrapper<>();
            factAttributePoQueryWrapper.select("associate_dimension_id")
                    .in("fact_id", factIds)
                    .in("associate_dimension_id", dimensionIds);
            List<Long> factDimensionId = (List) factAttributeMapper.selectObjs(factAttributePoQueryWrapper);
            if (!CollectionUtils.isEmpty(factDimensionId)) {
                idArray.addAll(factDimensionId);
            }
        }
        return idArray;
    }

    /**
     * 根据业务域id,删除所有业务过程/事实
     *
     * @param id
     */
    private void delBusinessProcessFact(long id) {
        //删除业务域下所有业务过程
        QueryWrapper<BusinessProcessPO> businessProcessPoQueryWrapper = new QueryWrapper<>();
        businessProcessPoQueryWrapper.select("id").lambda().eq(BusinessProcessPO::getBusinessId, id);
        List<Integer> businessProcessPoList = (List) businessProcessMapper.selectObjs(businessProcessPoQueryWrapper);
        if (!CollectionUtils.isEmpty(businessProcessPoList)) {
            if (businessProcessMapper.deleteBatchIds(businessProcessPoList) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        //删除业务域下所有事实表
        QueryWrapper<FactPO> factPoQueryWrapper1 = new QueryWrapper<>();
        factPoQueryWrapper1.select("id").lambda().eq(FactPO::getBusinessId, id);
        List<Integer> factIdList = (List) factMapper.selectObjs(factPoQueryWrapper1);
        if (!CollectionUtils.isEmpty(factIdList)) {
            if (factMapper.deleteBatchIds(factIdList) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
    }

    /**
     * 拼接niFi删除表参数
     *
     * @param id
     * @return
     */
    private DataModelVO niFiDelTable(long id) {
        DataModelVO vo = new DataModelVO();
        vo.dataClassifyEnum = DataClassifyEnum.DATAMODELING;
        vo.businessId = String.valueOf(id);
        //获取业务域下所有维度id
        DataModelTableVO dimensionTable = new DataModelTableVO();
        dimensionTable.type = OlapTableEnum.DIMENSION;
        QueryWrapper<DimensionPO> queryWrapperPo = new QueryWrapper<>();
        queryWrapperPo.select("id").lambda().eq(DimensionPO::getBusinessId, id);
        dimensionTable.ids = (List) dimensionMapper.selectObjs(queryWrapperPo).stream().collect(Collectors.toList());
        vo.dimensionIdList = dimensionTable;
        //获取业务域下所有事实id
        DataModelTableVO factTable = new DataModelTableVO();
        factTable.type = OlapTableEnum.FACT;
        QueryWrapper<FactPO> factPoQueryWrapper2 = new QueryWrapper<>();
        factPoQueryWrapper2.select("id").lambda().eq(FactPO::getBusinessId, id);
        factTable.ids = (List) factMapper.selectObjs(factPoQueryWrapper2).stream().collect(Collectors.toList());
        vo.factIdList = factTable;
        vo.userId = userHelper.getLoginUserInfo().id;
        return vo;
    }

    private PgsqlDelTableDTO delDwDorisTable(List<Long> idArray, long businessAreaId) {
        PgsqlDelTableDTO dto = new PgsqlDelTableDTO();
        dto.delApp = true;
        dto.userId = userHelper.getLoginUserInfo().id;
        dto.businessTypeEnum = BusinessTypeEnum.DATAMODEL;
        List<TableListDTO> tableList = new ArrayList<>();

        //获取维度表名称集合
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("dimension_tab_name").lambda()
                .eq(DimensionPO::getBusinessId, businessAreaId);
        if (!CollectionUtils.isEmpty(idArray)) {
            queryWrapper.notIn("id", idArray);
        }
        List<String> dimensionNameList = (List) dimensionMapper.selectObjs(queryWrapper);
        if (!CollectionUtils.isEmpty(dimensionNameList)) {
            for (String name : dimensionNameList) {
                TableListDTO table = new TableListDTO();
                table.tableName = name;
                tableList.add(table);
            }
        }
        //获取事实表名称集合
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        factPoQueryWrapper.select("fact_tab_name").lambda().eq(FactPO::getBusinessId, businessAreaId);
        List<String> factNameList = (List) factMapper.selectObjs(factPoQueryWrapper);
        if (!CollectionUtils.isEmpty(factNameList)) {
            for (String name : factNameList) {
                TableListDTO table = new TableListDTO();
                table.tableName = name;
                tableList.add(table);
            }
        }
        dto.tableList = tableList;
        return dto;
    }

    @Override
    public Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows) {

        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    @Override
    public List<FilterFieldDTO> getBusinessAreaColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.tableName = "tb_area_business";
        dto.filterSql = FilterSqlConstants.BUSINESS_AREA_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<BusinessPageResultDTO> getDataList(BusinessQueryDTO query) {
        StringBuilder str = new StringBuilder();
        if (query != null && StringUtils.isNotEmpty(query.key)) {
            str.append(" and business_name like concat('%', " + "'" + query.key + "'" + ", '%') ");
        }
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        BusinessPageDTO data = new BusinessPageDTO();
        data.page = query.page;
        data.where = str.toString();
        return baseMapper.queryList(query.page, data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum getBusinessAreaPublicData(IndicatorQueryDTO dto) {
        BusinessAreaGetDataDTO data = new BusinessAreaGetDataDTO();
        try {
            data.userId = userHelper.getLoginUserInfo().id;
            data.businessAreaId = dto.businessAreaId;
            //根据事实表id获取指标
            data.atomicIndicatorList = atomicIndicators.atomicIndicatorPush(dto.factIds);
            //获取事实表关联的维度
            data.dimensionList = dimensionAttribute.getDimensionMetaDataList(dto.factIds);
            //更改事实表Doris发布状态
            List<FactPO> factPoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(dto.factIds)) {
                QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("id", dto.factIds);
                factPoList = factMapper.selectList(queryWrapper);
            }
            if (!CollectionUtils.isEmpty(factPoList)) {
                for (FactPO po : factPoList) {
                    po.dorisPublish = PublicStatusEnum.PUBLIC_ING.getValue();
                    if (factMapper.updateById(po) == 0) {
                        throw new FkException(ResultEnum.PUBLISH_FAILURE);
                    }
                }
            }
            //更改维度表Doris发布状态
            if (!CollectionUtils.isEmpty(data.dimensionList)) {
                for (ModelMetaDataDTO item : data.dimensionList) {
                    DimensionPO dimensionPo = dimensionMapper.selectById(item.id);
                    if (dimensionPo == null) {
                        continue;
                    }
                    dimensionPo.dorisPublish = PublicStatusEnum.PUBLIC_ING.getValue();
                    if (dimensionMapper.updateById(dimensionPo) == 0) {
                        throw new FkException(ResultEnum.PUBLISH_FAILURE);
                    }
                }
            }
            //发布历史
            addTableHistory(dto);
            if (!CollectionUtils.isEmpty(dto.factIds)) {
                //消息推送
                publishTaskClient.publishOlapCreateModel(data);
            }
            //宽表发布
            wideTable.publishWideTable(dto);
        } catch (Exception e) {
            log.error("BusinessAreaImpl,getBusinessAreaPublicData：" + e.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }

    private void addTableHistory(IndicatorQueryDTO dto) {
        List<TableHistoryDTO> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dto.factIds)) {
            for (Integer id : dto.factIds) {
                TableHistoryDTO data = new TableHistoryDTO();
                data.remark = dto.remark;
                data.tableId = id;
                data.tableType = CreateTypeEnum.CREATE_DORIS.getValue();
                list.add(data);
            }
        }
        if (CollectionUtils.isEmpty(dto.wideTableIds)) {
            for (Integer id : dto.wideTableIds) {
                TableHistoryDTO data = new TableHistoryDTO();
                data.remark = dto.remark;
                data.tableId = id;
                data.tableType = CreateTypeEnum.CREATE_WIDE_TABLE.getValue();
                list.add(data);
            }
        }
        tableHistory.addTableHistory(list);
    }

    @Override
    public WebIndexDTO getBusinessArea() {
        WebIndexDTO dto = new WebIndexDTO();
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        dto.businessAreaCount = mapper.selectCount(queryWrapper);
        return dto;
    }

    @Override
    public Page<PipelineTableLogVO> getBusinessAreaTable(PipelineTableQueryDTO dto) {
        //建模日志完善
        log.info("建模日志参数:" + JSON.toJSONString(dto));
        Page<PipelineTableLogVO> pipelineTableLogVOPage = baseMapper.businessAreaTable(dto.page, dto);
        List<PipelineTableLogVO> records = pipelineTableLogVOPage.getRecords();
        ResultEntity<List<PipelineTableLogVO>> pipelineTableLog = publishTaskClient.getPipelineTableLog(JSON.toJSONString(records), JSON.toJSONString(dto));
        if (pipelineTableLog.code == ResultEnum.SUCCESS.getCode()) {
            List<PipelineTableLogVO> data = pipelineTableLog.data;
            pipelineTableLogVOPage.setRecords(data);
            pipelineTableLogVOPage.setTotal(data.size());
            return pipelineTableLogVOPage;
        } else {
            return baseMapper.businessAreaTable(dto.page, dto);
        }

    }

    @Override
    public BusinessAreaTableDetailDTO getBusinessAreaTableDetail(BusinessAreaQueryTableDTO dto) {
        BusinessAreaTableDetailDTO data = new BusinessAreaTableDetailDTO();
        if (OlapTableEnum.DIMENSION.getValue() == dto.tableEnum.getValue()) {
            DimensionPO dimensionPO = dimensionMapper.selectById(dto.tableId);
            data.tableName = dimensionPO == null ? "" : dimensionPO.dimensionTabName;
        } else {
            FactPO factPO = factMapper.selectById(dto.tableId);
            data.tableName = factPO == null ? "" : factPO.factTabName;
        }
        return data;
    }

    @Override
    public List<DispatchRedirectDTO> redirect(ModelRedirectDTO dto) {

        NifiCustomWorkflowDetailDTO detailDto = new NifiCustomWorkflowDetailDTO();
        detailDto.appId = String.valueOf(dto.getBusinessId());
        detailDto.tableId = String.valueOf(dto.getTableId());
        // 根据tableType对应具体的管道组件
        DataFactoryEnum dataFactoryEnum = DataFactoryEnum.getName(dto.getTableType());
        switch (dataFactoryEnum) {
            // 数仓维度
            case NUMBER_DIMENSION:
                detailDto.componentType = ChannelDataEnum.DW_DIMENSION_TASK.getName();
                break;
            // 数仓事实
            case NUMBER_FACT:
                detailDto.componentType = ChannelDataEnum.DW_FACT_TASK.getName();
                break;
            // 分析维度
            case ANALYSIS_DIMENSION:
                detailDto.componentType = ChannelDataEnum.OLAP_DIMENSION_TASK.getName();
                break;
            // 分析事实
            case ANALYSIS_FACT:
                detailDto.componentType = ChannelDataEnum.OLAP_FACT_TASK.getName();
                break;
            // 宽表
            case WIDE_TABLE:
                detailDto.componentType = ChannelDataEnum.OLAP_WIDETABLE_TASK.getName();
                break;
            default:
                break;
        }

        try {
            ResultEntity<List<DispatchRedirectDTO>> result = dataFactoryClient.redirect(detailDto);
            if (result.code == ResultEnum.SUCCESS.getCode()) {
                return result.data;
            }
        } catch (Exception e) {
            log.error("远程调用失败,方法名: 【data-factory:redirect】");
            return null;
        }

        return null;
    }

    @Override
    public List<FiDataMetaDataDTO> getDataModelStructure(FiDataMetaDataReqDTO reqDto) {
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId));
        if (!flag) {
            // 将数据建模结构存入redis
            setDataModelStructure(reqDto);
        }
        List<FiDataMetaDataDTO> list = null;
        String dataModelStructure = redisUtil.get(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId)).toString();
        if (StringUtils.isNotBlank(dataModelStructure)) {
            list = JSONObject.parseArray(dataModelStructure, FiDataMetaDataDTO.class);
        }
        return list;
    }

    @Override
    public boolean setDataModelStructure(FiDataMetaDataReqDTO reqDto) {

        List<FiDataMetaDataDTO> list = new ArrayList<>();
        if ("1".equalsIgnoreCase(reqDto.dataSourceId)) {
            // 第一层 - 1: dmp_dw
            FiDataMetaDataDTO dwDto = new FiDataMetaDataDTO();
            // FiData数据源id: 数据资产自定义
            dwDto.setDataSourceId(Integer.parseInt(reqDto.dataSourceId));

            // 第一层id
//            String dwGuid = UUID.randomUUID().toString();
            // 第一层子级
            List<FiDataMetaDataTreeDTO> dwDataTreeList = new ArrayList<>();
            FiDataMetaDataTreeDTO dwDataTree = new FiDataMetaDataTreeDTO();
            dwDataTree.setId(reqDto.dataSourceId);
            dwDataTree.setParentId("-10");
            dwDataTree.setLabel(reqDto.dataSourceName);
            dwDataTree.setLabelAlias(reqDto.dataSourceName);
            dwDataTree.setLevelType(LevelTypeEnum.DATABASE);

            // 封装dw所有结构数据
            dwDataTree.setChildren(buildBusinessChildren(reqDto.dataSourceId, "dw", TableBusinessTypeEnum.FACTTABLE));
            dwDataTreeList.add(dwDataTree);

            dwDto.setChildren(dwDataTreeList);
            list.add(dwDto);
        } else if ("4".equalsIgnoreCase(reqDto.dataSourceId)) {
            FiDataMetaDataDTO olapDto = new FiDataMetaDataDTO();
            // FiData数据源id: 数据资产自定义
            olapDto.setDataSourceId(Integer.parseInt(StringUtils.isBlank(reqDto.dataSourceId) ? String.valueOf(0) : reqDto.dataSourceId));

            // 第一层id
//            String olapGuid = UUID.randomUUID().toString();
            List<FiDataMetaDataTreeDTO> olapDataTreeList = new ArrayList<>();
            FiDataMetaDataTreeDTO olapDataTree = new FiDataMetaDataTreeDTO();
            olapDataTree.setId(reqDto.dataSourceId);
            olapDataTree.setParentId("-10");
            olapDataTree.setLabel(reqDto.dataSourceName);
            olapDataTree.setLabelAlias(reqDto.dataSourceName);
            olapDataTree.setLevelType(LevelTypeEnum.DATABASE);

            // 封装data-access所有结构数据
            olapDataTree.setChildren(buildBusinessChildren(reqDto.dataSourceId, "olap", TableBusinessTypeEnum.QUOTATABLE));
            olapDataTreeList.add(olapDataTree);

            olapDto.setChildren(olapDataTreeList);
            list.add(olapDto);
        }
        // 入redis
        if (!CollectionUtils.isEmpty(list)) {
            redisUtil.set(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId), JSON.toJSONString(list));
        }
        return true;
    }

/*    private List<FiDataMetaDataTreeDTO> buildChildren(String guid, String dourceType) {

        List<FiDataMetaDataTreeDTO> businessTreeList = new ArrayList<>();

        // 第二层: 业务域总目录
        FiDataMetaDataTreeDTO businessTreeDto = new FiDataMetaDataTreeDTO();
        String businessGuid = UUID.randomUUID().toString();
        businessTreeDto.setId(businessGuid);
        businessTreeDto.setParentId(guid);
        businessTreeDto.setLabel("业务域");
        businessTreeDto.setLabelAlias("业务域");
        businessTreeDto.setLevelType(LevelTypeEnum.FOLDER);

        List<BusinessAreaPO> businessPoList = this.query().orderByDesc("create_time").list();

        // 业务域子级
        businessTreeDto.setChildren(buildBusinessChildren(guid, businessPoList, dourceType));
        businessTreeList.add(businessTreeDto);

        return businessTreeList;
    }*/

    /**
     * 构建业务域子级
     *
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 构建业务域子级
     * @author Lock
     * @date 2022/6/16 18:03
     * @version v1.0
     * @params id FiData数据源id
     * @params businessPoList
     */
    private List<FiDataMetaDataTreeDTO> buildBusinessChildren(String id, String dourceType, TableBusinessTypeEnum tableBusinessTypeEnum) {

        List<BusinessAreaPO> businessPoList = this.query().orderByDesc("create_time").list();

        List<FiDataMetaDataTreeDTO> collect = businessPoList.stream()
                .filter(Objects::nonNull)
                .map(business -> {
                    // 第三层: 业务域
                    FiDataMetaDataTreeDTO businessPoTreeDto = new FiDataMetaDataTreeDTO();
                    businessPoTreeDto.setId(String.valueOf(business.id));
                    businessPoTreeDto.setParentId(id);
                    businessPoTreeDto.setLabel(business.getBusinessName());
                    businessPoTreeDto.setLabelAlias(business.getBusinessName());
                    businessPoTreeDto.setLabelDesc(business.getBusinessDes());
                    businessPoTreeDto.setLevelType(LevelTypeEnum.FOLDER);

                    // 第四层 - 1: 维度文件夹
                    List<FiDataMetaDataTreeDTO> dimensionFolderTreeList = dimensionFolderImpl.query()
                            .eq("business_id", business.id)
                            .list()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(dimensionFolder -> {
                                FiDataMetaDataTreeDTO dimensionFolderTreeDto = new FiDataMetaDataTreeDTO();
                                dimensionFolderTreeDto.setId(String.valueOf(dimensionFolder.id));
                                dimensionFolderTreeDto.setParentId(String.valueOf(business.id));
                                dimensionFolderTreeDto.setLabel(dimensionFolder.dimensionFolderCnName);
                                dimensionFolderTreeDto.setLabelAlias(dimensionFolder.dimensionFolderCnName);
                                dimensionFolderTreeDto.setLabelDesc(dimensionFolder.dimensionFolderDesc);
                                dimensionFolderTreeDto.setLevelType(LevelTypeEnum.FOLDER);

                                // 第五层: 维度表
                                List<FiDataMetaDataTreeDTO> dimensionTreeList = dimensionImpl.query()
                                        .eq("dimension_folder_id", dimensionFolder.id)
                                        .list()
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .map(dimension -> {
                                            FiDataMetaDataTreeDTO dimensionTreeDto = new FiDataMetaDataTreeDTO();
                                            dimensionTreeDto.setId(String.valueOf(dimension.id));
                                            dimensionTreeDto.setParentId(String.valueOf(dimensionFolder.id));
                                            dimensionTreeDto.setLabel(dimension.dimensionTabName);
                                            dimensionTreeDto.setLabelAlias(dimension.dimensionTabName);
                                            dimensionTreeDto.setLevelType(LevelTypeEnum.TABLE);
                                            dimensionTreeDto.setPublishState(String.valueOf(dimension.isPublish != 1 ? 0 : 1));
                                            dimensionTreeDto.setLabelDesc(dimension.dimensionDesc);
                                            dimensionTreeDto.setSourceId(Integer.parseInt(id));
                                            dimensionTreeDto.setSourceType(1);
                                            dimensionTreeDto.setLabelBusinessType(TableBusinessTypeEnum.DIMENSIONTABLE);
                                            // 第六层: 维度字段
                                            List<FiDataMetaDataTreeDTO> dimensionAttributeTreeList = dimensionAttribute.query()
                                                    .eq("dimension_id", dimension.id)
                                                    .list()
                                                    .stream()
                                                    .filter(Objects::nonNull)
                                                    .map(field -> {
                                                        FiDataMetaDataTreeDTO dimensionAttributeTreeDto = new FiDataMetaDataTreeDTO();
                                                        dimensionAttributeTreeDto.setId(String.valueOf(field.id));
                                                        dimensionAttributeTreeDto.setParentId(String.valueOf(dimension.id));
                                                        dimensionAttributeTreeDto.setLabel(field.dimensionFieldEnName);
                                                        dimensionAttributeTreeDto.setLabelAlias(field.dimensionFieldEnName);
                                                        dimensionAttributeTreeDto.setLevelType(LevelTypeEnum.FIELD);
                                                        dimensionAttributeTreeDto.setPublishState(String.valueOf(dimension.isPublish != 1 ? 0 : 1));
                                                        dimensionAttributeTreeDto.setLabelLength(String.valueOf(field.dimensionFieldLength));
                                                        dimensionAttributeTreeDto.setLabelType(field.dimensionFieldType);
                                                        dimensionAttributeTreeDto.setLabelDesc(field.dimensionFieldDes);
                                                        dimensionAttributeTreeDto.setSourceId(Integer.parseInt(id));
                                                        dimensionAttributeTreeDto.setSourceType(1);
                                                        dimensionAttributeTreeDto.setLabelBusinessType(TableBusinessTypeEnum.DIMENSIONTABLE);
                                                        dimensionAttributeTreeDto.setParentName(dimension.dimensionTabName);
                                                        dimensionAttributeTreeDto.setParentNameAlias(dimension.dimensionTabName);
                                                        return dimensionAttributeTreeDto;
                                                    }).collect(Collectors.toList());

                                            // 维度表子级
                                            dimensionTreeDto.setChildren(dimensionAttributeTreeList);
                                            return dimensionTreeDto;
                                        }).collect(Collectors.toList());

                                // 维度文件夹子级
                                dimensionFolderTreeDto.setChildren(dimensionTreeList);
                                return dimensionFolderTreeDto;
                            }).collect(Collectors.toList());

                    // 第四层 - 2: 事实文件夹
                    List<FiDataMetaDataTreeDTO> businessProcessTreeList = this.businessProcessImpl.query()
                            .eq("business_id", business.id)
                            .list()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(businessProcess -> {
                                FiDataMetaDataTreeDTO businessProcessTreeDto = new FiDataMetaDataTreeDTO();
                                businessProcessTreeDto.setId(String.valueOf(businessProcess.id));
                                businessProcessTreeDto.setParentId(String.valueOf(business.id));
                                businessProcessTreeDto.setLabel(businessProcess.businessProcessCnName);
                                businessProcessTreeDto.setLabelAlias(businessProcess.businessProcessCnName);
                                businessProcessTreeDto.setLabelDesc(businessProcess.businessProcessDesc);
                                businessProcessTreeDto.setLevelType(LevelTypeEnum.FOLDER);

                                // 第五层: 事实表
                                List<FiDataMetaDataTreeDTO> factTreeList = factImpl.query()
                                        .eq("business_process_id", businessProcess.id)
                                        .list()
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .map(fact -> {
                                            FiDataMetaDataTreeDTO factTreeDto = new FiDataMetaDataTreeDTO();
                                            factTreeDto.setId(String.valueOf(fact.id));
                                            factTreeDto.setParentId(String.valueOf(businessProcess.id));
                                            factTreeDto.setLabel(fact.factTabName);
                                            factTreeDto.setLabelAlias(fact.factTabName);
                                            factTreeDto.setLevelType(LevelTypeEnum.TABLE);
                                            factTreeDto.setPublishState(String.valueOf(fact.isPublish != 1 ? 0 : 1));
                                            factTreeDto.setLabelDesc(fact.factTableDesc);
                                            factTreeDto.setSourceId(Integer.parseInt(id));
                                            factTreeDto.setSourceType(1);
                                            factTreeDto.setLabelBusinessType(tableBusinessTypeEnum);
                                            // 第六层: 事实字段
                                            List<FiDataMetaDataTreeDTO> factAttributeTreeList;
                                            // olap: 分析指标
                                            if ("olap".equalsIgnoreCase(dourceType)) {
                                                factAttributeTreeList = atomicIndicators.getAtomicIndicator(Math.toIntExact(fact.id))
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .map(field -> {
                                                            FiDataMetaDataTreeDTO factAttributeTreeDto = new FiDataMetaDataTreeDTO();
                                                            factAttributeTreeDto.setId(String.valueOf(field.id));
                                                            factAttributeTreeDto.setParentId(String.valueOf(fact.id));
                                                            factAttributeTreeDto.setLabel(field.factFieldName);
                                                            factAttributeTreeDto.setLabelAlias(field.factFieldName);
                                                            factAttributeTreeDto.setLevelType(LevelTypeEnum.FIELD);
                                                            factAttributeTreeDto.setPublishState(String.valueOf(fact.dorisPublish != 1 ? 0 : 1));
//                                                        factAttributeTreeDto.setLabelLength(String.valueOf(""));
                                                            factAttributeTreeDto.setLabelType(field.factFieldType);
//                                                        factAttributeTreeDto.setLabelDesc(field.indicatorsDes);
                                                            factAttributeTreeDto.setSourceId(Integer.parseInt(id));
                                                            factAttributeTreeDto.setSourceType(1);
                                                            factAttributeTreeDto.setLabelBusinessType(TableBusinessTypeEnum.QUOTATABLE);
                                                            factAttributeTreeDto.setParentName(fact.factTabName);
                                                            factAttributeTreeDto.setParentNameAlias(fact.factTabName);
                                                            return factAttributeTreeDto;
                                                        }).collect(Collectors.toList());
                                            } else {
                                                // 数仓
                                                factAttributeTreeList = factAttributeImpl.query()
                                                        .eq("fact_id", fact.id)
                                                        .list()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .map(field -> {
                                                            FiDataMetaDataTreeDTO factAttributeTreeDto = new FiDataMetaDataTreeDTO();
                                                            factAttributeTreeDto.setId(String.valueOf(field.id));
                                                            factAttributeTreeDto.setParentId(String.valueOf(fact.id));
                                                            factAttributeTreeDto.setLabel(field.factFieldEnName);
                                                            factAttributeTreeDto.setLabelAlias(field.factFieldEnName);
                                                            factAttributeTreeDto.setLevelType(LevelTypeEnum.FIELD);
                                                            factAttributeTreeDto.setPublishState(String.valueOf(fact.isPublish != 1 ? 0 : 1));
                                                            factAttributeTreeDto.setLabelLength(String.valueOf(field.factFieldLength));
                                                            factAttributeTreeDto.setLabelType(field.factFieldType);
                                                            factAttributeTreeDto.setLabelDesc(field.factFieldDes);
                                                            factAttributeTreeDto.setSourceId(Integer.parseInt(id));
                                                            factAttributeTreeDto.setSourceType(1);
                                                            factAttributeTreeDto.setParentName(fact.factTabName);
                                                            factAttributeTreeDto.setParentNameAlias(fact.factTabName);
                                                            return factAttributeTreeDto;
                                                        }).collect(Collectors.toList());
                                            }

                                            // 事实表子级
                                            factTreeDto.setChildren(factAttributeTreeList);
                                            return factTreeDto;
                                        }).collect(Collectors.toList());

                                // 事实文件夹子级
                                businessProcessTreeDto.setChildren(factTreeList);
                                return businessProcessTreeDto;
                            }).collect(Collectors.toList());

                    List<FiDataMetaDataTreeDTO> folderList = new ArrayList<>();

                    // 第四层 - 3: 宽表文件夹
                    if ("olap".equalsIgnoreCase(dourceType)) {
                        FiDataMetaDataTreeDTO wideTableFolderTreeDto = new FiDataMetaDataTreeDTO();
                        wideTableFolderTreeDto.setId(wideTableGuid);
                        wideTableFolderTreeDto.setParentId(String.valueOf(business.id));
                        wideTableFolderTreeDto.setLabel("宽表");
                        wideTableFolderTreeDto.setLabelAlias("宽表");
                        wideTableFolderTreeDto.setLabelDesc("宽表");
                        wideTableFolderTreeDto.setLevelType(LevelTypeEnum.FOLDER);

                        // 第五层: 宽表
                        List<FiDataMetaDataTreeDTO> wideTableTreeList = this.wideTableImpl.query()
                                .eq("business_id", business.id)
                                .list()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(wideTable1 -> {
                                    FiDataMetaDataTreeDTO wideTableTreeDto = new FiDataMetaDataTreeDTO();
                                    wideTableTreeDto.setId(String.valueOf(wideTable1.id));
                                    wideTableTreeDto.setParentId(wideTableGuid);
                                    wideTableTreeDto.setLabel(wideTable1.name);
                                    wideTableTreeDto.setLabelAlias(wideTable1.name);
                                    wideTableTreeDto.setLevelType(LevelTypeEnum.TABLE);
                                    wideTableTreeDto.setPublishState(String.valueOf(wideTable1.dorisPublish != 1 ? 0 : 1));
                                    wideTableTreeDto.setSourceId(Integer.parseInt(id));
                                    wideTableTreeDto.setSourceType(1);
                                    wideTableTreeDto.setLabelBusinessType(TableBusinessTypeEnum.WIDETABLE);

                                    // 第六层: 宽表字段
                                    WideTableFieldConfigDTO wideTableFieldDto = JSON.parseObject(wideTable1.configDetails, WideTableFieldConfigDTO.class);
                                    List<FiDataMetaDataTreeDTO> wideTableFieldTreeList = new ArrayList<>();
                                    if (wideTableFieldDto != null && !CollectionUtils.isEmpty(wideTableFieldDto.entity)) {

                                        wideTableFieldDto.entity.forEach(e -> {
                                            List<FiDataMetaDataTreeDTO> fieldList = e.columnConfig
                                                    .stream()
                                                    .filter(Objects::nonNull)
                                                    .map(field -> {
                                                        FiDataMetaDataTreeDTO wideTableFieldTreeDto = new FiDataMetaDataTreeDTO();
                                                        wideTableFieldTreeDto.setId(String.valueOf(field.fieldId));
                                                        wideTableFieldTreeDto.setParentId(String.valueOf(wideTable1.id));
                                                        wideTableFieldTreeDto.setLabel(StringUtils.isNotEmpty(field.alias) ? field.alias : field.fieldName);
                                                        wideTableFieldTreeDto.setLabelAlias(field.alias);
                                                        wideTableFieldTreeDto.setLevelType(LevelTypeEnum.FIELD);
                                                        wideTableFieldTreeDto.setPublishState(String.valueOf(wideTable1.dorisPublish != 1 ? 0 : 1));
                                                        wideTableFieldTreeDto.setLabelLength(String.valueOf(field.fieldLength));
                                                        wideTableFieldTreeDto.setLabelType(field.fieldType);
                                                        wideTableFieldTreeDto.setSourceId(Integer.parseInt(id));
                                                        wideTableFieldTreeDto.setSourceType(1);
                                                        wideTableFieldTreeDto.setLabelBusinessType(TableBusinessTypeEnum.WIDETABLE);
                                                        wideTableFieldTreeDto.setParentName(wideTable1.name);
                                                        wideTableFieldTreeDto.setParentNameAlias(wideTable1.name);
                                                        return wideTableFieldTreeDto;
                                                    }).collect(Collectors.toList());
                                            wideTableFieldTreeList.addAll(fieldList);
                                        });
                                    }

                                    // 宽表子级
                                    wideTableTreeDto.setChildren(wideTableFieldTreeList);
                                    return wideTableTreeDto;
                                }).collect(Collectors.toList());
                        // 宽表文件夹子级
                        wideTableFolderTreeDto.setChildren(wideTableTreeList);

                        // 业务域子级-宽表
                        folderList.add(wideTableFolderTreeDto);
                    }

                    // 业务域子级-维度
                    folderList.addAll(dimensionFolderTreeList);
                    // 业务域子级-事实
                    folderList.addAll(businessProcessTreeList);

                    businessPoTreeDto.setChildren(folderList);
                    return businessPoTreeDto;
                }).collect(Collectors.toList());
        return collect;
    }
}
