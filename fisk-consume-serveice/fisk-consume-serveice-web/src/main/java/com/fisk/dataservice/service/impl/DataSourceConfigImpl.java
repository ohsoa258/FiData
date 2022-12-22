package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.core.utils.dbutils.utils.MySqlConUtils;
import com.fisk.common.core.utils.dbutils.utils.OracleUtils;
import com.fisk.common.core.utils.dbutils.utils.PgSqlUtils;
import com.fisk.common.core.utils.dbutils.utils.SqlServerUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryResultDTO;
import com.fisk.dataservice.service.IDataSourceConfig;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class DataSourceConfigImpl implements IDataSourceConfig {

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    UserClient client;

    @Resource
    TableServiceImpl tableService;

    public static DataSourceQueryResultDTO resultSetToJsonArrayTableService(ResultSet rs) throws SQLException, JSONException {
        DataSourceQueryResultDTO data = new DataSourceQueryResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldNameDTO> fieldNameDTOList = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        int count = 1;
        // 预览展示10行
        int row = 10;
        while (rs.next() && count <= row) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(columnName) || tableName.equals("ods_" + columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }
        //获取列名
        for (int i = 1; i <= columnCount; i++) {
            FieldNameDTO dto = new FieldNameDTO();
            dto.sourceTableName = metaData.getTableName(i);
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.sourceFieldType = metaData.getColumnTypeName(i).toUpperCase();
            dto.sourceFieldPrecision = metaData.getScale(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName = metaData.getTableName(i) + "key";
            if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(dto.fieldName)
                    || tableName.equals("ods_" + dto.fieldName)) {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toLowerCase();
            dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i))) ? "255" : String.valueOf(metaData.getColumnDisplaySize(i));
            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;

        return data;
    }

    /**
     * 不同数据库类型转换
     *
     * @param dataSourceTypeEnum
     * @param fieldList
     * @param sqlType
     */
    public void typeConversion(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum dataSourceTypeEnum,
                               List<FieldNameDTO> fieldList,
                               DataSourceTypeEnum sqlType) {

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(dataSourceTypeEnum);
        DataTypeConversionDTO dto = new DataTypeConversionDTO();
        for (FieldNameDTO field : fieldList) {
            dto.dataLength = field.fieldLength;
            dto.dataType = field.fieldType;
            dto.precision = field.sourceFieldPrecision;
            String[] data = command.dataTypeConversion(dto, sqlType);
            field.fieldType = data[0].toUpperCase();
            field.fieldLength = data[1];
        }

    }

    @Override
    public List<TableColumnDTO> getColumn(DataSourceColumnQueryDTO dto) {
        //获取数据源
        List<DataSourceDTO> allFiDataDataSource = getAllFiDataDataSource();

        //过滤数据
        Optional<DataSourceDTO> first = allFiDataDataSource.stream()
                .filter(e -> e.conDbname.equals(dto.dbName)).findFirst();
        if (!first.isPresent()) {
            return new ArrayList<>();
        }

        return getColumnByTableName(first.get(), dto.tableName);
    }

    @Override
    public List<DataSourceInfoDTO> getTableInfoList() {
        //获取数据源
        List<DataSourceDTO> allFiDataDataSource = getAllFiDataDataSource();

        //过滤数据
        List<DataSourceDTO> dataList = allFiDataDataSource.stream()
                .filter(e -> e.sourceBusinessType == SourceBusinessTypeEnum.DW
                        || e.sourceBusinessType == SourceBusinessTypeEnum.ODS)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataList)) {
            return new ArrayList<>();
        }

        List<DataSourceInfoDTO> list = new ArrayList<>();

        for (DataSourceDTO item : dataList) {
            DataSourceInfoDTO data = new DataSourceInfoDTO();
            data.dbId = item.id;
            data.dbName = item.name;
            data.tableNameList = getDbTable(item);
            list.add(data);
        }

        return list;
    }

    @Override
    public DataSourceQueryResultDTO getTableServiceQueryList(DataSourceQueryDTO dto) {
        //获取数据源
        List<DataSourceDTO> allFiDataDataSource = getAllFiDataDataSource();

        //过滤数据
        Optional<DataSourceDTO> first = allFiDataDataSource.stream()
                .filter(e -> e.conDbname.equals(dto.dbName)).findFirst();
        if (!first.isPresent()) {
            return new DataSourceQueryResultDTO();
        }

        Instant inst1 = Instant.now();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        DataSourceQueryResultDTO data = new DataSourceQueryResultDTO();

        try {

            AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
            conn = helper.connection(first.get().conStr, first.get().conAccount, first.get().conPassword, first.get().conType);
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setMaxRows(10);

            //系统变量替换
            if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
                for (DeltaTimeDTO item : dto.deltaTimes) {
                    boolean empty = StringUtils.isEmpty(item.variableValue);
                    if (item.deltaTimeParameterTypeEnum != DeltaTimeParameterTypeEnum.VARIABLE || empty) {
                        continue;
                    }
                    item.variableValue = AbstractCommonDbHelper.executeTotalSql(item.variableValue, conn, item.systemVariableTypeEnum.getName());
                }
            }

            assert st != null;
            Instant inst2 = Instant.now();
            log.info("流式设置执行时间 : " + Duration.between(inst1, inst2).toMillis());
            Instant inst3 = Instant.now();
            log.info("时间增量值:{}", JSON.toJSONString(dto.deltaTimes));
            Map<String, String> converSql = publishTaskClient.converSql(dto.tableName, dto.querySql, first.get().conType.getName().toUpperCase(), JSON.toJSONString(dto.deltaTimes)).data;
            log.info("拼语句执行时间 : " + Duration.between(inst2, inst3).toMillis());

            String sql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            rs = st.executeQuery(dto.querySql);
            Instant inst4 = Instant.now();
            log.info("执行sql时间 : " + Duration.between(inst3, inst4).toMillis());

            //获取数据集
            data = resultSetToJsonArrayTableService(rs);
            data.sql = sql;

            Instant inst5 = Instant.now();
            log.info("封装数据执行时间 : " + Duration.between(inst4, inst5).toMillis());

        } catch (Exception e) {
            log.error("数据服务执行自定义sql失败,ex:{}", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        Instant inst5 = Instant.now();
        System.out.println("最终执行时间 : " + Duration.between(inst1, inst5).toMillis());

        //数据类型转换
        typeConversion(first.get().conType, data.fieldNameDTOList, DataSourceTypeEnum.SQLSERVER);

        return data;
    }

    @Override
    public List<TableNameDTO> getAllTableByDb(Integer dataSourceId) {
        //获取数据源
        ResultEntity<List<DataSourceDTO>> allFiDataDataSource = client.getAllExternalDataSource();
        if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        Optional<DataSourceDTO> first = allFiDataDataSource.data.stream().filter(e -> e.id.equals(dataSourceId)).findFirst();
        if (!first.isPresent()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        List<TableNameDTO> data = new ArrayList<>();

        Connection conn = null;
        try {
            AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
            conn = helper.connection(first.get().conStr, first.get().conAccount, first.get().conPassword, first.get().conType);
            switch (first.get().conType) {
                case MYSQL:
                    data = MySqlConUtils.getTableName(conn);
                    break;
                case SQLSERVER:
                    data = SqlServerUtils.getTableName(conn);
                    break;
                case POSTGRESQL:
                    data = PgSqlUtils.getTableName(conn);
                    break;
                case ORACLE:
                    data = OracleUtils.getTableName(conn);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
        } catch (Exception e) {
            log.error("【根据库获取表集合】,{}", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return data;

    }

    /**
     * 获取数据源所有表
     *
     * @param dto
     * @return
     */
    public List<TableNameDTO> getDbTable(DataSourceDTO dto) {
        Connection conn = null;
        try {
            AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
            conn = commonDbHelper.connection(dto.conStr, dto.conAccount, dto.conPassword, dto.conType);
            List<TableNameDTO> data = new ArrayList<>();
            switch (dto.conType) {
                case MYSQL:
                    data = MySqlConUtils.getTableName(conn);
                    break;
                case SQLSERVER:
                    data = SqlServerUtils.getTableName(conn);
                    break;
                case POSTGRESQL:
                    data = PgSqlUtils.getTableName(conn);
                    break;
                case ORACLE:
                    data = OracleUtils.getTableName(conn);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
            return data;
        } catch (Exception e) {
            log.error("【获取表信息失败】,{}", e);
            return null;
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }

    /**
     * 根据表名获取字段
     *
     * @param dto
     * @param tableName
     * @return
     */
    public List<TableColumnDTO> getColumnByTableName(DataSourceDTO dto, String tableName) {
        Connection conn = null;
        try {
            AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
            conn = commonDbHelper.connection(dto.conStr, dto.conAccount, dto.conPassword, dto.conType);
            List<TableColumnDTO> data = new ArrayList<>();
            switch (dto.conType) {
                case MYSQL:
                    data = MySqlConUtils.getColNames(conn, tableName);
                    break;
                case SQLSERVER:
                    data = SqlServerUtils.getColumnsName(conn, tableName);
                    break;
                case POSTGRESQL:
                    data = PgSqlUtils.getTableColumnName(conn, tableName);
                    break;
                case ORACLE:
                    data = OracleUtils.getTableColumnInfoList(conn, dto.conDbname, tableName);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
            return data;
        } catch (Exception e) {
            log.error("【获取表信息失败】,{}", e);
            return null;
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }

    /**
     * 获取fidata内部数据源
     *
     * @return
     */
    public List<DataSourceDTO> getAllFiDataDataSource() {
        ResultEntity<List<DataSourceDTO>> allFiDataDataSource = client.getAllFiDataDataSource();
        if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        if (CollectionUtils.isEmpty(allFiDataDataSource.data)) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return allFiDataDataSource.data;
    }

}
