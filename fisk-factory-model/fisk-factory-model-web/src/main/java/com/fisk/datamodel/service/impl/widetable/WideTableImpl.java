package com.fisk.datamodel.service.impl.widetable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.datamodel.BuildDataModelHelper;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceFieldConfigDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.dto.widetableconfig.*;
import com.fisk.datamodel.dto.widetablefieldconfig.WideTableFieldConfigsDTO;
import com.fisk.datamodel.entity.widetable.WideTableConfigPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DataBaseTypeEnum;
import com.fisk.datamodel.enums.RelateTableTypeEnum;
import com.fisk.datamodel.map.widetable.WideTableFieldConfigMap;
import com.fisk.datamodel.map.widetable.WideTableMap;
import com.fisk.datamodel.mapper.widetable.WideTableMapper;
import com.fisk.datamodel.service.IWideTable;
import com.fisk.datamodel.service.impl.AtomicIndicatorsImpl;
import com.fisk.datamodel.service.impl.dimension.DimensionAttributeImpl;
import com.fisk.datamodel.utils.mysql.DataSourceConfigUtil;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class WideTableImpl
        extends ServiceImpl<WideTableMapper, WideTableConfigPO>
        implements IWideTable {

    @Resource
    WideTableMapper mapper;
    @Resource
    UserHelper userHelper;
    @Resource
    DimensionAttributeImpl dimensionAttribute;
    @Resource
    AtomicIndicatorsImpl atomicIndicators;
    @Resource
    WideTableRelationConfigImpl wideTableRelationConfig;
    @Resource
    WideTableFieldConfigImpl wideTableFieldConfig;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    DataSourceConfigUtil dataSourceConfigUtil;
    @Resource
    PublishTaskClient publishTaskClient;

    private static String prefix = "external_";

    @Override
    public List<WideTableListDTO> getWideTableList(int businessId) {
        List<WideTableListDTO> list = new ArrayList<>();
        QueryWrapper<WideTableConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getBusinessId, businessId);
        List<WideTableConfigPO> wideTableConfigPoList = mapper.selectList(queryWrapper);
        for (WideTableConfigPO item : wideTableConfigPoList) {
            WideTableListDTO dto = new WideTableListDTO();
            dto.id = item.id;
            dto.name = item.name;
            dto.dorisPublish = item.dorisPublish;
            List<String> fieldList = new ArrayList<>();
            //获取宽表字段
            WideTableFieldConfigDTO configDTO = JSONObject.parseObject(item.configDetails, WideTableFieldConfigDTO.class);
            for (TableSourceTableConfigDTO e : configDTO.entity) {
                fieldList.addAll(e.columnConfig.stream().map(p -> p.getFieldName()).collect(Collectors.toList()));
            }
            dto.fieldList=fieldList;
            list.add(dto);
        }
        return list;
    }

    @Override
    public WideTableQueryPageDTO executeWideTableSql(WideTableFieldConfigDTO dto)
    {
        if (CollectionUtils.isEmpty(dto.entity) || CollectionUtils.isEmpty(dto.relations)) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }
        StringBuilder appendSql=new StringBuilder();
        appendSql.append("select ");
        //拼接查询字段
        WideTableAliasDTO wideTableAliasDTO = appendField(dto.entity);
        appendSql.append(wideTableAliasDTO.sql);
        //拼接关联表
        appendSql.append(appendRelateTable(dto.relations));
        WideTableQueryPageDTO wideTableData = getWideTableData(appendSql.toString(), dto.pageSize);
        dto.entity=wideTableAliasDTO.entity;
        wideTableData.configDTO=dto;
        return wideTableData;
    }

    /**
     * SQL拼接字段
     *
     * @param entity
     * @return
     */
    public WideTableAliasDTO appendField(List<TableSourceTableConfigDTO> entity) {
        WideTableAliasDTO dto = new WideTableAliasDTO();
        DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
        IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dwSource.conType);
        String sql = command.buildAppendField(entity);
        sql.substring(0, sql.length() - 1);
        dto.entity = entity;
        dto.sql = sql;
        return dto;
    }

    /**
     * SQL拼接关联表
     *
     * @param relations
     * @return
     */
    public String appendRelateTable(List<TableSourceRelationsDTO> relations) {
        DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
        if (dwSource.conType.getValue() == DataBaseTypeEnum.MYSQL.getValue()) {
            List<TableSourceRelationsDTO> fullJoin = relations.stream().filter(e -> RelateTableTypeEnum.FULL_JOIN.getName().equals(e.joinType)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(fullJoin)) {
                throw new FkException(ResultEnum.NOT_SUPPORT_FULL_JOIN);
            }
        }
        IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dwSource.conType);
        return command.buildAppendRelationTable(relations);
    }

    /**
     * 表名添加前缀
     *
     * @param tableName
     * @return
     */
    public String prefixTable(String tableName) {
        return prefix + tableName;
    }

    /**
     * 查询关联表数据
     *
     * @param sql
     * @param pageSize
     * @return
     */
    public WideTableQueryPageDTO getWideTableData(String sql, int pageSize) {
        WideTableQueryPageDTO data = new WideTableQueryPageDTO();
        Connection conn = null;
        Statement st = null;
        try {
            String newSql = sql.replace(prefix, "");

            DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
            IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dwSource.conType);
            newSql = command.buildPageSql(newSql, pageSize);

            conn = dataSourceConfigUtil.getConnection();
            st = conn.createStatement();

            ResultSet rs = st.executeQuery(newSql);
            // json数组
            JSONArray array = new JSONArray();
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    String value = rs.getString(columnName);
                    jsonObj.put(columnName, value==null?"":value);
                }
                array.add(jsonObj);
            }
            data.dataArray=array;
            data.sqlScript=sql;
            //获取列名
            List<String> columnList = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(metaData.getColumnLabel(i));
            }
            data.columnList = columnList;
        } catch (SQLException e) {
            log.error("getWideTableData:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return data;
    }


    public WideTableQueryPageDTO getWideTableData(String sql, int pageSize, String aliasName) {
        WideTableQueryPageDTO data = new WideTableQueryPageDTO();
        Connection conn = null;
        Statement st = null;
        try {
            String newSql = sql.replace(prefix, "");

            DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
            IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dwSource.conType);
            newSql = command.buildPageSql(newSql, pageSize);

            conn = dataSourceConfigUtil.getConnection();
            st = conn.createStatement();

            ResultSet rs = st.executeQuery(newSql);
            // json数组
            JSONArray array = new JSONArray();
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    String value = rs.getString(columnName);
                    jsonObj.put(aliasName+columnName, value==null?"":value);
                }
                array.add(jsonObj);
            }
            data.dataArray = array;
            data.sqlScript = sql;
            //获取列名
            List<String> columnList = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(aliasName + metaData.getColumnLabel(i));
            }
            data.columnList = columnList;
        } catch (SQLException e) {
            log.error("getWideTableData ex:", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addWideTable(WideTableConfigDTO dto) {
        QueryWrapper<WideTableConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getName, dto.name);
        WideTableConfigPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        //新增宽表
        int flat = mapper.insertWideTable(dto);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return WideTableOtherConfig(dto);
    }

    @Override
    public WideTableConfigDTO getWideTable(int id)
    {
        WideTableConfigPO po=mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return WideTableMap.INSTANCES.poToDto(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateWideTable(WideTableConfigDTO dto)
    {
        //判断是否存在
        WideTableConfigPO po=mapper.selectById(dto.id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //判断表名是否重复
        QueryWrapper<WideTableConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getName, dto.name);
        WideTableConfigPO model = mapper.selectOne(queryWrapper);
        if (model != null && model.id != dto.id) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        int flat = mapper.updateById(WideTableMap.INSTANCES.dtoToPo(dto));
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return WideTableOtherConfig(dto);
    }

    public ResultEnum WideTableOtherConfig(WideTableConfigDTO dto) {
        //新增字段和关联关系配置
        WideTableConfigInfoDTO data = JSONObject.parseObject(dto.configDetails, WideTableConfigInfoDTO.class);
        wideTableRelationConfig.wideTableRelationConfig(dto.id, data.relations);
        //获取字段配置
        List<WideTableFieldConfigsDTO> fieldList = new ArrayList<>();
        for (TableSourceTableConfigDTO table : data.entity) {
            for (TableSourceFieldConfigDTO fields : table.columnConfig) {
                WideTableFieldConfigsDTO field = WideTableFieldConfigMap.INSTANCES.sourceToDto(fields);
                field.tableName = table.tableName;
                field.tableType = table.tableType;
                field.wideTableId = dto.id;
                fieldList.add(field);
            }
        }
        wideTableFieldConfig.wideTableFieldConfig(dto.id, fieldList);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteWideTable(int id) {
        Connection conn = null;
        Statement st = null;
        try {
            WideTableConfigPO po = mapper.selectById(id);
            if (po == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            conn = dataSourceConfigUtil.getConnection();
            st = conn.createStatement();
            String delSql = "drop table " + po.name;
            boolean execute = st.execute(delSql);
            if (execute) {
                throw new FkException(ResultEnum.SQL_ERROR);
            }
            // 删除factory-dispatch对应的表配置
            List<DeleteTableDetailDTO> list = new ArrayList<>();
            DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
            deleteTableDetailDto.appId = String.valueOf(po.businessId);
            deleteTableDetailDto.tableId = String.valueOf(id);
            // 分析宽表
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.OLAP_WIDETABLE_TASK;
            list.add(deleteTableDetailDto);
            dataFactoryClient.editByDeleteTable(list);

            return mapper.deleteByIdWithFill(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } catch (SQLException e) {
            log.error("deleteWideTable ex:", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }

    /**
     * 宽表发布
     *
     * @param dto
     */
    public void publishWideTable(IndicatorQueryDTO dto) {
        try {
            if (CollectionUtils.isEmpty(dto.wideTableIds)) {
                return;
            }
            for (Integer id : dto.wideTableIds) {
                WideTableConfigPO po = mapper.selectById(id);
                if (po == null) {
                    continue;
                }
                WideTableFieldConfigTaskDTO data = WideTableMap.INSTANCES.poToTaskDto(po);
                data.userId = userHelper.getLoginUserInfo().id;
                data.sqlScript.toLowerCase();
                JSONObject jsonObject = JSONObject.parseObject(po.configDetails);
                data.entity = JSONObject.parseArray(jsonObject.getString("entity"), TableSourceTableConfigDTO.class);
                data.relations = JSONObject.parseArray(jsonObject.getString("relations"), TableSourceRelationsDTO.class);
                //创建外部表
                createExternalTable(data.entity, dto);
                //宽表创建
                publishTaskClient.publishBuildWideTableTask(data);
            }
        } catch (Exception e) {
            log.error("publishWideTable ex:", e);
        }
    }

    /**
     * doris创建维度和事实外部表
     *
     * @param entity
     * @param dto
     */
    public void createExternalTable(List<TableSourceTableConfigDTO> entity, IndicatorQueryDTO dto) {
        BusinessAreaGetDataDTO data = new BusinessAreaGetDataDTO();
        data.businessAreaId = dto.businessAreaId;
        data.userId = userHelper.getLoginUserInfo().id;
        //过滤维度
        List<Integer> dimensionIdList = entity.stream()
                .filter(e -> e.tableType == CreateTypeEnum.CREATE_DIMENSION.getValue())
                .map(e -> e.getTableId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionIdList)) {
            List<ModelMetaDataDTO> dimensionList = new ArrayList<>();
            for (Integer dimensionId : dimensionIdList) {
                dimensionList.add(dimensionAttribute.getDimensionMetaData(dimensionId));
            }
            data.dimensionList = dimensionList;
        }
        //过滤事实
        List<Integer> factIdList = entity.stream()
                .filter(e->e.tableType==CreateTypeEnum.CREATE_FACT.getValue())
                .map(e -> e.getTableId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(factIdList)) {
            data.atomicIndicatorList = atomicIndicators.atomicIndicatorPush(factIdList);
        }
        if (CollectionUtils.isEmpty(data.atomicIndicatorList) && CollectionUtils.isEmpty(data.dimensionList)) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }
        publishTaskClient.publishOlapCreateModel(data);
    }

    @Override
    public void updateWideTablePublishStatus(ModelPublishStatusDTO dto) {
        WideTableConfigPO po = mapper.selectById(dto.id);
        if (po == null) {
            return;
        }
        po.dorisPublish = dto.status;
        mapper.updateById(po);
    }

}
