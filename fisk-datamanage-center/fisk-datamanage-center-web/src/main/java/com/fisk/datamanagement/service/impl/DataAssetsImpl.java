package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;
import com.fisk.datamanagement.service.IDataAssets;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class DataAssetsImpl implements IDataAssets {

    @Resource
    GenerateCondition generateCondition;
    @Resource
    EntityImpl entity;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    UserClient userClient;

    @Override
    public DataAssetsResultDTO getDataAssetsTableList(DataAssetsParameterDTO dto) {
        DataAssetsResultDTO data = new DataAssetsResultDTO();
        Connection conn = null;
        Statement st = null;
        try {
            JSONObject instanceEntity = null;
            Boolean exist = redisTemplate.hasKey("metaDataEntityData:" + dto.instanceGuid);
            if (exist) {
                String datas = redisTemplate.opsForValue().get("metaDataEntityData:" + dto.instanceGuid).toString();
                instanceEntity = JSON.parseObject(datas);
            } else {
                entity.setRedis(dto.instanceGuid);
                String datas = redisTemplate.opsForValue().get("metaDataEntityData:" + dto.instanceGuid).toString();
                instanceEntity = JSON.parseObject(datas);
            }

            JSONObject entity = JSON.parseObject(instanceEntity.getString("entity"));
            JSONObject attributes = JSON.parseObject(entity.getString("attributes"));
            String hostname = attributes.getString("hostname");
            //获取账号密码
            ResultEntity<List<DataSourceDTO>> allFiDataDataSource = userClient.getAllFiDataDataSource();
            if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            Optional<DataSourceDTO> first = allFiDataDataSource.data
                    .stream()
                    .filter(e -> dto.dbName.equals(e.conDbname) && hostname.equals(e.conIp))
                    .findFirst();
            if (!first.isPresent()) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            //连接数据源
            conn = getConnection(first.get());
            st = conn.createStatement();
            //拼接筛选条件
            String condition = " where 1=1 ";
            if (CollectionUtils.isNotEmpty(dto.filterQueryDTOList)) {
                condition += generateCondition.getCondition(dto.filterQueryDTOList);
            }
            String sql = null;
            //是否导出
            if (dto.export) {
                sql = "select * from " + dto.tableName + condition;
            }else {
                //获取总条数
                String getTotalSql = "select count(*) as totalNum from " + dto.tableName+condition;
                ResultSet rSet = st.executeQuery(getTotalSql);
                int rowCount = 0;
                if (rSet.next()) {
                    rowCount = rSet.getInt("totalNum");
                }
                rSet.close();
                data.total=rowCount;
                //分页获取数据
                sql = buildSelectSql(dto, condition, first.get().conType);
            }
            ResultSet rs = st.executeQuery(sql);
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            data.dataArray=columnDataList(rs,metaData,columnCount);
            //获取列名
            List<String> columnList=new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(metaData.getColumnLabel(i));
            }
            data.columnList = columnList;
            data.pageIndex = dto.pageIndex;
            data.pageSize = dto.pageSize;
        } catch (Exception e) {
            log.error("数据资产,查询表数据失败:{}", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return data;
    }

    /**
     * 获取行数据
     * @param rs
     * @param metaData
     * @param columnCount
     * @return
     */
    public JSONArray columnDataList(ResultSet rs,ResultSetMetaData metaData,int columnCount){
        try {
            // json数组
            JSONArray array = new JSONArray();
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
            return array;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
    }

    /**
     * 连接数据库
     *
     * @param dto
     * @return statement
     */
    private Connection getConnection(DataSourceDTO dto) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(dto.conStr, dto.conAccount, dto.conPassword, dto.conType);
    }

    /**
     * 拼接分页语句
     *
     * @param dto
     * @param condition
     * @param typeEnum
     * @return
     */
    public String buildSelectSql(DataAssetsParameterDTO dto, String condition, DataSourceTypeEnum typeEnum) {
        StringBuilder str = new StringBuilder();
        //分页获取数据
        int offset = (dto.pageIndex - 1) * dto.pageSize;
        int skipCount = dto.pageIndex * dto.pageSize;
        switch (typeEnum) {
            case SQLSERVER:
                str.append("select top ");
                str.append(dto.pageSize);
                str.append(" * from (select row_number() over(order by ");
                str.append(dto.columnName + " asc ) as rownumber,* from ");
                str.append(dto.tableName);
                str.append(") temp_row ");
                str.append(condition);
                str.append(" and rownumber>");
                str.append(offset);
                break;
            case ORACLE:
                str.append("select * from ( select rownum, t.* from ");
                str.append(dto.tableName);
                str.append(" t ");
                str.append(condition);
                str.append(" and rownum <= " + skipCount);
                str.append(" ) table_alias where table_alias.\"ROWNUM\" >= " + offset);
                break;
            case MYSQL:
            case POSTGRESQL:
            case DORIS:
                str.append("select * from ");
                str.append(dto.tableName + condition);
                str.append(" order by " + dto.columnName);
                str.append(" limit ");
                str.append(dto.pageSize);
                str.append(" offset ");
                str.append(offset);
                break;
            default:
                throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }
        return str.toString();
    }

}
