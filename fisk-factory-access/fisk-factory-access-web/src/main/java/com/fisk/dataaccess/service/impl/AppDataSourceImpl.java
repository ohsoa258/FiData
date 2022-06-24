package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
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
    @Resource
    RedisUtil redisUtil;

    @Override
    public DataSourceDTO getDataSourceMeta(long appId) {

        DataSourceDTO dataSource = mapper.getDataSource(appId);

        // 查询缓存里有没有redis的数据
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildDataSoureKey(appId));
        if (!flag) {
            // 将表和视图的结构存入redis
            setDataSourceMeta(appId);
        }

        String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildDataSoureKey(appId)).toString();
        if (StringUtils.isNotBlank(datasourceMetaJson)) {
            dataSource = JSON.parseObject(datasourceMetaJson, DataSourceDTO.class);
        }
        return dataSource;
    }

    @Override
    public DataSourceDTO setDataSourceMeta(long appId) {
        try {
            DataSourceDTO dataSource = mapper.getDataSource(appId);
            if (dataSource == null) {
                log.error(appId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
                return null;
            }
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

            if (CollectionUtils.isNotEmpty(dataSource.tableDtoList)) {
                redisUtil.set(RedisKeyBuild.buildDataSoureKey(appId), JSON.toJSONString(dataSource));
            }

            return dataSource;
        } catch (Exception e) {
            log.error(appId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
            return null;
        }
    }
}
