package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.widetableconfig.*;
import com.fisk.datamodel.entity.WideTableConfigPO;
import com.fisk.datamodel.mapper.WideTableMapper;
import com.fisk.datamodel.service.IWideTable;
import org.junit.Test;
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
        appendSql.append(appendField(dto.entity));
        WideTableSourceRelationsDTO firstTable = dto.relations.get(0);
        appendSql.append(" from "+firstTable.sourceTable+" "+firstTable.joinType+" "+firstTable.targetTable
                        + " on "+firstTable.sourceTable+"."+firstTable.sourceColumn
                        +" = "+firstTable.targetTable+"."+firstTable.targetColumn
        );
        if (dto.relations.size()>1)
        {
            for (int i=1;i<dto.relations.size();i++)
            {
                WideTableSourceRelationsDTO attribute=dto.relations.get(i);
                appendSql.append(" "+attribute.joinType+" ");
                appendSql.append(attribute.targetTable+" on ");
                appendSql.append(attribute.sourceTable+"."+attribute.sourceColumn+" = ");
                appendSql.append(attribute.targetTable+"."+attribute.targetColumn+" ");
            }
        }
        return getWideTableData(appendSql.toString());
    }

    /**
     * SQL拼接字段
     * @param entity
     * @return
     */
    public String appendField(List<WideTableSourceTableConfigDTO> entity)
    {
        StringBuilder str=new StringBuilder();
        for (WideTableSourceTableConfigDTO item:entity)
        {
            for (WideTableSourceFieldConfigDTO field:item.columnConfig)
            {
                str.append(item.tableName+"."+field.fieldName+",");
            }
        }
        str.deleteCharAt(str.length()-1);
        return str.toString();
    }

    public WideTableQueryPageDTO getWideTableData(String sql) {
        WideTableQueryPageDTO data=new WideTableQueryPageDTO();
        try {
            String drive="com.mysql.jdbc.Driver";
            String url="jdbc:mysql://192.168.11.130:3306/dmp_datamodel_db";
            String username="root";
            String password="root123";
            Connection conn = dimensionImpl.getStatement(drive, url, username, password);
            Statement st = conn.createStatement();
            String sqlType="mysql";
            switch (sqlType)
            {
                case "mysql":
                    sql=sql+" limit "+10;
                    break;
                default:
                    break;
            }
            ResultSet rs = st.executeQuery(sql);
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
            //获取列名
            List<String> columnList=new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(metaData.getColumnLabel(i));
            }
            data.columnList=columnList;
            data.pageSize=10;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

}
