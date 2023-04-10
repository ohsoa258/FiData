package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.dto.v3.SourceColumnMetaQueryDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.utils.sql.*;
import com.fisk.system.client.UserClient;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AppDataSourceImpl extends ServiceImpl<AppDataSourceMapper, AppDataSourcePO> implements IAppDataSource {

    @Resource
    AppDataSourceMapper mapper;
    @Resource
    RedisUtil redisUtil;
    @Resource
    ApiResultConfigImpl apiResultConfig;
    @Resource
    PgsqlUtils pgsqlUtils;

    @Resource
    private UserClient userClient;

    @Override
    public List<DataSourceDTO> getDataSourceMeta(long appId) {

        List<DataSourceDTO> dsList = mapper.getDataSourceListById(appId);
        if (CollectionUtils.isEmpty(dsList)) {
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }

        List<DataSourceDTO> result = new ArrayList<>();
        for (DataSourceDTO dataSource : dsList) {
            if ("ftp".equalsIgnoreCase(dataSource.driveType) || "RestfulAPI".equalsIgnoreCase(dataSource.driveType) || "api".equalsIgnoreCase(dataSource.driveType)) {
                return null;
            }

            // 查询缓存里有没有redis的数据
            boolean flag = redisUtil.hasKey(RedisKeyBuild.buildDataSoureKey(dataSource.id));
            if (!flag) {
                // 将表和视图的结构存入redis
                setDataSourceMeta(appId);
            }

            try {
                String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildDataSoureKey(dataSource.id)).toString();
                if (StringUtils.isNotBlank(datasourceMetaJson)) {
                    dataSource = JSON.parseObject(datasourceMetaJson, DataSourceDTO.class);
                }
            } catch (Exception e) {
                log.error("redis中获取数据失败");
                dataSource = null;
            }
            result.add(dataSource);
        }

        return result;
    }

    @Override
    public DataSourceDTO setDataSourceMeta(long appId) {
        try {
            DataSourceDTO dataSource = mapper.getDataSourceById(appId);
            if (dataSource == null) {
                log.error(appId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
                return null;
            }
            AppDataSourcePO po = this.query().eq("id", appId).one();
            dataSource.appName = po.dbName;
            if (DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(dataSource.driveType)) {
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                // 表结构
                dataSource.tableDtoList = mysqlConUtils.getTableNameAndColumns(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL));
                //视图结构
                dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL));
            } else if (DataSourceTypeEnum.ORACLE.getName().equalsIgnoreCase(dataSource.driveType)) {
                dataSource.appName = po.serviceName;
                OracleUtils oracleUtils = new OracleUtils();
                // 表结构
                dataSource.tableDtoList = oracleUtils.getTableNameList(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.dbName);
                //视图结构
                ////dataSource.viewDtoList = oracleUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.connectAccount);
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
                dataSource.tableDtoList = oracleUtils.getTableNameList(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.dbName);
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
    public List<TableStructureDTO> getSourceColumnMeta(SourceColumnMetaQueryDTO dto) {
        DataSourceDTO dataSource = mapper.getDataSourceById(dto.appId);
        if (dataSource == null) {
            log.error(dto.appId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
            return null;
        }
        AppDataSourcePO po = this.query().eq("id", dto.appId).one();
        if (DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(dataSource.driveType)) {
            MysqlConUtils mysqlConUtils = new MysqlConUtils();
            Connection conn = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
            return mysqlConUtils.getColNames(conn, dto.name);
        } else if (DataSourceTypeEnum.ORACLE.getName().equalsIgnoreCase(dataSource.driveType)) {
            OracleUtils oracleUtils = new OracleUtils();
            Connection conn = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE);
            return dto.queryType == 1 ? oracleUtils.getTableColumnInfoList(conn, po.serviceName, dto.name) : null;
        } else if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dataSource.driveType)) {
            SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
            Connection conn = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER);
            return sqlServerPlusUtils.getViewField(conn, dto.name);
        } else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dataSource.driveType)) {

            Connection conn = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.POSTGRESQL);
            return dto.queryType == 1 ? pgsqlUtils.getTableColumnName(conn, dto.name) : null;
        }
        return null;
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

    @Override
    public List<DataSourceInfoDTO> getDataSourcesByAppId(Integer appId) {
        List<AppDataSourcePO> list = this.query().select("db_name", "id").eq("app_id", appId).list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<DataSourceInfoDTO> data = new ArrayList<>();

        for (AppDataSourcePO po : list) {
            DataSourceInfoDTO dto = new DataSourceInfoDTO();
            dto.id = po.id;
            dto.name = po.dbName;
            data.add(dto);
        }

        return data;
    }

    @Override
    public List<com.fisk.system.dto.datasource.DataSourceDTO> getOutDataSourcesByTypeId(String driverType) {
        //远程调用接口，获取外部数据源信息
        ResultEntity<List<com.fisk.system.dto.datasource.DataSourceDTO>> allExternalDataSource = userClient.getAllExternalDataSource();
        //判断获取是否成功
        if (allExternalDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        //获取筛选前外部数据源集合
        List<com.fisk.system.dto.datasource.DataSourceDTO> data = allExternalDataSource.getData();
        //新建集合--预装载筛选后的外部数据源
        List<com.fisk.system.dto.datasource.DataSourceDTO> dataSourceDTOS = new ArrayList<>();
        //调用封装的方法
        List<com.fisk.system.dto.datasource.DataSourceDTO> result = checkConType(data, dataSourceDTOS, driverType);

        //判断筛选后的集合是否有内容
        if (CollectionUtils.isEmpty(result)) {
            log.info("平台配置的外部数据源中没有" + driverType + "类型的数据库！");
        }
        //数据库密码不显示
        result.forEach(dataSourceDTO -> {
            dataSourceDTO.setConPassword("********");
        });
        return result;
    }

    /**
     * 方法封装：根据数据源类型筛选所需的外部数据源
     *
     * @param data           筛选前的外部数据源集合
     * @param dataSourceDTOS 预装载筛选后的外部数据源
     * @param driverType     数据源类型
     * @return
     */
    public static List<com.fisk.system.dto.datasource.DataSourceDTO> checkConType(List<com.fisk.system.dto.datasource.DataSourceDTO> data,
                                                                                  List<com.fisk.system.dto.datasource.DataSourceDTO> dataSourceDTOS,
                                                                                  String driverType) {
        if (driverType.equalsIgnoreCase(DataSourceTypeEnum.MYSQL.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 0 && dataSourceDTO.conPort != 0) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 1) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.FTP.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 10) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.ORACLE.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 6) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.RestfulAPI.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 0 && dataSourceDTO.conPort == 0) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.API.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 9) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 4) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.SFTP.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 11) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        }
        return dataSourceDTOS;
    }

    @Override
    public ResultEntity<com.fisk.system.dto.datasource.DataSourceDTO> getOutSourceById(Integer id) {
        ResultEntity<com.fisk.system.dto.datasource.DataSourceDTO> data = userClient.getById(id);
        //数据库密码不显示
        com.fisk.system.dto.datasource.DataSourceDTO dto = data.getData();
        dto.setConPassword("********");
        if (data.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return data;
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
