package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.utils.sql.MysqlConUtils;
import com.fisk.dataaccess.utils.sql.OracleUtils;
import com.fisk.dataaccess.utils.sql.SqlServerPlusUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Service
public class AppDataSourceImpl extends ServiceImpl<AppDataSourceMapper, AppDataSourcePO> implements IAppDataSource {

    @Resource
    AppDataSourceMapper mapper;

    @Override
    public DataSourceDTO getDataSourceMeta(long appId) {

        DataSourceDTO dataSource = mapper.getDataSource(appId);
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        OracleUtils oracleUtils = new OracleUtils();
        AppDataSourcePO po = this.query().eq("app_id", appId).one();
        dataSource.appName = po.dbName;
        if (DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(dataSource.driveType)) {
            // 表结构
            dataSource.tableDtoList = mysqlConUtils.getTableNameAndColumns(po.connectStr, po.connectAccount, po.connectPwd, DriverTypeEnum.MYSQL);
            //视图结构
            dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DriverTypeEnum.MYSQL, po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
        } else if (DataSourceTypeEnum.ORACLE.getName().equalsIgnoreCase(dataSource.driveType)) {
            // 表结构
            dataSource.tableDtoList = oracleUtils.getTableNameAndColumns(po.connectStr, po.connectAccount, po.connectPwd, DriverTypeEnum.ORACLE);
            //视图结构
            dataSource.viewDtoList = oracleUtils.loadViewDetails(DriverTypeEnum.ORACLE, po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
        } else if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dataSource.driveType)) {
            // 表结构
            dataSource.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
            // 视图结构
            dataSource.viewDtoList = sqlServerPlusUtils.loadViewDetails(DriverTypeEnum.SQLSERVER, po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
        }

        return dataSource;
    }
}
