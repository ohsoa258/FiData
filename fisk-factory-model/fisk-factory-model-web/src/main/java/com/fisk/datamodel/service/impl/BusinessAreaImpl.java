package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datamodel.dto.GetConfigDTO;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.webindex.WebIndexDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.CreateTypeEnum;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    TableHistoryImpl tableHistory;
    @Resource
    DimensionFolderMapper dimensionFolderMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;
    @Resource
    WideTableImpl wideTable;

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

}
