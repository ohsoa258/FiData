package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.utils.sql.MysqlConUtils;
import com.fisk.dataaccess.utils.sql.SqlServerConUtils;
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

        String mysqlTpye = "mysql";
        String sqlserverTpye = "sqlserver";

        DataSourceDTO dataSource = mapper.getDataSource(appId);
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerConUtils sqlServerConUtils = new SqlServerConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        AppDataSourcePO po = this.query().eq("app_id", appId).one();
        dataSource.appName = po.dbName;
        if (mysqlTpye.equalsIgnoreCase(dataSource.driveType)) {
            dataSource.tableDtoList = mysqlConUtils.getTableNameAndColumns(po.connectStr, po.connectAccount, po.connectPwd);
            dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DriverTypeEnum.MYSQL, po.connectStr, po.connectAccount, po.connectPwd,po.dbName);

        } else if (sqlserverTpye.equalsIgnoreCase(dataSource.driveType)) {
            dataSource.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
            dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DriverTypeEnum.SQLSERVER, po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
        }

        return dataSource;
    }
}
