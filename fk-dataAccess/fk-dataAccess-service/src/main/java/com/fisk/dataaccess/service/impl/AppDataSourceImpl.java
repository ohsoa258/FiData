package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.utils.MysqlConUtils;
import com.fisk.dataaccess.utils.SqlServerConUtils;
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
        SqlServerConUtils sqlServerConUtils = new SqlServerConUtils();
        AppDataSourcePO po = this.query().eq("app_id", appId).one();
        dataSource.databaseName = po.dbName;
        if ("mysql".equalsIgnoreCase(dataSource.driveType)) {
            dataSource.list = mysqlConUtils.getTableNameAndColumns(po.connectStr, po.connectAccount, po.connectPwd);
        } else if ("sqlserver".equalsIgnoreCase(dataSource.driveType)) {
            dataSource.list = sqlServerConUtils.getTableNameAndColumns(po.connectStr, po.connectAccount, po.connectPwd, po.dbName);
        }

        return dataSource;
    }
}
