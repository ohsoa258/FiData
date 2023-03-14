package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.enums.task.FuncNameEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.StringBuildUtils;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.core.utils.dbutils.utils.SqlServerUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.dto.GetConfigDTO;
import com.fisk.datamodel.dto.TableStructDTO;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.webindex.WebIndexDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionFolderPO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.BusinessProcessPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.BusinessAreaMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionFolderMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.BusinessProcessMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.mapper.fact.FactMapper;
import com.fisk.datamodel.service.IBusinessArea;
import com.fisk.datamodel.service.impl.dimension.DimensionAttributeImpl;
import com.fisk.datamodel.service.impl.dimension.DimensionFolderImpl;
import com.fisk.datamodel.service.impl.dimension.DimensionImpl;
import com.fisk.datamodel.service.impl.fact.BusinessProcessImpl;
import com.fisk.datamodel.service.impl.fact.FactAttributeImpl;
import com.fisk.datamodel.service.impl.fact.FactImpl;
import com.fisk.datamodel.service.impl.widetable.WideTableImpl;
import com.fisk.datamodel.utils.mysql.DataSourceConfigUtil;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.daconfig.ProcessorConfig;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lock
 */
@Service
@Slf4j
public class BusinessAreaImpl
        extends ServiceImpl<BusinessAreaMapper,
        BusinessAreaPO> implements IBusinessArea {

    @Value("${fiData-data-dw-source}")
    private Integer dwSource;

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
    DimensionFolderImpl dimensionFolder;
    @Resource
    WideTableImpl wideTable;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private WideTableImpl wideTableImpl;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DataManageClient dataManageClient;
    @Resource
    private UserClient userClient;

    @Value("${spring.open-metadata}")
    private Boolean openMetadata;
    @Value("${fiData-data-dw-source}")
    private Integer targetDbId;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessAreaDTO dto) {
        //判断名称是否重复
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessAreaPO::getBusinessName, dto.businessName);
        BusinessAreaPO businessAreaPo = mapper.selectOne(queryWrapper);
        if (businessAreaPo != null) {
            return ResultEnum.BUSINESS_AREA_EXIST;
        }
        Integer flat = mapper.insertBusinessArea(dto, userHelper.getLoginUserInfo().id, LocalDateTime.now());
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        dimensionFolder.addPublicDimensionFolder();
        dimensionFolder.addSystemDimensionFolder(dto.id);

        if (openMetadata) {
            // 添加元数据信息
            ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
            classificationInfoDto.setName(dto.businessName);
            classificationInfoDto.setDescription(dto.businessDes);
            classificationInfoDto.setSourceType(2);
            classificationInfoDto.setDelete(false);
            try {
                dataManageClient.appSynchronousClassification(classificationInfoDto);
            } catch (Exception e) {
                log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
            }
        }

        return ResultEnum.SUCCESS;
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

            if (openMetadata) {
                //删除元数据信息
                ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
                classificationInfoDto.setName(model.getBusinessName());
                classificationInfoDto.setDescription(model.getBusinessDes());
                classificationInfoDto.setSourceType(2);
                classificationInfoDto.setDelete(true);
                try {
                    dataManageClient.appSynchronousClassification(classificationInfoDto);
                } catch (Exception e) {
                    // 不同场景下，元数据可能不会部署，在这里只做日志记录，不影响正常流程
                    log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
                }
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
        dto.driver = getConfig.driver;
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
                //doris表默认勾选同步
                data.openTransmission = true;
                list.add(data);
            }
        }
        if (CollectionUtils.isEmpty(dto.wideTableIds)) {
            for (Integer id : dto.wideTableIds) {
                TableHistoryDTO data = new TableHistoryDTO();
                data.remark = dto.remark;
                data.tableId = id;
                data.tableType = CreateTypeEnum.CREATE_WIDE_TABLE.getValue();
                //doris表默认勾选同步
                data.openTransmission = true;
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
    public List<FiDataMetaDataTreeDTO> getDataModelTableStructure(FiDataMetaDataReqDTO reqDto) {

        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId));
        if (!flag) {
            // 将数据接入结构存入redis
            setDataModelStructure(reqDto);
        }
        List<FiDataMetaDataTreeDTO> list = null;
        String dataAccessStructure = redisUtil.get(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId)).toString();
        if (StringUtils.isNotBlank(dataAccessStructure)) {
            list = JSONObject.parseArray(dataAccessStructure, FiDataMetaDataTreeDTO.class);
        }
        return list;
    }

    @Override
    public boolean setDataModelStructure(FiDataMetaDataReqDTO reqDto) {
        List<FiDataMetaDataDTO> list = new ArrayList<>();

        // 表字段信息
        List<FiDataMetaDataTreeDTO> tableFieldList = new ArrayList<>();

        if ("1".equalsIgnoreCase(reqDto.dataSourceId)) {
            // 第一层 - 1: dmp_dw
            FiDataMetaDataDTO dwDto = new FiDataMetaDataDTO();
            // FiData数据源id: 数据资产自定义
            dwDto.setDataSourceId(Integer.parseInt(reqDto.dataSourceId));
            // 第一层id
            List<FiDataMetaDataTreeDTO> dwDataTreeList = new ArrayList<>();
            FiDataMetaDataTreeDTO dwDataTree = new FiDataMetaDataTreeDTO();
            dwDataTree.setId(reqDto.dataSourceId);
            dwDataTree.setParentId("-10");
            dwDataTree.setLabel(reqDto.dataSourceName);
            dwDataTree.setLabelAlias(reqDto.dataSourceName);
            dwDataTree.setLevelType(LevelTypeEnum.DATABASE);
            dwDataTree.setSourceId(Integer.parseInt(reqDto.dataSourceId));
            dwDataTree.setSourceType(1);

            // 封装dw所有结构数据
            HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTree_DW = buildBusinessChildren(reqDto.dataSourceId, "dw", TableBusinessTypeEnum.DW_FACT);
            Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTree_DW = fiDataMetaDataTree_DW.entrySet().iterator().next();
            tableFieldList.addAll(nextTree_DW.getKey());
            dwDataTree.setChildren(nextTree_DW.getValue());
            dwDataTreeList.add(dwDataTree);
            dwDto.setChildren(dwDataTreeList);
            list.add(dwDto);
        } else if ("4".equalsIgnoreCase(reqDto.dataSourceId)) {
            // 第一层 - 1: dmp_olap
            FiDataMetaDataDTO olapDto = new FiDataMetaDataDTO();
            // FiData数据源id: 数据资产自定义
            olapDto.setDataSourceId(Integer.parseInt(reqDto.dataSourceId));
            // 第一层id
            List<FiDataMetaDataTreeDTO> olapDataTreeList = new ArrayList<>();
            FiDataMetaDataTreeDTO olapDataTree = new FiDataMetaDataTreeDTO();
            olapDataTree.setId(reqDto.dataSourceId);
            olapDataTree.setParentId("-10");
            olapDataTree.setLabel(reqDto.dataSourceName);
            olapDataTree.setLabelAlias(reqDto.dataSourceName);
            olapDataTree.setLevelType(LevelTypeEnum.DATABASE);
            olapDataTree.setSourceId(Integer.parseInt(reqDto.dataSourceId));
            olapDataTree.setSourceType(1);

            // 封装olap、宽表 所有结构数据
            HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTree_OLAP = buildBusinessChildren(reqDto.dataSourceId, "olap", TableBusinessTypeEnum.DORIS_FACT);
            Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTree_OLAP = fiDataMetaDataTree_OLAP.entrySet().iterator().next();
            tableFieldList.addAll(nextTree_OLAP.getKey());
            olapDataTree.setChildren(nextTree_OLAP.getValue());
            olapDataTreeList.add(olapDataTree);
            olapDto.setChildren(olapDataTreeList);
            list.add(olapDto);
        }
        // 入redis
        if (!CollectionUtils.isEmpty(list)) {
            redisUtil.set(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId), JSON.toJSONString(list));
        }
        if (!CollectionUtils.isEmpty(tableFieldList)) {
            String s = JSON.toJSONString(tableFieldList);
            redisUtil.set(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId), s);
        }
        return true;
    }

    @Override
    public List<FiDataTableMetaDataDTO> getFiDataTableMetaData(FiDataTableMetaDataReqDTO dto) {

        if (CollectionUtils.isEmpty(dto.getTableUniques())) {
            return null;
        }

        return null;
    }

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
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> buildBusinessChildren(String id, String dourceType, TableBusinessTypeEnum tableBusinessTypeEnum) {

        // 建模暂时没有schema的设置
        List<BusinessAreaPO> businessPoList = this.query().orderByDesc("create_time").list();

        // 维度文件夹（公共域维度）
        DimensionFolderPO dimensionFolderPO_Public = null;
        List<DimensionFolderPO> dimensionFolderPOList_Public = dimensionFolderImpl.query()
                .eq("share", 1)
                .list()
                .stream()
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionFolderPOList_Public)) {
            dimensionFolderPO_Public = dimensionFolderPOList_Public.get(0);
        }
        DimensionFolderPO finalDimensionFolderPO_Public = dimensionFolderPO_Public;
        final boolean[] isSetTb_Public = {false};

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = businessPoList.stream()
                .filter(Objects::nonNull)
                .map(business -> {
                    // 第三层: 业务域
                    FiDataMetaDataTreeDTO businessPoTreeDto = new FiDataMetaDataTreeDTO();
                    String uuid_businessId = UUID.randomUUID().toString().replace("-", "");
                    businessPoTreeDto.setId(uuid_businessId); // String.valueOf(business.id)
                    businessPoTreeDto.setParentId(id);
                    businessPoTreeDto.setLabel(business.getBusinessName());
                    businessPoTreeDto.setLabelAlias(business.getBusinessName());
                    businessPoTreeDto.setLabelDesc(business.getBusinessDes());
                    businessPoTreeDto.setLevelType(LevelTypeEnum.FOLDER);
                    businessPoTreeDto.setSourceId(Integer.parseInt(id));
                    businessPoTreeDto.setSourceType(1);

                    List<FiDataMetaDataTreeDTO> folderList = new ArrayList<>();

                    List<FiDataMetaDataTreeDTO> dimensionFolderTreeList = new ArrayList<>();

                    // 第四层 - 1: 维度文件夹（公共域维度）
                    if (finalDimensionFolderPO_Public != null) {
                        FiDataMetaDataTreeDTO dimensionFolderTreeDto_public = getDimensionFolder(uuid_businessId, finalDimensionFolderPO_Public, id);
                        // 表字段信息单独再保存一份
                        if (dimensionFolderTreeDto_public != null && !isSetTb_Public[0] && !CollectionUtils.isEmpty(dimensionFolderTreeDto_public.getChildren())) {
                            isSetTb_Public[0] = true;
                            key.addAll(dimensionFolderTreeDto_public.getChildren());
                        }
                        dimensionFolderTreeList.add(dimensionFolderTreeDto_public);
                    }

                    // 第四层 - 1: 维度文件夹（当前域维度）
                    List<DimensionFolderPO> dimensionFolderPOS = dimensionFolderImpl.query()
                            .eq("business_id", business.id)
                            .list()
                            .stream()
                            .filter(Objects::nonNull).collect(Collectors.toList());
                    for (DimensionFolderPO dimensionFolder : dimensionFolderPOS) {
                        FiDataMetaDataTreeDTO dimensionFolderTreeDto = getDimensionFolder(uuid_businessId, dimensionFolder, id);
                        // 表字段信息单独再保存一份
                        if (dimensionFolderTreeDto != null &&
                                !CollectionUtils.isEmpty(dimensionFolderTreeDto.getChildren())) {
                            key.addAll(dimensionFolderTreeDto.getChildren());
                        }
                        dimensionFolderTreeList.add(dimensionFolderTreeDto);
                    }

                    // 第四层 - 2: 事实文件夹
                    List<FiDataMetaDataTreeDTO> businessProcessTreeList = this.businessProcessImpl.query()
                            .eq("business_id", business.id)
                            .list()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(businessProcess -> {
                                FiDataMetaDataTreeDTO businessProcessTreeDto = new FiDataMetaDataTreeDTO();
                                String uuid_businessProcessId = UUID.randomUUID().toString().replace("-", "");
                                businessProcessTreeDto.setId(uuid_businessProcessId); // String.valueOf(businessProcess.id)
                                businessProcessTreeDto.setParentId(uuid_businessId); //String.valueOf(business.id)
                                businessProcessTreeDto.setLabel(businessProcess.businessProcessCnName);
                                businessProcessTreeDto.setLabelAlias(businessProcess.businessProcessCnName);
                                businessProcessTreeDto.setLabelDesc(businessProcess.businessProcessDesc);
                                businessProcessTreeDto.setLevelType(LevelTypeEnum.FOLDER);
                                businessProcessTreeDto.setSourceId(Integer.parseInt(id));
                                businessProcessTreeDto.setSourceType(1);

                                // 第五层: 事实表
                                List<FiDataMetaDataTreeDTO> factTreeList = factImpl.query()
                                        .eq("business_process_id", businessProcess.id)
                                        .list()
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .map(fact -> {
                                            FiDataMetaDataTreeDTO factTreeDto = new FiDataMetaDataTreeDTO();
                                            factTreeDto.setId(String.valueOf(fact.id));
                                            factTreeDto.setParentId(uuid_businessProcessId); // String.valueOf(businessProcess.id)
                                            factTreeDto.setLabel(fact.factTabName);
                                            factTreeDto.setLabelAlias(fact.factTabName);
                                            factTreeDto.setLabelRelName(fact.factTabName);
                                            factTreeDto.setLevelType(LevelTypeEnum.TABLE);
                                            factTreeDto.setPublishState(String.valueOf(fact.isPublish != 1 ? 0 : 1));
                                            factTreeDto.setLabelDesc(fact.factTableDesc);
                                            factTreeDto.setSourceId(Integer.parseInt(id));
                                            factTreeDto.setSourceType(1);
                                            factTreeDto.setLabelBusinessType(tableBusinessTypeEnum.getValue());
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
                                                            factAttributeTreeDto.setLabelBusinessType(tableBusinessTypeEnum.getValue());
                                                            factAttributeTreeDto.setParentName(fact.factTabName);
                                                            factAttributeTreeDto.setParentNameAlias(fact.factTabName);
                                                            factAttributeTreeDto.setParentLabelRelName(fact.factTabName);
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
                                                            factAttributeTreeDto.setLabelBusinessType(tableBusinessTypeEnum.getValue());
                                                            factAttributeTreeDto.setParentName(fact.factTabName);
                                                            factAttributeTreeDto.setParentNameAlias(fact.factTabName);
                                                            factAttributeTreeDto.setParentLabelRelName(fact.factTabName);
                                                            return factAttributeTreeDto;
                                                        }).collect(Collectors.toList());
                                            }

                                            // 事实表子级
                                            factTreeDto.setChildren(factAttributeTreeList);
                                            return factTreeDto;
                                        }).collect(Collectors.toList());

                                // 事实文件夹子级
                                businessProcessTreeDto.setChildren(factTreeList);
                                // 表字段信息单独再保存一份
                                if (!CollectionUtils.isEmpty(factTreeList)) {
                                    key.addAll(factTreeList);
                                }
                                return businessProcessTreeDto;
                            }).collect(Collectors.toList());

                    // 第四层 - 3: 宽表文件夹
                    FiDataMetaDataTreeDTO wideTableFolderTreeDto = null;
                    if ("olap".equalsIgnoreCase(dourceType)) {
                        wideTableFolderTreeDto = new FiDataMetaDataTreeDTO();
                        String uuid_wideTableFolderId = UUID.randomUUID().toString().replace("-", ""); //"9bf0e48e-aef8-44c1-9871-0cb4d0560dd6"
                        wideTableFolderTreeDto.setId(uuid_wideTableFolderId);
                        wideTableFolderTreeDto.setParentId(uuid_businessId); // String.valueOf(business.id)
                        wideTableFolderTreeDto.setLabel("宽表");
                        wideTableFolderTreeDto.setLabelAlias("宽表");
                        wideTableFolderTreeDto.setLabelDesc("宽表");
                        wideTableFolderTreeDto.setLevelType(LevelTypeEnum.FOLDER);
                        wideTableFolderTreeDto.setSourceId(Integer.parseInt(id));
                        wideTableFolderTreeDto.setSourceType(1);

                        // 第五层: 宽表
                        List<FiDataMetaDataTreeDTO> wideTableTreeList = this.wideTableImpl.query()
                                .eq("business_id", business.id)
                                .list()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(wideTable1 -> {
                                    FiDataMetaDataTreeDTO wideTableTreeDto = new FiDataMetaDataTreeDTO();
                                    wideTableTreeDto.setId(String.valueOf(wideTable1.id));
                                    wideTableTreeDto.setParentId(uuid_wideTableFolderId);
                                    wideTableTreeDto.setLabel(wideTable1.name);
                                    wideTableTreeDto.setLabelAlias(wideTable1.name);
                                    wideTableTreeDto.setLabelRelName(wideTable1.name);
                                    wideTableTreeDto.setLevelType(LevelTypeEnum.TABLE);
                                    wideTableTreeDto.setPublishState(String.valueOf(wideTable1.dorisPublish != 1 ? 0 : 1));
                                    wideTableTreeDto.setSourceId(Integer.parseInt(id));
                                    wideTableTreeDto.setSourceType(1);
                                    wideTableTreeDto.setLabelBusinessType(TableBusinessTypeEnum.WIDE_TABLE.getValue());

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
                                                        wideTableFieldTreeDto.setLabelBusinessType(TableBusinessTypeEnum.WIDE_TABLE.getValue());
                                                        wideTableFieldTreeDto.setParentName(wideTable1.name);
                                                        wideTableFieldTreeDto.setParentNameAlias(wideTable1.name);
                                                        wideTableFieldTreeDto.setParentLabelRelName(wideTable1.name);
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
                        // 表字段信息单独再保存一份
                        if (!CollectionUtils.isEmpty(wideTableTreeList)) {
                            key.addAll(wideTableTreeList);
                        }
                    }

                    // 业务域子级-宽表
                    if (wideTableFolderTreeDto != null) {
                        folderList.add(wideTableFolderTreeDto);
                    }
                    // 业务域子级-维度
                    folderList.addAll(dimensionFolderTreeList);
                    // 业务域子级-事实
                    folderList.addAll(businessProcessTreeList);

                    businessPoTreeDto.setChildren(folderList);
                    return businessPoTreeDto;
                }).collect(Collectors.toList());

        hashMap.put(key, value);
        return hashMap;
    }

    public FiDataMetaDataTreeDTO getDimensionFolder(String uuid_businessId, DimensionFolderPO dimensionFolder, String id) {
        FiDataMetaDataTreeDTO dimensionFolderTreeDto = new FiDataMetaDataTreeDTO();
        String uuid_dimensionFolderId = UUID.randomUUID().toString().replace("-", "");
        dimensionFolderTreeDto.setId(uuid_dimensionFolderId); //  String.valueOf(dimensionFolder.id)
        dimensionFolderTreeDto.setParentId(uuid_businessId); //  String.valueOf(business.id)
        dimensionFolderTreeDto.setLabel(dimensionFolder.dimensionFolderCnName);
        dimensionFolderTreeDto.setLabelAlias(dimensionFolder.dimensionFolderCnName);
        dimensionFolderTreeDto.setLabelDesc(dimensionFolder.dimensionFolderDesc);
        dimensionFolderTreeDto.setLevelType(LevelTypeEnum.FOLDER);
        dimensionFolderTreeDto.setSourceId(Integer.parseInt(id));
        dimensionFolderTreeDto.setSourceType(1);

        // 第五层: 维度表
        List<FiDataMetaDataTreeDTO> dimensionTreeList = dimensionImpl.query()
                .eq("dimension_folder_id", dimensionFolder.id)
                .list()
                .stream()
                .filter(Objects::nonNull)
                .map(dimension -> {
                    FiDataMetaDataTreeDTO dimensionTreeDto = new FiDataMetaDataTreeDTO();
                    dimensionTreeDto.setId(String.valueOf(dimension.id));
                    dimensionTreeDto.setParentId(uuid_dimensionFolderId); // String.valueOf(dimensionFolder.id)
                    dimensionTreeDto.setLabel(dimension.dimensionTabName);
                    dimensionTreeDto.setLabelAlias(dimension.dimensionTabName);
                    dimensionTreeDto.setLabelRelName(dimension.dimensionTabName);
                    dimensionTreeDto.setLevelType(LevelTypeEnum.TABLE);
                    dimensionTreeDto.setPublishState(String.valueOf(dimension.isPublish != 1 ? 0 : 1));
                    dimensionTreeDto.setLabelDesc(dimension.dimensionDesc);
                    dimensionTreeDto.setSourceId(Integer.parseInt(id));
                    dimensionTreeDto.setSourceType(1);
                    dimensionTreeDto.setLabelBusinessType(TableBusinessTypeEnum.DW_DIMENSION.getValue());
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
                                dimensionAttributeTreeDto.setLabelBusinessType(TableBusinessTypeEnum.DW_DIMENSION.getValue());
                                dimensionAttributeTreeDto.setParentName(dimension.dimensionTabName);
                                dimensionAttributeTreeDto.setParentNameAlias(dimension.dimensionTabName);
                                dimensionAttributeTreeDto.setParentLabelRelName(dimension.dimensionTabName);
                                return dimensionAttributeTreeDto;
                            }).collect(Collectors.toList());

                    // 维度表子级
                    dimensionTreeDto.setChildren(dimensionAttributeTreeList);
                    return dimensionTreeDto;
                }).collect(Collectors.toList());

        dimensionFolderTreeDto.setChildren(dimensionTreeList);

        return dimensionFolderTreeDto;
    }

    @Override
    public List<AppBusinessInfoDTO> getBusinessAreaList() {
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(BusinessAreaPO::getId, BusinessAreaPO::getBusinessName, BusinessAreaPO::getBusinessDes);
        List<BusinessAreaPO> pos = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(pos)) {
            return new ArrayList<>();
        }
        return BusinessAreaMap.INSTANCES.poListToBusinessAreaInfo(pos);

    }

    @Override
    public List<TableNameDTO> getPublishSuccessTab(Integer businessId) {
        List<TableNameDTO> list = new ArrayList<>();

        list.addAll(dimensionImpl.getPublishSuccessDimTable(businessId));

        list.addAll(factImpl.getPublishSuccessFactTable(businessId));

        return list;
    }

    @Override
    public List<MetaDataInstanceAttributeDTO> getDataModelMetaData() {

        List<BusinessAreaPO> businessAreaPOList = this.query().list();
        if (CollectionUtils.isEmpty(businessAreaPOList)) {
            return new ArrayList<>();
        }

        MetaDataInstanceAttributeDTO instance = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_DW.getValue());
        instance.dbList.get(0).tableList = new ArrayList<>();
        for (BusinessAreaPO item : businessAreaPOList) {
            instance.dbList.get(0).tableList.addAll(factImpl.getFactMetaData(item.id, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_FACT.getValue(), item.getBusinessAdmin()));
            instance.dbList.get(0).tableList.addAll(dimensionImpl.getDimensionMetaData(item.id, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_DIMENSION.getValue(), item.getBusinessAdmin()));
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        list.add(instance);

        return list;
    }

    @Override
    public Object buildDimensionKeyScript(List<TableSourceRelationsDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return "";
        }

        StringBuilder str = new StringBuilder();

        for (TableSourceRelationsDTO item : dto) {
            str.append("update ");
            str.append(item.sourceTable);
            str.append(" set ");
            str.append(item.sourceTable).append(".").append(StringBuildUtils.dimensionKeyName(item.targetTable));
            str.append(" = ");
            str.append(item.targetTable).append(".").append(StringBuildUtils.dimensionKeyName(item.targetTable));
            str.append(" from ");
            str.append(item.sourceTable);
            if (!StringUtils.isEmpty(item.joinType)){
                str.append(" ").append(item.joinType);
                str.append(" ").append(item.targetTable);
            }
            str.append(" on ");
            str.append(item.sourceTable).append(".").append(item.sourceColumn);
            str.append(" = ");
            str.append(item.targetTable).append(".").append(item.targetColumn);
            str.append(";");
        }

        return str.toString();
    }

    @Override
    public JSONObject dataTypeList(Integer businessId) {
        //todo 暂时不传参 方便后期可能dw存在多个
        if (businessId != 0) {
            return new JSONObject();
        }

        ResultEntity<List<DataSourceDTO>> all = userClient.getAll();
        if (all.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        Optional<DataSourceDTO> first = all.data.stream().filter(e -> e.sourceBusinessType == SourceBusinessTypeEnum.DW).findFirst();
        if (!first.isPresent()) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(first.get().conType);
        return command.dataTypeList();


    }

    /**
     * 覆盖方式代码预览
     *
     * @return
     */
    @Override
    public Object overlayCodePreview(OverlayCodePreviewDTO dto) {

        String tableName;
        String prefixTempName;
        if (dto.type == 1) {
            DimensionPO po = dimensionMapper.selectById(dto.id);
            if (po == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            tableName = po.dimensionTabName;
            prefixTempName = po.prefixTempName;
        } else {
            FactPO factPO = factMapper.selectById(dto.id);
            if (factPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            tableName = factPO.factTabName;
            prefixTempName = factPO.prefixTempName;
        }

        DataAccessConfigDTO data = new DataAccessConfigDTO();

        ProcessorConfig processorConfig = new ProcessorConfig();
        processorConfig.targetTableName = tableName;
        data.processorConfig = processorConfig;

        DataSourceConfig targetDsConfig = new DataSourceConfig();
        targetDsConfig.syncMode = dto.syncMode;
        data.targetDsConfig = targetDsConfig;

        data.businessDTO = dto.tableBusiness == null ? new TableBusinessDTO() : dto.tableBusiness;
        data.businessDTO.otherLogic = 1;
        if (dto.syncMode == 4) {
            data.businessDTO.otherLogic = 2;
        }

        data.modelPublishFieldDTOList = dto.modelPublishFieldDTOList;

        List<String> collect = dto.modelPublishFieldDTOList.stream().filter(e -> e.isPrimaryKey == 1).map(e -> e.fieldEnName).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            data.businessKeyAppend = String.join(",", collect);
        }

        BuildNifiFlowDTO buildNifiFlow = new BuildNifiFlowDTO();
        buildNifiFlow.updateSql = dto.updateSql;
        buildNifiFlow.prefixTempName = prefixTempName;

        OverLoadCodeDTO dataModel = new OverLoadCodeDTO();
        dataModel.buildNifiFlow = buildNifiFlow;
        dataModel.config = data;
        dataModel.funcName = FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL_OUTPUT.getName();
        dataModel.dataSourceType = DataSourceTypeEnum.SQLSERVER;
        dataModel.synchronousTypeEnum = SynchronousTypeEnum.PGTOPG;

        ResultEntity<Object> objectResultEntity = publishTaskClient.overlayCodePreview(dataModel);
        if (objectResultEntity.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        /*Connection conn = null;
        Statement stat = null;
        try {
            conn = dataSourceConfigUtil.getConnection();
            stat = conn.createStatement();

            stat.executeQuery(objectResultEntity.data.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        return objectResultEntity.data;
    }

    @Override
    public Object overlayCodePreviewTest(OverlayCodePreviewDTO dto) {
        // 事实表全量覆盖
        String tableName;
        String prefixTempName;
        if (dto.type == 2){
            // 事实表全量覆盖
            FactPO factPO = factMapper.selectById(dto.id);
            if (factPO == null){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            tableName = factPO.factTabName;
            prefixTempName = factPO.prefixTempName;
        }else{
            DimensionPO po = dimensionMapper.selectById(dto.id);
            if (po == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            tableName = po.dimensionTabName;
            prefixTempName = po.prefixTempName;
        }

        // 获取数据源信息
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try{
            dataSourceConfig = userClient.getFiDataDataSourceById(dwSource);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
        }catch (Exception e){
            log.error("调用userClient服务获取数据源失败,", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        DataAccessConfigDTO data = new DataAccessConfigDTO();

        ProcessorConfig processorConfig = new ProcessorConfig();
        processorConfig.targetTableName = tableName;
        data.processorConfig = processorConfig;

        DataSourceConfig targetDsConfig = new DataSourceConfig();
        targetDsConfig.syncMode = dto.syncMode;
        data.targetDsConfig = targetDsConfig;

        data.businessDTO = dto.tableBusiness == null ? new TableBusinessDTO() : dto.tableBusiness;
//        data.businessDTO.otherLogic = 1;
//        if (dto.syncMode == 4) {
//            data.businessDTO.otherLogic = 2;
//        }

        data.modelPublishFieldDTOList = dto.modelPublishFieldDTOList;

        List<String> collect = dto.modelPublishFieldDTOList.stream().filter(e -> e.isPrimaryKey == 1).map(e -> e.fieldEnName).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            data.businessKeyAppend = String.join(",", collect);
        }

        BuildNifiFlowDTO buildNifiFlow = new BuildNifiFlowDTO();
        buildNifiFlow.updateSql = dto.updateSql;
        buildNifiFlow.prefixTempName = prefixTempName;

        OverLoadCodeDTO dataModel = new OverLoadCodeDTO();
        dataModel.buildNifiFlow = buildNifiFlow;
        dataModel.config = data;
        dataModel.funcName = FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL_OUTPUT.getName();
        dataModel.dataSourceType = DataSourceTypeEnum.SQLSERVER;
        dataModel.synchronousTypeEnum = SynchronousTypeEnum.PGTOPG;

        // 获取ods表字段数据
        List<TableStructDTO> odsFieldList = getColumnsName(dataSourceConfig.data.id, tableName);
        if (CollectionUtils.isEmpty(odsFieldList)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "ods表字段信息查询失败");
        }
        return getOdsSql(odsFieldList, dataModel, dataSourceConfig.data, dto);

    }

    /**
     * 拼接ods插入sql语句
     * @param odsFieldList
     * @param dataModel
     * @param data
     * @param dto
     */
    private String getOdsSql(List<TableStructDTO> odsFieldList, OverLoadCodeDTO dataModel, DataSourceDTO data, OverlayCodePreviewDTO dto) {
        log.info("获取ods插入sql参数{},{}", JSON.toJSONString(odsFieldList), dataModel);

        String tableKey = "";
        String targetTableName = dataModel.config.processorConfig.targetTableName;
        List<String> stgAndTableName = TableNameGenerateUtils.getStgAndTableName(targetTableName);
        log.info("stgAntTableName集合{}", JSON.toJSONString(stgAndTableName));
        String fieldStr = "";
        StringBuilder odsInsertSql = new StringBuilder("insert into " + targetTableName + "(");
        StringBuilder fieldBuilder = new StringBuilder();
        String selStgField = "";
        // 按照不同数据源获取key字段
        if (Objects.equals(dataModel.synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)){
            tableKey = targetTableName.substring(targetTableName.indexOf("_") + 1) + "key";
        }else{
            tableKey = stgAndTableName.get(2);
        }
        // 字段去重
        String finalTableKey = tableKey;
        odsFieldList = odsFieldList.stream().filter(item -> !item.getFieldName().equals(finalTableKey)).collect(Collectors.toList());
        // 业务主键
        if (dataModel.config.targetDsConfig.syncMode == syncModeTypeEnum.INCREMENT_MERGE.getValue()){
            return getMergeSql(odsFieldList, dataModel, tableKey, dto, stgAndTableName);
        }

        // 非业务主键
        fieldBuilder.append(tableKey).append(",");

        // 拼接ods字段
        for (TableStructDTO field : odsFieldList){
            fieldBuilder.append(field.fieldName).append(",");
        }
        fieldStr = fieldBuilder.toString();
        fieldStr = fieldStr.substring(0,fieldStr.length() - 1);
        odsInsertSql.append(fieldStr);
        odsInsertSql.append(") select ");

        // 拼接查询stg字段及字段转换
        StringBuilder selStgSql = new StringBuilder();
        selStgSql.append(tableKey);
        selStgSql.append(",");
        for (TableStructDTO item : odsFieldList){
            selStgSql.append(fieldTypeTransform(item));
            selStgSql.append(",");
        }
        selStgField = selStgSql.toString();
        selStgField = selStgField.substring(0, selStgField.length() - 1);

        // 拼接条件
        odsInsertSql.append(selStgField);
        odsInsertSql.append(" from ");
        odsInsertSql.append(stgAndTableName.get(0));
        // 业务时间覆盖拼接参数
        if (dataModel.config.targetDsConfig.syncMode == syncModeTypeEnum.TIME_INCREMENT.getValue()){
            TableBusinessDTO tbDto = dataModel.config.businessDTO;
            String whereStr = previewCoverCondition(tbDto, data);
            odsInsertSql.append(" ");
            odsInsertSql.append(whereStr);
        }
        String sql = odsInsertSql.toString();

        // 判断有无更新语句
        if (!StringUtils.isEmpty(dto.getUpdateSql())){
            sql += ";";
            sql += dto.updateSql;
        }
        log.info("非业务主键sql预览：{}", JSON.toJSONString(sql));

        return sql;

    }

    private String getMergeSql(List<TableStructDTO> odsFieldList, OverLoadCodeDTO dataModel, String tableKey, OverlayCodePreviewDTO dto, List<String> stgAndTableName) {
        String targetTableName = dataModel.config.processorConfig.targetTableName;
        String stgName = stgAndTableName.get(0);
        String mergeSql = "";
        // 1、拼接stg表
        mergeSql = "MERGE INTO " + targetTableName + " AS T USING " + stgName + " AS S ON( ";

        // 2、拼接主键关联条件
        StringBuilder pkBuilder = new StringBuilder();
        String pkSql = "";
        List<String> collect = dto.modelPublishFieldDTOList.stream().filter(e -> e.isPrimaryKey == 1).map(e -> e.fieldEnName).collect(Collectors.toList());
        for (String key : collect){
            pkBuilder.append(" AND T.");
            pkBuilder.append(key);
            pkBuilder.append("= S.");
            pkBuilder.append(key);
        }
        pkBuilder.append(")");
        pkSql = pkBuilder.toString();
        // 去掉前面的and
        pkSql = pkSql.substring(5, pkSql.length());
        pkSql = pkSql.replace(",)", ")");
        // 组合
        mergeSql += pkSql;

        // 3、拼接更新语句
        StringBuilder upBuilder = new StringBuilder(" WHEN MATCHED THEN UPDATE SET ");
        String upSql = "";
        upBuilder.append("T.");
        upBuilder.append(tableKey);
        upBuilder.append(" = S.");
        upBuilder.append(tableKey);
        upBuilder.append(",");
        for (TableStructDTO item : odsFieldList){
            upBuilder.append("T.");
            upBuilder.append(item.fieldName);
            // 类型转换
            upBuilder.append(fieldTypeTransformKey(item));
            upBuilder.append(",");
        }
        // 去除尾部的,符号
        upSql = upBuilder.toString();
        upSql = upSql.substring(0, upSql.length() - 1);
        // 组合
        mergeSql += upSql;

        // 4、拼接插入语句
        StringBuilder insBuilder = new StringBuilder(" WHEN NOT MATCHED THEN INSERT (");
        String insSql = "";
        insBuilder.append(tableKey);
        insBuilder.append(",");
        for (TableStructDTO item : odsFieldList){
            insBuilder.append(item.fieldName);
            insBuilder.append(",");
        }
        insBuilder.append(") VALUES( ");
        insBuilder.append("S.");
        insBuilder.append(tableKey);
        insBuilder.append(",");
        for (TableStructDTO item : odsFieldList){
            // 类型转换
            String str = fieldTypeTransformKey(item);
            insBuilder.append(str.replace(" = ", ""));
            insBuilder.append(",");
        }
        insBuilder.append(")");
        // 去除尾部的,符号
        insSql = insBuilder.toString();
        insSql = insSql.replace(",)", ")");
        // 组合
        mergeSql += insSql;
        mergeSql += ";";

        // 判断有无更新语句
        if (!StringUtils.isEmpty(dto.getUpdateSql())){
            mergeSql += dto.updateSql;
        }
        log.info("业务主键sql{}", mergeSql);
        return mergeSql;
    }

    private String fieldTypeTransformKey(TableStructDTO item) {
        log.info("字段名称-类型：{}-{}", item.fieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.contains("date") || item.fieldType.contains("time")){
            fieldInfo = " = DATEADD(minute, cast(left(S." + item.fieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.equals("nvarchar")){
            fieldInfo = " = CAST(S." + item.fieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = " = S." + item.fieldName;
        }
        return fieldInfo;
    }

    private String fieldTypeTransform(TableStructDTO item) {
        log.info("字段名称-类型：{}-{}", item.fieldName, item.fieldType);
        String fieldInfo = "";
        if (item.fieldType.contains("date") || item.fieldType.contains("time")){
            fieldInfo = "DATEADD(minute, cast(left(" + item.fieldName + ",10) as bigint)/60, '1970-1-1')";
        }else if (!item.fieldType.equals("nvarchar")){
            fieldInfo = "CAST(" + item.fieldName + " AS " + item.fieldType + ")";
        }else{
            fieldInfo = item.fieldName;
        }
        return fieldInfo;
    }

    private String previewCoverCondition(TableBusinessDTO dto, DataSourceDTO dataSource) {
        log.info("拼接条件{}", JSON.toJSONString(dto));
        //数据库时间
        Integer businessDate = 0;

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(dataSource.conType);

        //查询数据库时间sql
        String timeSql = command.buildQueryTimeSql(BusinessTimeEnum.getValue(dto.businessTimeFlag));
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        Connection connection = commonDbHelper.connection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword, dataSource.conType);
        businessDate = Integer.parseInt(AbstractCommonDbHelper.executeTotalSql(timeSql, connection, "tmp"));

        StringBuilder str = new StringBuilder();
        str.append("where ");
        str.append(dto.businessTimeField + " ");

        //普通模式
        if (dto.otherLogic == 1 || businessDate < dto.businessDate) {
            str.append(dto.businessOperator + " ");
            str.append("DATEADD");
            str.append("(");
            str.append(dto.rangeDateUnit);
            str.append(",");
            str.append(dto.businessRange);
            str.append(",GETDATE());");
            return str.toString();
        }
        //高级模式
        str.append(dto.businessOperatorStandby);
        str.append("DATEADD");
        str.append("(");
        str.append(dto.rangeDateUnitStandby);
        str.append(",");
        str.append(dto.businessRangeStandby);
        str.append(",GETDATE());");
        log.info("预览业务时间覆盖,where条件:{}", str);
        return str.toString();
    }

    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public List<TableStructDTO> getColumnsName(Integer targetDbId, String tableName) {
        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(targetDbId);
        DataSourceDTO data = result.getData();
        log.info("数据源连接信息：{}", JSON.toJSONString(data));
        String selOdsFieldSql = "SELECT name AS column_name,TYPE_NAME(system_type_id) AS column_type,ROW_NUMBER() OVER(ORDER BY system_type_id ) AS rid " +
                "FROM sys.columns WHERE object_id = OBJECT_ID('" + tableName + "')";
        List<TableStructDTO> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(data.conStr, data.conAccount, data.conPassword);
             Statement st = connection.createStatement();
             ResultSet res = st.executeQuery(selOdsFieldSql)){
            while (res.next()){
                TableStructDTO dto = new TableStructDTO();
                dto.setFieldName(res.getString("column_name"));
                dto.setFieldType(res.getString("column_type"));
                dto.setRid(res.getInt("rid"));
                list.add(dto);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.info("获取表字段数据错误");
        }
        log.info("字段数据{}", JSON.toJSONString(list));
        return list;
    }

    public DataSourceDTO getTargetDbInfo() {
        ResultEntity<DataSourceDTO> dataDataSource = userClient.getFiDataDataSourceById(targetDbId);
        if (dataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        return dataDataSource.data;
    }

}
