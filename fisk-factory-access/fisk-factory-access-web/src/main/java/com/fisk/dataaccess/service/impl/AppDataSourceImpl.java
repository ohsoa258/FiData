package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataaccess.dto.sapbw.ProviderAndDestination;
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
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.ext.Environment;
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
    DM8Utils dm8Utils;
    @Resource
    AppRegistrationImpl appRegistration;

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
            DataSourceDTO dataSourceDTO = new DataSourceDTO();
            if (!flag) {
                // 将表和视图的结构存入redis
                dataSourceDTO = setDataSourceMeta(appId, dataSource.id);
            }

            try {
                String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildDataSoureKey(dataSource.id)).toString();
                if (StringUtils.isNotBlank(datasourceMetaJson)) {
                    dataSource = JSON.parseObject(datasourceMetaJson, DataSourceDTO.class);
                } else {
                    dataSource = dataSourceDTO;
                }
            } catch (Exception e) {
                log.error("redis中获取数据失败");
                //在测试openedge数据库时发现，如果库内表过多，导致存不进redis里面时，会导致返回空数据
                dataSource = dataSourceDTO;
            }
            result.add(dataSource);
        }

        return result;
    }

    @Override
    public DataSourceDTO setDataSourceMeta(long appId, long appDataSourceId) {
        MyDestinationDataProvider myProvider = null;
        try {
            DataSourceDTO dataSource = mapper.getDataSource(appDataSourceId);
            if (dataSource == null) {
                log.error(appId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
                return null;
            }
            AppDataSourcePO po = this.query().eq("id", appDataSourceId).one();
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
            } else if (DataSourceTypeEnum.OPENEDGE.getName().equalsIgnoreCase(dataSource.driveType)) {
                // 表结构
                Connection con = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.OPENEDGE);
                dataSource.tableDtoList = OpenEdgeUtils.getTableNameAndColumnsPlus(con, po.dbName);
            } else if (DataSourceTypeEnum.SAPBW.getName().equalsIgnoreCase(dataSource.driveType)) {
                ProviderAndDestination providerAndDestination =
                        DbConnectionHelper.myDestination(po.host, po.sysNr, po.port, po.connectAccount, po.connectPwd, po.lang);
                JCoDestination destination = providerAndDestination.getDestination();
                myProvider = providerAndDestination.getMyProvider();
                dataSource.tableDtoList = SapBwUtils.getAllCubesV2(destination, myProvider);
            } else if (DataSourceTypeEnum.DORIS_CATALOG.getName().equalsIgnoreCase(dataSource.driveType)) {
                dataSource.tableDtoList = null;
            } else if (DataSourceTypeEnum.DM8.getName().equalsIgnoreCase(dataSource.driveType)) {
                // 表结构
                dataSource.tableDtoList = dm8Utils.getTableNameAndColumns(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.DM8), po.getDbName());
            }

            if (CollectionUtils.isNotEmpty(dataSource.tableDtoList)) {
                redisUtil.set(RedisKeyBuild.buildDataSoureKey(appDataSourceId), JSON.toJSONString(dataSource));
            }
            return dataSource;
        } catch (Exception e) {
            log.error("查询数据源表信息失败：" + e);
            log.error(appId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
            return null;
        } finally {
            if (myProvider != null) {
                Environment.unregisterDestinationDataProvider(myProvider);
            }
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
        } else if (DataSourceTypeEnum.OPENEDGE.getName().equalsIgnoreCase(dataSource.driveType)) {
            // 表结构
            Connection con = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.OPENEDGE);
            return OpenEdgeUtils.getColumnsName(con, dto.name);
        } else if (DataSourceTypeEnum.SAPBW.getName().equalsIgnoreCase(dataSource.driveType)) {
            ProviderAndDestination providerAndDestination =
                    DbConnectionHelper.myDestination(po.host, po.sysNr, po.port, po.connectAccount, po.connectPwd, po.lang);
            JCoDestination destination = providerAndDestination.getDestination();
            MyDestinationDataProvider myProvider = providerAndDestination.getMyProvider();
            return SapBwUtils.getVariablesByCubeName(destination, myProvider, dto.name);
        } else if (DataSourceTypeEnum.DM8.getName().equalsIgnoreCase(dataSource.driveType)) {
            Connection con = DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.DM8);
            return dm8Utils.getColumnsName(con, dto.name, po.dbName);
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
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
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
        //数据库密码不显示    目前需要显示，因为数据接入在引用平台配置的外部数据源时，仍然需要测试连接这一动作，
