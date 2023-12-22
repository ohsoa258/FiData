package com.fisk.datagovernance.service.impl.monitor;

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
import java.util.ArrayList;
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
        String selectSourceSql = null;
        switch (DataSourceTypeEnum.getValue(app.getDbType())) {
            case MYSQL:
                selectSourceSql = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "select '" + i.getDbName() + "' as dbName,'" + i.getTableName() + "' as tableName,count(1) as rowCount from " + i.getDbName() + "." + i.getTableName();
                    return str;
                }).collect(Collectors.joining(" UNION ALL "));
                break;
            case SQLSERVER:
                selectSourceSql = tableDbNameAndNameVO.stream().map(i -> {
                    String dbName = i.getDbName().toLowerCase();
                    String tableName = i.getTableName();
                    String str = "select '" + i.getDbName() + "' as dbName,'" + tableName + "' as tableName,count(1) as rowCount from " + dbName + "." + tableName;
                    return str;
                }).collect(Collectors.joining(" UNION ALL "));
                break;
            default:
                break;
        }
        log.info("源待查询sql:"+selectSourceSql);
        String selectTargetSql = null;
        switch (dataSourceDTO.conType) {
            case DORIS_CATALOG:
                selectTargetSql = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "select '" + i.getDbName() + "' as dbName,'" + i.getTableName() + "' as tableName,count(1) as rowCount from qs_dmp_ods." + i.getDbName() + "." + i.getTableName();
                    return str;
                }).collect(Collectors.joining(" UNION ALL "));
                break;
            case MYSQL:
                selectTargetSql = tableDbNameAndNameVO.stream().map(i -> {
                    String str = "select '" + i.getDbName() + "' as dbName,'" + i.getTableName() + "' as tableName,count(1) as rowCount from " + i.getDbName() + "." + i.getTableName();
                    return str;
                }).collect(Collectors.joining(" UNION ALL "));
                break;
            case SQLSERVER:
                selectTargetSql = tableDbNameAndNameVO.stream().map(i -> {
                    String dbName = i.getDbName().toLowerCase();
                    String tableName = i.getTableName();
                    String str = "select '" + i.getDbName() + "' as dbName,'" + tableName + "' as tableName,count(1) as rowCount from " + dbName + "." + tableName;
                    return str;
                }).collect(Collectors.joining(" UNION ALL "));
                break;
            default:
                break;
        }
        log.info("目标待查询sql:"+selectTargetSql);
        List<TablesRowsDTO> sourceTablesRows = getSourceTablesRows(appDataSourceDTO, selectSourceSql);
        List<TablesRowsDTO> targetTablesRows = getTargetTablesRows(dataSourceDTO, selectTargetSql);
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
                detailVOS.add(detailVO);
            }
        }
        Object catchTotal = redisUtil.get(RedisKeyEnum.MONITOR_ACCESSLAKE.getName() + ":" + appId);
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

    private List<TablesRowsDTO> getTargetTablesRows(DataSourceDTO dataSourceDTO, String selectSql) {
        Connection conn = null;
        Statement st = null;
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum driveType = dataSourceDTO.conType;
        try {
            Class.forName(driveType.getDriverName());
            conn = DriverManager.getConnection(dataSourceDTO.conStr, dataSourceDTO.conAccount, dataSourceDTO.conPassword);
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
}
