package com.fisk.datagovernance.service.impl.monitor;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.vo.CDCAppNameAndTableVO;
import com.fisk.dataaccess.vo.TableDbNameAndNameVO;
import com.fisk.datagovernance.dto.monitor.TablesRowsDTO;
import com.fisk.datagovernance.service.monitor.AccessLakeMonitorService;
import com.fisk.datagovernance.vo.monitor.AccessLakeMonitorDetailVO;
import com.fisk.datagovernance.vo.monitor.AccessLakeMonitorVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Slf4j
@Service("AccessLakeMonitorService")
public class AccessLakeMonitorServiceImpl implements AccessLakeMonitorService {

    @Resource
    DataAccessClient dataAccessClient;

    @Resource
    UserClient userClient;

    @Resource
    RedisUtil redisUtil;

    @Value("${database.ods_id}")
    private Integer odsId;

    @Value("${doris.catalogName}")
    private String catalogName;

    @Override
    public AccessLakeMonitorVO getAccessLakeMonitor(Integer appId) {
        ResultEntity<List<CDCAppNameAndTableVO>> cdcAppNameAndTables = dataAccessClient.getCDCAppNameAndTables(appId);
        CDCAppNameAndTableVO app = null;
        if (cdcAppNameAndTables.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(cdcAppNameAndTables.getData())) {
            List<CDCAppNameAndTableVO> data = cdcAppNameAndTables.getData();
            app = data.get(0);
        } else {
            log.error("dataAccessClient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        AppDataSourceDTO appDataSourceDTO = null;
        ResultEntity<List<AppDataSourceDTO>> appSourcesByAppId = dataAccessClient.getAppSourcesByAppId(appId);
        if (cdcAppNameAndTables.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(appSourcesByAppId.getData())) {
            List<AppDataSourceDTO> data = appSourcesByAppId.getData();
            appDataSourceDTO = data.get(0);
        } else {
            log.error("dataAccessClient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        DataSourceDTO dataSourceDTO = null;
        ResultEntity<DataSourceDTO> dataSourceById = userClient.getFiDataDataSourceById(odsId);
        if (cdcAppNameAndTables.code == ResultEnum.SUCCESS.getCode() && dataSourceById.getData() != null) {
            dataSourceDTO = dataSourceById.getData();
        } else {
            log.error("dataAccessClient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        AccessLakeMonitorVO accessLakeMonitorVO = new AccessLakeMonitorVO();
        List<TableDbNameAndNameVO> tableDbNameAndNameVO = app.getTableDbNameAndNameVO();
        DataSourceTypeEnum type = DataSourceTypeEnum.getValue(app.getDbType());
        String selectSourceSql = getSelectSourceSql(type, tableDbNameAndNameVO);
        log.info("源待查询sql:"+selectSourceSql);
        String selectTargetSql = getSelectTargetSql(dataSourceDTO.conType,tableDbNameAndNameVO);
        log.info("目标待查询sql:"+selectTargetSql);
        List<TablesRowsDTO> sourceTablesRows = getSourceTablesRows(appDataSourceDTO, selectSourceSql);
        List<TablesRowsDTO> targetTablesRows = getTargetTablesRows(tableDbNameAndNameVO);
        int sourceTotal = sourceTablesRows.stream().mapToInt(TablesRowsDTO::getRows).sum();
        int targetTotal = targetTablesRows.stream().mapToInt(TablesRowsDTO::getRows).sum();
        Map<String, TablesRowsDTO> targetTables = targetTablesRows.stream().collect(Collectors.toMap(i -> i.getDbName() + "." + i.getTableName(), i -> i));
        accessLakeMonitorVO.setSourceTotal(sourceTotal);
        accessLakeMonitorVO.setTargetTotal(targetTotal);
        List<AccessLakeMonitorDetailVO> detailVOS = new ArrayList<>();
        for (TablesRowsDTO sourceTablesRow : sourceTablesRows) {
            AccessLakeMonitorDetailVO detailVO = new AccessLakeMonitorDetailVO();
            TablesRowsDTO tablesRowsDTO = targetTables.get(sourceTablesRow.getDbName() + "." + sourceTablesRow.getTableName());
            if (tablesRowsDTO != null) {
                detailVO.setSourceDriverName(sourceTablesRow.getDriverType());
                detailVO.setSourceDbName(sourceTablesRow.getDbName());
                detailVO.setSourceTableName(sourceTablesRow.getTableName());
                detailVO.setSourceRows(sourceTablesRow.getRows());
                detailVO.setTargetDriverName(tablesRowsDTO.getDriverType());
                detailVO.setTargetDbName(tablesRowsDTO.getDbName());
                detailVO.setTargetTableName(tablesRowsDTO.getTableName());
                detailVO.setTargetRows(tablesRowsDTO.getRows());
                detailVO.setCatchTime(tablesRowsDTO.getCatchTime());
                detailVOS.add(detailVO);
            }
        }
        Object catchTotal = redisUtil.get(RedisKeyEnum.MONITOR_ACCESSLAKE_KAFKA.getName() + ":" + appId);
        if (catchTotal != null){
            accessLakeMonitorVO.setCatchTotal((int)catchTotal);
        }
        accessLakeMonitorVO.setDetailVO(detailVOS);
        return accessLakeMonitorVO;
    }

    private List<TablesRowsDTO> getSourceTablesRows(AppDataSourceDTO appDataSourceDTO, String selectSql) {
        Connection conn = null;
        Statement st = null;
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum driveType = com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.getEnum(appDataSourceDTO.driveType.toUpperCase());
        try {
            Class.forName(driveType.getDriverName());
            conn = DriverManager.getConnection(appDataSourceDTO.connectStr, appDataSourceDTO.connectAccount, appDataSourceDTO.connectPwd);
            st = conn.createStatement();
            ResultSet result = st.executeQuery(selectSql);
            List<TablesRowsDTO> tablesRowsDTOS = new ArrayList<>();
            while (result.next()) {
                TablesRowsDTO tablesRowsDTO = new TablesRowsDTO();
                tablesRowsDTO.setDbName(result.getString("dbName"));
                tablesRowsDTO.setTableName(result.getString("tableName"));
                tablesRowsDTO.setRows(Integer.valueOf(result.getString("rowCount")));
                tablesRowsDTO.setDriverType(driveType.getDriverName());
                tablesRowsDTOS.add(tablesRowsDTO);
            }
            return tablesRowsDTOS;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        } finally {
            try {
                st.close();
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new FkException(ResultEnum.ERROR);
            }
        }
    }

    private List<TablesRowsDTO> getTargetTablesRows(List<TableDbNameAndNameVO> tableDbNameAndNameVO ) {
//        Connection conn = null;
//        Statement st = null;
//        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum driveType = dataSourceDTO.conType;
//        try {
//            Class.forName(driveType.getDriverName());
//            conn = DriverManager.getConnection(dataSourceDTO.conStr, dataSourceDTO.conAccount, dataSourceDTO.conPassword);
//            st = conn.createStatement();
//            ResultSet result = st.executeQuery(selectSql);
//            List<TablesRowsDTO> tablesRowsDTOS = new ArrayList<>();
//            while (result.next()) {
//                TablesRowsDTO tablesRowsDTO = new TablesRowsDTO();
//                tablesRowsDTO.setDbName(result.getString("dbName"));
//                tablesRowsDTO.setTableName(result.getString("tableName"));
//                tablesRowsDTO.setRows(Integer.valueOf(result.getString("rowCount")));
//                tablesRowsDTO.setDriverType(driveType.getDriverName());
//                tablesRowsDTOS.add(tablesRowsDTO);
//            }
//            return tablesRowsDTOS;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
//        } finally {
//            try {
//                st.close();
//                conn.close();
//            } catch (SQLException e) {
//                log.error(e.getMessage());
//                throw new FkException(ResultEnum.ERROR);
//            }
//        }
        List<TablesRowsDTO> tablesRowsDTOS = new ArrayList<>();
        for (TableDbNameAndNameVO dbNameAndNameVO : tableDbNameAndNameVO) {
            Object json = redisUtil.get(RedisKeyEnum.MONITOR_ACCESSLAKE_DORIS.getName() + ":" + catalogName + "." + dbNameAndNameVO.getDbName() + "." + dbNameAndNameVO.getTableName());
            if (json != null){
                TablesRowsDTO tablesRowsDTO = JSON.parseObject(json.toString(), TablesRowsDTO.class);
                tablesRowsDTOS.add(tablesRowsDTO);
            }
        }
        return tablesRowsDTOS;
    }


    private String getSelectSourceSql(DataSourceTypeEnum type,List<TableDbNameAndNameVO> tableDbNameAndNameVO){
        String selectSourceSql = null;
        switch (type) {
            case MYSQL:
                selectSourceSql = "SELECT  '"+tableDbNameAndNameVO.get(0).getDbName()+"' as dbName,table_name as tableName, table_rows as rowCount\n" +
                        "FROM information_schema.tables\n" +
                        "WHERE table_schema = '"+tableDbNameAndNameVO.get(0).getDbName()+"'\n" +
                        "AND table_name in(";
                String mysqlTableName = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "'"+i.getTableName()+"'";
                    return str;
                }).collect(Collectors.joining(","));
                selectSourceSql = selectSourceSql+mysqlTableName+")";
                break;
            case SQLSERVER:
                selectSourceSql = "SELECT '"+tableDbNameAndNameVO.get(0).getDbName()+"' as dbName,\n" +
                        "    t.name AS tableName,\n" +
                        "    SUM(p.rows) AS rowCount\n" +
                        "FROM \n" +
                        "    dmp_dw.sys.tables t\n" +
                        "INNER JOIN \n" +
                        "    dmp_dw.sys.partitions p ON t.object_id = p.object_id\n" +
                        "WHERE \n" +
                        "    t.is_ms_shipped = 0\n" +
                        "\t\tAND t.name in (";
                String sqlServerTableName = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "'"+i.getTableName()+"'";
                    return str;
                }).collect(Collectors.joining(","));
                selectSourceSql = selectSourceSql+sqlServerTableName+") GROUP BY t.name ORDER BY t.name";
                break;
            default:
                break;
        }
        return selectSourceSql;
    }

    private String getSelectTargetSql(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum type,List<TableDbNameAndNameVO> tableDbNameAndNameVO){
        String selectTargetSql = null;
        switch (type) {
            case DORIS:
                selectTargetSql = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "select '" + i.getDbName() + "' as dbName,'" + i.getTableName() + "' as tableName,count(1) as rowCount from "+catalogName+"." + i.getDbName() + ".`" + i.getTableName()+"`";
                    return str;
                }).collect(Collectors.joining(" UNION ALL "));
                break;
            case MYSQL:
                selectTargetSql = "SELECT "+tableDbNameAndNameVO.get(0).getDbName()+" as dbName,table_name as tableName, table_rows as rowCount\n" +
                        "FROM information_schema.tables\n" +
                        "WHERE table_schema = '"+tableDbNameAndNameVO.get(0).getDbName()+"'\n" +
                        "AND table_name in(";
                String mysqlTableName = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "'"+i.getTableName()+"'";
                    return str;
                }).collect(Collectors.joining(","));
                selectTargetSql = selectTargetSql+mysqlTableName+")";
                break;
            case SQLSERVER:
                selectTargetSql = "SELECT "+tableDbNameAndNameVO.get(0).getDbName()+" as dbName,\n" +
                        "    t.name AS tableName,\n" +
                        "    SUM(p.rows) AS rowCount\n" +
                        "FROM \n" +
                        "    dmp_dw.sys.tables t\n" +
                        "INNER JOIN \n" +
                        "    dmp_dw.sys.partitions p ON t.object_id = p.object_id\n" +
                        "WHERE \n" +
                        "    t.is_ms_shipped = 0\n" +
                        "\t\tAND t.name in (";
                String sqlServerTableName = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "'"+i.getTableName()+"'";
                    return str;
                }).collect(Collectors.joining(","));
                selectTargetSql = selectTargetSql+sqlServerTableName+") GROUP BY t.name ORDER BY t.name";
                break;
            default:
                break;
        }
        return selectTargetSql;
    }

    @Override
    public ResultEnum saveCatchTargetTableRows(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = simpleDateFormat.format(new Date());
        long startTime = System.currentTimeMillis();
        log.info("开始更新每个应用入湖DORIS表数据量:开始--"+start);
        try {
            ResultEntity<List<CDCAppNameAndTableVO>> cdcAppNameAndTables =
                    dataAccessClient.getCDCAppNameAndTables(0);
            List<CDCAppNameAndTableVO> data = new ArrayList<>();
            if (cdcAppNameAndTables.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(cdcAppNameAndTables.getData())) {
                data = cdcAppNameAndTables.getData();
            } else {
                log.error("dataAccessClient无法查询到目标库的连接信息");
                Thread.sleep(1800000);
                new Thread(this::saveCatchTargetTableRows).start();
                return ResultEnum.ERROR;
            }
            DataSourceDTO dataSourceDTO = null;
            ResultEntity<DataSourceDTO> dataSourceById = userClient.getFiDataDataSourceById(odsId);
            if (cdcAppNameAndTables.code == ResultEnum.SUCCESS.getCode() && dataSourceById.getData() != null) {
                dataSourceDTO = dataSourceById.getData();
            } else {
                log.error("dataAccessClient无法查询到目标库的连接信息");
                Thread.sleep(1800000);
                new Thread(this::saveCatchTargetTableRows).start();
                return ResultEnum.ERROR;
            }
            if (CollectionUtils.isNotEmpty(data)){
                for (CDCAppNameAndTableVO app : data) {
                    List<TableDbNameAndNameVO> tableDbNameAndNameVO = app.getTableDbNameAndNameVO();
                    if (CollectionUtils.isNotEmpty(tableDbNameAndNameVO)){
                        for (TableDbNameAndNameVO dbNameAndNameVO : tableDbNameAndNameVO) {
                            Connection conn = null;
                            Statement st = null;
                            String selectSql = null;
                            try {
                                switch (dataSourceDTO.getConType()){
                                    case DORIS:
                                        selectSql = "select count(1) as rowCount from "+catalogName+"."+dbNameAndNameVO.getDbName()+".`"+dbNameAndNameVO.getTableName()+"`";
                                        break;
                                }
                                Class.forName(dataSourceDTO.getConType().getDriverName());
                                conn = DriverManager.getConnection(dataSourceDTO.conStr, dataSourceDTO.conAccount, dataSourceDTO.conPassword);
                                st = conn.createStatement();
                                ResultSet result = st.executeQuery(selectSql);
                                while (result.next()) {
                                    TablesRowsDTO tablesRowsDTO = new TablesRowsDTO();
                                    tablesRowsDTO.setDbName(dbNameAndNameVO.getDbName());
                                    tablesRowsDTO.setTableName(dbNameAndNameVO.getTableName());
                                    tablesRowsDTO.setRows(Integer.valueOf(result.getString("rowCount")));
                                    tablesRowsDTO.setDriverType(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.DORIS.getDriverName());
                                    LocalDateTime now = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    String nowTime = now.format(formatter);
                                    tablesRowsDTO.setCatchTime(nowTime);
                                    redisUtil.set(RedisKeyEnum.MONITOR_ACCESSLAKE_DORIS.getName()+":"+catalogName+"."+dbNameAndNameVO.getDbName()+"."+dbNameAndNameVO.getTableName(), JSON.toJSONString(tablesRowsDTO));
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            } finally {
                                try {
                                    st.close();
                                    conn.close();
                                } catch (SQLException e) {
                                    log.error(e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            String end = simpleDateFormat.format(new Date());
            // 记录结束时间
            long endTime = System.currentTimeMillis();
            // 计算接口耗时，单位为毫秒
            long elapsedTime = endTime - startTime;
            log.info("更新每个应用入湖DORIS表数据量:结束--"+end+"--接口耗时：" + elapsedTime + " 毫秒");
            Thread.sleep(100);
            new Thread(this::saveCatchTargetTableRows).start();

        } catch (InterruptedException e) {
            log.info("---------saveCatchTargetTableRows方法停止---------");
            e.printStackTrace();
        }
        return ResultEnum.SUCCESS;
    }
}
