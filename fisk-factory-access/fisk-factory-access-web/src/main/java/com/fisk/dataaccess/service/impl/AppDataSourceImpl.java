package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.utils.sql.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lock
 */
@Service
public class AppDataSourceImpl extends ServiceImpl<AppDataSourceMapper, AppDataSourcePO> implements IAppDataSource {

    @Resource
    AppDataSourceMapper mapper;
    @Resource
    RedisUtil redisUtil;
    @Resource
    PgsqlUtils pgsqlUtils;
    @Resource
    ApiResultConfigImpl apiResultConfig;

    @Override
    public DataSourceDTO getDataSourceMeta(long appId) {

        DataSourceDTO dataSource = mapper.getDataSource(appId);

        if ("ftp".equalsIgnoreCase(dataSource.driveType) || "RestfulAPI".equalsIgnoreCase(dataSource.driveType) || "api".equalsIgnoreCase(dataSource.driveType)) {
            return null;
        }

        // 查询缓存里有没有redis的数据
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildDataSoureKey(appId));
        if (!flag) {
            // 将表和视图的结构存入redis
            setDataSourceMeta(appId);
        }

        try {
            String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildDataSoureKey(appId)).toString();
            if (StringUtils.isNotBlank(datasourceMetaJson)) {
                dataSource = JSON.parseObject(datasourceMetaJson, DataSourceDTO.class);
            }
        } catch (Exception e) {
            log.error("redis中获取数据失败");
            dataSource = null;
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
            AppDataSourcePO po = this.query().eq("app_id", appId).one();
            dataSource.appName = po.dbName;
            if (DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(dataSource.driveType)) {
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                // 表结构
                dataSource.tableDtoList = mysqlConUtils.getTableNameAndColumns(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL));
                //视图结构
                dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL));
            } else if (DataSourceTypeEnum.ORACLE.getName().equalsIgnoreCase(dataSource.driveType)) {
                OracleUtils oracleUtils = new OracleUtils();
                // 表结构
                dataSource.tableDtoList = oracleUtils.getTableNameAndColumn(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.dbName);
                //视图结构
                //dataSource.viewDtoList = oracleUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.connectAccount);
            } else if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dataSource.driveType)) {
                SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                // 表结构
                dataSource.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER), po.dbName);
                // 视图结构
                dataSource.viewDtoList = sqlServerPlusUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER));
            } else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dataSource.driveType)) {
                // 表结构
                dataSource.tableDtoList = pgsqlUtils.getTableNameAndColumnsPlus(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.POSTGRESQL));
                //视图结构
                dataSource.viewDtoList = new ArrayList<>();

            } else if (DataSourceTypeEnum.ORACLE_CDC.getName().equalsIgnoreCase(dataSource.driveType)) {
                OracleUtils oracleUtils = new OracleUtils();
                // 表结构
                dataSource.tableDtoList = oracleUtils.getTableNameAndColumn(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.dbName);
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

    @Override
    public List<String> getDatabaseNameList(AppDataSourceDTO dto) {

        if (StringUtils.isBlank(dto.driveType)) {
            throw new FkException(ResultEnum.DRIVETYPE_IS_NULL);
        }

        // jdbc连接信息
        String url = "";

        DataSourceTypeEnum driveType = DataSourceTypeEnum.getValue(dto.driveType);
        switch (Objects.requireNonNull(driveType)) {
            case MYSQL:
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                url = "jdbc:mysql://" + dto.host + ":" + dto.port;
                Connection conn = DbConnectionHelper.connection(url, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
                return mysqlConUtils.getAllDatabases(conn);
            case SQLSERVER:
                url = "jdbc:sqlserver://" + dto.host + ":" + dto.port;
                SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                conn = DbConnectionHelper.connection(url, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER);
                return sqlServerPlusUtils.getAllDatabases(conn);
            case ORACLE:
                return null;
            default:
                break;
        }

        return null;
    }

    public AppDataSourceDTO getDataSourceByAppId(long appId) {
        QueryWrapper<AppDataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataSourcePO::getAppId, appId);
        AppDataSourcePO po = mapper.selectOne(queryWrapper);
        AppDataSourceDTO data = AppDataSourceMap.INSTANCES.poToDto(po);
        data.apiResultConfigDtoList = apiResultConfig.getApiResultConfig(po.id);
        return data;
    }

}
