package com.fisk.dataaccess.utils.sql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.dto.pgsqlmetadata.ApiSqlResultDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.utils.json.JsonUtils;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/21 17:13
 */
@Slf4j
@Component
public class PgsqlUtils {

    @Resource
    UserClient userClient;

    /**
     * 创建pgsql连接驱动
     *
     * @return java.sql.Connection
     * @description
     * @author Lock
     * @date 2022/1/21 17:18
     * @version v1.0
     * @params
     */

    public Connection getPgConn(Integer targetDbId) {

        AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return helper.connection(dataSourceConfig.data.conStr,
                dataSourceConfig.data.conAccount,
                dataSourceConfig.data.conPassword,
                DataSourceTypeEnum.POSTGRESQL);
    }


    /**
     * 获取sqlserver表详情(表名+字段)
     *
     * @return java.util.List<com.fisk.dataaccess.table.TablePyhNameDTO>
     * @description 获取sqlserver表详情(表名 + 字段)
     * @author Lock
     * @date 2022/4/1 14:56
     * @version v1.0
     * @params url
     * @params user
     * @params password
     * @params dbName
     */
    public List<TablePyhNameDTO> getTableNameAndColumnsPlus(Connection conn) {

        List<TablePyhNameDTO> list = new ArrayList<>();
        Statement stmt = null;
        try {

            stmt = conn.createStatement();
            list = new ArrayList<>();
            ResultSet resultSet = null;
            // 获取指定数据库所有表
            resultSet = stmt.executeQuery("select tablename from pg_tables where schemaname = 'public'  ORDER BY tablename;");
            while (resultSet.next()) {
                TablePyhNameDTO tablePyhName = new TablePyhNameDTO();
                String tablename = resultSet.getString("tablename");
                tablePyhName.tableName = tablename;
                list.add(tablePyhName);
            }
            for (TablePyhNameDTO tablePyhName : list) {
                resultSet = stmt.executeQuery("select a.attname as fieldname, col_description(a.attrelid,a.attnum) as comment,format_type(a.atttypid,a.atttypmod) as type, a.attnotnull as notnull\n" +
                        "from pg_class as c,pg_attribute as a\n" +
                        "where c.relname = '" + tablePyhName.tableName + "' and a.attrelid = c.oid and a.attnum > 0;");
                List<TableStructureDTO> tableStructures = new ArrayList<>();
                while (resultSet.next()) {
                    TableStructureDTO tableStructure = new TableStructureDTO();
                    String fieldname = resultSet.getString("fieldname");
                    tableStructure.fieldName = fieldname;
                    tableStructures.add(tableStructure);
                }
                tablePyhName.fields = tableStructures;
            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumnsPlus】获取表名及表字段失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return list;
    }

    /**
     * 将数据从stg同步到ods
     *
     * @return void
     * @description 将数据从stg同步到ods
     * @author Lock
     * @date 2022/2/25 15:39
     * @version v1.0
     * @params sqlList sql集合
     * @params flag 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     */
    public ResultEnum stgToOds(List<String> sqlList, int flag, int targetDbId) throws SQLException {
        Connection pgConn = getPgConn(targetDbId);
        Statement statement = pgConn.createStatement();
        try {
            // 执行sql
            log.info("操作类型AE86: 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods;  " + flag);
            log.info("pg推送数据的函数: " + sqlList.get(flag));
            statement.executeUpdate(sqlList.get(flag));
        } catch (SQLException e) {
            log.error("批量执行SQL异常: " + e);
            return ResultEnum.STG_TO_ODS_ERROR;
        } finally {
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(pgConn);
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 批量执行pgsql
     *
     * @param res             json数据
     * @param tablePrefixName pg中的物理表前缀名
     * @return void
     * @author Lock
     * @date 2022/1/21 17:24
     */
    public ResultEntity<Object> executeBatchPgsql(String tablePrefixName,
                                                  List<JsonTableData> res,
                                                  List<ApiTableDTO> apiTableDtoList,
                                                  Integer targetDbId) throws Exception {
        Connection con = getPgConn(targetDbId);
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        ////PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");

        // TODO 调用JsonUtils获取表对象集合
        int countSql = 0;
        List<ApiSqlResultDTO> list = new ArrayList<>();
        try {
            JSONArray data = new JSONArray();
            for (JsonTableData re : res) {

                ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
                for (ApiTableDTO apiTable : apiTableDtoList) {
                    if (Objects.equals(apiTable.tableName, re.table)) {
                        List<TableFieldsDTO> tableFields = apiTable.list;
                        Map<String, String> map = new HashMap<>();
                        for (TableFieldsDTO tableField : tableFields) {
                            if (!Objects.equals(tableField.sourceFieldName, tableField.fieldName)) {
                                map.put(tableField.sourceFieldName, tableField.fieldName);
                            }
                        }
                        data = re.data;
                        if (map.size() != 0) {
                            String newData = JsonUtils.updateJsonArray(JSON.toJSONString(data), map);
                            data = JSON.parseArray(newData);
                        }
                    }
                }
                String tableName = re.table;
                for (Object datum : data) {
                    String insertSqlIndex = "insert into ";
                    String insertSqlLast = "(";
                    String inserSql = "";
                    insertSqlIndex = insertSqlIndex + tablePrefixName + tableName + "(";
                    JSONObject object = (JSONObject) datum;
                    Iterator<Map.Entry<String, Object>> iter = object.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = iter.next();
                        insertSqlIndex = insertSqlIndex + entry.getKey() + ",";
                        if (StringUtils.isEmpty(entry.getValue() == null ? "" : entry.getValue().toString())) {
                            insertSqlLast = insertSqlLast + "null" + ",";
                            continue;
                        }
                        insertSqlLast = insertSqlLast + "'" + entry.getValue() + "'" + ",";
                    }
                    insertSqlIndex = insertSqlIndex.substring(0, insertSqlIndex.lastIndexOf(",")) + ") values";
                    insertSqlLast = insertSqlLast.substring(0, insertSqlLast.lastIndexOf(",")) + ")";
                    inserSql = insertSqlIndex + insertSqlLast;
                    log.info("数据推送到stg的sql为: " + inserSql);
                    countSql++;
                    statement.addBatch(inserSql);
                }
                // 批量执行sql
                statement.executeBatch();

                // 保存本次信息
                apiSqlResultDto.setCount(countSql);
                // 多表插入时重新清空条数
                countSql = 0;
                // stg表名
                apiSqlResultDto.setTableName(tablePrefixName + tableName);
                apiSqlResultDto.setMsg("成功");

                list.add(apiSqlResultDto);
            }
            log.info("本次添加的sql个数为: " + countSql);
            // 提交要执行的批处理，防止 JDBC 执行事务处理
            con.commit();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            // 执行sql异常,重置记录的条数
            countSql = 0;
            ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
            apiSqlResultDto.setMsg("失败");
            apiSqlResultDto.setCount(0);
            list.add(apiSqlResultDto);
            return ResultEntityBuild.build(ResultEnum.PUSH_DATA_SQL_ERROR, list);
        } finally {
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(con);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.toJSONString(list));
    }

    /**
     * 批量执行pgsql
     *
     * @param importDataDto   task调用的入参
     * @param tablePrefixName 表前缀
     * @param res             json数据
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @author Lock
     * @date 2022/7/18 14:26
     */
    public ResultEntity<Object> executeBatchPgsql(ApiImportDataDTO importDataDto,
                                                  String tablePrefixName,
                                                  List<JsonTableData> res,
                                                  List<ApiTableDTO> apiTableDtoList,
                                                  Integer targetDbId) throws Exception {
        Connection con = getPgConn(targetDbId);
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        ////PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");

        // TODO 调用JsonUtils获取表对象集合
        int countSql = 0;
        List<ApiSqlResultDTO> list = new ArrayList<>();
        try {
            JSONArray data = new JSONArray();
            for (JsonTableData re : res) {
                for (ApiTableDTO apiTable : apiTableDtoList) {
                    if (Objects.equals(apiTable.tableName, re.table)) {
                        List<TableFieldsDTO> tableFields = apiTable.list;
                        Map<String, String> map = new HashMap<>();
                        for (TableFieldsDTO tableField : tableFields) {
                            if (!Objects.equals(tableField.sourceFieldName, tableField.fieldName)) {
                                map.put(tableField.sourceFieldName, tableField.fieldName);
                            }
                        }
                        data = re.data;
                        if (map.size() != 0) {
                            String newData = JsonUtils.updateJsonArray(JSON.toJSONString(data), map);
                            data = JSON.parseArray(newData);
                        }
                    }
                }
                ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
                String tableName = re.table;
                for (Object datum : data) {
                    String insertSqlIndex = "insert into ";
                    String insertSqlLast = "(";
                    String inserSql = "";
                    insertSqlIndex = insertSqlIndex + tablePrefixName + tableName + "(";
                    JSONObject object = (JSONObject) datum;
                    Iterator<Map.Entry<String, Object>> iter = object.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = iter.next();
                        insertSqlIndex = insertSqlIndex + entry.getKey() + ",";
                        if (StringUtils.isEmpty(entry.getValue() == null ? "" : entry.getValue().toString())) {
                            insertSqlLast = insertSqlLast + "null" + ",";
                            continue;
                        }
                        insertSqlLast = insertSqlLast + "'" + entry.getValue() + "'" + ",";
                    }
                    insertSqlIndex = insertSqlIndex.substring(0, insertSqlIndex.lastIndexOf(",")) + ") values";
                    insertSqlLast = insertSqlLast.substring(0, insertSqlLast.lastIndexOf(",")) + ")";
                    inserSql = insertSqlIndex + insertSqlLast;
                    log.info("数据推送到stg的sql为: " + inserSql);
                    countSql++;
                    statement.addBatch(inserSql);
                }
                if (importDataDto != null) {
                    String s1 = StringUtils.isNotBlank(importDataDto.pipelTraceId) ? importDataDto.pipelTraceId : "";
                    String s2 = StringUtils.isNotBlank(importDataDto.pipelTaskTraceId) ? importDataDto.pipelTaskTraceId : "";
                    String traceId = StringUtils.isNotBlank(s1) ? s1 : s2;
                    String updateBatchSql = "UPDATE " + tablePrefixName + tableName + " SET " + NifiConstants.AttrConstants.FIDATA_BATCH_CODE
                            + " = '" + traceId + "' WHERE " + NifiConstants.AttrConstants.FIDATA_BATCH_CODE + " IS NULL";

                    log.info("设置" + NifiConstants.AttrConstants.FIDATA_BATCH_CODE + "的update语句为: " + updateBatchSql);

                    statement.addBatch(updateBatchSql);
                }
                // 批量执行sql
                statement.executeBatch();

                // 保存本次信息
                apiSqlResultDto.setCount(countSql);
                // 多表插入时重新清空条数
                countSql = 0;
                // stg表名
                apiSqlResultDto.setTableName(tablePrefixName + tableName);
                apiSqlResultDto.setMsg("成功");

                list.add(apiSqlResultDto);
            }
            System.out.println("本次添加的sql个数为: " + countSql);
            // 提交要执行的批处理，防止 JDBC 执行事务处理
            con.commit();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            // 执行sql异常,重置记录的条数
            countSql = 0;
            ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
            apiSqlResultDto.setMsg("失败");
            apiSqlResultDto.setCount(0);
            list.add(apiSqlResultDto);
            return ResultEntityBuild.build(ResultEnum.PUSH_DATA_SQL_ERROR, list);
        } finally {
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(con);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.toJSONString(list));
    }

    /**
     * 获取pg数据
     *
     * @param sql
     * @return
     */
    public List<Map<String, Object>> executePgSql(String sql, Integer targetDbId) {
        return AbstractDbHelper.execQueryResultMaps(sql, getPgConn(targetDbId));
    }

    public List<String> getPgDatabases(Connection conn) {

        List<String> dbName = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("select * from pg_database;");
            while (resultSet.next()) {
                dbName.add(resultSet.getString("datname"));
            }
        } catch (SQLException e) {
            log.error("pg获取数据库集合失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return dbName;
    }

}
