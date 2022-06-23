package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.dto.widetableconfig.*;
import com.fisk.datamodel.entity.WideTableConfigPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DataBaseTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.RelateTableTypeEnum;
import com.fisk.datamodel.map.WideTableMap;
import com.fisk.datamodel.mapper.WideTableMapper;
import com.fisk.datamodel.service.IWideTable;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class WideTableImpl extends ServiceImpl<WideTableMapper,WideTableConfigPO> implements IWideTable {

    @Resource
    WideTableMapper mapper;
    @Resource
    DimensionImpl dimensionImpl;
    @Resource
    UserHelper userHelper;
    @Resource
    DimensionAttributeImpl dimensionAttribute;
    @Resource
    AtomicIndicatorsImpl atomicIndicators;
    @Resource
    private DataFactoryClient dataFactoryClient;

    @Value("${generate.date-dimension.datasource.typeName}")
    private String typeName;
    @Value("${generate.date-dimension.datasource.driver}")
    private String driver;
    @Value("${generate.date-dimension.datasource.url}")
    private String url;
    @Value("${generate.date-dimension.datasource.userName}")
    private String userName;
    @Value("${generate.date-dimension.datasource.password}")
    private String password;
    @Resource
    PublishTaskClient publishTaskClient;

    @Override
    public List<WideTableListDTO> getWideTableList(int businessId)
    {
        List<WideTableListDTO> list=new ArrayList<>();
        QueryWrapper<WideTableConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getBusinessId,businessId);
        List<WideTableConfigPO> wideTableConfigPoList=mapper.selectList(queryWrapper);
        for (WideTableConfigPO item:wideTableConfigPoList)
        {
            WideTableListDTO dto=new WideTableListDTO();
            dto.id=item.id;
            dto.name=item.name;
            dto.dorisPublish=item.dorisPublish;
            List<String> fieldList=new ArrayList<>();
            //获取宽表字段
            WideTableFieldConfigDTO configDTO = JSONObject.parseObject(item.configDetails, WideTableFieldConfigDTO.class);
            for (WideTableSourceTableConfigDTO e : configDTO.entity) {
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
        if (CollectionUtils.isEmpty(dto.entity) || CollectionUtils.isEmpty(dto.relations))
        {
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
     * @param entity
     * @return
     */
    public WideTableAliasDTO appendField(List<WideTableSourceTableConfigDTO> entity)
    {
        WideTableAliasDTO dto=new WideTableAliasDTO();
        StringBuilder str=new StringBuilder();
        List<String> fieldList=new ArrayList<>();
        for (WideTableSourceTableConfigDTO item:entity)
        {
            for (WideTableSourceFieldConfigDTO field:item.columnConfig)
            {
                //判断字段名称是否重复
                if (fieldList.contains(field.fieldName))
                {
                    field.alias = item.tableName + "_" + field.fieldName;
                    str.append("external_" + item.tableName + "." + field.fieldName + " as " + field.alias + ",");
                }
                else {
                    str.append("external_"+item.tableName+"."+field.fieldName+",");
                    fieldList.add(field.fieldName);
                }
            }
        }
        str.deleteCharAt(str.length()-1);
        dto.entity=entity;
        dto.sql=str.toString();
        return dto;
    }

    /**
     * SQL拼接关联表
     * @param relations
     * @return
     */
    public StringBuilder appendRelateTable(List<WideTableSourceRelationsDTO> relations) {
        DataBaseTypeEnum value = DataBaseTypeEnum.getValue(typeName.toLowerCase());
        if (value.getValue() == DataBaseTypeEnum.MYSQL.getValue()) {
            List<WideTableSourceRelationsDTO> fullJoin = relations.stream().filter(e -> RelateTableTypeEnum.FULL_JOIN.getName().equals(e.joinType)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(fullJoin)) {
                throw new FkException(ResultEnum.NOT_SUPPORT_FULL_JOIN);
            }
        }
        StringBuilder appendSql = new StringBuilder();
        Map<String, List<WideTableSourceRelationsDTO>> groupMap = relations.stream().collect(Collectors.groupingBy(WideTableSourceRelationsDTO::getSourceTable));
        for (int i = 0; i < relations.size(); i++) {
            if (i == 0) {
                appendSql.append(" from " + prefixTable(relations.get(i).sourceTable) + " ");
                appendSql.append(relations.get(i).joinType + " " + prefixTable(relations.get(i).targetTable));
                appendSql.append(" on " + prefixTable(relations.get(i).sourceTable) + "." + relations.get(i).sourceColumn);
                appendSql.append(" = ");
                appendSql.append(prefixTable(relations.get(i).targetTable) + "." + relations.get(i).targetColumn);
            } else {
                WideTableSourceRelationsDTO attribute = relations.get(i);
                appendSql.append(" " + attribute.joinType + " ");
                appendSql.append(prefixTable(attribute.targetTable));
                appendSql.append(" on " + prefixTable(attribute.sourceTable) + "." + attribute.sourceColumn + " = ");
                appendSql.append(prefixTable(attribute.targetTable) + "." + attribute.targetColumn + " ");
            }
            //判断on后面关联条件是否存在多个
            for (Map.Entry<String, List<WideTableSourceRelationsDTO>> map : groupMap.entrySet()) {
                for (WideTableSourceRelationsDTO item : map.getValue()) {
                    if (relations.get(i) != item && item.targetTable.equals(relations.get(i).targetTable)) {
                        appendSql.append(" and " + prefixTable(item.sourceTable) + "." + item.sourceColumn);
                        appendSql.append(" = ");
                        appendSql.append(prefixTable(item.targetTable) + "." + item.targetColumn);
                        relations.remove(item);
                    }
                }
            }
        }
        return appendSql;
    }

    /**
     * 表名添加前缀
     *
     * @param tableName
     * @return
     */
    public String prefixTable(String tableName) {
        return "external_" + tableName;
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
        try {
            String newSql = sql.replace("external_", "");
            Connection conn = dimensionImpl.getStatement(driver, url, userName, password);
            Statement st = conn.createStatement();
            switch (typeName.toLowerCase()) {
                case "mysql":
                    newSql = newSql + " limit " + pageSize;
                    break;
                case "postgresql":
                    newSql=newSql+" limit  "+pageSize;
                    break;
                case "doris":
                    newSql=newSql+"  limit "+pageSize;
                    break;
                case "sqlserver":
                    newSql="select top "+pageSize+" * from ("+sql+") as tabInfo";
                    break;
                default:
                    throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }

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
            List<String> columnList=new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(metaData.getColumnLabel(i));
            }
            data.columnList=columnList;
        } catch (SQLException e) {
            log.error("getWideTableData:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e.getMessage());
        }
        return data;
    }


    public WideTableQueryPageDTO getWideTableData(String sql,int pageSize,String aliasName) {
        WideTableQueryPageDTO data=new WideTableQueryPageDTO();
        try {
            String newSql=sql.replace("external_","");
            Connection conn = dimensionImpl.getStatement(driver, url, userName, password);
            Statement st = conn.createStatement();
            switch (typeName.toLowerCase())
            {
                case "mysql":
                    newSql=newSql+" limit "+pageSize;
                    break;
                case "postgresql":
                    newSql=newSql+" limit  "+pageSize;
                    break;
                case "doris":
                    newSql=newSql+"  limit "+pageSize;
                    break;
                case "sqlserver":
                    newSql="select top "+pageSize+" * from ("+sql+") as tabInfo";
                    break;
                default:
                    throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }

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
            data.dataArray=array;
            data.sqlScript=sql;
            //获取列名
            List<String> columnList=new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(aliasName+metaData.getColumnLabel(i));
            }
            data.columnList=columnList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public ResultEnum addWideTable(WideTableConfigDTO dto)
    {
        QueryWrapper<WideTableConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getName,dto.name);
        WideTableConfigPO po=mapper.selectOne(queryWrapper);
        if (po!=null)
        {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        WideTableConfigPO data = WideTableMap.INSTANCES.dtoToPo(dto);
        data.dorisPublish=PublicStatusEnum.UN_PUBLIC.getValue();
        return mapper.insert(data)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public WideTableConfigDTO getWideTable(int id)
    {
        WideTableConfigPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return WideTableMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateWideTable(WideTableConfigDTO dto)
    {
        WideTableConfigPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        QueryWrapper<WideTableConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getName,dto.name);
        WideTableConfigPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        return mapper.updateById(WideTableMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteWideTable(int id)
    {
        try {
            WideTableConfigPO po=mapper.selectById(id);
            if (po==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            Connection conn = dimensionImpl.getStatement(driver, url, userName, password);
            Statement st = conn.createStatement();
            String delSql="drop table "+po.name;
            boolean execute = st.execute(delSql);
            if (execute)
            {
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

            return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 宽表发布
     * @param dto
     */
    public void publishWideTable(IndicatorQueryDTO dto){
        try {
            if (CollectionUtils.isEmpty(dto.wideTableIds))
            {
                return;
            }
            for (Integer id:dto.wideTableIds)
            {
                WideTableConfigPO po=mapper.selectById(id);
                if (po==null)
                {
                    continue;
                }
                WideTableFieldConfigTaskDTO data=WideTableMap.INSTANCES.poToTaskDto(po);
                data.userId=userHelper.getLoginUserInfo().id;
                data.sqlScript.toLowerCase();
                JSONObject jsonObject=JSONObject.parseObject(po.configDetails);
                data.entity=JSONObject.parseArray(jsonObject.getString("entity"),WideTableSourceTableConfigDTO.class);
                data.relations=JSONObject.parseArray(jsonObject.getString("relations"),WideTableSourceRelationsDTO.class);
                //创建外部表
                createExternalTable(data.entity,dto);
                //宽表创建
                publishTaskClient.publishBuildWideTableTask(data);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * doris创建维度和事实外部表
     * @param entity
     * @param dto
     */
    public void createExternalTable(List<WideTableSourceTableConfigDTO> entity,IndicatorQueryDTO dto){
        BusinessAreaGetDataDTO data=new BusinessAreaGetDataDTO();
        data.businessAreaId=dto.businessAreaId;
        data.userId=userHelper.getLoginUserInfo().id;
        //过滤维度
        List<Integer> dimensionIdList = entity.stream()
                .filter(e->e.tableType==CreateTypeEnum.CREATE_DIMENSION.getValue())
                .map(e -> e.getTableId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionIdList))
        {
            List<ModelMetaDataDTO> dimensionList=new ArrayList<>();
            for (Integer dimensionId:dimensionIdList)
            {
                dimensionList.add(dimensionAttribute.getDimensionMetaData(dimensionId));
            }
            data.dimensionList=dimensionList;
        }
        //过滤事实
        List<Integer> factIdList = entity.stream()
                .filter(e->e.tableType==CreateTypeEnum.CREATE_FACT.getValue())
                .map(e -> e.getTableId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(factIdList))
        {
            data.atomicIndicatorList=atomicIndicators.atomicIndicatorPush(factIdList);
        }
        if (CollectionUtils.isEmpty(data.atomicIndicatorList) && CollectionUtils.isEmpty(data.dimensionList))
        {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }
        publishTaskClient.publishOlapCreateModel(data);
    }

    @Override
    public void updateWideTablePublishStatus(ModelPublishStatusDTO dto)
    {
        WideTableConfigPO po=mapper.selectById(dto.id);
        if (po !=null)
        {
            po.dorisPublish=dto.status;
            mapper.updateById(po);
        }
    }

}
