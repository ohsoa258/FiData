package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.enums.datamodel.ModelTblTypePrefixEnum;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.enums.task.FuncNameEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.accessAndModel.*;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.factorycodepreview.IBuildFactoryCodePreview;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PublishFieldDTO;
import com.fisk.common.service.factorycodepreview.impl.CodePreviewHelper;
import com.fisk.common.service.factorymodelkeyscript.IBuildFactoryModelKeyScript;
import com.fisk.common.service.factorymodelkeyscript.impl.ModelKeyScriptHelper;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.metamap.MetaMapAppDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
import com.fisk.datamodel.dto.GetConfigDTO;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessListDTO;
import com.fisk.datamodel.dto.codepreview.CodePreviewDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDataDTO;
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
import com.fisk.datamodel.entity.mainpage.DataCountVO;
import com.fisk.datamodel.entity.mainpage.DataModelCountVO;
import com.fisk.datamodel.entity.mainpage.Top5DataCount;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.BusinessAreaMap;
import com.fisk.datamodel.map.codepreview.CodePreviewMapper;
import com.fisk.datamodel.map.dimension.DimensionAttributeMap;
import com.fisk.datamodel.map.fact.FactAttributeMap;
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
import com.fisk.datamodel.utils.sql.DbConnectionHelper;
import com.fisk.datamodel.utils.sql.ModelMySqlConUtils;
import com.fisk.datamodel.utils.sql.ModelPgSqlUtils;
import com.fisk.datamodel.utils.sql.ModelSqlServerUtils;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.datamodel.vo.DimAndFactCountVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.daconfig.ProcessorConfig;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
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

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class BusinessAreaImpl extends ServiceImpl<BusinessAreaMapper, BusinessAreaPO> implements IBusinessArea {

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
            classificationInfoDto.setSourceType(ClassificationTypeEnum.ANALYZE_DATA);
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
        BusinessAreaPO po = this.query().eq("id", id).eq("del_flag", 1).one();

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
                dimensionPoQueryWrapper.notIn("id", idArray).lambda().eq(DimensionPO::getBusinessId, id);
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
                    queryWrapper1.select("id").notIn("id", folder.stream().distinct().collect(Collectors.toList())).lambda().eq(DimensionFolderPO::getBusinessId, id);
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
                classificationInfoDto.setSourceType(ClassificationTypeEnum.ANALYZE_DATA);
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
        queryWrapper.select("id").lambda().eq(DimensionFolderPO::getBusinessId, id);
        List<Long> dimensionFolderIds = (List) dimensionFolderMapper.selectObjs(queryWrapper);
        if (!CollectionUtils.isEmpty(dimensionFolderIds)) {
            //获取维度文件夹下维度表
            QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
            dimensionPoQueryWrapper.select("id").in("dimension_folder_id", dimensionFolderIds).lambda().eq(DimensionPO::getBusinessId, id);
            dimensionIds = (List) dimensionMapper.selectObjs(dimensionPoQueryWrapper);
            if (!CollectionUtils.isEmpty(dimensionIds)) {
                QueryWrapper<DimensionAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
                attributePoQueryWrapper.select("associate_dimension_id").in("associate_dimension_id", dimensionIds).notIn("dimension_id", dimensionIds);
                idArray.addAll((List) dimensionAttributeMapper.selectObjs(attributePoQueryWrapper));
            }
        }
        //查看事实表与共享维度是否存在关联
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        factPoQueryWrapper.select("id").lambda().ne(FactPO::getBusinessId, id);
        List<Long> factIds = (List) factMapper.selectObjs(factPoQueryWrapper);
        if (!CollectionUtils.isEmpty(factIds) && !CollectionUtils.isEmpty(dimensionIds)) {
            QueryWrapper<FactAttributePO> factAttributePoQueryWrapper = new QueryWrapper<>();
            factAttributePoQueryWrapper.select("associate_dimension_id").in("fact_id", factIds).in("associate_dimension_id", dimensionIds);
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
        queryWrapper.select("dimension_tab_name").lambda().eq(DimensionPO::getBusinessId, businessAreaId);
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
        //获取数仓类型
        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(dwSource);
        if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
            log.error("获取数仓在系统模块的配置失败！");
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        DataSourceTypeEnum type = result.getData().getConType();
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        BusinessPageDTO data = new BusinessPageDTO();
        data.page = query.page;
        data.where = str.toString();
        Page<BusinessPageResultDTO> page = baseMapper.queryList(query.page, data);
        List<BusinessPageResultDTO> records = page.getRecords();
        records.forEach(businessPageResultDTO -> {
            int id = (int) businessPageResultDTO.getId();
            int dimCount = dimensionImpl.getDimCountByBid(id);
            int factCount = factImpl.getFactCountByBid(id);
            int dwdCount = factImpl.getDwdCountByBid(id);
            int dwsCount = factImpl.getDwsCountByBid(id);
            //当前应用下的所有表总数
            int totalCount = dimCount + factCount + dwdCount + dwsCount;
            businessPageResultDTO.setDimCount(dimCount);
            businessPageResultDTO.setFactCount(factCount);
            businessPageResultDTO.setDwdCount(dwdCount);
            businessPageResultDTO.setDwsCount(dwsCount);
            businessPageResultDTO.setDwType(type);
        });
        return page;
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
        List<DimensionFolderPO> dimensionFolderPOList_Public = dimensionFolderImpl.query().eq("share", 1).list().stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionFolderPOList_Public)) {
            dimensionFolderPO_Public = dimensionFolderPOList_Public.get(0);
        }
        DimensionFolderPO finalDimensionFolderPO_Public = dimensionFolderPO_Public;
        final boolean[] isSetTb_Public = {false};

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = businessPoList.stream().filter(Objects::nonNull).map(business -> {
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
            List<DimensionFolderPO> dimensionFolderPOS = dimensionFolderImpl.query().eq("business_id", business.id).list().stream().filter(Objects::nonNull).collect(Collectors.toList());
            for (DimensionFolderPO dimensionFolder : dimensionFolderPOS) {
                FiDataMetaDataTreeDTO dimensionFolderTreeDto = getDimensionFolder(uuid_businessId, dimensionFolder, id);
                // 表字段信息单独再保存一份
                if (dimensionFolderTreeDto != null && !CollectionUtils.isEmpty(dimensionFolderTreeDto.getChildren())) {
                    key.addAll(dimensionFolderTreeDto.getChildren());
                }
                dimensionFolderTreeList.add(dimensionFolderTreeDto);
            }

            // 第四层 - 2: 事实文件夹
            List<FiDataMetaDataTreeDTO> businessProcessTreeList = this.businessProcessImpl.query().eq("business_id", business.id).list().stream().filter(Objects::nonNull).map(businessProcess -> {
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
                List<FiDataMetaDataTreeDTO> factTreeList = factImpl.query().eq("business_process_id", businessProcess.id).list().stream().filter(Objects::nonNull).map(fact -> {
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
                        factAttributeTreeList = atomicIndicators.getAtomicIndicator(Math.toIntExact(fact.id)).stream().filter(Objects::nonNull).map(field -> {
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
                        factAttributeTreeList = factAttributeImpl.query().eq("fact_id", fact.id).list().stream().filter(Objects::nonNull).map(field -> {
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
                List<FiDataMetaDataTreeDTO> wideTableTreeList = this.wideTableImpl.query().eq("business_id", business.id).list().stream().filter(Objects::nonNull).map(wideTable1 -> {
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
                            List<FiDataMetaDataTreeDTO> fieldList = e.columnConfig.stream().filter(Objects::nonNull).map(field -> {
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
        List<FiDataMetaDataTreeDTO> dimensionTreeList = dimensionImpl.query().eq("dimension_folder_id", dimensionFolder.id).list().stream().filter(Objects::nonNull).map(dimension -> {
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
            List<FiDataMetaDataTreeDTO> dimensionAttributeTreeList = dimensionAttribute.query().eq("dimension_id", dimension.id).list().stream().filter(Objects::nonNull).map(field -> {
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
            instance.dbList.get(0).tableList.addAll(dimensionImpl.getDimensionMetaData(item, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_DIMENSION.getValue(), item.getCreateUser()));
            instance.dbList.get(0).tableList.addAll(factImpl.getFactMetaData(item, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_FACT.getValue(), item.getCreateUser()));
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        list.add(instance);

        return list;
    }

    /**
     * 根据上次更新元数据的时间获取数据建模所有元数据
     *
     * @param lastSyncTime
     * @return
     */
    @Override
    public List<MetaDataInstanceAttributeDTO> getDataModelMetaDataByLastSyncTime(String lastSyncTime) {
        //通过做过更新的字段 先找到有哪些表的字段做过更新
        //事实表
        List<FactAttributePO> factAttributePOS = factAttributeImpl.list(new QueryWrapper<FactAttributePO>()
                .select("distinct fact_id")
                .lambda()
                .ge(FactAttributePO::getCreateTime, lastSyncTime)
                .or()
                .ge(FactAttributePO::getUpdateTime, lastSyncTime)
        );
        List<Integer> factIds = factAttributePOS.stream().map(FactAttributePO::getFactId).collect(Collectors.toList());

        //再根据这些表找到这些业务域
        LambdaQueryWrapper<FactPO> wrapper = new QueryWrapper<FactPO>()
                .select("distinct business_id")
                .lambda()
                .ge(FactPO::getCreateTime, lastSyncTime)
                .or()
                .ge(FactPO::getUpdateTime, lastSyncTime);

        if (!CollectionUtils.isEmpty(factIds)) {
            wrapper.or()
                    .in(FactPO::getId, factIds);
        }

        List<FactPO> factPOS = factImpl.list(wrapper);
        List<Integer> businessIds = factPOS.stream().map(FactPO::getBusinessId).collect(Collectors.toList());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //维度表
        List<DimensionAttributePO> dimensionAttributePOS = dimensionAttribute.list(new QueryWrapper<DimensionAttributePO>()
                .select("distinct dimension_id")
                .lambda()
                .ge(DimensionAttributePO::getCreateTime, lastSyncTime)
                .or()
                .ge(DimensionAttributePO::getUpdateTime, lastSyncTime)
        );
        List<Integer> dimIds = dimensionAttributePOS.stream().map(DimensionAttributePO::getDimensionId).collect(Collectors.toList());

        LambdaQueryWrapper<DimensionPO> wrapper2 = new QueryWrapper<DimensionPO>()
                .select("distinct business_id")
                .lambda()
                .ge(DimensionPO::getCreateTime, lastSyncTime)
                .or()
                .ge(DimensionPO::getUpdateTime, lastSyncTime);

        if (!CollectionUtils.isEmpty(dimIds)) {
            wrapper2.or()
                    .in(DimensionPO::getId, dimIds);
        }

        List<DimensionPO> dimensionPOS = dimensionImpl.list(wrapper2);
        List<Integer> dimBusinessIds = dimensionPOS.stream().map(DimensionPO::getBusinessId).collect(Collectors.toList());

        businessIds.addAll(dimBusinessIds);
        List<BusinessAreaPO> businessAreaPOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(businessIds)) {
            businessAreaPOList = this.listByIds(businessIds);
        }

        if (CollectionUtils.isEmpty(businessAreaPOList)) {
            return new ArrayList<>();
        }

        MetaDataInstanceAttributeDTO instance = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_DW.getValue());
        instance.dbList.get(0).tableList = new ArrayList<>();
        for (BusinessAreaPO item : businessAreaPOList) {
            //维度表
            if (!CollectionUtils.isEmpty(dimIds)) {
                instance.dbList.get(0).tableList.addAll(dimensionImpl.getDimensionMetaDataByLastSyncTime(item, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_DIMENSION.getValue(), item.getCreateUser(), dimIds));
            }

            //事实表
            if (!CollectionUtils.isEmpty(factIds)) {
                instance.dbList.get(0).tableList.addAll(factImpl.getFactMetaDataBySyncTime(item, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_FACT.getValue(), item.getCreateUser(), factIds));
            }
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        list.add(instance);

        return list;
    }

    /**
     * 获取数仓建模单个维度/事实表的元数据
     *
     * @return
     */
    @Override
    public List<MetaDataInstanceAttributeDTO> getDimensionMetaDataOfBatchTbl(Integer areaId, List<Integer> ids, DataModelTableTypeEnum modelTableTypeEnum) {

        LambdaQueryWrapper<BusinessAreaPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessAreaPO::getId, areaId);
        BusinessAreaPO item = getOne(wrapper);

        if (Objects.isNull(item)) {
            return new ArrayList<>();
        }

        //获取dmp_dw的数据源配置信息
        MetaDataInstanceAttributeDTO instance = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_DW.getValue());
        instance.dbList.get(0).tableList = new ArrayList<>();

        switch (modelTableTypeEnum) {
            case DW_FACT:
                //事实表
                instance.dbList.get(0).tableList.addAll(factImpl.getFactMetaDataOfBatchTbl(areaId, ids, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_FACT.getValue(), item.getCreateUser()));
                break;
            case DW_DIMENSION:
                //维度表
                instance.dbList.get(0).tableList.addAll(dimensionImpl.getDimensionMetaDataOfBatchTbl(areaId, ids, instance.dbList.get(0).qualifiedName, DataModelTableTypeEnum.DW_DIMENSION.getValue(), item.getCreateUser()));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        list.add(instance);

        return list;
    }

    /**
     * 构建维度key脚本
     *
     * @param dto
     * @return
     */
    @Override
    public Object buildDimensionKeyScript(List<TableSourceRelationsDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return "";
        }
        //获取连接类型
        DataSourceTypeEnum conType = getTargetDbInfo(targetDbId).conType;

        //获取对应连接类型的keyScriptHelper
        IBuildFactoryModelKeyScript keyScriptHelper = ModelKeyScriptHelper.getKeyScriptHelperByConType(conType);

        return keyScriptHelper.buildKeyScript(dto);
    }

    @Override
    public JSONObject dataTypeList(Integer businessId) {
        //todo 暂时不传参 方便后期可能dw存在多个
        if (businessId != 0) {
            return new JSONObject();
        }

        //不使用系统配置--平台数据源表里的第一条数据作为数据库的连接类型
//        ResultEntity<List<DataSourceDTO>> all = userClient.getAll();
//        if (all.code != ResultEnum.SUCCESS.getCode()) {
//            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
//        }
//
//        Optional<DataSourceDTO> first = all.data.stream().filter(e -> e.sourceBusinessType == SourceBusinessTypeEnum.DW).findFirst();
//        if (!first.isPresent()) {
//            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
//        }

        //改用配置文件中指定的数据源id的连接类型
        DataSourceTypeEnum conType = getTargetDbInfo(dwSource).conType;

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(conType);
        return command.dataTypeList();

    }

    /**
     * 覆盖方式代码预览
     *
     * @return
     */
    @Override
    public Object overlayCodePreview(OverlayCodePreviewDTO dto) {
        //表名
        String tableName;
        //临时表名称前缀
        String prefixTempName;
        //如果是维度表  2、事实表  1、维度表
        if (dto.type == 1) {
            //通过维度表id获取维度表
            DimensionPO po = dimensionMapper.selectById(dto.id);
            if (po == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //维度逻辑表名称
            tableName = po.dimensionTabName;
            //临时表名称前缀
            prefixTempName = po.prefixTempName;
        } else {
            //通过事实表id获取事实表
            FactPO factPO = factMapper.selectById(dto.id);
            if (factPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //事实逻辑表名称
            tableName = factPO.factTabName;
            //临时表名称前缀
            prefixTempName = factPO.prefixTempName;
        }

        DataAccessConfigDTO data = new DataAccessConfigDTO();

        ProcessorConfig processorConfig = new ProcessorConfig();
        processorConfig.targetTableName = tableName;
        data.processorConfig = processorConfig;

        DataSourceConfig targetDsConfig = new DataSourceConfig();
        targetDsConfig.syncMode = dto.syncMode;
        data.targetDsConfig = targetDsConfig;

//        2023-04-24李世纪注释掉。页面业务时间覆盖时，选项一的otherLogic是1，选项二的otherLogic是2
//        data.businessDTO = dto.tableBusiness == null ? new TableBusinessDTO() : dto.tableBusiness;
//        data.businessDTO.otherLogic = 1;
//        if (dto.syncMode == 4) {
//            data.businessDTO.otherLogic = 2;
//        }

        data.modelPublishFieldDTOList = dto.modelPublishFieldDTOList;

        //e.isPrimaryKey 主键
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
        //获取连接类型
        dataModel.dataSourceType = getTargetDbInfo(targetDbId).conType;

        //2023-04-21李世纪注释掉   ：下面是生成存储过程的数仓建模sql预览
//        ResultEntity<Object> objectResultEntity = publishTaskClient.overlayCodePreview(dataModel);
//
//        if (objectResultEntity.code != ResultEnum.SUCCESS.getCode()) {
//            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
//        }

        /*
        l's'j修改... 下面是生成sql语句的数仓建模sql预览
         */
        CodePreviewDTO codePreviewDTO = new CodePreviewDTO();
        codePreviewDTO.setOverLoadCodeDTO(dataModel);
        codePreviewDTO.setOverlayCodePreviewDTO(dto);
        String finalSql = codePreviewBySyncMode(codePreviewDTO, dto.type, dto.updateSql);

        //doris是否开启严格模式 0否  1是
        if (dto.dorisIfOpenStrictMode != null) {
            if (dto.dorisIfOpenStrictMode == 1) {
                finalSql = " SET enable_insert_strict = true; " + finalSql;
            } else {
                finalSql = " SET enable_insert_strict = false; " + finalSql;
            }
        }

        //检测获取到的sql预览结果
        log.info("预返回的覆盖方式预览sql为" + finalSql);

        /*Connection conn = null;
        Statement stat = null;
        try {
            conn = dataSourceConfigUtil.getConnection();
            stat = conn.createStatement();

            stat.executeQuery(objectResultEntity.data.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        //2023-04-21李世纪注释掉
//        return objectResultEntity.data;

        //返回最终拼接好的sql
        return finalSql;
    }

    /**
     * 数仓建模首页--获取总共的维度表和事实表--不包含公共域维度
     *
     * @return
     */
    @Override
    public DimAndFactCountVO getTotalDimAndFactCount() {
        int factTotalCount = factImpl.getFactTotalCount();
        int dimTotalCount = dimensionImpl.getDimTotalCount();
        int publicDimTotalCount = dimensionImpl.getPublicDimTotalCount();
        DimAndFactCountVO dimAndFactCountVO = new DimAndFactCountVO();
        dimAndFactCountVO.setDimCount(dimTotalCount);
        dimAndFactCountVO.setFactCount(factTotalCount);
        dimAndFactCountVO.setPublicDimCount(publicDimTotalCount);
        return dimAndFactCountVO;
    }

    /**
     * 获取数仓建模所有业务域和业务域下的所有表（包含事实表和维度表和应用下建的公共域维度表）
     *
     * @return
     */
    @Override
    public List<AccessAndModelAppDTO> getAllAreaAndTables() {

        //先查询所有业务域
        QueryWrapper<BusinessAreaPO> w = new QueryWrapper<>();
        w.select("id", "business_name");
        List<BusinessAreaPO> businessAreaPOS = this.list(w);
        List<AccessAndModelAppDTO> areaList = new ArrayList<>();

        for (BusinessAreaPO businessAreaPO : businessAreaPOS) {
            AccessAndModelAppDTO accessAndModelAppDTO = new AccessAndModelAppDTO();
            accessAndModelAppDTO.setAppId((int) businessAreaPO.getId());
            accessAndModelAppDTO.setAppName(businessAreaPO.getBusinessName());
            accessAndModelAppDTO.setServerType(ServerTypeEnum.MODEL.getValue());

            List<AccessAndModelTableDTO> accessAndModelTableDTOS = new ArrayList<>();

            //获取获取业务域下的所有维度表
            LambdaQueryWrapper<DimensionPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.select(DimensionPO::getDimensionTabName, DimensionPO::getId, DimensionPO::getDimensionCnName).eq(DimensionPO::getBusinessId, businessAreaPO.getId());
            List<DimensionPO> dimensionPOS = dimensionImpl.list(wrapper1);
            for (DimensionPO dimensionPO : dimensionPOS) {
                AccessAndModelTableDTO dimTable = new AccessAndModelTableDTO();
                dimTable.setTblId((int) dimensionPO.getId());
                dimTable.setTableName(dimensionPO.getDimensionTabName());
                dimTable.setDisplayTableName(dimensionPO.getDimensionCnName());
                dimTable.setTableType(AccessAndModelTableTypeEnum.DIMENSION.getValue());
                accessAndModelTableDTOS.add(dimTable);
            }

            //获取业务域下的所有事实表
            LambdaQueryWrapper<FactPO> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.select(FactPO::getFactTabName, FactPO::getId, FactPO::getFactTableCnName).eq(FactPO::getBusinessId, businessAreaPO.getId())
                    //数据表处理方式是批处理或流处理  0批处理 1流处理
                    .eq(FactPO::getBatchOrStream, 0);
            List<FactPO> factPOS = factImpl.list(wrapper2);
            for (FactPO factPO : factPOS) {
                AccessAndModelTableDTO factTable = new AccessAndModelTableDTO();
                factTable.setTblId((int) factPO.getId());
                factTable.setTableName(factPO.getFactTabName());
                factTable.setDisplayTableName(factPO.getFactTableCnName());
                factTable.setTableType(AccessAndModelTableTypeEnum.FACT.getValue());
                accessAndModelTableDTOS.add(factTable);
            }

            accessAndModelAppDTO.setTables(accessAndModelTableDTOS);
            areaList.add(accessAndModelAppDTO);
        }

        return areaList;
    }

    /**
     * 为数仓etl树获取数仓建模所有业务域和业务域下的所有表
     *
     * @return
     */
    @Override
    public List<AccessAndModelAppDTO> getAllAreaAndTablesForEtlTree() {
        //查询所有维度表字段 id 英文名 类型
        LambdaQueryWrapper<DimensionAttributePO> dimW = new LambdaQueryWrapper<>();
        dimW.select(DimensionAttributePO::getId, DimensionAttributePO::getDimensionFieldEnName, DimensionAttributePO::getDimensionFieldType, DimensionAttributePO::getDimensionId);
        List<DimensionAttributePO> dimFields = dimensionAttribute.list(dimW);
        List<DimensionAttributeDTO> dimFieldList = DimensionAttributeMap.INSTANCES.poListToDtoList(dimFields);

        //查询所有事实表字段 id 英文名 类型
        LambdaQueryWrapper<FactAttributePO> factW = new LambdaQueryWrapper<>();
        factW.select(FactAttributePO::getId, FactAttributePO::getFactFieldEnName, FactAttributePO::getFactFieldType, FactAttributePO::getFactId);
        List<FactAttributePO> factFields = factAttributeImpl.list(factW);
        List<FactAttributeDataDTO> factFieldList = FactAttributeMap.INSTANCES.poListToDtoList(factFields);

        //先查询所有业务域
        QueryWrapper<BusinessAreaPO> w = new QueryWrapper<>();
        w.select("id", "business_name");
        List<BusinessAreaPO> businessAreaPOS = this.list(w);
        List<AccessAndModelAppDTO> areaList = new ArrayList<>();

        for (BusinessAreaPO businessAreaPO : businessAreaPOS) {
            AccessAndModelAppDTO accessAndModelAppDTO = new AccessAndModelAppDTO();
            accessAndModelAppDTO.setAppId((int) businessAreaPO.getId());
            accessAndModelAppDTO.setAppName(businessAreaPO.getBusinessName());
            accessAndModelAppDTO.setServerType(ServerTypeEnum.MODEL.getValue());

            List<AccessAndModelTableDTO> accessAndModelTableDTOS = new ArrayList<>();

            //获取获取业务域下的所有维度表
            LambdaQueryWrapper<DimensionPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.select(DimensionPO::getDimensionTabName, DimensionPO::getId, DimensionPO::getDimensionCnName)
                    .eq(DimensionPO::getBusinessId, businessAreaPO.getId());
            List<DimensionPO> dimensionPOS = dimensionImpl.list(wrapper1);
            for (DimensionPO dimensionPO : dimensionPOS) {
                AccessAndModelTableDTO dimTable = new AccessAndModelTableDTO();
                dimTable.setTblId((int) dimensionPO.getId());
                dimTable.setTableName(dimensionPO.getDimensionTabName());
                //显示名称暂时不要
//                dimTable.setDisplayTableName(dimensionPO.getDimensionCnName());
                //表类型也暂时不要
//                dimTable.setTableType(AccessAndModelTableTypeEnum.DIMENSION.getValue());

                //获取该维度表的字段
                List<DimensionAttributeDTO> myFileds = dimFieldList.stream().filter(dto -> dto.getDimensionId() == dimensionPO.getId()).collect(Collectors.toList());
                List<AccessAndModelFieldDTO> accessAndModelFieldDTOS = new ArrayList<>();
                for (DimensionAttributeDTO myFiled : myFileds) {
                    AccessAndModelFieldDTO accessAndModelFieldDTO = new AccessAndModelFieldDTO();
                    accessAndModelFieldDTO.setId(myFiled.id);
                    accessAndModelFieldDTO.setFieldEnName(myFiled.dimensionFieldEnName);
                    accessAndModelFieldDTO.setFieldType(myFiled.dimensionFieldType);
                    accessAndModelFieldDTOS.add(accessAndModelFieldDTO);
                }
                dimTable.setTblFields(accessAndModelFieldDTOS);
                accessAndModelTableDTOS.add(dimTable);
            }

            //获取业务域下的所有事实表
            LambdaQueryWrapper<FactPO> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.select(FactPO::getFactTabName, FactPO::getId, FactPO::getFactTableCnName).eq(FactPO::getBusinessId, businessAreaPO.getId())
                    //数据表处理方式是批处理或流处理  0批处理 1流处理
                    .eq(FactPO::getBatchOrStream, 0);
            List<FactPO> factPOS = factImpl.list(wrapper2);
            for (FactPO factPO : factPOS) {
                AccessAndModelTableDTO factTable = new AccessAndModelTableDTO();
                factTable.setTblId((int) factPO.getId());
                factTable.setTableName(factPO.getFactTabName());
                //显示名称暂时不要
//                factTable.setDisplayTableName(factPO.getFactTableCnName());
                //表类型也暂时不要
//                factTable.setTableType(AccessAndModelTableTypeEnum.FACT.getValue());
                //获取该事实表的字段
                List<FactAttributeDataDTO> myFileds = factFieldList.stream().filter(dto -> dto.getFactId() == factPO.getId()).collect(Collectors.toList());
                List<AccessAndModelFieldDTO> accessAndModelFieldDTOS = new ArrayList<>();
                for (FactAttributeDataDTO myFiled : myFileds) {
                    AccessAndModelFieldDTO accessAndModelFieldDTO = new AccessAndModelFieldDTO();
                    accessAndModelFieldDTO.setId(myFiled.id);
                    accessAndModelFieldDTO.setFieldEnName(myFiled.factFieldEnName);
                    accessAndModelFieldDTO.setFieldType(myFiled.factFieldType);
                    accessAndModelFieldDTOS.add(accessAndModelFieldDTO);
                }
                factTable.setTblFields(accessAndModelFieldDTOS);
                accessAndModelTableDTOS.add(factTable);
            }

            accessAndModelAppDTO.setTables(accessAndModelTableDTOS);
            areaList.add(accessAndModelAppDTO);
        }

        return areaList;
    }

    /**
     * 数据建模覆盖方式预览sql
     *
     * @param dto
     * @return
     * @author lishiji
     */
    private String codePreviewBySyncMode(CodePreviewDTO dto, Integer type, String updateSql) {
        //分别获取参数dto
        OverLoadCodeDTO overLoadCodeDTO = dto.overLoadCodeDTO;
        OverlayCodePreviewDTO originalDTO = dto.overlayCodePreviewDTO;
        DataAccessConfigDTO configDTO = overLoadCodeDTO.config;
        BuildNifiFlowDTO buildNifiFlow = overLoadCodeDTO.buildNifiFlow;
        //获取业务时间覆盖所需的逻辑
        TableBusinessDTO tableBusiness = originalDTO.tableBusiness;
        //TableBusinessDTO ==> PreviewTableBusinessDTO
        PreviewTableBusinessDTO previewTableBusinessDTO = CodePreviewMapper.INSTANCES.dtoToDto(tableBusiness);

        //获取数据源类型
        DataSourceTypeEnum sourceType = overLoadCodeDTO.dataSourceType;
        //获取同步方式
        int syncMode = configDTO.targetDsConfig.syncMode;
        String tableName = "";
        String tempTableName = "";

        //根据数据库的不同连接类型，获取不同的表名格式
        if (sourceType.getValue() == DataSourceTypeEnum.SQLSERVER.getValue()) {
            //获取表名
            tableName = "[" + configDTO.processorConfig.targetTableName + "]";
            String tableName1 = configDTO.processorConfig.targetTableName;
            //获取临时表前缀
            String prefixTempName = buildNifiFlow.prefixTempName;
            //拼接临时表名称
            tempTableName = "[" + prefixTempName + "_" + tableName1 + "]";
        } else if (sourceType.getValue() == DataSourceTypeEnum.POSTGRESQL.getValue()) {
            //获取表名
            tableName = "\"" + configDTO.processorConfig.targetTableName + "\"";
            String tableName1 = configDTO.processorConfig.targetTableName;
            //获取临时表前缀
            String prefixTempName = buildNifiFlow.prefixTempName;
            //拼接临时表名称
            tempTableName = "\"" + prefixTempName + "_" + tableName1 + "\"";
        } else if (sourceType.getValue() == DataSourceTypeEnum.DORIS.getValue()) {
            //获取表名
            tableName = "`" + configDTO.processorConfig.targetTableName + "`";
            String tableName1 = configDTO.processorConfig.targetTableName;
            //获取临时表前缀
            String prefixTempName = buildNifiFlow.prefixTempName;
            //拼接临时表名称
            tempTableName = "`" + prefixTempName + "_" + tableName1 + "`";
        }

        //获取前端传递的表字段集合
        List<ModelPublishFieldDTO> fields = originalDTO.modelPublishFieldDTOList;
        //ModelPublishFieldDTO List ==> PublishFieldDTO List
        List<PublishFieldDTO> fieldList = CodePreviewMapper.INSTANCES.listToList(fields);
        //获取集合大小（字段数量）
        int size = fieldList.size();

        //根据数据库的不同连接类型，获取不同的sqlHelper实现类
        IBuildFactoryCodePreview sqlHelper = CodePreviewHelper.getSqlHelperByConType(sourceType);

        if (fields.isEmpty()) {
            return "请检查字段映射...";
        }

        //根据覆盖方式决定返回的sql
        switch (syncMode) {
            //如果是0的话，不拼接任何sql
            case 0:
                return "'请选择覆盖方式...'";
            //全量
            case 1:
                //调用封装的全量覆盖方式拼接sql方法并返回
                return sqlHelper.fullVolumeSql(tableName, tempTableName, fieldList, updateSql);
            //追加
            case 2:
                //调用封装的追加覆盖方式拼接sql方法并返回
                return sqlHelper.insertAndSelectSql(tableName, tempTableName, fieldList, updateSql);
            //业务标识覆盖（业务主键覆盖）---merge覆盖
            case 3:
                //调用封装的业务标识覆盖方式--merge覆盖(业务标识可以作为业务主键)拼接sql方法并返回
                return sqlHelper.merge(tableName, tempTableName, fieldList, type, updateSql);
            //业务时间覆盖
            case 4:
                //调用封装的业务时间覆盖方式的拼接sql方法并返回
                return sqlHelper.businessTimeOverLay(tableName, tempTableName, fieldList, previewTableBusinessDTO, updateSql);
            //业务标识覆盖（业务主键覆盖）--- delete insert 删除插入
            case 5:
                //调用封装的业务标识覆盖方式--删除插入(按照业务主键删除，再重新插入)拼接sql方法并返回
                return sqlHelper.delAndInsert(tableName, tempTableName, fieldList, type, updateSql);
            case 6:
                //调用封装的业务标识覆盖方式--merge覆盖(业务标识可以作为业务主键)拼接sql方法并返回
                return sqlHelper.mergeWithMark(tableName, tempTableName, fieldList, type, updateSql);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
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

    /**
     * 获取当前业务域的首页计数信息
     *
     * @param areaId
     * @return
     */
    @Override
    public DataModelCountVO mainPageCount(Integer areaId) {
        DataModelCountVO dataModelCountVO = new DataModelCountVO();
        try {
            //获取当前业务域下的维度文件夹个数
            LambdaQueryWrapper<DimensionFolderPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DimensionFolderPO::getBusinessId, areaId);
            int dimFolderCount = dimensionFolderImpl.count(wrapper);

            //先获取当前业务域下的所有维度表名
            LambdaQueryWrapper<DimensionPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.select(DimensionPO::getDimensionTabName)
                    .eq(DimensionPO::getBusinessId, areaId)
                    .eq(DimensionPO::getIsPublish, 1);
            List<DimensionPO> dimList = dimensionImpl.list(wrapper1);

            //维度表个数
            int dimCount = dimList.size();

            //业务文件夹个数
            LambdaQueryWrapper<BusinessProcessPO> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(BusinessProcessPO::getBusinessId, areaId);
            int businessProcessCount = businessProcessImpl.count(wrapper2);

            //事实表个数
            int factCount = getCountByPrefix(areaId, ModelTblTypePrefixEnum.FACT.getName());
            //帮助表个数
            int helpCount = getCountByPrefix(areaId, ModelTblTypePrefixEnum.HELP.getName());
            //配置表个数
            int configCount = getCountByPrefix(areaId, ModelTblTypePrefixEnum.CONFIG.getName());
            //DWD个数
            int dwdCount = getCountByPrefix(areaId, ModelTblTypePrefixEnum.DWD.getName());
            //DWS个数
            int dwsCount = getCountByPrefix(areaId, ModelTblTypePrefixEnum.DWS.getName());

            //获取当前业务域下的所有非维度表的表名 fact help config dwd dws
            LambdaQueryWrapper<FactPO> wrapper3 = new LambdaQueryWrapper<>();
            wrapper3.select(FactPO::getFactTabName)
                    .eq(FactPO::getBusinessId, areaId)
                    .eq(FactPO::getIsPublish, 1);
            List<FactPO> factList = factImpl.list(wrapper3);

            //获取dmp_dw的类型 sqlserver / pg / doris
            ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(targetDbId);
            if (datasource.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            DataSourceDTO data = datasource.getData();
            DataSourceTypeEnum conType = data.getConType();


            //开始获取当前业务域下的六种类型的表在dmp_dw下的数据量 以及 各自的top5数据量
            DataModelCountVO countVO = dataCount(conType, data, dimList, factList);

            dataModelCountVO.setDimFolderCount(dimFolderCount);
            dataModelCountVO.setDimCount(dimCount);
            dataModelCountVO.setBusinessProcessCount(businessProcessCount);
            dataModelCountVO.setFactCount(factCount);
            dataModelCountVO.setHelpCount(helpCount);
            dataModelCountVO.setConfigCount(configCount);
            dataModelCountVO.setDwdCount(dwdCount);
            dataModelCountVO.setDwsCount(dwsCount);
            if (countVO.getDataCountVO() != null) {
                dataModelCountVO.setDataCountVO(countVO.getDataCountVO());
            } else {
                dataModelCountVO.setDataCountVO(new DataCountVO());
            }

            if (countVO.getTop5DataCount() != null) {
                dataModelCountVO.setTop5DataCount(countVO.getTop5DataCount());
            } else {
                dataModelCountVO.setTop5DataCount(new Top5DataCount());
            }


            return dataModelCountVO;
        } catch (Exception e) {
            log.error("获取dw主页数据量失败：" + e);
            throw new FkException(ResultEnum.MODEL_MAIN_PAGE_COUNT_ERROR);
        }
    }

    /**
     * 获取数仓建模所有业务域和业务域下文件夹
     *
     * @return
     */
    @Override
    public List<ModelAreaAndFolderDTO> getAllAreaAndFolder() {
        LambdaQueryWrapper<BusinessAreaPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BusinessAreaPO::getId, BusinessAreaPO::getBusinessName);
        //获取除当前业务域外的其他所有业务域
        List<BusinessAreaPO> list = list(wrapper);

        List<ModelAreaAndFolderDTO> dtos = new ArrayList<>();

        //分别获取业务域下的维度和事实文件夹信息
        for (BusinessAreaPO businessAreaPO : list) {
            ModelAreaAndFolderDTO dto = new ModelAreaAndFolderDTO();
            dto.setId(Math.toIntExact(businessAreaPO.getId()));
            dto.setName(businessAreaPO.getBusinessName());

            List<FolderDTO> folderDTOList = new ArrayList<>();

            //获取当前业务域下的所有维度文件夹
            LambdaQueryWrapper<DimensionFolderPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.select(DimensionFolderPO::getId, DimensionFolderPO::getDimensionFolderCnName)
                    .eq(DimensionFolderPO::getBusinessId, businessAreaPO.getId());
            List<DimensionFolderPO> folderPOS = dimensionFolderImpl.list(wrapper1);
            for (DimensionFolderPO folderPO : folderPOS) {
                FolderDTO folderDTO = new FolderDTO();
                folderDTO.setId((int) folderPO.getId());
                folderDTO.setName(folderPO.getDimensionFolderCnName());
                folderDTO.setType(0);
                folderDTOList.add(folderDTO);
            }

            //获取当前业务域下的所有事实文件夹
            LambdaQueryWrapper<BusinessProcessPO> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.select(BusinessProcessPO::getId, BusinessProcessPO::getBusinessProcessCnName)
                    .eq(BusinessProcessPO::getBusinessId, businessAreaPO.getId());
            List<BusinessProcessPO> processPOS = businessProcessImpl.list(wrapper2);
            for (BusinessProcessPO processPO : processPOS) {
                FolderDTO folderDTO = new FolderDTO();
                folderDTO.setId((int) processPO.getId());
                folderDTO.setName(processPO.getBusinessProcessCnName());
                folderDTO.setType(1);
                folderDTOList.add(folderDTO);
            }
            dto.setChildren(folderDTOList);
            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public List<TableNameDTO> getTableDataStructure(FiDataMetaDataReqDTO dto) {
        List<TableNameDTO> tableNames = null;
        if ("1".equalsIgnoreCase(dto.dataSourceId)) {
            tableNames = buildTableNames(TableBusinessTypeEnum.DW_FACT);
        } else if ("4".equalsIgnoreCase(dto.dataSourceId)) {
            tableNames = buildTableNames(TableBusinessTypeEnum.DORIS_FACT);
        }
        return tableNames;
    }

    @Override
    public List<TableColumnDTO> getFieldDataStructure(ColumnQueryDTO dto) {
        List<TableColumnDTO> tableColumnDTOS = new ArrayList<>();
        switch (dto.tableBusinessTypeEnum) {
            case DW_DIMENSION:
                tableColumnDTOS = dimensionAttribute.query().eq("dimension_id", dto.tableId).list().stream().filter(Objects::nonNull).map(field -> {
                    TableColumnDTO tableColumnDTO = new TableColumnDTO();
                    tableColumnDTO.setFieldId(String.valueOf(field.id));
                    tableColumnDTO.setFieldName(field.dimensionFieldEnName);
                    tableColumnDTO.setFieldType(field.dimensionFieldType);
                    tableColumnDTO.setFieldLength(field.dimensionFieldLength);
                    tableColumnDTO.setFieldDes(field.dimensionFieldDes);
                    return tableColumnDTO;
                }).collect(Collectors.toList());
                break;
            case DW_FACT:
                tableColumnDTOS = factAttributeImpl.query().eq("fact_id", dto.tableId).list().stream().filter(Objects::nonNull).map(field -> {

                    TableColumnDTO tableColumnDTO = new TableColumnDTO();
                    tableColumnDTO.setFieldId(String.valueOf(field.id));
                    tableColumnDTO.setFieldName(field.factFieldEnName);
                    tableColumnDTO.setFieldType(field.factFieldType);
                    tableColumnDTO.setFieldLength(field.factFieldLength);
                    tableColumnDTO.setFieldDes(field.factFieldDes);
                    return tableColumnDTO;
                }).collect(Collectors.toList());
                break;
            case DORIS_FACT:
                tableColumnDTOS = atomicIndicators.getAtomicIndicator(Integer.parseInt(dto.tableId)).stream().filter(Objects::nonNull).map(field -> {
                    TableColumnDTO tableColumnDTO = new TableColumnDTO();
                    tableColumnDTO.setFieldId(String.valueOf(field.id));
                    tableColumnDTO.setFieldName(field.factFieldName);
                    tableColumnDTO.setFieldType(field.factFieldType);
                    tableColumnDTO.setFieldLength(field.factFieldLength);
                    return tableColumnDTO;
                }).collect(Collectors.toList());
                break;

            case WIDE_TABLE:
                break;
            case DORIS_DIMENSION:
                break;
        }
        return tableColumnDTOS;
    }

    @Override
    public List<BusinessAreaDTO> getBusinessAreaByIds(List<Integer> ids) {
        return BusinessAreaMap.INSTANCES.poListToDtoList(baseMapper.selectBatchIds(ids));
    }

    /**
     * 获取元数据地图 数仓建模
     */
    @Override
    public List<MetaMapDTO> modelGetMetaMap() {
        List<MetaMapDTO> metaMapDTOS = new ArrayList<>();

        List<BusinessAreaPO> list = this.list();
        for (BusinessAreaPO businessAreaPO : list) {
            MetaMapDTO metaMapDTO = new MetaMapDTO();
            metaMapDTO.setDbOrAreaId((int) businessAreaPO.getId());
            metaMapDTO.setDbOrAreaName(businessAreaPO.getBusinessName());

            List<MetaMapAppDTO> porcessList = new ArrayList<>();

            //获取当前业务域下的维度文件夹
            List<DimensionFolderDataDTO> dimensionFolderList = dimensionFolderImpl.getDimensionFolderList(Math.toIntExact(businessAreaPO.getId()));
            for (DimensionFolderDataDTO dimensionFolderDataDTO : dimensionFolderList) {
                MetaMapAppDTO metaMapAppDTO = new MetaMapAppDTO();
                metaMapAppDTO.setAppOrProcessId((int) dimensionFolderDataDTO.getId());
                metaMapAppDTO.setAppOrProcessName(dimensionFolderDataDTO.getDimensionFolderCnName());
                metaMapAppDTO.setType(1);
                porcessList.add(metaMapAppDTO);
            }

            //获取当前业务域下的业务过程
            List<BusinessProcessListDTO> businessProcessList = businessProcessImpl.getBusinessProcessList((int) businessAreaPO.getId());
            for (BusinessProcessListDTO businessProcessListDTO : businessProcessList) {
                MetaMapAppDTO metaMapAppDTO = new MetaMapAppDTO();
                metaMapAppDTO.setAppOrProcessId((int) businessProcessListDTO.getId());
                metaMapAppDTO.setAppOrProcessName(businessProcessListDTO.getBusinessProcessCnName());
                metaMapAppDTO.setType(2);
                porcessList.add(metaMapAppDTO);
            }
            metaMapDTO.setAppOrPorcessList(porcessList);
            metaMapDTOS.add(metaMapDTO);
        }
        return metaMapDTOS;
    }

    /**
     * 元数据地图 获取业务过程下的表
     *
     * @param processId   业务过程id或维度文件夹id
     * @param processType 类型 1维度文件夹 2业务过程
     * @return
     */
    @Override
    public List<MetaMapTblDTO> modelGetMetaMapTableDetail(Integer processId, Integer processType) {
        List<MetaMapTblDTO> metaMapTblDTOS = new ArrayList<>();
        ResultEntity<List<UserDTO>> resultEntity = userClient.getAllUserList();
        List<Long> userIds = new ArrayList<>();
        List<UserDTO> data = new ArrayList<>();
        if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
            data = resultEntity.getData();
            userIds = data.stream().map(UserDTO::getId).collect(Collectors.toList());
        }

        if (processType == 1) {
            List<DimensionPO> list = dimensionImpl.list(new LambdaQueryWrapper<DimensionPO>().eq(DimensionPO::getDimensionFolderId, processId));
            for (DimensionPO dimensionPO : list) {
                MetaMapTblDTO metaMapTblDTO = new MetaMapTblDTO();
                metaMapTblDTO.setTblId((int) dimensionPO.getId());
                metaMapTblDTO.setTblName(dimensionPO.getDimensionTabName());
                //获取创建人id
                String createUserId = dimensionPO.getCreateUser();
                metaMapTblDTO.setCreateUser(createUserId);
                metaMapTblDTO.setDisplayName(dimensionPO.getDimensionCnName());

                //获取创建人名称
                if (!CollectionUtils.isEmpty(userIds) && !CollectionUtils.isEmpty(data) && createUserId != null) {
                    if (userIds.contains(Long.valueOf(createUserId))) {
                        data.stream().filter(user -> user.getId().equals(Long.valueOf(createUserId)))
                                .findFirst()
                                .ifPresent(userDTO -> metaMapTblDTO.setCreateUserName(userDTO.getUsername()));
                    }
                }
                metaMapTblDTO.setCreateTime(dimensionPO.getCreateTime());
                metaMapTblDTOS.add(metaMapTblDTO);
            }
            return metaMapTblDTOS;
        } else if (processType == 2) {
            List<FactPO> list = factImpl.list(new LambdaQueryWrapper<FactPO>().eq(FactPO::getBusinessProcessId, processId));
            for (FactPO FactPO : list) {
                MetaMapTblDTO metaMapTblDTO = new MetaMapTblDTO();
                metaMapTblDTO.setTblId((int) FactPO.getId());
                metaMapTblDTO.setTblName(FactPO.getFactTabName());
                //获取创建人id
                String createUserId = FactPO.getCreateUser();
                metaMapTblDTO.setCreateUser(createUserId);
                metaMapTblDTO.setDisplayName(FactPO.getFactTableCnName());
                //获取创建人名称
                if (!CollectionUtils.isEmpty(userIds) && !CollectionUtils.isEmpty(data) && createUserId != null) {
                    if (userIds.contains(Long.valueOf(createUserId))) {
                        data.stream().filter(user -> user.getId().equals(Long.valueOf(createUserId)))
                                .findFirst()
                                .ifPresent(userDTO -> metaMapTblDTO.setCreateUserName(userDTO.getUsername()));
                    }
                }
                metaMapTblDTO.setCreateTime(FactPO.getCreateTime());
                metaMapTblDTOS.add(metaMapTblDTO);
            }
            return metaMapTblDTOS;
        } else {
            return null;
        }
    }

    @Override
    public Integer getBusinessTotal() {
        return mapper.getBusinessTotal();
    }

    @Override
    public Integer getBusinessTableTotal() {
        int total = 0;
        total += dimensionMapper.getTableTotal();
        total += factMapper.getTableTotal();
        return total;
    }

    private List<TableNameDTO> buildTableNames(TableBusinessTypeEnum tableBusinessTypeEnum) {

        // 建模暂时没有schema的设置
        List<BusinessAreaPO> businessPoList = this.query().orderByDesc("create_time").list();

        // 维度文件夹（公共域维度）
        DimensionFolderPO dimensionFolderPO_Public = null;
        List<DimensionFolderPO> dimensionFolderPOList_Public = dimensionFolderImpl.query().eq("share", 1).list().stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionFolderPOList_Public)) {
            dimensionFolderPO_Public = dimensionFolderPOList_Public.get(0);
        }
        DimensionFolderPO finalDimensionFolderPO_Public = dimensionFolderPO_Public;
        List<TableNameDTO> dimension_folder_public_table = dimensionImpl.query().eq("dimension_folder_id", finalDimensionFolderPO_Public.id).list().stream().filter(Objects::nonNull).map(dimension -> {
            TableNameDTO tableNameDTO = new TableNameDTO();
            tableNameDTO.setTableId(String.valueOf(dimension.id));
            tableNameDTO.setTableName(dimension.dimensionTabName);
            tableNameDTO.setSchemaName(dimension.dimensionTabName);
            tableNameDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.DW_DIMENSION);
            return tableNameDTO;
        }).collect(Collectors.toList());
        List<TableNameDTO> tableNameDTOList = new ArrayList<>();
        tableNameDTOList.addAll(dimension_folder_public_table);
        List<BusinessAreaPO> businessAreaPOS = businessPoList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> businessAreaIds = businessAreaPOS.stream().map(i -> i.getId()).collect(Collectors.toList());
        // 1: 维度表（当前域维度）
        List<DimensionFolderPO> dimensionFolderPOS = dimensionFolderImpl.query().in("business_id", businessAreaIds).list().stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> dimensionFolderIds = dimensionFolderPOS.stream().map(BasePO::getId).collect(Collectors.toList());
        List<TableNameDTO> dimension_folder_table = dimensionImpl.query().in("dimension_folder_id", dimensionFolderIds).list().stream().filter(Objects::nonNull).map(dimension -> {
            TableNameDTO tableNameDTO = new TableNameDTO();
            tableNameDTO.setTableId(String.valueOf(dimension.id));
            tableNameDTO.setTableName(dimension.dimensionTabName);
            tableNameDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.DW_DIMENSION);
            return tableNameDTO;
        }).collect(Collectors.toList());
        tableNameDTOList.addAll(dimension_folder_table);
        // 2: 事实表
        List<BusinessProcessPO> businessProcess = this.businessProcessImpl.query().in("business_id", businessAreaIds).list().stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> businessProcessIds = businessProcess.stream().map(BasePO::getId).collect(Collectors.toList());
        // 事实表
        List<TableNameDTO> factTableNameList = factImpl.query().in("business_process_id", businessProcessIds).list().stream().filter(Objects::nonNull).map(fact -> {
            TableNameDTO tableNameDTO = new TableNameDTO();
            tableNameDTO.setTableId(String.valueOf(fact.id));
            tableNameDTO.setTableName(fact.factTabName);
            tableNameDTO.setTableBusinessTypeEnum(tableBusinessTypeEnum);
            return tableNameDTO;
        }).collect(Collectors.toList());
        tableNameDTOList.addAll(factTableNameList);

        // 3: 宽表
        List<TableNameDTO> wideTableTreeList = this.wideTableImpl.query().in("business_id", businessAreaIds).list().stream().filter(Objects::nonNull).map(wideTable1 -> {
            TableNameDTO tableNameDTO = new TableNameDTO();
            tableNameDTO.setTableId(String.valueOf(wideTable1.id));
            tableNameDTO.setTableName(wideTable1.name);
            tableNameDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.WIDE_TABLE);
            return tableNameDTO;
        }).collect(Collectors.toList());
        tableNameDTOList.addAll(wideTableTreeList);
        return tableNameDTOList;
    }

    /**
     * 获取数据量
     *
     * @param conType  dw库类型
     * @param data     dw配置参数
     * @param dimList  维度表名称集合
     * @param factList 事实表名称集合
     * @return
     */
    private DataModelCountVO dataCount(DataSourceTypeEnum conType, DataSourceDTO data, List<DimensionPO> dimList, List<FactPO> factList) {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String dimSql = null;
        String factSql = null;
        String finalSql = null;
        DataModelCountVO countVO = new DataModelCountVO();
        DataCountVO dataCountVO = new DataCountVO();
        Top5DataCount top5DataCount = new Top5DataCount();

        try {
            //根据连接类型获取con对象
            connection = DbConnectionHelper.connection(data.conStr, data.conAccount, data.conPassword, conType);

            switch (conType) {
                case SQLSERVER:
                    if (!CollectionUtils.isEmpty(dimList)) {
                        dimSql = ModelSqlServerUtils.buildDataModelDimCountSql(dimList);
                    }
                    if (!CollectionUtils.isEmpty(factList)) {
                        factSql = ModelSqlServerUtils.buildDataModelCountSql(factList);
                    }

                    if (StringUtils.isNotEmpty(dimSql) && StringUtils.isNotEmpty(factSql)) {
                        finalSql = dimSql + " UNION " + factSql + ";";
                    } else if (StringUtils.isEmpty(dimSql) && StringUtils.isNotEmpty(factSql)) {
                        finalSql = factSql + ";";
                    } else if (StringUtils.isNotEmpty(dimSql) && StringUtils.isEmpty(factSql)) {
                        finalSql = dimSql + ";";
                    } else if (StringUtils.isEmpty(dimSql) && StringUtils.isEmpty(factSql)) {
                        return countVO;
                    } else {
                        return countVO;
                    }
                    break;
                case POSTGRESQL:
                    if (!CollectionUtils.isEmpty(dimList)) {
                        dimSql = ModelPgSqlUtils.buildDataModelDimCountSql(dimList);
                    }
                    if (!CollectionUtils.isEmpty(factList)) {
                        factSql = ModelPgSqlUtils.buildDataModelCountSql(factList);
                    }

                    if (StringUtils.isNotEmpty(dimSql) && StringUtils.isNotEmpty(factSql)) {
                        finalSql = dimSql + " UNION " + factSql + ";";
                    } else if (StringUtils.isEmpty(dimSql) && StringUtils.isNotEmpty(factSql)) {
                        finalSql = factSql + ";";
                    } else if (StringUtils.isNotEmpty(dimSql) && StringUtils.isEmpty(factSql)) {
                        finalSql = dimSql + ";";
                    } else if (StringUtils.isEmpty(dimSql) && StringUtils.isEmpty(factSql)) {
                        return countVO;
                    } else {
                        return countVO;
                    }
                    break;
                case DORIS:
                    if (!CollectionUtils.isEmpty(dimList)) {
                        dimSql = ModelMySqlConUtils.buildDataModelDimCountSql(dimList);
                    }
                    if (!CollectionUtils.isEmpty(factList)) {
                        factSql = ModelMySqlConUtils.buildDataModelCountSql(factList);
                    }

                    if (StringUtils.isNotEmpty(dimSql) && StringUtils.isNotEmpty(factSql)) {
                        finalSql = dimSql + " UNION " + factSql + ";";
                    } else if (StringUtils.isEmpty(dimSql) && StringUtils.isNotEmpty(factSql)) {
                        finalSql = factSql + ";";
                    } else if (StringUtils.isNotEmpty(dimSql) && StringUtils.isEmpty(factSql)) {
                        finalSql = dimSql + ";";
                    } else if (StringUtils.isEmpty(dimSql) && StringUtils.isEmpty(factSql)) {
                        return countVO;
                    } else {
                        return countVO;
                    }

                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
            statement = connection.createStatement();
            log.info("数仓建模首页统计信息：执行的获取当前业务域下的所有目标表数据量的sql:" + finalSql);
            resultSet = statement.executeQuery(finalSql);
            Map<String, Long> tbl_row = new HashMap<>();
            while (resultSet.next()) {
                String table_name = resultSet.getString("TABLE_NAME");
                long table_rows = resultSet.getInt("TABLE_ROWS");
                tbl_row.put(table_name, table_rows);
            }

            Map<String, Long> dim = new HashMap<>();
            Map<String, Long> fact = new HashMap<>();
            Map<String, Long> help = new HashMap<>();
            Map<String, Long> config = new HashMap<>();
            Map<String, Long> dwd = new HashMap<>();
            Map<String, Long> dws = new HashMap<>();

            long dimDataCount = 0;
            long factDataCount = 0;
            long helpDataCount = 0;
            long configDataCount = 0;
            long dwdDataCount = 0;
            long dwsDataCount = 0;

            //1:分别获取dim fact help config dwd dws 的数据量总计数
            for (String tblName : tbl_row.keySet()) {
                if (tblName.startsWith(ModelTblTypePrefixEnum.DIM.getName())) {
                    dimDataCount += tbl_row.get(tblName);
                    dim.put(tblName, tbl_row.get(tblName));
                } else if (tblName.startsWith(ModelTblTypePrefixEnum.FACT.getName())) {
                    factDataCount += tbl_row.get(tblName);
                    fact.put(tblName, tbl_row.get(tblName));
                } else if (tblName.startsWith(ModelTblTypePrefixEnum.HELP.getName())) {
                    helpDataCount += tbl_row.get(tblName);
                    help.put(tblName, tbl_row.get(tblName));
                } else if (tblName.startsWith(ModelTblTypePrefixEnum.CONFIG.getName())) {
                    configDataCount += tbl_row.get(tblName);
                    config.put(tblName, tbl_row.get(tblName));
                } else if (tblName.startsWith(ModelTblTypePrefixEnum.DWD.getName())) {
                    dwdDataCount += tbl_row.get(tblName);
                    dwd.put(tblName, tbl_row.get(tblName));
                } else if (tblName.startsWith(ModelTblTypePrefixEnum.DWS.getName())) {
                    dwsDataCount += tbl_row.get(tblName);
                    dws.put(tblName, tbl_row.get(tblName));
                }
            }
            //属性赋值
            dataCountVO.setDimDataCount(dimDataCount);
            dataCountVO.setFactDataCount(factDataCount);
            dataCountVO.setHelpDataCount(helpDataCount);
            dataCountVO.setConfigDataCount(configDataCount);
            dataCountVO.setDwdDataCount(dwdDataCount);
            dataCountVO.setDwsDataCount(dwsDataCount);

            //2:分别获取dim fact help config dwd dws 的top5数据量和表名
            //dim top5
            List<Map.Entry<String, Long>> dimTables = dim.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            //fact top5
            List<Map.Entry<String, Long>> factTables = fact.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            //help top5
            List<Map.Entry<String, Long>> helpTables = help.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            //config top5
            List<Map.Entry<String, Long>> configTables = config.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            //dwd top5
            List<Map.Entry<String, Long>> dwdTables = dwd.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            //dws top5
            List<Map.Entry<String, Long>> dwsTables = dws.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            //属性赋值
            top5DataCount.setTop5DimDataCount(dimTables);
            top5DataCount.setTop5FactDataCount(factTables);
            top5DataCount.setTop5HelpDataCount(helpTables);
            top5DataCount.setTop5ConfigDataCount(configTables);
            top5DataCount.setTop5DwdDataCount(dwdTables);
            top5DataCount.setTop5DwsDataCount(dwsTables);

            countVO.setDataCountVO(dataCountVO);
            countVO.setTop5DataCount(top5DataCount);
            return countVO;
        } catch (Exception e) {
            log.error("获取dw数据量失败：" + e);
            throw new FkException(ResultEnum.MODEL_MAIN_PAGE_COUNT_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(resultSet);
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(connection);
        }
    }

    /**
     * 根据前缀获取对应前缀的表在当前业务域下的个数
     *
     * @param areaId
     * @param prefix
     * @return
     */
    private Integer getCountByPrefix(Integer areaId, String prefix) {
        LambdaQueryWrapper<FactPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactPO::getBusinessId, areaId).likeRight(FactPO::getFactTabName, prefix);
        return factImpl.count(wrapper);
    }

}
