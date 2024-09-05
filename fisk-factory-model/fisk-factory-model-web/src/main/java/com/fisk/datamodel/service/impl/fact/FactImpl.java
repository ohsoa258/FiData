package com.fisk.datamodel.service.impl.fact;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.datamodel.dto.RelationDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.check.CheckPhyDimFactTableIfExistsDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datagovernance.client.DataGovernanceClient;
import com.fisk.datagovernance.dto.dataops.TableDataSyncDTO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessPublishQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.*;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.enums.*;
import com.fisk.datamodel.map.fact.FactAttributeMap;
import com.fisk.datamodel.map.fact.FactMap;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.mapper.fact.FactMapper;
import com.fisk.datamodel.service.IFact;
import com.fisk.datamodel.service.impl.AtomicIndicatorsImpl;
import com.fisk.datamodel.service.impl.BusinessAreaImpl;
import com.fisk.datamodel.service.impl.SystemVariablesImpl;
import com.fisk.datamodel.service.impl.dimension.DimensionImpl;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class FactImpl extends ServiceImpl<FactMapper, FactPO> implements IFact {

    @Resource
    FactMapper mapper;
    @Resource
    FactAttributeMapper attributeMapper;
    @Resource
    FactAttributeImpl factAttributeImpl;
    @Resource
    BusinessAreaImpl businessAreaImpl;
    @Resource
    AtomicIndicatorsImpl atomicIndicatorsImpl;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    UserHelper userHelper;
    @Resource
    DimensionImpl dimensionImpl;
    @Resource
    DataManageClient dataManageClient;
    @Resource
    private DataGovernanceClient dataGovernanceClient;
    @Resource
    private UserClient userClient;
    @Resource
    SystemVariablesImpl systemVariables;
    @Resource
    BusinessProcessImpl businessProcess;

    @Value("${spring.open-metadata}")
    private Boolean openMetadata;

    @Value("${fiData-data-dw-source}")
    private Integer dateDwSourceId;

    @Override
    public ResultEnum addFact(FactDTO dto) {
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getFactTabName, dto.factTabName);
        FactPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.FACT_EXIST;
        }
        FactPO model = FactMap.INSTANCES.dtoToPo(dto);
        model.setPrefixTempName(PrefixTempNameEnum.DIMENSION_TEMP_NAME.getName());
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteFact(int id) {
        try {
            // 删除之前检查该事实表是否已经被配置到存在的管道里面：
            // 方式：检查配置库-dmp_factory_db库 tb_nifi_custom_workflow_detail表内是否存在该事实表，
            // 如果存在则不允许删除，给出提示并告知该表被配置到哪个管道里面    tips:数仓建模的事实表对应的table type是5  数仓表任务-数仓事实表任务
            CheckPhyDimFactTableIfExistsDTO checkDto = new CheckPhyDimFactTableIfExistsDTO();
            checkDto.setTblId((long) id);
            checkDto.setChannelDataEnum(ChannelDataEnum.getName(5));
            ResultEntity<List<NifiCustomWorkflowDetailDTO>> booleanResultEntity = dataFactoryClient.checkPhyTableIfExists(checkDto);
            if (booleanResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                return ResultEnum.DISPATCH_REMOTE_ERROR;
            }
            List<NifiCustomWorkflowDetailDTO> data = booleanResultEntity.getData();
            if (!CollectionUtils.isEmpty(data)) {
                //这里的getWorkflowId 已经被替换为 workflowName
                List<String> collect = data.stream().map(NifiCustomWorkflowDetailDTO::getWorkflowId).collect(Collectors.toList());
                log.info("当前要删除的表存在于以下管道中：" + collect);
                return ResultEnum.ACCESS_PHYTABLE_EXISTS_IN_DISPATCH;
            }

            FactPO po = mapper.selectById(id);
            if (po == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            BusinessAreaPO businessArea = businessAreaImpl.getById(po.businessId);
            if (businessArea == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            //删除事实字段表
            QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id").lambda().eq(FactAttributePO::getFactId, id);
            List<Integer> factAttributeIds = (List) attributeMapper.selectObjs(queryWrapper);
            if (!CollectionUtils.isEmpty(factAttributeIds)) {
                ResultEnum resultEnum = factAttributeImpl.deleteFactAttribute(factAttributeIds);
                if (ResultEnum.SUCCESS != resultEnum) {
                    throw new FkException(resultEnum);
                }
            }
            //拼接删除niFi参数
            DataModelVO vo = niFiDelTable(po.businessId, id);
            publishTaskClient.deleteNifiFlow(vo);
            //拼接删除DW/Doris库中维度表
            PgsqlDelTableDTO dto = delDwDorisTable(po.factTabName);
            publishTaskClient.publishBuildDeletePgsqlTableTask(dto);

            // 删除factory-dispatch对应的表配置
            List<DeleteTableDetailDTO> list = new ArrayList<>();
            DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
            deleteTableDetailDto.appId = String.valueOf(po.businessId);
            deleteTableDetailDto.tableId = String.valueOf(id);
            // 数仓事实
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DW_FACT_TASK;
            //解决对象赋值混乱
            DeleteTableDetailDTO deleteTableDetail = JSON.parseObject(JSON.toJSONString(deleteTableDetailDto), DeleteTableDetailDTO.class);
            list.add(deleteTableDetail);
            // 分析事实
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.OLAP_FACT_TASK;
            list.add(deleteTableDetailDto);
            dataFactoryClient.editByDeleteTable(list);

            int flat = mapper.deleteByIdWithFill(po);
            if (flat > 0) {
                //删除atlas
                MetaDataDeleteAttributeDTO deleteDto = new MetaDataDeleteAttributeDTO();
                List<String> delQualifiedName = new ArrayList<>();
                //删除dw
                MetaDataInstanceAttributeDTO dataSourceConfigDw = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_DW.getValue());
                if (dataSourceConfigDw != null && !CollectionUtils.isEmpty(dataSourceConfigDw.dbList)) {
                    delQualifiedName.add(dataSourceConfigDw.dbList.get(0).qualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + id);
                }
                //删除Olap
                MetaDataInstanceAttributeDTO dataSourceConfigOlap = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_OLAP.getValue());
                if (dataSourceConfigOlap != null && !CollectionUtils.isEmpty(dataSourceConfigOlap.dbList)) {
                    delQualifiedName.add(dataSourceConfigOlap.dbList.get(0).qualifiedName + "_" + DataModelTableTypeEnum.DORIS_FACT.getValue() + "_" + id);
                }

                //原来开启同步元数据时 删除元数据的方法
                if (openMetadata) {
                    deleteDto.qualifiedNames = delQualifiedName;
                    deleteDto.classifications = businessArea.getBusinessName();
                    dataManageClient.deleteMetaData(deleteDto);
                }

                //新版 删除元数据的方法
                //创建固定大小的线程池 异步执行
                ExecutorService executor = Executors.newFixedThreadPool(1);
                //提交任务并立即返回
                executor.submit(() -> {
                    log.info("异步任务开始执行");
                    try {
                        deleteDto.qualifiedNames = delQualifiedName;
                        deleteDto.classifications = businessArea.getBusinessName();
                        //删除字段元数据
                        dataManageClient.deleteFieldMetaData(deleteDto);
                    } catch (Exception e) {
                        log.error("数仓建模-删除事实表时-异步删除元数据任务执行出错：" + e);
                    }
                    log.info("异步任务执行结束");
                });

            }

            return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } catch (Exception e) {
            log.error("deleteFact:" + e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    @Override
    public ResultEntity<Object> deleteFactByCheck(int id) {
        try {
            // 删除之前检查该事实表是否已经被配置到存在的管道里面：
            // 方式：检查配置库-dmp_factory_db库 tb_nifi_custom_workflow_detail表内是否存在该事实表，
            // 如果存在则不允许删除，给出提示并告知该表被配置到哪个管道里面    tips:数仓建模的事实表对应的table type是5  数仓表任务-数仓事实表任务
            CheckPhyDimFactTableIfExistsDTO checkDto = new CheckPhyDimFactTableIfExistsDTO();
            checkDto.setTblId((long) id);
            checkDto.setChannelDataEnum(ChannelDataEnum.getName(5));
            ResultEntity<List<NifiCustomWorkflowDetailDTO>> booleanResultEntity = dataFactoryClient.checkPhyTableIfExists(checkDto);
            if (booleanResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                return ResultEntityBuild.build(ResultEnum.DISPATCH_REMOTE_ERROR);
            }
            List<NifiCustomWorkflowDetailDTO> data = booleanResultEntity.getData();
            if (!CollectionUtils.isEmpty(data)) {
                //这里的getWorkflowId 已经被替换为 workflowName
                List<String> collect = data.stream().map(NifiCustomWorkflowDetailDTO::getWorkflowId).collect(Collectors.toList());
                log.info("当前要删除的表存在于以下管道中：" + collect);
                return ResultEntityBuild.build(ResultEnum.ACCESS_PHYTABLE_EXISTS_IN_DISPATCH,collect);
            }

            FactPO po = mapper.selectById(id);
            if (po == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
            }
            BusinessAreaPO businessArea = businessAreaImpl.getById(po.businessId);
            if (businessArea == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
            }
            //删除事实字段表
            QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id").lambda().eq(FactAttributePO::getFactId, id);
            List<Integer> factAttributeIds = (List) attributeMapper.selectObjs(queryWrapper);
            if (!CollectionUtils.isEmpty(factAttributeIds)) {
                ResultEnum resultEnum = factAttributeImpl.deleteFactAttribute(factAttributeIds);
                if (ResultEnum.SUCCESS != resultEnum) {
                    throw new FkException(resultEnum);
                }
            }
            //拼接删除niFi参数
            DataModelVO vo = niFiDelTable(po.businessId, id);
            publishTaskClient.deleteNifiFlow(vo);
            //拼接删除DW/Doris库中维度表
            PgsqlDelTableDTO dto = delDwDorisTable(po.factTabName);
            publishTaskClient.publishBuildDeletePgsqlTableTask(dto);

            // 删除factory-dispatch对应的表配置
            List<DeleteTableDetailDTO> list = new ArrayList<>();
            DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
            deleteTableDetailDto.appId = String.valueOf(po.businessId);
            deleteTableDetailDto.tableId = String.valueOf(id);
            // 数仓事实
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DW_FACT_TASK;
            //解决对象赋值混乱
            DeleteTableDetailDTO deleteTableDetail = JSON.parseObject(JSON.toJSONString(deleteTableDetailDto), DeleteTableDetailDTO.class);
            list.add(deleteTableDetail);
            // 分析事实
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.OLAP_FACT_TASK;
            list.add(deleteTableDetailDto);
            dataFactoryClient.editByDeleteTable(list);

            int flat = mapper.deleteByIdWithFill(po);
            if (flat > 0) {
                //删除atlas
                MetaDataDeleteAttributeDTO deleteDto = new MetaDataDeleteAttributeDTO();
                List<String> delQualifiedName = new ArrayList<>();
                //删除dw
                MetaDataInstanceAttributeDTO dataSourceConfigDw = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_DW.getValue());
                if (dataSourceConfigDw != null && !CollectionUtils.isEmpty(dataSourceConfigDw.dbList)) {
                    delQualifiedName.add(dataSourceConfigDw.dbList.get(0).qualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + id);
                }
                //删除Olap
                MetaDataInstanceAttributeDTO dataSourceConfigOlap = dimensionImpl.getDataSourceConfig(DataSourceConfigEnum.DMP_OLAP.getValue());
                if (dataSourceConfigOlap != null && !CollectionUtils.isEmpty(dataSourceConfigOlap.dbList)) {
                    delQualifiedName.add(dataSourceConfigOlap.dbList.get(0).qualifiedName + "_" + DataModelTableTypeEnum.DORIS_FACT.getValue() + "_" + id);
                }

                //原来开启同步元数据时 删除元数据的方法
                if (openMetadata) {
                    deleteDto.qualifiedNames = delQualifiedName;
                    deleteDto.classifications = businessArea.getBusinessName();
                    dataManageClient.deleteMetaData(deleteDto);
                }

                //新版 删除元数据的方法
                //创建固定大小的线程池 异步执行
                ExecutorService executor = Executors.newFixedThreadPool(1);
                //提交任务并立即返回
                executor.submit(() -> {
                    log.info("异步任务开始执行");
                    try {
                        deleteDto.qualifiedNames = delQualifiedName;
                        deleteDto.classifications = businessArea.getBusinessName();
                        //删除字段元数据
                        dataManageClient.deleteFieldMetaData(deleteDto);
                    } catch (Exception e) {
                        log.error("数仓建模-删除事实表时-异步删除元数据任务执行出错：" + e);
                    }
                    log.info("异步任务执行结束");
                });

            }

            if (flat > 0){
                return ResultEntityBuild.build(ResultEnum.SUCCESS);
            } else {
                return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
            }
        } catch (Exception e) {
            log.error("deleteFact:" + e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 拼接niFi删除表参数
     *
     * @param businessAreaId
     * @param factId
     * @return
     */
    public DataModelVO niFiDelTable(int businessAreaId, int factId) {
        DataModelVO vo = new DataModelVO();
        vo.businessId = String.valueOf(businessAreaId);
        vo.dataClassifyEnum = DataClassifyEnum.DATAMODELING;
        vo.delBusiness = false;
        DataModelTableVO tableVO = new DataModelTableVO();
        tableVO.type = OlapTableEnum.FACT;
        List<Long> ids = new ArrayList<>();
        ids.add(Long.valueOf(factId));
        tableVO.ids = ids;
        vo.factIdList = tableVO;
        return vo;
    }

    /**
     * 拼接删除DW/Doris表
     *
     * @param factName
     * @return
     */
    public PgsqlDelTableDTO delDwDorisTable(String factName) {
        PgsqlDelTableDTO dto = new PgsqlDelTableDTO();
        dto.businessTypeEnum = BusinessTypeEnum.DATAMODEL;
        dto.delApp = false;
        List<TableListDTO> tableList = new ArrayList<>();
        TableListDTO table = new TableListDTO();
        table.tableName = factName;
        tableList.add(table);
        dto.tableList = tableList;
        dto.userId = userHelper.getLoginUserInfo().id;
        return dto;
    }

    @Override
    public FactDTO getFact(int id) {
        FactDTO factDTO = FactMap.INSTANCES.poToDto(mapper.selectById(id));
        if (factDTO != null) {
            factDTO.deltaTimes = systemVariables.getSystemVariable(id, CreateTypeEnum.CREATE_FACT.getValue());
        }
        return factDTO;
    }

    @Override
    public ResultEnum updateFact(FactDTO dto) {
        FactPO po = mapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getFactTabName, dto.factTabName);
        FactPO model = mapper.selectOne(queryWrapper);
        if (model != null && model.id != dto.id) {
            return ResultEnum.DATA_EXISTS;
        }

        po.factTableCnName = dto.factTableCnName;
        po.factTableDesc = dto.factTableDesc;
        po.factTabName = dto.factTabName;
        po.businessProcessId = dto.businessProcessId;
        po.batchOrStream = dto.batchOrStream;

        //修改表发布状态
        if (po.isPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue()) {
            ModelPublishStatusDTO publishStatusDTO = new ModelPublishStatusDTO();
            publishStatusDTO.id = dto.id;
            publishStatusDTO.status = po.isPublish;
            publishStatusDTO.type = DataSourceConfigEnum.DMP_DW.getValue();
            updateFactPublishStatus(publishStatusDTO);
        }

        return mapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public IPage<FactListDTO> getFactList(QueryDTO dto) {
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getBusinessProcessId, dto.id);
        Page<FactPO> data = new Page<>(dto.getPage(), dto.getSize());
        return FactMap.INSTANCES.pagePoToDto(mapper.selectPage(data, queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public List<FactDropDTO> getFactDropList() {
        //获取事实表数据
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        List<FactDropDTO> list = FactMap.INSTANCES.dropPoToDto(mapper.selectList(queryWrapper));
        //获取事实字段表数据
        QueryWrapper<FactAttributePO> attribute = new QueryWrapper<>();
        for (FactDropDTO dto : list) {
            //向字段集合添加数据,只获取字段为度量类型的数据
            dto.list = FactAttributeMap.INSTANCES.poDropToDto(attributeMapper.selectList(attribute).stream().filter(e -> e.getFactId() == dto.id && e.attributeType == FactAttributeEnum.MEASURE.getValue()).collect(Collectors.toList()));
        }
        return list;
    }

    @Override
    public List<FactScreenDropDTO> getFactScreenDropList() {
        //获取事实表数据
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        return FactMap.INSTANCES.dropScreenPoToDto(mapper.selectList(queryWrapper.orderByDesc("create_time")));
    }

    @Override
    public ResultEnum updateFactSql(DimensionSqlDTO dto) {
        FactPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript = dto.sqlScript;
        model.dataSourceId = dto.dataSourceId;
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public void updateFactPublishStatus(ModelPublishStatusDTO dto) {
        FactPO fact = mapper.selectById(dto.id);
        if (fact == null) {
            log.info("数据建模元数据实时同步失败,事实表不存在!");
            return;
        }

        BusinessAreaPO businessAreaPO = businessAreaImpl.query().eq("id", fact.businessId).one();
        if (businessAreaPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //0:DW发布状态
        int dataSourceId;
        int dataModelType;
        if (dto.type == 0) {
            fact.isPublish = dto.status;
            dataSourceId = DataSourceConfigEnum.DMP_DW.getValue();
            dataModelType = DataModelTableTypeEnum.DW_FACT.getValue();
        } else {
            fact.dorisPublish = dto.status;
            dataSourceId = DataSourceConfigEnum.DMP_OLAP.getValue();
            dataModelType = DataModelTableTypeEnum.DORIS_FACT.getValue();
        }
        int flat = mapper.updateById(fact);
        if (flat == 0 || dto.status != PublicStatusEnum.PUBLIC_SUCCESS.getValue()) {
            log.info("维度表更改状态失败!");
            return;
        }
        //实时更新元数据
        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        MetaDataInstanceAttributeDTO data = dimensionImpl.getDataSourceConfig(dataSourceId);
        if (data == null) {
            log.info("事实表元数据实时更新,查询实例数据失败!");
            return;
        }
        //表
        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();
        MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
        table.contact_info = "";
        table.description = fact.factTableDesc;
        table.name = fact.factTabName;
        table.comment = String.valueOf(fact.businessId);
        table.qualifiedName = data.dbList.get(0).qualifiedName + "_" + dataModelType + "_" + fact.id;
        table.displayName = fact.factTableCnName;

        table.owner = businessAreaPO.getBusinessAdmin();

        //所属人
        /*List<Long> ids = new ArrayList<>();
        ids.add(Long.parseLong(fact.createUser));
        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(ids);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
            table.owner = userListByIds.data.get(0).getUsername();
        }*/

        //字段
        List<MetaDataColumnAttributeDTO> columnList = setFactField(dto, table);
        if (CollectionUtils.isEmpty(columnList)) {
            log.info("事实/指标表不存在字段!");
            return;
        }
        table.columnList = columnList;
        tableList.add(table);
        data.dbList.get(0).tableList = tableList;
        list.add(data);

        if (openMetadata) {
            //修改元数据
            ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 更新元数据内容
                        log.info("维度表构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
                        dataManageClient.consumeMetaData(list);
                    } catch (Exception e) {
                        log.error("【dataManageClient.MetaData()】方法报错,ex", e);
                    }
                }
            });
        }

    }

    @Override
    public List<TableNameDTO> getPublishSuccessFactTable(Integer businessId) {
        List<FactPO> list = this.query()
                .select("fact_tab_name", "business_id", "id")
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .eq("business_id", businessId)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<TableNameDTO> data = new ArrayList<>();
        for (FactPO po : list) {
            TableNameDTO dto = new TableNameDTO();
            dto.tableName = po.factTabName;

            FactAttributeDetailDTO factAttributeDataList = factAttributeImpl.getFactAttributeDataList((int) po.id);
            if (CollectionUtils.isEmpty(factAttributeDataList.attributeDTO)) {
                continue;
            }
            List<TableColumnDTO> columnList = new ArrayList<>();
            for (FactAttributeDTO item : factAttributeDataList.attributeDTO) {
                TableColumnDTO column = new TableColumnDTO();
                column.fieldName = item.factFieldEnName;
                columnList.add(column);
            }

            dto.columnList = columnList;

            data.add(dto);
        }

        return data;
    }

    /**
     * 获取业务域下的事实表计数 fact config help
     *
     * @return
     */
    @Override
    public Integer getFactCountByBid(Integer id) {
        LambdaQueryWrapper<FactPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactPO::getBusinessId, id)
                .likeRight(FactPO::getFactTabName, "fact_");
        return mapper.selectCount(wrapper);
    }

    /**
     * 获取业务域下的dwd表计数
     *
     * @return
     */
    @Override
    public Integer getDwdCountByBid(Integer id) {
        LambdaQueryWrapper<FactPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactPO::getBusinessId, id)
                .likeRight(FactPO::getFactTabName, "dwd_");
        ;
        return mapper.selectCount(wrapper);
    }

    /**
     * 获取业务域下的dws表计数
     *
     * @return
     */
    @Override
    public Integer getDwsCountByBid(Integer id) {
        LambdaQueryWrapper<FactPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactPO::getBusinessId, id)
                .likeRight(FactPO::getFactTabName, "dws_");
        ;
        return mapper.selectCount(wrapper);
    }

    /**
     * 获取所有事实表计数
     *
     * @return
     */
    @Override
    public Integer getFactTotalCount() {
        LambdaQueryWrapper<FactPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactPO::getDelFlag, 1);
        return mapper.selectCount(wrapper);
    }

    @Override
    public List<FactTreeDTO> getFactTree() {
        List<BusinessAreaPO> businessAreaPOS = businessAreaImpl.list();

        List<BusinessAreaFactDTO> areaFactDTOS = new ArrayList<>();

        for (BusinessAreaPO businessAreaPO : businessAreaPOS) {
            LambdaQueryWrapper<FactPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(FactPO::getBusinessId, businessAreaPO.getId())
                    .orderByAsc(FactPO::getFactTabName);
            List<FactPO> factPOS = list(wrapper1);

            BusinessAreaFactDTO businessAreaFactDTO = new BusinessAreaFactDTO();
            businessAreaFactDTO.setBusinessName(businessAreaPO.getBusinessName());
            businessAreaFactDTO.setId(businessAreaPO.getId());
            List<FactListDTO> factListDTOS = new ArrayList<>();

            for (FactPO factPO : factPOS) {
                LambdaQueryWrapper<FactAttributePO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(FactAttributePO::getFactId, factPO.getId());
                List<FactAttributePO> attributePOList = factAttributeImpl.list(wrapper);
                FactListDTO factListDTO = new FactListDTO();
                factListDTO.factTabName = factPO.factTabName;
                factListDTO.setId((int) factPO.getId());
                factListDTO.factTableCnName = factPO.factTableCnName;
                List<FactAttributeDTO> attributeDataDTOS = new ArrayList<>();
                for (FactAttributePO factAttributePO : attributePOList) {
                    FactAttributeDTO attributeDTO = new FactAttributeDTO();
                    attributeDTO.setId(factAttributePO.getId());
                    attributeDTO.setFactFieldCnName(factAttributePO.getFactFieldCnName());
                    attributeDTO.setFactFieldEnName(factAttributePO.getFactFieldEnName());
                    attributeDTO.setFactFieldDes(factAttributePO.getFactFieldDes());
                    attributeDTO.setFactFieldType(factAttributePO.getFactFieldType());
                    attributeDTO.setFactFieldLength(factAttributePO.getFactFieldLength());
                    attributeDataDTOS.add(attributeDTO);
                }
                //维度字段
                factListDTO.setAttributeList(attributeDataDTOS);
                factListDTOS.add(factListDTO);
            }
            businessAreaFactDTO.setFactList(factListDTOS);
            areaFactDTOS.add(businessAreaFactDTO);
        }
        FactTreeDTO factTreeDTO = new FactTreeDTO();
        factTreeDTO.setFactByArea(areaFactDTOS);
        List<FactTreeDTO> factTreeDTOS = new ArrayList<>();
        factTreeDTOS.add(factTreeDTO);
        return factTreeDTOS;
    }

    /**
     * 事实表跨业务域移动
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEntity<Object> transFactToBArea(FactTransDTO dto) {
        ResultEnum resultEnum = null;
        /*
        如果该事实表已被配置到管道中，则不允许转移，牵扯到kafka发消息的topic，因此给出提示
         */
        // 移动之前检查该事实表是否已经被配置到存在的管道里面：如果管道中有则不允许移动
        // 方式：检查配置库-dmp_factory_db库 tb_nifi_custom_workflow_detail表内是否存在该事实表，
        // 如果存在则不允许删除，给出提示并告知该表被配置到哪个管道里面    tips:数仓建模的事实表对应的table type是5  数仓表任务-数仓事实表任务
        CheckPhyDimFactTableIfExistsDTO checkDto = new CheckPhyDimFactTableIfExistsDTO();
        checkDto.setTblId((long) dto.factId);
        checkDto.setChannelDataEnum(ChannelDataEnum.getName(5));
        ResultEntity<List<NifiCustomWorkflowDetailDTO>> booleanResultEntity = dataFactoryClient.checkPhyTableIfExists(checkDto);
        if (booleanResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEntityBuild.build(ResultEnum.DISPATCH_REMOTE_ERROR);
        }
        List<NifiCustomWorkflowDetailDTO> data = booleanResultEntity.getData();
        if (!CollectionUtils.isEmpty(data)) {
            //这里的getWorkflowId 已经被替换为 workflowName
            List<String> collect = data.stream().map(NifiCustomWorkflowDetailDTO::getWorkflowId).collect(Collectors.toList());
            log.info("当前要删除的表存在于以下管道中：" + collect);
            return ResultEntityBuild.build(ResultEnum.FACT_EXISTS_IN_DISPATCH, collect);
        }

        /*
        1、转移事实表
         */
        log.info("==========开始转移事实表==========");
        //获取要转移的事实表信息
        LambdaQueryWrapper<FactPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactPO::getId, dto.getFactId());
        FactPO one = getOne(wrapper);
        //修改信息
        one.setBusinessProcessId(dto.toBusinessFolderId);
        one.setBusinessId(dto.toBusinessId);
        updateById(one);
        log.info("==========事实表转移完成==========");

        //数据表处理方式是批处理或流处理  0批处理 1流处理
        if (one.batchOrStream == 1) {
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        }

        /*
        2、删除原nifi
         */
        log.info("==========开始删除nifi原流程==========");
        //拼接删除niFi参数
        DataModelVO vo = niFiDelTable(dto.curBusinessId, dto.factId);
        ResultEntity<Object> result = publishTaskClient.deleteNifiFlowByKafka(vo);
        log.info("==========nifi删除结果:" + result.getCode());

        /*
        3、重新发布
         */
        log.info("==========开始重新发布==========");
        BusinessProcessPublishQueryDTO queryDTO = new BusinessProcessPublishQueryDTO();
        queryDTO.setBusinessAreaId(dto.toBusinessId);
        List<Integer> factIds = new ArrayList<>();
        factIds.add(dto.factId);
        queryDTO.setFactIds(factIds);
        //不同步数据
        queryDTO.setOpenTransmission(false);
        queryDTO.setRemark("从业务域" + dto.curBusinessId + " 移动到业务域 " + dto.toBusinessId);
        resultEnum = businessProcess.batchPublishBusinessProcess(queryDTO);
        log.info("==========重新发布结果:" + result.getCode());
        return ResultEntityBuild.build(resultEnum);
    }

    /**
     * 数仓建模 同步表数据（触发nifi）
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEntity<Object> modelSyncData(ModelSyncDataDTO dto) {
        TableDataSyncDTO tableDataSyncDTO = new TableDataSyncDTO();
        switch (dto.getTblType()) {
            //维度表
            case 0:
                //获取维度表
                DimensionDTO dimension = dimensionImpl.getDimension(Math.toIntExact(dto.getTblId()));
                //1数仓 2数接 3mdm
                tableDataSyncDTO.setDatasourceId(1);
                tableDataSyncDTO.setTableFullName(dimension.getDimensionTabName());
                ResultEntity<Object> result = dataGovernanceClient.tableDataSyncForModel(tableDataSyncDTO);
                if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
                    throw new FkException(ResultEnum.TABLE_DATA_SYNC_FAIL);
                }
                return result;
            //事实表
            case 1:
                FactDTO fact = getFact(Math.toIntExact(dto.getTblId()));
                tableDataSyncDTO.setDatasourceId(1);
                tableDataSyncDTO.setTableFullName(fact.getFactTabName());
                ResultEntity<Object> result1 = dataGovernanceClient.tableDataSyncForModel(tableDataSyncDTO);
                if (result1.getCode() != ResultEnum.SUCCESS.getCode()) {
                    throw new FkException(ResultEnum.TABLE_DATA_SYNC_FAIL);
                }
                return result1;
            default:
                return ResultEntityBuild.build(ResultEnum.WRONG_TABLE_TYPE_ERROR);
        }

    }

    @Override
    public List<FactDTO> getFactTableByIds(List<Integer> ids) {
        return FactMap.INSTANCES.poListToFactDtoList(baseMapper.selectBatchIds(ids));
    }

    /**
     * 事实/指标表获取字段
     *
     * @param dto
     * @param table
     * @return
     */
    private List<MetaDataColumnAttributeDTO> setFactField(ModelPublishStatusDTO dto, MetaDataTableAttributeDTO table) {
        List<MetaDataColumnAttributeDTO> columnList = new ArrayList<>();
        //事实表
        if (dto.type == 0) {
            FactAttributeDetailDTO factAttributeDataList = factAttributeImpl.getFactAttributeDataList(dto.id);
            if (factAttributeDataList == null || factAttributeDataList.attributeDTO.size() == 0) {
                return null;
            }
            for (FactAttributeDTO field : factAttributeDataList.attributeDTO) {
                MetaDataColumnAttributeDTO column = new MetaDataColumnAttributeDTO();
                column.name = field.factFieldEnName;
                column.qualifiedName = table.qualifiedName + "_" + field.id;
                column.description = field.factFieldDes;
                String fieldTypeLength = field.factFieldLength == 0 ? "" : "(" + field.factFieldLength + ")";
                column.dataType = field.factFieldType + fieldTypeLength;
                column.displayName = field.factFieldCnName;
                column.owner = table.owner;
                columnList.add(column);
            }
        }
        //指标表
        else {
            List<AtomicIndicatorPushDTO> factAttributeList = atomicIndicatorsImpl.getAtomicIndicator(dto.id);
            if (CollectionUtils.isEmpty(factAttributeList)) {
                return null;
            }
            for (AtomicIndicatorPushDTO field : factAttributeList) {
                MetaDataColumnAttributeDTO column = new MetaDataColumnAttributeDTO();
                if (field.attributeType == FactAttributeEnum.DEGENERATION_DIMENSION.getValue()) {
                    column.name = field.factFieldName;
                    column.qualifiedName = table.qualifiedName + "_" + field.attributeType + "_" + field.id;
                    String fieldTypeLength = field.factFieldLength == 0 ? "" : "(" + field.factFieldLength + ")";
                    column.dataType = field.factFieldType + fieldTypeLength;
                } else if (field.attributeType == FactAttributeEnum.DIMENSION_KEY.getValue()) {
                    column.name = field.factFieldName + "key";
                    column.qualifiedName = table.qualifiedName + "_" + field.attributeType + "_" + field.id;
                    column.dataType = "VARCHAR(50)";
                } else if (field.attributeType == FactAttributeEnum.MEASURE.getValue()) {
                    column.name = field.atomicIndicatorName;
                    column.qualifiedName = table.qualifiedName + "_" + field.attributeType + "_" + field.id;
                    column.dataType = "BIGINT";
                    column.comment = field.aggregationLogic;
                }
                column.owner = table.owner;
                columnList.add(column);
            }
        }
        return columnList;
    }

    public List<MetaDataTableAttributeDTO> getFactMetaData(BusinessAreaPO item,
                                                           String dbQualifiedName,
                                                           Integer dataModelType,
                                                           String businessAdmin) {
        List<FactPO> factPOList = this.query()
                .eq("business_id", item.getId())
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .list();
        if (CollectionUtils.isEmpty(factPOList)) {
            return new ArrayList<>();
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        for (FactPO fact : factPOList) {

            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.contact_info = "";
            table.description = fact.factTableDesc;
            table.name = fact.factTabName;
            table.comment = String.valueOf(fact.businessId);
            table.qualifiedName = dbQualifiedName + "_" + dataModelType + "_" + fact.id;
            table.displayName = fact.factTableCnName;
            //表创建人
            table.owner = fact.createUser;
            table.sqlScript = fact.sqlScript;
            table.coverScript = fact.coverScript;
            table.tableConfigId = Long.valueOf(fact.id).intValue();
            table.dataSourceId = fact.dataSourceId;
            table.isExistClassification = true;
            table.isExistStg = true;
            table.AppName = item.getBusinessName();

            //字段
            table.columnList = getFactAttributeMetaData(fact.id, table);
            table.whetherSchema = false;

            //获取该事实表关联外键时关联了哪些维度表
            List<String> dimsByFactId = new ArrayList<>();
            try {
                dimsByFactId = getDimsByFactId(fact.id);
            } catch (Exception ex) {
                log.error("获取事实表关联的维度失败" + ex);
            }
            table.setDimQNames(dimsByFactId);

            tableList.add(table);
        }

        return tableList;
    }

    /**
     * 根据事实表id 获取事实表关联的维度外键
     *
     * @param factId
     * @return
     */
    public List<String> getDimsByFactId(Long factId) {
        List<String> dimNames = new ArrayList<>();

        List<FactAttributeUpdateDTO> factAttribute = factAttributeImpl.getFactAttribute(Math.toIntExact(factId));

        //获取所有维度表信息
        List<DimensionPO> dimPos =  dimensionImpl.list(
                new LambdaQueryWrapper<DimensionPO>().select(DimensionPO::getDimensionTabName, DimensionPO::getId)
        );

        //从system模块获取dmp_dw数仓的信息
        ResultEntity<DataSourceDTO> resultEntity =  userClient.getFiDataDataSourceById(dateDwSourceId);
        DataSourceDTO dataSourceDTO;
        if (resultEntity.getCode()==ResultEnum.SUCCESS.getCode()){
            dataSourceDTO = resultEntity.getData();
        }else {
            log.error("获取平台配置数据源dmp_dw信息失败");
            return dimNames;
        }

        if (!CollectionUtils.isEmpty(factAttribute)){
            String configDetails = factAttribute.get(0).getConfigDetails();
            if (StringUtils.isNotBlank(configDetails)){
                List<RelationDTO> relationDTOS = JSON.parseArray(configDetails, RelationDTO.class);
                //获取到关联外键的维度表的名称
                List<String> dimOriginalNames = relationDTOS.stream().map(RelationDTO::getTargetTable).collect(Collectors.toList());

                //通过名称获取到这些维度表的id 因为维度表名在项目中是是唯一的  为了最终拼接元数据那边所需要的唯一限定表名称
                //元数据唯一限定表名称格式为: 数据库地址_数据库名称_表类型(1是维度)_表id  192.168.0.61_dmp_dw_1_50

                for (DimensionPO dimPo : dimPos) {
                    if (dimOriginalNames.contains(dimPo.getDimensionTabName())){
                        dimNames.add(dataSourceDTO.getConIp() + "_" + dataSourceDTO.getConDbname() + "_" + 1 + "_" + dimPo.getId());
                    }
                }
            }
        }
        return dimNames;
    }

    public List<MetaDataTableAttributeDTO> getFactMetaDataBySyncTime(BusinessAreaPO item,
                                                                     String dbQualifiedName,
                                                                     Integer dataModelType,
                                                                     String businessAdmin,
                                                                     List<Integer> factIds
    ) {
        List<FactPO> factPOList = this.query()
                .eq("business_id", item.getId())
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .in("id", factIds)
                .list();
        if (CollectionUtils.isEmpty(factPOList)) {
            return new ArrayList<>();
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        for (FactPO fact : factPOList) {

            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.contact_info = "";
            table.description = fact.factTableDesc;
            table.name = fact.factTabName;
            table.comment = String.valueOf(fact.businessId);
            table.qualifiedName = dbQualifiedName + "_" + dataModelType + "_" + fact.id;
            table.displayName = fact.factTableCnName;
            table.owner = fact.createUser;
            table.sqlScript = fact.sqlScript;
            table.coverScript = fact.coverScript;
            table.tableConfigId = Long.valueOf(fact.id).intValue();
            table.dataSourceId = fact.dataSourceId;
            table.isExistClassification = true;
            table.isExistStg = true;
            table.AppName = item.getBusinessName();

            //字段
            table.columnList = getFactAttributeMetaData(fact.id, table);
            table.whetherSchema = false;

            //获取该事实表关联外键时关联了哪些维度表
            List<String> dimsByFactId = new ArrayList<>();
            try {
                dimsByFactId = getDimsByFactId(fact.id);
            } catch (Exception ex) {
                log.error("获取事实表关联的维度失败" + ex);
            }
            table.setDimQNames(dimsByFactId);

            tableList.add(table);
        }

        return tableList;
    }

    public List<MetaDataTableAttributeDTO> getFactMetaDataOfBatchTbl(long businessId,
                                                                     List<Integer> factIds,
                                                                     String dbQualifiedName,
                                                                     Integer dataModelType,
                                                                     String businessAdmin) {
        List<FactPO> factPOList = this.query()
                .eq("business_id", businessId)
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .in("id", factIds)
                .list();
        if (CollectionUtils.isEmpty(factPOList)) {
            return new ArrayList<>();
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        for (FactPO fact : factPOList) {

            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.contact_info = "";
            table.description = fact.factTableDesc;
            table.name = fact.factTabName;
            table.comment = String.valueOf(fact.businessId);
            table.qualifiedName = dbQualifiedName + "_" + dataModelType + "_" + fact.id;
            table.displayName = fact.factTableCnName;
            table.owner = businessAdmin;

            //字段
            table.columnList = getFactAttributeMetaData(fact.id, table);

            tableList.add(table);
        }

        return tableList;
    }

    public List<MetaDataColumnAttributeDTO> getFactAttributeMetaData(long factId, MetaDataTableAttributeDTO table) {
        FactAttributeDetailDTO factAttributeDataList = factAttributeImpl.getFactAttributeDataList((int) factId);
        if (factAttributeDataList == null || factAttributeDataList.attributeDTO.size() == 0) {
            return new ArrayList<>();
        }
        List<MetaDataColumnAttributeDTO> columnList = new ArrayList<>();
        for (FactAttributeDTO field : factAttributeDataList.attributeDTO) {
            MetaDataColumnAttributeDTO column = new MetaDataColumnAttributeDTO();
            column.name = field.factFieldEnName;
            column.qualifiedName = table.qualifiedName + "_" + field.id;
            column.description = field.factFieldDes;
            String fieldTypeLength = field.factFieldLength == 0 ? "" : "(" + field.factFieldLength + ")";
            column.dataType = field.factFieldType + fieldTypeLength;
            column.displayName = field.factFieldCnName;
            //字段创建人
            column.owner = field.createUser;
            column.dataClassification = field.dataClassification;
            column.dataLevel = field.dataLevel;
            columnList.add(column);
        }
        return columnList;
    }

    /**
     * 根据事实表名获取事实表id
     *
     * @param tblName
     * @return
     */
    public FactPO getFactIdByFactName(String tblName) {
        return getOne(new LambdaQueryWrapper<FactPO>().eq(FactPO::getFactTabName, tblName));
    }

}
