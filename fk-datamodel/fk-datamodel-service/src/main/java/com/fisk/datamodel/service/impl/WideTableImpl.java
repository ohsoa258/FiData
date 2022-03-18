package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.widetableconfig.*;
import com.fisk.datamodel.entity.WideTableConfigPO;
import com.fisk.datamodel.map.WideTableMap;
import com.fisk.datamodel.mapper.WideTableMapper;
import com.fisk.datamodel.service.IWideTable;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class WideTableImpl implements IWideTable {

    @Resource
    WideTableMapper mapper;
    @Resource
    DimensionImpl dimensionImpl;
    @Resource
    UserHelper userHelper;

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
        List<WideTableConfigPO> wideTableConfigPOList=mapper.selectList(queryWrapper);
        for (WideTableConfigPO item:wideTableConfigPOList)
        {
            WideTableListDTO dto=new WideTableListDTO();
            dto.id=item.id;
            dto.name=item.name;
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
        WideTableAliasDTO wideTableAliasDTO = appendField(dto.entity);
        appendSql.append(wideTableAliasDTO.sql);
        WideTableSourceRelationsDTO firstTable = dto.relations.get(0);
        appendSql.append(" from "+"external_"+firstTable.sourceTable+" "+firstTable.joinType+" "+"external_"+firstTable.targetTable
                        + " on "+"external_"+firstTable.sourceTable+"."+firstTable.sourceColumn
                        +" = "+"external_"+firstTable.targetTable+"."+firstTable.targetColumn
        );
        if (dto.relations.size()>1)
        {
            for (int i=1;i<dto.relations.size();i++)
            {
                WideTableSourceRelationsDTO attribute=dto.relations.get(i);
                appendSql.append(" "+attribute.joinType+" ");
                appendSql.append("external_"+attribute.targetTable+" on ");
                appendSql.append("external_"+attribute.sourceTable+"."+attribute.sourceColumn+" = ");
                appendSql.append("external_"+attribute.targetTable+"."+attribute.targetColumn+" ");
            }
        }
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
                    field.alias="external_"+item.tableName+"_"+field.fieldName;
                    str.append("external_"+item.tableName+"."+field.fieldName+" as "+"external_"+item.tableName+"_"+field.fieldName+",");
                }
                else {
                    str.append("external_"+item.tableName+"."+field.fieldName+",");
                }
                fieldList.add(field.fieldName);
            }
        }
        str.deleteCharAt(str.length()-1);
        dto.entity=entity;
        dto.sql=str.toString();
        return dto;
    }

    public WideTableQueryPageDTO getWideTableData(String sql,int pageSize) {
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
            return ResultEnum.DATA_EXISTS;
        }
        return mapper.insert(WideTableMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<WideTableConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getName,dto.name);
        WideTableConfigPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
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
                return ResultEnum.DATA_NOTEXISTS;
            }
            Connection conn = dimensionImpl.getStatement(driver, url, userName, password);
            Statement st = conn.createStatement();
            String delSql="drop table "+po.name;
            boolean execute = st.execute(delSql);
            if (execute)
            {
                return ResultEnum.SQL_ERROR;
            }
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
     * @param ids
     */
    public void publishWideTable(List<Integer> ids){

        try {
            if (!CollectionUtils.isEmpty(ids))
            {
                return;
            }
            for (Integer id:ids)
            {
                WideTableConfigPO po=mapper.selectById(id);
                if (po==null)
                {
                    continue;
                }
                WideTableFieldConfigTaskDTO dto=WideTableMap.INSTANCES.poToTaskDto(po);
                dto.userId=userHelper.getLoginUserInfo().id;
                JSONObject jsonObject=JSONObject.parseObject(po.configDetails);
                dto.entity=JSONObject.parseArray(jsonObject.getString("entity"),WideTableSourceTableConfigDTO.class);
                dto.relations=JSONObject.parseArray(jsonObject.getString("relations"),WideTableSourceRelationsDTO.class);
                publishTaskClient.publishBuildWideTableTask(dto);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
