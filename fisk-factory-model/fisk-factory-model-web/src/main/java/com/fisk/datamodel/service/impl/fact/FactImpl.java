package com.fisk.datamodel.service.impl.fact;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.*;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.fact.FactAttributeMap;
import com.fisk.datamodel.map.fact.FactMap;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.mapper.fact.FactMapper;
import com.fisk.datamodel.service.IFact;
import com.fisk.datamodel.service.impl.AtomicIndicatorsImpl;
import com.fisk.datamodel.service.impl.BusinessAreaImpl;
import com.fisk.datamodel.service.impl.dimension.DimensionImpl;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
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
    UserClient userClient;
    @Resource
    DimensionImpl dimensionImpl;
    @Resource
    DataManageClient dataManageClient;

    @Override
    public ResultEnum addFact(FactDTO dto) {
        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getFactTabName, dto.factTabName);
        FactPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.FACT_EXIST;
        }
        FactPO model = FactMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteFact(int id)
    {
        try {
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
                deleteDto.qualifiedNames = delQualifiedName;
                deleteDto.classifications = businessArea.getBusinessName();
                dataManageClient.deleteMetaData(deleteDto);
            }

            return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e)
        {
            log.error("deleteFact:"+e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 拼接niFi删除表参数
     * @param businessAreaId
     * @param factId
     * @return
     */
    public DataModelVO niFiDelTable(int businessAreaId,int factId)
    {
        DataModelVO vo=new DataModelVO();
        vo.businessId= String.valueOf(businessAreaId);
        vo.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
        vo.delBusiness=false;
        DataModelTableVO tableVO=new DataModelTableVO();
        tableVO.type= OlapTableEnum.FACT;
        List<Long> ids=new ArrayList<>();
        ids.add(Long.valueOf(factId));
        tableVO.ids=ids;
        vo.factIdList=tableVO;
        return vo;
    }

    /**
     * 拼接删除DW/Doris表
     * @param factName
     * @return
     */
    public PgsqlDelTableDTO delDwDorisTable(String factName)
    {
        PgsqlDelTableDTO dto=new PgsqlDelTableDTO();
        dto.businessTypeEnum= BusinessTypeEnum.DATAMODEL;
        dto.delApp=false;
        List<TableListDTO> tableList=new ArrayList<>();
        TableListDTO table=new TableListDTO();
        table.tableName=factName;
        tableList.add(table);
        dto.tableList=tableList;
        dto.userId=userHelper.getLoginUserInfo().id;
        return dto;
    }

    @Override
    public FactDTO getFact(int id)
    {
        return FactMap.INSTANCES.poToDto(mapper.selectById(id));
    }

    @Override
    public ResultEnum updateFact(FactDTO dto)
    {
        FactPO po=mapper.selectById(dto.id);
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

        //修改元数据方法
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
    public IPage<FactListDTO> getFactList(QueryDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getBusinessProcessId,dto.id);
        Page<FactPO> data=new Page<>(dto.getPage(),dto.getSize());
        return FactMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public List<FactDropDTO> getFactDropList()
    {
        //获取事实表数据
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        List<FactDropDTO> list=FactMap.INSTANCES.dropPoToDto(mapper.selectList(queryWrapper));
        //获取事实字段表数据
        QueryWrapper<FactAttributePO> attribute=new QueryWrapper<>();
        for (FactDropDTO dto:list)
        {
            //向字段集合添加数据,只获取字段为度量类型的数据
             dto.list= FactAttributeMap.INSTANCES.poDropToDto(attributeMapper.selectList(attribute).stream().filter(e->e.getFactId()==dto.id && e.attributeType== FactAttributeEnum.MEASURE.getValue()).collect(Collectors.toList()));
        }
        return list;
    }

    @Override
    public List<FactScreenDropDTO> getFactScreenDropList()
    {
        //获取事实表数据
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        return FactMap.INSTANCES.dropScreenPoToDto(mapper.selectList(queryWrapper.orderByDesc("create_time")));
    }

    @Override
    public ResultEnum updateFactSql(DimensionSqlDTO dto)
    {
        FactPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript = dto.sqlScript;
        model.appId = dto.appId;
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

    public List<MetaDataTableAttributeDTO> getFactMetaData(long businessId,
                                                           String dbQualifiedName,
                                                           Integer dataModelType,
                                                           String businessAdmin) {
        List<FactPO> factPOList = this.query()
                .eq("business_id", businessId)
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
            column.owner = table.owner;
            columnList.add(column);
        }
        return columnList;
    }

}
