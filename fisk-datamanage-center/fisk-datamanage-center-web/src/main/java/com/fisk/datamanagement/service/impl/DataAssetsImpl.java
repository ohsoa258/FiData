package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;
import com.fisk.datamanagement.service.IDataAssets;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
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
    UserClient userClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;

    @Override
    public DataAssetsResultDTO getDataAssetsTableList(DataAssetsParameterDTO dto) {
        DataAssetsResultDTO data = new DataAssetsResultDTO();
        Connection conn = null;
        Statement st = null;
        PreparedStatement psst = null;
        try {
            //获取账号密码
            ResultEntity<List<DataSourceDTO>> allFiDataDataSource = userClient.getAllFiDataDataSource();
            if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            log.debug("get datasource");
            Optional<DataSourceDTO> first = allFiDataDataSource.data
                    .stream()
                    .filter(e -> dto.dbName.equals(e.conDbname))
                    .findFirst();
            if (!first.isPresent()) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            log.debug("constr{},conip{},conport{}", first.get().conStr,first.get().conIp,first.get().conPort);
            //连接数据源
            conn = getConnection(first.get());

            conn.setAutoCommit(false);
            log.debug("con commit");
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
                st = conn.createStatement();
                ResultSet rSet = st.executeQuery(getTotalSql);
                int rowCount = 0;
                if (rSet.next()) {
                    rowCount = rSet.getInt("totalNum");
                }
                rSet.close();
                data.total = rowCount;
                //分页获取数据
                sql = buildSelectSql(dto, condition, first.get().conType);
            }
            log.debug("sqlstr:"+sql);
            psst = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            psst.setFetchSize(1000);

            ResultSet rs = psst.executeQuery();
            log.debug("sql play success");
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            data.dataArray = columnDataList(rs, metaData, columnCount);
            log.debug("data:"+ JSON.toJSONString(data.dataArray));
            //获取表头
            List<String[]> displayList = getTableColumnDisplay(first.get().sourceBusinessType, dto.tableName);
            if (CollectionUtils.isEmpty(displayList)) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            if (!dto.export) {
                displayList.addAll(systemTableColumn());
            }

            psst.close();

            data.columnList = displayList;
            data.pageIndex = dto.pageIndex;
            data.pageSize = dto.pageSize;
            log.debug("end");
        } catch (Exception e) {

            log.debug("数据资产,查询表数据失败:{}", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR_INVALID, e);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return data;
    }

    /**
     * 获取表字段显示名称和英文名称
     *
     * @param sourceBusinessType
     * @param tableName
     * @return
     */
    public List<String[]> getTableColumnDisplay(SourceBusinessTypeEnum sourceBusinessType, String tableName) {
        switch (sourceBusinessType) {
            case ODS:
                return dataAccessClient.getTableColumnDisplay(tableName).data;
            case DW:
                return dataModelClient.getTableColumnDisplay(tableName).data;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 系统表字段
     *
     * @return
     */
    public List<String[]> systemTableColumn() {
        List<String[]> list = new ArrayList<>();

        String[] version = new String[2];
        version[0] = "fi_version";
        version[1] = "系统版本号";

        String[] createTime = new String[2];
        createTime[0] = "fi_createtime";
        createTime[1] = "系统创建时间";

        String[] updateTime = new String[2];
        updateTime[0] = "fi_updatetime";
        updateTime[1] = "系统更新时间";

        String[] batchCode = new String[2];
        batchCode[0] = "fidata_batch_code";
        batchCode[1] = "系统批次号";


        list.add(version);
        list.add(createTime);
        list.add(updateTime);
        list.add(batchCode);
        return list;
    }

    /**
     * 获取行数据
     *
     * @param rs
     * @param metaData
     * @param columnCount
     * @return
     */
    public JSONArray columnDataList(ResultSet rs, ResultSetMetaData metaData, int columnCount) {
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
            log.error("元数据获取行数据失败：{}", e);
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