//        result.forEach(dataSourceDTO -> {
//            dataSourceDTO.setConPassword("********");
//        });

//        //目前这个操作交由前端过滤  如果想要重用下述代码，将当前方法入参多入一个appId 当前应用id即可
//        //不允许app重复选取同个数据源
//        LambdaQueryWrapper<AppDataSourcePO> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(AppDataSourcePO::getAppId, appid);
//        //获取到当前应用已经拥有的所有数据源
//        List<AppDataSourcePO> appDataSourcePOS = list(wrapper);
//        if (CollectionUtils.isEmpty(appDataSourcePOS)) {
//            log.info("当前app下无数据源，可以继续选择");
//        } else {
//            log.info("当前app下已经有数据源，筛选ing.....");
//            //新建集合预装载当前应用下的数据源id集合
//            List<Integer> sourceIds = new ArrayList<>();
//            //遍历当前应用下的数据源集合
//            appDataSourcePOS.forEach(e -> {
//                //将数据源id插入到我们预先准备的集合中
//                sourceIds.add(e.systemDataSourceId);
//            });
//            //从筛选后的外部数据源集合中再次筛选，过滤掉当前应用已经拥有的数据源
//            return result.stream().filter(e -> !sourceIds.contains(e.id)).collect(Collectors.toList());
//        }
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
                if (dataSourceDTO.conType.getValue() == 8) {
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
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.OPENEDGE.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 12) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.SAPBW.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 13) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.WEBSERVICE.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 14) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.DORIS_CATALOG.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 15) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.DM8.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 16) {
                    dataSourceDTOS.add(dataSourceDTO);
                }
            });
        } else if (driverType.equalsIgnoreCase(DataSourceTypeEnum.HUDI.getName())) {
            data.forEach(dataSourceDTO -> {
                if (dataSourceDTO.conType.getValue() == 17) {
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

    /**
     * 获取指定app下的非重复驱动类型
     *
     * @param id
     * @return
     */
    @Override
    public List<AppDataSourcePO> getDataSourceDrivesTypeByAppId(Long id) {
//        代码层面去重
//        LambdaQueryWrapper<AppDataSourcePO> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(AppDataSourcePO::getAppId, id);
//        List<AppDataSourcePO> list = list(wrapper);
//        // 根据 driveType驱动类型 进行去重
//        return list.stream()
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AppDataSourcePO::getDriveType))),
//                        ArrayList::new));
        //sql去重查询
        QueryWrapper<AppDataSourcePO> wrapper = new QueryWrapper<>();
        wrapper.select("Distinct drive_type").lambda().eq(AppDataSourcePO::getAppId, id);
        return list(wrapper);
    }

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 配合task模块，当平台配置修改数据源信息时，数据接入引用的数据源信息一并修改
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean editDataSource(DataSourceSaveDTO dto) {
        try {
            //获取平台配置那边修改的数据源的id
            Integer systemDataSourceId = dto.id;
            QueryWrapper<AppDataSourcePO> wrapper = new QueryWrapper<>();
            wrapper.eq("system_data_source_id", systemDataSourceId);
            //通过平台配置--数据源id获取
            List<AppDataSourcePO> list = list(wrapper);

            //新建集合预装载批量更新参数
            List<AppDataSourcePO> appDataSourcePOS = new ArrayList<>();
            //遍历装载预更新的po
            for (AppDataSourcePO appDataSourcePO : list) {
                //新建AppDataSourcePO对象，预装载参数
                AppDataSourcePO dataSourcePO = new AppDataSourcePO();
                //1、将从数据库里查询到的po对象赋予给我们的dataSourcePO
                dataSourcePO = appDataSourcePO;

                //2、再给 dataSourcePO 装载页面修改的数值（也就是task那边传来的参数）
                dataSourcePO.connectStr = dto.conStr;
                dataSourcePO.host = dto.conIp;
                dataSourcePO.port = String.valueOf(dto.conPort);
                dataSourcePO.dbName = dto.conDbname;

                String driverName = dto.conType.getName();

                dataSourcePO.driveType = changeEnum(driverName);

                dataSourcePO.connectAccount = dto.conAccount;
                dataSourcePO.connectPwd = dto.conPassword;

                dataSourcePO.realtimeAccount = dto.conAccount;
                dataSourcePO.realtimePwd = dto.conPassword;

                dataSourcePO.serviceType = dto.serviceType;
                dataSourcePO.serviceName = dto.serviceName;
                dataSourcePO.domainName = dto.domainName;
                dataSourcePO.fileSuffix = dto.fileSuffix;
                dataSourcePO.fileBinary = dto.fileBinary;
                dataSourcePO.pdbName = dto.pdbName;
                dataSourcePO.signatureMethod = dto.signatureMethod;
                dataSourcePO.consumerKey = dto.consumerKey;
                dataSourcePO.consumerSecret = dto.consumerSecret;
                dataSourcePO.accessToken = dto.accessToken;
                dataSourcePO.tokenSecret = dto.tokenSecret;
                dataSourcePO.accountKey = dto.accountKey;
                dataSourcePO.pwdKey = dto.pwdKey;
                dataSourcePO.expirationTime = dto.expirationTime;
                dataSourcePO.token = dto.token;
                dataSourcePO.authenticationMethod = dto.authenticationMethod;
                dataSourcePO.sysNr = dto.sysNr;
                dataSourcePO.lang = dto.lang;
                appDataSourcePOS.add(dataSourcePO);
            }
            return updateBatchById(appDataSourcePOS);
        } catch (Exception e) {
            log.error("平台配置修改系统数据源时，连带修改数据接入引用的数据源失败！");
            throw new FkException(ResultEnum.SAVE_ACCESS_DATA_SOURCE_ERROR, "平台配置修改系统数据源时，连带修改数据接入引用的数据源失败！报错明细：" + e.getMessage());
        }
    }

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据SystemDataSourceId获取数据接入引用的数据源信息
     *
     * @param id
     * @return
     */
    @Override
    public List<AppDataSourceDTO> getDataSourcesBySystemDataSourceId(Integer id) {
        try {
            QueryWrapper<AppDataSourcePO> wrapper = new QueryWrapper<>();
            wrapper.eq("system_data_source_id", id);
            List<AppDataSourcePO> list = list(wrapper);
            //通过平台配置--数据源id获取
            return AppDataSourceMap.INSTANCES.listPoToDto(list);
        } catch (Exception e) {
            log.error("根据SystemDataSourceId获取数据接入引用的数据源信息失败！");
            throw new FkException(ResultEnum.GET_ACCESS_DATA_SOURCE_ERROR, "根据SystemDataSourceId获取数据接入引用的数据源信息失败,报错详情：" + e.getMessage());
        }
    }

    /**
     * 获取数据接入引用的数据源id
     *
     * @param id
     * @return
     */
    @Override
    public AppDataSourceDTO getAccessDataSources(Long id) {
        QueryWrapper<AppDataSourcePO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        AppDataSourcePO one = getOne(wrapper);
        if (one != null) {
            AppDataSourceDTO appDataSourceDTO = AppDataSourceMap.INSTANCES.poToDto(one);
            return appDataSourceDTO;
        } else {
            return null;
        }
    }

    @Override
    public ResultEntity<com.fisk.system.dto.datasource.DataSourceDTO> getSystemDataSourceById(Integer id) {
        LambdaQueryWrapper<AppDataSourcePO> wrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<AppDataSourcePO> eq = wrapper.eq(AppDataSourcePO::getId, id);
        AppDataSourcePO one = getOne(eq);

        long appId = one.getAppId();
        AppRegistrationDTO data1 = appRegistration.getData(appId);
        Integer targetDbId = data1.getTargetDbId();

        ResultEntity<com.fisk.system.dto.datasource.DataSourceDTO> data = userClient.getById(targetDbId);
        //数据库密码不显示
        com.fisk.system.dto.datasource.DataSourceDTO dto = data.getData();
        dto.setConPassword("********");
        if (data.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return data;

    }

    /**
     * 数据接入，刷新redis里存储的表信息
     *
     * @param appId
     * @return
     */
    @Override
    public List<DataSourceDTO> refreshRedis(long appId) {

        List<DataSourceDTO> dsList = mapper.getDataSourceListById(appId);
        if (CollectionUtils.isEmpty(dsList)) {
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }

        List<DataSourceDTO> result = new ArrayList<>();
        for (DataSourceDTO dataSource : dsList) {
            log.info("刷新redis..........");
            if ("ftp".equalsIgnoreCase(dataSource.driveType) || "RestfulAPI".equalsIgnoreCase(dataSource.driveType) || "api".equalsIgnoreCase(dataSource.driveType)) {
                return null;
            }

            /**
             * 删除旧缓存
             */
            redisUtil.del(RedisKeyBuild.buildDataSoureKey(appId));
            redisUtil.del(RedisKeyBuild.buildDataSoureKey(dataSource.id));

            DataSourceDTO dataSourceDTO = new DataSourceDTO();
            // 将表和视图的结构重新查询，重新存入redis
            dataSourceDTO = setDataSourceMeta(appId, dataSource.id);

            try {
                String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildDataSoureKey(dataSource.id)).toString();
                if (StringUtils.isNotBlank(datasourceMetaJson)) {
                    dataSource = JSON.parseObject(datasourceMetaJson, DataSourceDTO.class);
                } else {
                    dataSource = dataSourceDTO;
                }
            } catch (Exception e) {
                log.error("redis中获取数据失败");
                //在测试openedge数据库时发现，如果库内表过多，导致存不进redis里面时，会导致返回空数据
                dataSource = dataSourceDTO;
            }
            result.add(dataSource);
        }

        return result;
    }

    /**
     * 通过应用id获取应用引用的所有数据源信息
     *
     * @param appId
     * @return
     */
    @Override
    public List<AppDataSourceDTO> getAppSourcesByAppId(long appId) {
        LambdaQueryWrapper<AppDataSourcePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppDataSourcePO::getAppId, appId);
        List<AppDataSourcePO> list = list(wrapper);
        return AppDataSourceMap.INSTANCES.listPoToDto(list);
    }

    /**
     * 枚举数值转换
     *
     * @param driverName
     * @return
     */
    private String changeEnum(String driverName) {
        if (DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.MYSQL.getName();
        } else if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.SQLSERVER.getName();
        } else if (DataSourceTypeEnum.FTP.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.FTP.getName();
        } else if (DataSourceTypeEnum.ORACLE.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.ORACLE.getName();
        } else if (DataSourceTypeEnum.RestfulAPI.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.RestfulAPI.getName();
        } else if (DataSourceTypeEnum.API.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.API.getName();
        } else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.POSTGRESQL.getName();
        } else if (DataSourceTypeEnum.ORACLE_CDC.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.ORACLE_CDC.getName();
        } else if (DataSourceTypeEnum.SFTP.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.SFTP.getName();
        } else if (DataSourceTypeEnum.OPENEDGE.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.OPENEDGE.getName();
        } else if (DataSourceTypeEnum.SAPBW.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.SAPBW.getName();
        } else if (DataSourceTypeEnum.WEBSERVICE.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.WEBSERVICE.getName();
        } else if (DataSourceTypeEnum.DORIS_CATALOG.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.DORIS_CATALOG.getName();
        } else if (DataSourceTypeEnum.DM8.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.DM8.getName();
        } else if (DataSourceTypeEnum.HUDI.getName().equalsIgnoreCase(driverName)) {
            driverName = DataSourceTypeEnum.HUDI.getName();
        }
        return driverName;
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
