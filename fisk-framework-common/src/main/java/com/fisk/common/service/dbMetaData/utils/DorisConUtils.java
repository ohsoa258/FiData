package com.fisk.common.service.dbMetaData.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.DorisCatalogDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DorisConUtils {

    /**
     * 获取表详情(表信息+字段信息)
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DataSourceTypeEnum driverTypeEnum) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(driverTypeEnum.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();

            int tag = 0;
            List<String> tableNames = getTablesPlus(conn);
            if (CollectionUtils.isNotEmpty(tableNames)) {
                for (String tableName : tableNames) {
                    // mysql没有架构概念
                    List<TableStructureDTO> colNames = getColNames(stmt, tableName);
                    TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                    // mysql没有架构概念
                    tablePyhNameDTO.setTableFullName(tableName);
                    tablePyhNameDTO.setTableName(tableName);
                    tablePyhNameDTO.setFields(colNames);
                    tag++;
                    tablePyhNameDTO.setTag(tag);
                    list.add(tablePyhNameDTO);
                }
            }
        } catch (Exception e) {
            log.error("【getTableNameAndColumns】获取表名报错：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("【getTableNameAndColumnsPlus】关闭数据库连接异常：", e);
                throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
            }
        }
        return list;
    }

    /**
     * 获取数据库中所有表名称
     */
    public List<String> getTablesPlus(Connection conn) {
        // MySql没有架构概念，此处直接查表
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            tablesList = new ArrayList<>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取数据库中所有表名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 获取表及表字段
     *
     * @return 查询结果
     */
    public static List<TableNameDTO> getTableName(Connection conn) {
        // 获取数据库中所有表名称
        // MySql没有架构概念，此处直接查表
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            tablesList = new ArrayList<>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取数据库中所有表名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }

        List<TableNameDTO> list = new ArrayList<>();
        for (String tableName : tablesList) {
            TableNameDTO tablePyhNameDTO = new TableNameDTO();
            tablePyhNameDTO.setTableName(tableName);
            list.add(tablePyhNameDTO);
        }
        return list;

    }

    /**
     * 获取数据库中所有表名称
     */
    public List<TablePyhNameDTO> getTablesPlusForOps(Connection conn) {
        // doris直接获取顶级目录列表即可
        List<TablePyhNameDTO> tableList = new ArrayList<>();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("SHOW CATALOGS");
            while (resultSet.next()) {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(resultSet.getString("CatalogName"));
                tablePyhNameDTO.setTableFullName(resultSet.getString("CatalogName"));
                tableList.add(tablePyhNameDTO);
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取数据库中所有表名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(resultSet);
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return tableList;
    }

    /**
     * 获取表中所有字段名称
     */
    public List<TableStructureDTO> getColNames(Statement st, String tableName) {
        // tableName应携带架构名称
        List<TableStructureDTO> colNameList = null;
        try {
            ResultSet rs = st.executeQuery("select * from " + tableName + " LIMIT 0,10;");
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();
            colNameList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                TableStructureDTO tableStructureDTO = new TableStructureDTO();
                // 字段名称
                tableStructureDTO.fieldName = metaData.getColumnName(i);
                // 字段类型
                tableStructureDTO.fieldType = metaData.getColumnTypeName(i);
                // 字段长度
                tableStructureDTO.fieldLength = metaData.getColumnDisplaySize(i);
                colNameList.add(tableStructureDTO);
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("【getColNames】获取表中所有字段名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 获取指定doris外部目录catalog下的所有db
     */
    public List<TableStructureDTO> getCatalogNameAndTblName(Statement statement, String catalogName) {
        // tableName应携带架构名称
        List<TableStructureDTO> catalogList = new ArrayList<>();;
        ResultSet databases = null;
        ResultSet tbls = null;
        try {
            statement.executeQuery("SWITCH " + catalogName + ";");
            databases = statement.executeQuery("SHOW DATABASES");

            while (databases.next()) {
                String database = databases.getString("Database");
                TableStructureDTO tableStructureDTO = new TableStructureDTO();
                tableStructureDTO.fieldName = database;
                if ("default".equals(database)) continue;
                catalogList.add(tableStructureDTO);
            }

            //查询数据库下的所有表
            for (TableStructureDTO db : catalogList) {
                List<String> tblNames = new ArrayList<>();
                statement.executeQuery("USE " + db.fieldName + ";");
                tbls = statement.executeQuery("SHOW TABLES;");
                while (tbls.next()) {
                    String tblName = tbls.getString("Tables_in_" + db.fieldName);
                    tblNames.add(tblName);
                }
                db.setDorisTblNames(tblNames);
            }


        }catch (Exception e){
            log.error("【getCatalogNameAndTblName】获取doris外部目录异常：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return catalogList;
    }

    public static List<TableColumnDTO> getColNames(Connection conn, String tableName) {
        // tableName应携带架构名称
        List<TableColumnDTO> colNameList = null;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from " + tableName + " LIMIT 0,10;");
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();
            colNameList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                TableColumnDTO tableColumnDTO = new TableColumnDTO();
                // 字段名称
                tableColumnDTO.fieldName = metaData.getColumnName(i);
                // 字段类型
                tableColumnDTO.fieldType = metaData.getColumnTypeName(i);
                // 字段长度
                tableColumnDTO.fieldLength = metaData.getColumnDisplaySize(i);
                colNameList.add(tableColumnDTO);
            }
            if (rs != null) {
                AbstractCommonDbHelper.closeConnection(conn);
                AbstractCommonDbHelper.closeResultSet(rs);
                AbstractCommonDbHelper.closeStatement(st);
            }
        } catch (SQLException e) {
            log.error("【getColNames】获取表中所有字段名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }



    public List<DorisCatalogDTO> getCataLogNames(String url, String user, String password, DataSourceTypeEnum driverTypeEnum) {
        List<DorisCatalogDTO> cataLogList = null;
        Connection conn = null;
        try {
            Class.forName(driverTypeEnum.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            Statement statement = conn.createStatement();
            ResultSet catalogs = statement.executeQuery("SHOW CATALOGS;");
            cataLogList = new ArrayList<>();
            while (catalogs.next()) {
                String catalogId = catalogs.getString("CatalogId");
                if (catalogId != "0") {
                    DorisCatalogDTO catalogDTO = new DorisCatalogDTO();
                    catalogDTO.catalogName = catalogs.getString("CatalogName");
                    cataLogList.add(catalogDTO);
                }
            }
            cataLogList = cataLogList.stream().map(i -> {
                try {
                    List<DorisCatalogDTO.CataLogDatabase> databaseList = new ArrayList<>();
                    statement.executeQuery("SWITCH " + i.getCatalogName() + ";");
                    ResultSet databases = statement.executeQuery("SHOW DATABASES;");
                    while (databases.next()) {
                        String database = databases.getString("Database");
                        switch (database) {
                            case "default":
                            case "__internal_schema":
                            case "information_schema":
                            case "mysql":
                                continue;
                        }
                        DorisCatalogDTO.CataLogDatabase cataLogDatabase = new DorisCatalogDTO.CataLogDatabase();
                        cataLogDatabase.databaseName = database;
                        databaseList.add(cataLogDatabase);
                    }
                    databaseList = databaseList.stream().map(v -> {
                        List<DorisCatalogDTO.CataLogTables> tableList = new ArrayList<>();
                        try {
                            statement.executeQuery("USE " + v.getDatabaseName() + ";");
                            ResultSet tables = statement.executeQuery("SHOW TABLES;");
                            while (tables.next()) {
                                String tableName = tables.getString("Tables_in_" + v.databaseName);
                                DorisCatalogDTO.CataLogTables cataLogTables = new DorisCatalogDTO.CataLogTables();
                                cataLogTables.tableName = tableName;
                                tableList.add(cataLogTables);
                            }
                            tableList = tableList.stream().map(n -> {
                                try {
                                    ResultSet tblField = statement.executeQuery("DESC " + n.tableName + ";");
                                    List<DorisCatalogDTO.CataLogField> cataLogFieldList = new ArrayList<>();
                                    while (tblField.next()) {
                                        DorisCatalogDTO.CataLogField cataLogField = new DorisCatalogDTO.CataLogField();
                                        cataLogField.setFieldName(tblField.getString("Field"));
                                        cataLogField.setType(tblField.getString("Type"));
                                        cataLogField.setIfNull(tblField.getString("Null"));
                                        cataLogField.setKey(tblField.getString("Key"));
                                        cataLogField.setDefaultValue(tblField.getString("Default"));
                                        cataLogField.setExtra(tblField.getString("Extra"));
                                        cataLogFieldList.add(cataLogField);
                                    }
                                    n.setCataLogFields(cataLogFieldList);
                                    if (tblField != null) {
                                        tblField.close();
                                    }
                                } catch (Exception e) {
                                    log.error("【getCataLogNames】获取表字段名称异常", e);
                                    return n;
                                }
                                return n;
                            }).collect(Collectors.toList());
                            v.setCataLogTables(tableList);
                            if (tables != null) {
                                tables.close();
                            }
                        } catch (Exception e) {
                            log.error("【getTablesPlus】获取表名称异常", e);
                            return v;
                        }
                        return v;
                    }).collect(Collectors.toList());
                    i.setCataLogDatabases(databaseList);
                    if (databases != null) {
                        databases.close();
                    }
                } catch (Exception e) {
                    log.error("【getCataLogNames】获取数据库名称异常", e);
                    return i;
                }
                return i;
            }).collect(Collectors.toList());
            if (statement != null) {
                statement.close();
            }
            if (catalogs != null) {
                catalogs.close();
            }
        } catch (Exception e) {
            log.error("【getCataLogNames】获取数据库中所有CataLog名称异常", e);
            return cataLogList;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("【getTableNameAndColumnsPlus】关闭数据库连接异常：", e);
                throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
            }

        }
        return cataLogList;
    }
}
