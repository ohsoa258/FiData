package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.CreateSchemaSqlUtils;
import com.fisk.common.core.utils.FileBinaryUtils;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.server.datasource.ExternalDataSourceDTO;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelTableDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelTableTypeEnum;
import com.fisk.common.service.accessAndModel.ServerTypeEnum;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.GetConfigDTO;
import com.fisk.dataaccess.dto.SyncOneTblForHudiDTO;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datafactory.AccessRedirectDTO;
import com.fisk.dataaccess.dto.hudi.HudiSyncDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobParameterDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.sapbw.CubesAndCats;
import com.fisk.dataaccess.dto.sapbw.ProviderAndDestination;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.AppDriveTypeEnum;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.utils.httprequest.Impl.BuildHttpRequestImpl;
import com.fisk.dataaccess.utils.sql.*;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.CDCAppNameAndTableVO;
import com.fisk.dataaccess.vo.datafactory.SyncTableCountVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.dataaccess.vo.table.CDCAppNameVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceResultDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.ext.Environment;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.fisk.dataaccess.enums.HttpRequestEnum.POST;

/**
 * @author Lock
 */
@Service
@Slf4j
public class AppRegistrationImpl extends ServiceImpl<AppRegistrationMapper, AppRegistrationPO> implements IAppRegistration {
    @Resource
    private IAppDataSource iAppDataSource;
    @Resource
    private AppDataSourceMapper appDataSourceMapper;
    @Resource
    BuildHttpRequestImpl buildHttpRequest;
    @Resource
    private AppRegistrationMapper mapper;
    @Resource
    private ApiResultConfigImpl apiResultConfig;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private AppDriveTypeMapper appDriveTypeMapper;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;
    @Resource
    private TableAccessMapper tableAccessMapper;
    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private TableFieldsMapper tableFieldsMapper;
    @Resource
    private TableFieldsImpl tableFieldsImpl;
    @Resource
    private ApiConfigMapper apiConfigMapper;
    @Resource
    private ApiConfigImpl apiConfigImpl;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private DataManageClient dataManageClient;
    @Resource
    private UserClient userClient;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    OracleCdcUtils oracleCdcUtils;
    @Resource
    RedisUtil redisUtil;
    @Resource
    DM8Utils dm8Utils;
    @Resource
    HiveUtils hiveUtils;
    @Resource
    GetConfigDTO getConfig;

    @Value("${spring.open-metadata}")
    private Boolean openMetadata;
    @Resource
    PgsqlUtils pgsqlUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEntity<AtlasEntityQueryVO> addData(AppRegistrationDTO appRegistrationDTO) {
        //先获取到要添加的外部数据源们 driveType = mysql/sqlserver/oracle/postgresql
        List<AppDataSourceDTO> data = appRegistrationDTO.getAppDatasourceDTO();
        //调用封装的方法，校验当前应用选择的数据源类型是否存在冲突！
        Boolean aBoolean = checkSourcesTypeIfOk(data);
        if (!aBoolean) {
            log.error("当前应用选择的数据源类型存在冲突！");
            throw new FkException(ResultEnum.DATASOURCE_TYPE_ERROR, "当前应用选择的数据源类型存在冲突");
        }

        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // dto->po
        AppRegistrationPO po = appRegistrationDTO.toEntity(AppRegistrationPO.class);
        po.setCreateUser(String.valueOf(userId));

        // 数据保存需求更改: 添加应用的时候，相同的应用名称不可以再次添加
        List<String> appNameList = baseMapper.getAppName();
        String appName = po.getAppName();
        boolean contains = appNameList.contains(appName);
        if (contains) {
            return ResultEntityBuild.build(ResultEnum.DATA_EXISTS);
        }

        // 判断
        if (!po.whetherSchema) {
            List<String> appAbbreviationList = baseMapper.getAppAbbreviation();
            if (appAbbreviationList.contains(po.appAbbreviation)) {
                return ResultEntityBuild.build(ResultEnum.DATAACCESS_APPABBREVIATION_SUCCESS);
            }
        }

        List<AppDataSourceDTO> datasourceDTO = appRegistrationDTO.getAppDatasourceDTO();

        //
        /* todo 不筛查连接账号是否重复
        List<String> realtimeAccountList = appDataSourceMapper.getRealtimeAccountList(datasourceDTO.realtimeAccount);
        // 当前为实时应用
        if (po.appType == 0 && realtimeAccountList.contains(datasourceDTO.realtimeAccount)) {
            return ResultEntityBuild.build(ResultEnum.REALTIME_ACCOUNT_ISEXIST);
        }*/

        // 保存tb_app_registration数据
        boolean save = this.save(po);
        if (!save) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<AppDataSourcePO> modelDataSource = AppDataSourceMap.INSTANCES.listDtoToPo(datasourceDTO);
        // 保存tb_app_datasource数据
        modelDataSource.stream().filter(Objects::nonNull).forEach(e -> {
            e.setAppId(po.getId());
            e.createUser = String.valueOf(userId);

            //sftp秘钥方式,存储二进制数据
            if (DataSourceTypeEnum.SFTP.getName().equals(e.driveType.toLowerCase()) && e.serviceType == 1) {
                e.fileBinary = fileToBinaryStr(e.connectStr);
            }
//            //同步到平台配置  暂时注释，目前数据接入数据源已整合到平台配置，这段代码暂不需要（无需二次保存）
//            if (!DataSourceTypeEnum.SFTP.getName().equals(e.driveType.toLowerCase())
//                    && !DataSourceTypeEnum.FTP.getName().equals(e.driveType.toLowerCase())
//                    && !DataSourceTypeEnum.API.getName().equals(e.driveType.toLowerCase())
//                    && !DataSourceTypeEnum.RestfulAPI.getName().equals(e.driveType.toLowerCase())) {
//                e.systemDataSourceId = synchronizationSystemDataSource(e, po.appName).id;
//            }
        });

        //如果是RestfulAPI，进行如下操作  为了数接的/apiConfig/getToken接口可以正常使用
        //获取实时api的临时token
        modelDataSource.forEach(m -> {
            if (DataSourceTypeEnum.RestfulAPI.getName().equalsIgnoreCase(m.driveType)) {
                m.realtimeAccount = m.connectAccount;
                m.realtimePwd = m.connectPwd;
            }
        });

        boolean insert = appDataSourceImpl.saveBatch(modelDataSource);
        if (!insert) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        //jtw类型配置返回结果json串
        appRegistrationDTO.appDatasourceDTO.stream().map(e -> {
            if (e.authenticationMethod != null && e.authenticationMethod == 3) {
                AppDataSourceDTO dataSourceByAppId = appDataSourceImpl.getDataSourceByAppId(po.getId());
                apiResultConfig.apiResultConfig(dataSourceByAppId.id, e.apiResultConfigDtoList);
            }
            return e;
        });

        AtlasEntityQueryVO vo = new AtlasEntityQueryVO();
        vo.userId = userId;
        vo.appId = String.valueOf(po.getId());

        //hive不要在doris建schema
        boolean ifHive = false;
        for (AppDataSourcePO appDataSourcePO : modelDataSource) {
            if (DbTypeEnum.doris_catalog.getName().equalsIgnoreCase(appDataSourcePO.driveType)) {
                ifHive = true;
                break;
            }
        }

        if (!ifHive) {
            //是否添加schema
            if (appRegistrationDTO.whetherSchema) {
                VerifySchema(po.appAbbreviation, po.targetDbId);
            }
        }

        //hudi入参配置  同步所有表
        //如果是hudi 入仓配置 开启了同步所有表
        if (appRegistrationDTO.ifSyncAllTables != null) {
            if (appRegistrationDTO.ifSyncAllTables == 1) {
//            new Thread(() -> {
                log.info("hudi 入仓配置 - 开始同步所有表-------------------------------");
                long appId = po.getId();
                List<AppDataSourceDTO> appSourcesByAppId = appDataSourceImpl.getAppSourcesByAppId(appId);
                //获取来源数据源id
                int systemDataSourceId = appSourcesByAppId.get(0).getSystemDataSourceId();
                //获取appdatasourceid
                int appDatasourceId = Math.toIntExact(appSourcesByAppId.get(0).getId());
                //获取来源数据源id
                //hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
                hudiSyncAllTablesToFidataConfig(systemDataSourceId, appDatasourceId, appId, po.getAppName());
//            }).start();
            }
        }

        if (openMetadata) {
            //新增业务分类
            addClassification(appRegistrationDTO);

            //数据库应用,需要新增元数据对象
            List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
            for (AppDataSourcePO item : modelDataSource) {
                List<MetaDataInstanceAttributeDTO> metaData = addDataSourceMetaData(po, item);
                if (metaData != null) {
                    list.addAll(metaData);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                try {
                    MetaDataAttributeDTO metaDataAttribute = new MetaDataAttributeDTO();
                    metaDataAttribute.instanceList = list;
                    metaDataAttribute.userId = Long.parseLong(userHelper.getLoginUserInfo().id.toString());
                    // 更新元数据内容
                    log.info("数据接入ods构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
                    dataManageClient.consumeMetaData(list);
                } catch (Exception e) {
                    log.error("【dataManageClient.MetaData()】方法报错,ex", e);
                }
            }
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);
    }

    /**
     * hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
     *
     * @param dbId
     */
    public void hudiSyncAllTablesToFidataConfig(Integer dbId, Integer appDatasourceId, Long appId, String appName) {
        log.info("hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库");
        ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(dbId);
        DataSourceDTO dto = datasource.getData();
        if (dto == null) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        Connection conn = null;

        //获取到所有表名
        List<TablePyhNameDTO> tableNames = new ArrayList<>();

        log.info("引用的数据源类型" + dto.conType);
        try {
            switch (dto.conType) {
                case MYSQL:
                    MysqlConUtils mysqlConUtils = new MysqlConUtils();
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tableNames = mysqlConUtils.getTrueTableNameAndColumns(conn, dto.conDbname);
                    break;
                case SQLSERVER:
                    SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tableNames = sqlServerPlusUtils.getTrueTableNameAndColumnsPlus(conn, dto.conDbname);
                    break;
                case ORACLE:
                    OracleUtils oracleUtils = new OracleUtils();
                    log.info("ORACLE驱动开始加载");
                    log.info("ORACLE驱动基本信息：" + com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                    log.info("ORACLE驱动加载完毕");
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tableNames = oracleUtils.getTrueTableNameList(conn, dto.conDbname);
                    break;
                default:
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
            }

            log.info("查询到的库表字段详情：" + tableNames);

            for (TablePyhNameDTO table : tableNames) {

                TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                tableAccessDTO.setAppDataSourceId(appDatasourceId);
                tableAccessDTO.setAppId(appId);
                tableAccessDTO.setAppName(appName);
                tableAccessDTO.setDisplayName(table.getTableName());
                tableAccessDTO.setIsRealtime(1);
                tableAccessDTO.setPublish(0);
                tableAccessDTO.setSyncSrc("");
                tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                tableAccessDTO.setIfOpenCdc(1);
                String tableName = table.getTableName();
                if (tableName.contains(".")) {
                    tableName = tableName.replaceFirst("\\.", "_");
                }
                tableAccessDTO.setTableName(tableName);

                //将表插入都tb_table_access表 获取到表的主键id
                Integer accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);

                //获取当前表的字段
                List<TableStructureDTO> fields = table.getFields();

                List<TableFieldsDTO> list = new ArrayList<>();
                for (TableStructureDTO field : fields) {
                    TableFieldsDTO fieldDTO = new TableFieldsDTO();
                    fieldDTO.setTableAccessId(Long.valueOf(accessId));
                    fieldDTO.setSourceFieldName(field.fieldName);
                    fieldDTO.setSourceFieldType(field.fieldType);
                    fieldDTO.setFieldName(field.fieldName);
                    //字段类型暂时写死为string
                    fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                    //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                    fieldDTO.setFieldDes(field.getFieldDes());
                    fieldDTO.setIsPrimarykey(field.getIsPk());
                    //1：是实时物理表的字段，
                    //0：非实时物理表的字段
                    fieldDTO.setIsRealtime(1);
                    fieldDTO.setIsBusinesstime(0);
                    fieldDTO.setIsTimestamp(0);
                    fieldDTO.setSourceDbName(field.sourceDbName);
                    fieldDTO.setSourceTblName(table.getTableName());
                    list.add(fieldDTO);
                }
                List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
            }

        } catch (Exception e) {
            log.error("hudi-入仓配置add异常：" + e);
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }

    }

    /**
     * hudi入仓配置 同步指定单个来源数据库对应库下的表信息到fidata平台配置库
     *
     * @param dbId
     */
    public void hudiSyncOneTableToFidataConfig(Integer dbId, Integer appDatasourceId, Long appId, String appName, String tblName) {
        log.info("hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库");
        ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(dbId);
        DataSourceDTO dto = datasource.getData();
        if (dto == null) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        Connection conn = null;
        MongoClient mongoClient = null;

        //获取到所有表名
        List<TableStructureDTO> tables = new ArrayList<>();

        log.info("引用的数据源类型" + dto.conType);
        try {
            switch (dto.conType) {
                case MYSQL:
                    MysqlConUtils mysqlConUtils = new MysqlConUtils();
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tables = mysqlConUtils.getColNamesV2(conn, tblName, dto.conDbname);
                    break;
                case SQLSERVER:
                    SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tables = sqlServerPlusUtils.getColumnsNameV2(conn, tblName, dto.conDbname);
                    break;
                case ORACLE:
                    OracleUtils oracleUtils = new OracleUtils();
                    log.info("ORACLE驱动开始加载");
                    log.info("ORACLE驱动基本信息：" + com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                    log.info("ORACLE驱动加载完毕");
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tables = oracleUtils.getTableColumnInfoList(conn, tblName, dto.conDbname);
                    break;
                case MONGODB:
                    MongoDbUtils mongoDbUtils = new MongoDbUtils();
                    ServerAddress serverAddress = new ServerAddress(dto.conIp, dto.conPort);
                    List<ServerAddress> serverAddresses = new ArrayList<>();
                    serverAddresses.add(serverAddress);

                    //账号 验证数据库名 密码
                    MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential(dto.conAccount, dto.sysNr, dto.conPassword.toCharArray());
                    List<MongoCredential> mongoCredentials = new ArrayList<>();
                    mongoCredentials.add(scramSha1Credential);

                    mongoClient = new MongoClient(serverAddresses, mongoCredentials);

                    tables = mongoDbUtils.getTrueTableNameListForOneTbl(mongoClient, dto.conDbname, tblName);
                default:
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
            }

            log.info("查询到的库表字段详情：" + tables);

            TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
            tableAccessDTO.setAppDataSourceId(appDatasourceId);
            tableAccessDTO.setAppId(appId);
            tableAccessDTO.setAppName(appName);
            tableAccessDTO.setDisplayName(tblName);
            tableAccessDTO.setTableName(tblName);
            tableAccessDTO.setIsRealtime(1);
            tableAccessDTO.setPublish(0);
            tableAccessDTO.setSyncSrc("");
            tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
            tableAccessDTO.setIfOpenCdc(1);
            String tblName1 = "";
            if (tblName.contains(".")) {
                tblName1 = tblName.replaceFirst("\\.", "_");
            } else {
                tblName1 = tblName;
            }
            tableAccessDTO.setTableName(tblName1);

            //将表插入都tb_table_access表 获取到表的主键id
            Integer accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);

            //获取当前表的字段
            List<TableFieldsDTO> list = new ArrayList<>();
            for (TableStructureDTO field : tables) {
                TableFieldsDTO fieldDTO = new TableFieldsDTO();
                fieldDTO.setTableAccessId(Long.valueOf(accessId));
                fieldDTO.setSourceFieldName(field.fieldName);
                fieldDTO.setSourceFieldType(field.fieldType);
                fieldDTO.setFieldName(field.fieldName);
                //todo:字段类型暂时设置为STRING
                fieldDTO.setFieldType("STRING");
//                fieldDTO.setFieldType(field.fieldType);
                //todo:字段长度暂时不要
//                fieldDTO.setFieldLength((long) field.fieldLength);
                fieldDTO.setFieldDes(field.getFieldDes());
                fieldDTO.setIsPrimarykey(field.getIsPk());
                //1：是实时物理表的字段，
                //0：非实时物理表的字段
                fieldDTO.setIsRealtime(1);
                fieldDTO.setIsBusinesstime(0);
                fieldDTO.setIsTimestamp(0);
                fieldDTO.setSourceDbName(field.sourceDbName);
                fieldDTO.setSourceTblName(tblName);
                list.add(fieldDTO);
            }
            List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
            tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);

        } catch (Exception e) {
            log.error("hudi-入仓配置add异常：" + e);
            throw new FkException(ResultEnum.ERROR, e);
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

    }

    /**
     * hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
     * 同步方式 1全量  2增量
     *
     * @param syncDto
     */
    @Override
    public Object hudiSyncAllTables(HudiSyncDTO syncDto) {

        try {
            //获取应用id
            Long appId = syncDto.getAppId();

            LambdaQueryWrapper<AppRegistrationPO> w = new LambdaQueryWrapper<>();
            w.eq(AppRegistrationPO::getId, appId);
            //获取应用基本信息
            AppRegistrationPO one = getOne(w);

            //获取应用引用的appDatasource
            List<AppDataSourceDTO> appSource = appDataSourceImpl.getAppSourcesByAppId(appId);
            if (CollectionUtils.isEmpty(appSource)) {
                return ResultEnum.DATASOURCE_INFORMATION_ISNULL;
            }

            //应用引用的系统模块平台配置数据源id
            int systemDataSourceId = appSource.get(0).getSystemDataSourceId();
            //获取应用引用的appDatasource id
            int appDatasourceId = Math.toIntExact(appSource.get(0).getId());

            //同步方式 1全量  2增量
            int syncType = syncDto.getSyncType();
            switch (syncType) {
                case 1:
                    hudiSyncAllTablesByFull(systemDataSourceId, appDatasourceId, appId, one.getAppName());
                case 3:
                    hudiSyncAllTablesByTarget(systemDataSourceId, appDatasourceId, appId, one.getAppName());
                case 2:
                    hudiSyncAllTablesByMerge(systemDataSourceId, appDatasourceId, appId, one.getAppName());
            }

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("hudi入仓配置同步表失败：" + e);
            throw new FkException(ResultEnum.ACCESS_HUDI_SYNC_ERROR);
        }

    }


    /**
     * hudi入仓配置 全量同步所有来源数据库对应库下的表信息到fidata平台配置库
     *
     * @param dbId
     */
    public void hudiSyncAllTablesByFull(Integer dbId, Integer appDatasourceId, Long appId, String appName) {
        log.info("hudi 入仓配置 - 全量同步应用时先删除应用下的所有表-------------------------------");
        //先删除当前应用下的所有表和所有表的字段信息
        // 删除应用下的物理表
        List<TableAccessPO> accessList = tableAccessImpl.query().eq("app_id", appId).eq("del_flag", 1).list();
        if (!CollectionUtils.isEmpty(accessList)) {
            // 先遍历accessList,取出每个对象中的id,再去tb_table_fields表中查询相应数据,将查询到的对象删除
            accessList.stream().map(tableAccessPO -> tableFieldsImpl.query().eq("table_access_id", tableAccessPO.id).eq("del_flag", 1).list()).flatMap(Collection::stream).forEachOrdered(tableFieldsPO -> tableFieldsMapper.deleteByIdWithFill(tableFieldsPO));
            // 删除应用下面的所有表及表结构
            accessList.forEach(tableAccessPO -> {
                tableAccessMapper.deleteByIdWithFill(tableAccessPO);
            });

        }

        log.info("hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库");
        ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(dbId);
        DataSourceDTO dto = datasource.getData();
        if (dto == null) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        Connection conn = null;
        MongoClient mongoClient = null;

        //获取到所有表名
        List<TablePyhNameDTO> tableNames = new ArrayList<>();

        log.info("引用的数据源类型" + dto.conType);

        if (dto.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.MONGODB.getName())) {
            MongoDbUtils mongoDbUtils = new MongoDbUtils();
            ServerAddress serverAddress = new ServerAddress(dto.conIp, dto.conPort);
            List<ServerAddress> serverAddresses = new ArrayList<>();
            serverAddresses.add(serverAddress);

            //账号 验证数据库名 密码
            MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential(dto.conAccount, dto.sysNr, dto.conPassword.toCharArray());
            List<MongoCredential> mongoCredentials = new ArrayList<>();
            mongoCredentials.add(scramSha1Credential);

            mongoClient = new MongoClient(serverAddresses, mongoCredentials);

            tableNames = mongoDbUtils.getTrueTableNameList(mongoClient);
            mongoClient.close();
            log.info("查询到的库表字段详情：" + tableNames);
            for (TablePyhNameDTO table : tableNames) {

                TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                tableAccessDTO.setAppDataSourceId(appDatasourceId);
                tableAccessDTO.setAppId(appId);
                tableAccessDTO.setAppName(appName);
                tableAccessDTO.setDisplayName(table.getTableName());
                tableAccessDTO.setIsRealtime(1);
                tableAccessDTO.setPublish(0);
                tableAccessDTO.setSyncSrc("");
                tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                tableAccessDTO.setIfOpenCdc(1);
                String tableName = table.getTableName();
                if (tableName.contains(".")) {
                    tableName = tableName.replaceFirst("\\.", "_");
                }
                tableAccessDTO.setTableName(tableName);

                //将表插入都tb_table_access表 获取到表的主键id
                Integer accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);

                //获取当前表的字段
                List<TableStructureDTO> fields = table.getFields();

                List<TableFieldsDTO> list = new ArrayList<>();

                for (TableStructureDTO field : fields) {
                    TableFieldsDTO fieldDTO = new TableFieldsDTO();
                    fieldDTO.setTableAccessId(Long.valueOf(accessId));
                    fieldDTO.setSourceFieldName(field.fieldName);
                    fieldDTO.setSourceFieldType(field.fieldType);
                    fieldDTO.setFieldName(field.fieldName);
                    //字段类型暂时写死为string
                    fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                    //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                    fieldDTO.setFieldDes(field.getFieldDes());
                    fieldDTO.setIsPrimarykey(field.getIsPk());
                    //1：是实时物理表的字段，
                    //0：非实时物理表的字段
                    fieldDTO.setIsRealtime(1);
                    fieldDTO.setIsBusinesstime(0);
                    fieldDTO.setIsTimestamp(0);
                    fieldDTO.setSourceDbName(field.sourceDbName);
                    fieldDTO.setSourceTblName(field.sourceTblName);
                    list.add(fieldDTO);
                }
                List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                if (!CollectionUtils.isEmpty(tableFieldsPOS)) {
                    tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
                }
            }

        } else {
            try {
                switch (dto.conType) {
                    case MYSQL:
                        MysqlConUtils mysqlConUtils = new MysqlConUtils();
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL.getDriverName());
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = mysqlConUtils.getTrueTableNameAndColumns(conn, dto.conDbname);
                        break;
                    case SQLSERVER:
                        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER.getDriverName());
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = sqlServerPlusUtils.getTrueTableNameAndColumnsPlus(conn, dto.conDbname);
                        break;
                    case ORACLE:
                        OracleUtils oracleUtils = new OracleUtils();
                        log.info("ORACLE驱动开始加载");
                        log.info("ORACLE驱动基本信息：" + com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                        log.info("ORACLE驱动加载完毕");
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = oracleUtils.getTrueTableNameList(conn, dto.conDbname);
                        break;
                    default:
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                }

                log.info("查询到的库表字段详情：" + tableNames);

                for (TablePyhNameDTO table : tableNames) {

                    TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                    tableAccessDTO.setAppDataSourceId(appDatasourceId);
                    tableAccessDTO.setAppId(appId);
                    tableAccessDTO.setAppName(appName);
                    tableAccessDTO.setDisplayName(table.getTableName());
                    tableAccessDTO.setIsRealtime(1);
                    tableAccessDTO.setPublish(0);
                    tableAccessDTO.setSyncSrc("");
                    tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                    tableAccessDTO.setIfOpenCdc(1);
                    String tableName = table.getTableName();
                    if (tableName.contains(".")) {
                        tableName = tableName.replaceFirst("\\.", "_");
                    }
                    tableAccessDTO.setTableName(tableName);

                    //将表插入都tb_table_access表 获取到表的主键id
                    Integer accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);

                    //获取当前表的字段
                    List<TableStructureDTO> fields = table.getFields();

                    List<TableFieldsDTO> list = new ArrayList<>();
                    for (TableStructureDTO field : fields) {
                        TableFieldsDTO fieldDTO = new TableFieldsDTO();
                        fieldDTO.setTableAccessId(Long.valueOf(accessId));
                        fieldDTO.setSourceFieldName(field.fieldName);
                        fieldDTO.setSourceFieldType(field.fieldType);
                        fieldDTO.setFieldName(field.fieldName);
                        //字段类型暂时写死为string
                        fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                        //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                        fieldDTO.setFieldDes(field.getFieldDes());
                        fieldDTO.setIsPrimarykey(field.getIsPk());
                        //1：是实时物理表的字段，
                        //0：非实时物理表的字段
                        fieldDTO.setIsRealtime(1);
                        fieldDTO.setIsBusinesstime(0);
                        fieldDTO.setIsTimestamp(0);
                        fieldDTO.setSourceDbName(field.sourceDbName);
                        fieldDTO.setSourceTblName(table.getTableName());
                        list.add(fieldDTO);
                    }
                    List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                    tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
                }

            } catch (Exception e) {
                log.error("hudi-入仓配置add异常：" + e);
            } finally {
                AbstractCommonDbHelper.closeConnection(conn);
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }
        }
    }


    /**
     * hudi入仓配置 全量同步所有来源数据库对应库下的表信息到fidata平台配置库
     *
     * @param dbId
     */
    public void hudiSyncAllTablesByTarget(Integer dbId, Integer appDatasourceId, Long appId, String appName) {
        log.info("hudi 入仓配置 - 全量同步应用时先删除应用下的所有表-------------------------------");
        //先删除当前应用下的所有表和所有表的字段信息
        // 删除应用下的物理表
        List<TableAccessPO> accessList = tableAccessImpl.query().eq("app_id", appId).eq("del_flag", 1).list();
        if (!CollectionUtils.isEmpty(accessList)) {
            // 先遍历accessList,取出每个对象中的id,再去tb_table_fields表中查询相应数据,将查询到的对象删除
            accessList.stream().map(tableAccessPO -> tableFieldsImpl.query().eq("table_access_id", tableAccessPO.id).eq("del_flag", 1).list()).flatMap(Collection::stream).forEachOrdered(tableFieldsPO -> tableFieldsMapper.deleteByIdWithFill(tableFieldsPO));
            // 删除应用下面的所有表及表结构
            accessList.forEach(tableAccessPO -> {
                tableAccessMapper.deleteByIdWithFill(tableAccessPO);
            });

        }

        log.info("hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库");
        ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(dbId);
        DataSourceDTO dto = datasource.getData();
        if (dto == null) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        Connection conn = null;
        MongoClient mongoClient = null;

        //获取到所有表名
        List<TablePyhNameDTO> tableNames = new ArrayList<>();

        log.info("引用的数据源类型" + dto.conType);

        if (dto.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.MONGODB.getName())) {
            MongoDbUtils mongoDbUtils = new MongoDbUtils();
            ServerAddress serverAddress = new ServerAddress(dto.conIp, dto.conPort);
            List<ServerAddress> serverAddresses = new ArrayList<>();
            serverAddresses.add(serverAddress);

            //账号 验证数据库名 密码
            MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential(dto.conAccount, dto.sysNr, dto.conPassword.toCharArray());
            List<MongoCredential> mongoCredentials = new ArrayList<>();
            mongoCredentials.add(scramSha1Credential);

            mongoClient = new MongoClient(serverAddresses, mongoCredentials);

            tableNames = mongoDbUtils.getTrueTableNameListByTarget(mongoClient,dto.getConDbname());
            mongoClient.close();
            log.info("查询到的库表字段详情：" + tableNames);
            for (TablePyhNameDTO table : tableNames) {

                TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                tableAccessDTO.setAppDataSourceId(appDatasourceId);
                tableAccessDTO.setAppId(appId);
                tableAccessDTO.setAppName(appName);
                tableAccessDTO.setDisplayName(table.getTableName());
                tableAccessDTO.setIsRealtime(1);
                tableAccessDTO.setPublish(0);
                tableAccessDTO.setSyncSrc("");
                tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                tableAccessDTO.setIfOpenCdc(1);
                String tableName = table.getTableName();
                if (tableName.contains(".")) {
                    tableName = tableName.replaceFirst("\\.", "_");
                }
                tableAccessDTO.setTableName(tableName);

                //将表插入都tb_table_access表 获取到表的主键id
                Integer accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);

                //获取当前表的字段
                List<TableStructureDTO> fields = table.getFields();

                List<TableFieldsDTO> list = new ArrayList<>();

                for (TableStructureDTO field : fields) {
                    TableFieldsDTO fieldDTO = new TableFieldsDTO();
                    fieldDTO.setTableAccessId(Long.valueOf(accessId));
                    fieldDTO.setSourceFieldName(field.fieldName);
                    fieldDTO.setSourceFieldType(field.fieldType);
                    fieldDTO.setFieldName(field.fieldName);
                    //字段类型暂时写死为string
                    fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                    //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                    fieldDTO.setFieldDes(field.getFieldDes());
                    fieldDTO.setIsPrimarykey(field.getIsPk());
                    //1：是实时物理表的字段，
                    //0：非实时物理表的字段
                    fieldDTO.setIsRealtime(1);
                    fieldDTO.setIsBusinesstime(0);
                    fieldDTO.setIsTimestamp(0);
                    fieldDTO.setSourceDbName(field.sourceDbName);
                    fieldDTO.setSourceTblName(field.sourceTblName);
                    list.add(fieldDTO);
                }
                List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                if (!CollectionUtils.isEmpty(tableFieldsPOS)) {
                    tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
                }
            }

        } else {
            try {
                switch (dto.conType) {
                    case MYSQL:
                        MysqlConUtils mysqlConUtils = new MysqlConUtils();
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL.getDriverName());
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = mysqlConUtils.getTrueTableNameAndColumns(conn, dto.conDbname);
                        break;
                    case SQLSERVER:
                        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER.getDriverName());
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = sqlServerPlusUtils.getTrueTableNameAndColumnsPlus(conn, dto.conDbname);
                        break;
                    case ORACLE:
                        OracleUtils oracleUtils = new OracleUtils();
                        log.info("ORACLE驱动开始加载");
                        log.info("ORACLE驱动基本信息：" + com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                        log.info("ORACLE驱动加载完毕");
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = oracleUtils.getTrueTableNameList(conn, dto.conDbname);
                        break;
                    default:
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                }

                log.info("查询到的库表字段详情：" + tableNames);

                for (TablePyhNameDTO table : tableNames) {

                    TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                    tableAccessDTO.setAppDataSourceId(appDatasourceId);
                    tableAccessDTO.setAppId(appId);
                    tableAccessDTO.setAppName(appName);
                    tableAccessDTO.setDisplayName(table.getTableName());
                    tableAccessDTO.setIsRealtime(1);
                    tableAccessDTO.setPublish(0);
                    tableAccessDTO.setSyncSrc("");
                    tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                    tableAccessDTO.setIfOpenCdc(1);
                    String tableName = table.getTableName();
                    if (tableName.contains(".")) {
                        tableName = tableName.replaceFirst("\\.", "_");
                    }
                    tableAccessDTO.setTableName(tableName);

                    //将表插入都tb_table_access表 获取到表的主键id
                    Integer accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);

                    //获取当前表的字段
                    List<TableStructureDTO> fields = table.getFields();

                    List<TableFieldsDTO> list = new ArrayList<>();
                    for (TableStructureDTO field : fields) {
                        TableFieldsDTO fieldDTO = new TableFieldsDTO();
                        fieldDTO.setTableAccessId(Long.valueOf(accessId));
                        fieldDTO.setSourceFieldName(field.fieldName);
                        fieldDTO.setSourceFieldType(field.fieldType);
                        fieldDTO.setFieldName(field.fieldName);
                        //字段类型暂时写死为string
                        fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                        //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                        fieldDTO.setFieldDes(field.getFieldDes());
                        fieldDTO.setIsPrimarykey(field.getIsPk());
                        //1：是实时物理表的字段，
                        //0：非实时物理表的字段
                        fieldDTO.setIsRealtime(1);
                        fieldDTO.setIsBusinesstime(0);
                        fieldDTO.setIsTimestamp(0);
                        fieldDTO.setSourceDbName(field.sourceDbName);
                        fieldDTO.setSourceTblName(table.getTableName());
                        list.add(fieldDTO);
                    }
                    List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                    tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
                }

            } catch (Exception e) {
                log.error("hudi-入仓配置add异常：" + e);
            } finally {
                AbstractCommonDbHelper.closeConnection(conn);
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }
        }
    }

    /**
     * hudi入仓配置 增量同步所有来源数据库对应库下的表信息到fidata平台配置库
     *
     * @param dbId
     */
    public void hudiSyncAllTablesByMerge(Integer dbId, Integer appDatasourceId, Long appId, String appName) {
        log.info("hudi入仓配置 增量同步时，先获取已同步到当前应用下的所有表名");
        LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TableAccessPO::getTableName)
                .eq(TableAccessPO::getAppId, appId);
        //获取到已经同步过的表名
        List<TableAccessPO> posAlreadyHave = tableAccessImpl.list(wrapper);

        log.info("hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库");
        ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(dbId);
        DataSourceDTO dto = datasource.getData();
        if (dto == null) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        Connection conn = null;
        MongoClient mongoClient = null;

        //获取到所有表名
        List<TablePyhNameDTO> tableNames = new ArrayList<>();

        log.info("引用的数据源类型" + dto.conType);
        if (dto.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.MONGODB.getName())) {
            MongoDbUtils mongoDbUtils = new MongoDbUtils();
            ServerAddress serverAddress = new ServerAddress(dto.conIp, dto.conPort);
            List<ServerAddress> serverAddresses = new ArrayList<>();
            serverAddresses.add(serverAddress);

            //账号 验证数据库名 密码
            MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential(dto.conAccount, dto.sysNr, dto.conPassword.toCharArray());
            List<MongoCredential> mongoCredentials = new ArrayList<>();
            mongoCredentials.add(scramSha1Credential);

            mongoClient = new MongoClient(serverAddresses, mongoCredentials);

            tableNames = mongoDbUtils.getTrueTableNameList(mongoClient);
            log.info("获取到的mongodb库表信息：" + JSON.toJSONString(tableNames));
            mongoClient.close();
            boolean ifSync = true;
            for (TablePyhNameDTO table : tableNames) {
                TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                tableAccessDTO.setAppDataSourceId(appDatasourceId);
                tableAccessDTO.setAppId(appId);
                tableAccessDTO.setAppName(appName);
                tableAccessDTO.setDisplayName(table.getTableName());
                tableAccessDTO.setIsRealtime(1);
                tableAccessDTO.setPublish(0);
                tableAccessDTO.setSyncSrc("");
                tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                tableAccessDTO.setIfOpenCdc(1);
                String tableName = table.getTableName();
                if (tableName.contains(".")) {
                    tableName = tableName.replaceFirst("\\.", "_");
                }
                tableAccessDTO.setTableName(tableName);

                for (TableAccessPO tableAccessPO : posAlreadyHave) {
                    if (tableAccessPO.getTableName().equals(tableAccessDTO.getTableName())) {
                        ifSync = false;
                        break;
                    }
                }

                if (ifSync) {
                    //将表插入都tb_table_access表 获取到表的主键id
                    int accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);
                    //获取当前表的字段
                    List<TableStructureDTO> fields = table.getFields();

                    List<TableFieldsDTO> list = new ArrayList<>();
                    for (TableStructureDTO field : fields) {
                        TableFieldsDTO fieldDTO = new TableFieldsDTO();
                        fieldDTO.setTableAccessId((long) accessId);
                        fieldDTO.setSourceFieldName(field.fieldName);
                        fieldDTO.setSourceFieldType(field.fieldType);
                        fieldDTO.setFieldName(field.fieldName);
                        //字段类型暂时写死为string
                        fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                        //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                        fieldDTO.setFieldDes(field.getFieldDes());
                        fieldDTO.setIsPrimarykey(field.getIsPk());
                        //1：是实时物理表的字段，
                        //0：非实时物理表的字段
                        fieldDTO.setIsRealtime(1);
                        fieldDTO.setIsBusinesstime(0);
                        fieldDTO.setIsTimestamp(0);
                        fieldDTO.setSourceDbName(field.sourceDbName);
                        fieldDTO.setSourceTblName(table.getTableName());
                        list.add(fieldDTO);
                    }
                    List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                    tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
                }
            }

        } else {
            try {
                switch (dto.conType) {
                    case MYSQL:
                        MysqlConUtils mysqlConUtils = new MysqlConUtils();
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL.getDriverName());
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = mysqlConUtils.getTrueTableNameAndColumns(conn, dto.conDbname);
                        break;
                    case SQLSERVER:
                        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER.getDriverName());
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = sqlServerPlusUtils.getTrueTableNameAndColumnsPlus(conn, dto.conDbname);
                        break;
                    case ORACLE:
                        OracleUtils oracleUtils = new OracleUtils();
                        log.info("ORACLE驱动开始加载");
                        log.info("ORACLE驱动基本信息：" + com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                        Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                        log.info("ORACLE驱动加载完毕");
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                        tableNames = oracleUtils.getTrueTableNameList(conn, dto.conDbname);
                        break;
                    default:
                        conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                }

                log.info("查询到的库表字段详情：" + tableNames);

                boolean ifSync = true;
                for (TablePyhNameDTO table : tableNames) {

                    TbTableAccessDTO tableAccessDTO = new TbTableAccessDTO();
                    tableAccessDTO.setAppDataSourceId(appDatasourceId);
                    tableAccessDTO.setAppId(appId);
                    tableAccessDTO.setAppName(appName);
                    tableAccessDTO.setDisplayName(table.getTableName());
                    tableAccessDTO.setIsRealtime(1);
                    tableAccessDTO.setPublish(0);
                    tableAccessDTO.setSyncSrc("");
                    tableAccessDTO.setTableDes("fidata - hudi入仓配置表");
                    tableAccessDTO.setIfOpenCdc(1);
                    String tableName = table.getTableName();
                    if (tableName.contains(".")) {
                        tableName = tableName.replaceFirst("\\.", "_");
                    }
                    tableAccessDTO.setTableName(tableName);

                    for (TableAccessPO tableAccessPO : posAlreadyHave) {
                        if (tableAccessPO.getTableName().equals(tableAccessDTO.getTableName())) {
                            ifSync = false;
                            break;
                        }
                    }

                    if (ifSync) {
                        //将表插入都tb_table_access表 获取到表的主键id
                        int accessId = tableAccessImpl.addTableAccessTblForHudiConfig(tableAccessDTO);
                        //获取当前表的字段
                        List<TableStructureDTO> fields = table.getFields();

                        List<TableFieldsDTO> list = new ArrayList<>();
                        for (TableStructureDTO field : fields) {
                            TableFieldsDTO fieldDTO = new TableFieldsDTO();
                            fieldDTO.setTableAccessId((long) accessId);
                            fieldDTO.setSourceFieldName(field.fieldName);
                            fieldDTO.setSourceFieldType(field.fieldType);
                            fieldDTO.setFieldName(field.fieldName);
                            //字段类型暂时写死为string
                            fieldDTO.setFieldType("STRING");
//                    fieldDTO.setFieldType(field.fieldType);
                            //字段长度暂时不要
//                    fieldDTO.setFieldLength((long) field.fieldLength);
                            fieldDTO.setFieldDes(field.getFieldDes());
                            fieldDTO.setIsPrimarykey(field.getIsPk());
                            //1：是实时物理表的字段，
                            //0：非实时物理表的字段
                            fieldDTO.setIsRealtime(1);
                            fieldDTO.setIsBusinesstime(0);
                            fieldDTO.setIsTimestamp(0);
                            fieldDTO.setSourceDbName(field.sourceDbName);
                            fieldDTO.setSourceTblName(table.getTableName());
                            list.add(fieldDTO);
                        }
                        List<TableFieldsPO> tableFieldsPOS = TableFieldsMap.INSTANCES.listDtoToPo(list);
                        tableFieldsImpl.saveOrUpdateBatch(tableFieldsPOS);
                    }
                }

            } catch (Exception e) {
                log.error("hudi-入仓配置add异常：" + e);
            } finally {
                AbstractCommonDbHelper.closeConnection(conn);
            }
        }


    }

    /**
     * 校验单一应用下选择的数据源是否合理
     *
     * @param data
     * @return
     */
    public Boolean checkSourcesTypeIfOk(List<AppDataSourceDTO> data) {
        boolean b;
        //新建集合预装载数据库类型的数据源：
        List<AppDataSourceDTO> dbTypeSources = new ArrayList<>();
        //新建集合预装载非数据库类型的数据源:driveType = mysql/sqlserver/oracle/postgresql
        List<AppDataSourceDTO> otherSources = new ArrayList<>();
        if (!CollectionUtils.isEmpty(data)) {
            //遍历data时，根据各个外部数据源的驱动类型，存入到我们预先准备的集合中
            data.forEach(e -> {
                String driveType = e.driveType;
                if (AppDriveTypeEnum.MYSQL.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.SQLSERVER.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.ORACLE.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.OPENEDGE.getName().equalsIgnoreCase(driveType)) {
                    dbTypeSources.add(e);
                } else {
                    otherSources.add(e);
                }
            });
        }
        //如果装载数据库类型数据源的集合 和 装载非数据库类型数据源的集合 都不为空，就认为当前应用选择了不合理的数据源，抛出异常
        if (!CollectionUtils.isEmpty(dbTypeSources) && !CollectionUtils.isEmpty(otherSources)) {
            log.error("当前应用选择的数据源不合理！！！");
            b = false;
        } else {
            //反之则认为应用选择的数据源是合理的
            b = true;
        }
        return b;
    }

    /**
     * 新增业务分类
     *
     * @param appRegistrationDTO
     */
    public void addClassification(AppRegistrationDTO appRegistrationDTO) {
        // 添加业务分类元数据信息
        ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
        classificationInfoDto.setName(appRegistrationDTO.appName);
        classificationInfoDto.setDescription(appRegistrationDTO.appDes);
        classificationInfoDto.setSourceType(ClassificationTypeEnum.DATA_ACCESS);
        classificationInfoDto.setAppType(appRegistrationDTO.appType);
        classificationInfoDto.setDelete(false);

        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dataManageClient.appSynchronousClassification(classificationInfoDto);
                } catch (Exception e) {
                    // 不同场景下，元数据可能不会部署，在这里只做日志记录，不影响正常流程
                    log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
                }
            }
        });
    }

    /**
     * 新增元数据信息
     *
     * @param appRegistration
     * @param dataSource
     */
    public List<MetaDataInstanceAttributeDTO> addDataSourceMetaData(AppRegistrationPO appRegistration, AppDataSourcePO dataSource) {
        if (dataSource.driveType.toUpperCase().equals("API") || dataSource.driveType.toUpperCase().equals("RESTFULAPI") || dataSource.driveType.toUpperCase().equals("SFTP") || dataSource.driveType.toUpperCase().equals("FTP")) {
            return null;
        }
        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        MetaDataInstanceAttributeDTO data = new MetaDataInstanceAttributeDTO();
        data.name = dataSource.host;
        data.hostname = dataSource.host;
        data.port = dataSource.port;
        data.qualifiedName = dataSource.host;
        data.rdbms_type = dataSource.driveType;
        data.displayName = dataSource.host;
        data.description = "stg";
        data.comment = String.valueOf(dataSource.id);
        //库
        List<MetaDataDbAttributeDTO> dbList = new ArrayList<>();
        MetaDataDbAttributeDTO db = new MetaDataDbAttributeDTO();
        db.name = dataSource.dbName;
        db.displayName = dataSource.dbName;
        db.qualifiedName = data.qualifiedName + "_" + dataSource.dbName;
        dbList.add(db);
        data.dbList = dbList;

        list.add(data);

        return list;

    }

    /**
     * 将文件转为二进制字符串
     *
     * @param filePath
     * @return
     */
    public String fileToBinaryStr(String filePath) {
        return FileBinaryUtils.fileToBinStr(filePath);
    }

    /**
     * 数据源同步到系统平台配置中
     *
     * @param po
     * @param name
     * @return
     */
    public DataSourceResultDTO synchronizationSystemDataSource(AppDataSourcePO po, String name) {

        DataSourceSaveDTO data = new DataSourceSaveDTO();
        data.conAccount = po.connectAccount;
        data.conDbname = po.dbName;
        data.conIp = po.host;
        data.conAccount = po.connectAccount;
        data.conPassword = po.connectPwd;
        data.conPort = StringUtils.isNotEmpty(po.port) ? Integer.parseInt(po.port) : 0;
        data.name = name + "_" + po.dbName;
        data.serviceName = po.serviceName;
        data.serviceType = po.serviceType;
        data.conStr = po.connectStr;
        data.conType = com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.getEnum(po.driveType.toUpperCase());
        //外部数据源
        data.sourceType = 2;
        data.id = po.systemDataSourceId;

        DataSourceResultDTO dto = new DataSourceResultDTO();

        if (data.id != null && data.id != 0) {
            ResultEntity<Object> objectResultEntity = publishTaskClient.editDataSetParams(data);
            if (objectResultEntity.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
            }
            dto.id = (int) data.id;
            return dto;
        }

        ResultEntity<Object> objectResultEntity = publishTaskClient.addDataSetParams(data);
        if (objectResultEntity.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        dto.id = (int) objectResultEntity.data;
        return dto;

    }

    @Override
    public PageDTO<AppRegistrationDTO> listAppRegistration(String key, Integer page, Integer rows) {

        Page<AppRegistrationPO> pageReg = new Page<>(page, rows);

        boolean isKeyExists = StringUtils.isNoneBlank(key);
        query().like(isKeyExists, "app_name", key)
                // 未删除
                .eq("del_flag", 1).page(pageReg);

        // 分页封装
        Page<AppRegistrationPO> poPage = new Page<>(page, rows);

        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();

        // 查询数据
        queryWrapper.like(isKeyExists, "app_name", key).eq("del_flag", 1)
                // 未删除
                .orderByDesc("create_time");
        baseMapper.selectPage(poPage, queryWrapper);

        List<AppRegistrationPO> records2 = poPage.getRecords();
        PageDTO<AppRegistrationDTO> pageDTO = new PageDTO<>();

        // 总条数
        pageDTO.setTotal(pageReg.getTotal());
        // 总页数
        pageDTO.setTotalPage(pageReg.getPages());
        pageDTO.setItems(AppRegistrationMap.INSTANCES.listPoToDto(records2));

        return pageDTO;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ResultEnum updateAppRegistration(AppRegistrationEditDTO dto) {
        //先获取到要添加的外部数据源们 driveType = mysql/sqlserver/oracle/postgresql
        List<AppDataSourceDTO> data = dto.getAppDatasourceDTO();
        //调用封装的方法，校验当前应用选择的数据源类型是否存在冲突！
        Boolean aBoolean = checkSourcesTypeIfOk(data);
        if (!aBoolean) {
            log.error("当前应用选择的数据源类型存在冲突！");
            throw new FkException(ResultEnum.DATASOURCE_TYPE_ERROR);
        }

        // 判断名称是否重复
        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppRegistrationPO::getAppName, dto.appName);
        AppRegistrationPO registrationPo = mapper.selectOne(queryWrapper);
        if (registrationPo != null && registrationPo.id != dto.id) {
            return ResultEnum.DATAACCESS_APPNAME_ERROR;
        }

        // 1.0前端应用注册传来的id
        long id = dto.getId();

        // 1.1非空判断
        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 1.2dto->po
        AppRegistrationPO po = dto.toEntity(AppRegistrationPO.class);

        // 1.3修改tb_app_registration数据
        boolean edit = this.updateById(po);
        if (!edit) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 2.0修改关联表数据(tb_app_datasource)

        // 2.1dto->po
        List<AppDataSourceDTO> appDatasourceDTO = dto.getAppDatasourceDTO();
        List<AppDataSourcePO> modelDataSource = AppDataSourceMap.INSTANCES.listDtoToPo(appDatasourceDTO);

        //如果是RestfulAPI，进行如下操作  为了数接的/apiConfig/getToken接口可以正常使用
        //获取实时api的临时token
        modelDataSource.forEach(m -> {
            if (DataSourceTypeEnum.RestfulAPI.getName().equalsIgnoreCase(m.driveType)) {
                m.realtimeAccount = m.connectAccount;
                m.realtimePwd = m.connectPwd;
            }
        });

        // 实时应用
        if (po.appType == 0) {
            for (AppDataSourceDTO item : appDatasourceDTO) {
                if (!DbTypeEnum.doris_catalog.getName().equalsIgnoreCase(item.driveType)) {
                    QueryWrapper<AppDataSourcePO> wrapper = new QueryWrapper<>();
                    wrapper.lambda().eq(AppDataSourcePO::getRealtimeAccount, item.realtimeAccount).eq(AppDataSourcePO::getAppId, item.appId);
                    AppDataSourcePO appDataSourcePo = appDataSourceMapper.selectOne(wrapper);
                    if (appDataSourcePo != null && appDataSourcePo.id != item.id) {
                        throw new FkException(ResultEnum.REALTIME_ACCOUNT_ISEXIST);
                    }
                }
            }
        }

        // 2.2修改数据
        //long appDataSid = appDataSourceImpl.query().eq("app_id", id).one().getId();

        modelDataSource.stream().filter(Objects::nonNull).forEach(e -> {
            e.setAppId(po.getId());
            //e.id = appDataSid;

            //sftp秘钥方式,存储二进制数据
            if (DataSourceTypeEnum.SFTP.getName().equals(e.driveType.toLowerCase()) && e.serviceType == 1) {
                e.fileBinary = fileToBinaryStr(e.connectStr);
            }

            if (e.id != 0) {
                AppDataSourcePO byId = appDataSourceImpl.getById(e.id);
                e.systemDataSourceId = byId.systemDataSourceId;
            }

//            //同步到平台配置  暂时注释，目前数据接入数据源已整合到平台配置，这段代码暂不需要（无需二次保存）
//            if (!DataSourceTypeEnum.SFTP.getName().equals(e.driveType.toLowerCase())
//                    && !DataSourceTypeEnum.FTP.getName().equals(e.driveType.toLowerCase())
//                    && !DataSourceTypeEnum.API.getName().equals(e.driveType.toLowerCase())
//                    && !DataSourceTypeEnum.RestfulAPI.getName().equals(e.driveType.toLowerCase())) {
//                e.systemDataSourceId = synchronizationSystemDataSource(e, po.appName).id;
//            }
        });

        //jtw类型配置返回结果json串
        dto.appDatasourceDTO.stream().map(e -> {
            if (e.authenticationMethod != null && e.authenticationMethod == 3) {
                AppDataSourceDTO dataSourceByAppId = appDataSourceImpl.getDataSourceByAppId(po.getId());
                apiResultConfig.apiResultConfig(dataSourceByAppId.id, e.apiResultConfigDtoList);
            }
            return e;
        });

        //查询修改前的数据源
        List<AppDataSourcePO> list = appDataSourceImpl.query().select("id").eq("app_id", dto.id).list();
        //查询应用下的表信息，如果有表正在使用当前数据源，禁止删除
        List<TableAccessDTO> tables = tableAccessImpl.getTblByAppId((int) dto.id);
        ArrayList<Long> appDataSourceIds = new ArrayList<>();
        for (TableAccessDTO table : tables) {
            //获取应用id
            appDataSourceIds.add(Long.valueOf(table.appDataSourceId));
        }
        //去重  当前引用下的表正在使用的应用数据源id
        List<Long> collect2 = appDataSourceIds.stream().distinct().collect(Collectors.toList());
        // 修改前的数据源id
        List<Long> collect = list.stream().map(e -> e.id).collect(Collectors.toList());

        //获取前端传参的数据源id集合
        List<Long> collect3 = modelDataSource.stream().map(e -> e.id).collect(Collectors.toList());
        //找出重复出现的appDataSourceIds
        List<Long> duplicates = collect.stream().filter(collect2::contains).distinct().collect(Collectors.toList());

        //判断前端传的参数是否包含原有数据源
        if (!collect3.containsAll(duplicates)) {
            //如果不为空 就说明当前要移除的数据源有表在使用 则本次修改不能生效
            if (!CollectionUtils.isEmpty(duplicates)) {
                Map<Long, String> result = new HashMap<>();
                tables.forEach(tableAccessDTO -> {
                    if (duplicates.contains(tableAccessDTO.appDataSourceId)) {
                        result.put(tableAccessDTO.id, tableAccessDTO.tableName);
                    }
                });
                log.error("当前要删除的数据源正在使用，此次修改失败...事务已回滚...正在使用的表详情请查看报错日志...");
                log.info("表详情如下：[{}]", JSON.toJSONString(result));
                throw new FkException(ResultEnum.DATAACCESS_APP_EDIT_FAILURE);
            }
        }

        //筛选
        List<AppDataSourcePO> collect1 = list.stream().filter(e -> !collect3.contains(e.id)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect1)) {
            collect1.forEach(e -> {
                //删除数据源
                appDataSourceImpl.removeById(e.id);
            });
        }

        //hudi入参配置  同步所有表
        //如果是hudi 入仓配置 开启了同步所有表
        if (po.ifSyncAllTables != null) {
            if (po.ifSyncAllTables == 1) {
                log.info("hudi 入仓配置 - 二次编辑应用时先删除应用下的所有表-------------------------------");
                //先删除当前应用下的所有表和所有表的字段信息
                // 删除应用下的物理表
                List<TableAccessPO> accessList = tableAccessImpl.query().eq("app_id", model.id).eq("del_flag", 1).list();
                if (!CollectionUtils.isEmpty(accessList)) {
                    // 先遍历accessList,取出每个对象中的id,再去tb_table_fields表中查询相应数据,将查询到的对象删除
                    accessList.stream().map(tableAccessPO -> tableFieldsImpl.query().eq("table_access_id", tableAccessPO.id).eq("del_flag", 1).list()).flatMap(Collection::stream).forEachOrdered(tableFieldsPO -> tableFieldsMapper.deleteByIdWithFill(tableFieldsPO));
                    // 删除应用下面的所有表及表结构
                    accessList.forEach(tableAccessPO -> {
                        tableAccessMapper.deleteByIdWithFill(tableAccessPO);
                    });

                }

                log.info("hudi 入仓配置 - 二次编辑应用时开始同步所有表-------------------------------");
                long appId = po.getId();
                List<AppDataSourceDTO> appSourcesByAppId = appDataSourceImpl.getAppSourcesByAppId(appId);
                //获取来源数据源id
                Integer systemDataSourceId = appSourcesByAppId.get(0).getSystemDataSourceId();
                //获取appdatasourceid
                int appDatasourceId = Math.toIntExact(appSourcesByAppId.get(0).getId());
                //hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
                hudiSyncAllTablesToFidataConfig(systemDataSourceId, appDatasourceId, appId, po.getAppName());
            }
        }
        return appDataSourceImpl.saveOrUpdateBatch(modelDataSource) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editAppBasicInfo(AppRegistrationEditDTO dto) {

        // 判断名称是否重复
        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppRegistrationPO::getAppName, dto.appName);
        AppRegistrationPO registrationPo = mapper.selectOne(queryWrapper);
        if (registrationPo != null && registrationPo.id != dto.id) {
            return ResultEnum.DATAACCESS_APPNAME_ERROR;
        }

        // 1.1非空判断
        AppRegistrationPO model = this.getById(dto.getId());
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 1.2dto->po
        AppRegistrationPO po = dto.toEntity(AppRegistrationPO.class);

        // 1.3修改tb_app_registration数据
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<NifiVO> deleteAppRegistration(long id) {
        List<DeleteTableDetailDTO> deleteTableDetailDtoList = new ArrayList<>();

        UserInfo userInfo = userHelper.getLoginUserInfo();

        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // 删除应用之前，先查出应用简称
        AppRegistrationDTO appById = getAppById(Long.parseLong(String.valueOf(id)));

        // 1.删除tb_app_registration表数据
        int deleteReg = mapper.deleteByIdWithFill(model);
        if (deleteReg < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 2.删除tb_app_datasource表数据
        List<AppDataSourcePO> dsList = appDataSourceImpl.query().eq("app_id", id).list();

        for (AppDataSourcePO modelDataSource : dsList) {
            int delDataSource = appDataSourceMapper.deleteByIdWithFill(modelDataSource);
            if (delDataSource < 0) {
                return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
            }

            // 3.删除tb_api_result_config表数据
            if (modelDataSource.authenticationMethod != null && modelDataSource.authenticationMethod == 3) {
                ResultEnum resultEnum = apiResultConfig.delApiResultConfig(modelDataSource.id);
                if (resultEnum != ResultEnum.SUCCESS) {
                    return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }

        // 删除应用下的api(api是新增的功能,在改动最少代码的情况下,只删除api)
        QueryWrapper<ApiConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigPO::getAppId, model.id);
        List<ApiConfigPO> apiConfigPoList = apiConfigMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(apiConfigPoList)) {
            apiConfigPoList.forEach(e -> {
                apiConfigMapper.deleteByIdWithFill(e);

                // 封装要删除的api参数
                DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
                deleteTableDetailDto.appId = String.valueOf(id);
                deleteTableDetailDto.tableId = String.valueOf(e.id);
                deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DATALAKE_API_TASK;
                deleteTableDetailDtoList.add(deleteTableDetailDto);
            });


        }

        // 删除应用下的物理表
        List<TableAccessPO> accessList = tableAccessImpl.query().eq("app_id", model.id).eq("del_flag", 1).list();
        List<Long> tableIdList = new ArrayList<>();
        NifiVO vo = new NifiVO();
        //hudi入仓配置 是否同步所有表
        vo.ifSyncAllTables = appById.ifSyncAllTables;
        List<TableListVO> tableList = new ArrayList<>();
        List<String> qualifiedNames = new ArrayList<>();

        if (!CollectionUtils.isEmpty(accessList)) {
            // 删表之前,要将所有的数据提前查出来,不然会导致空指针异常
            tableIdList = accessList.stream().map(TableAccessPO::getId).collect(Collectors.toList());

            ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(model.targetDbId);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            String hostname = dataSourceConfig.data.conIp;
            String dbName = dataSourceConfig.data.conDbname;
            for (Long tableId : tableIdList) {
                TableListVO tableVO = new TableListVO();
                TableAccessPO po = tableAccessImpl.query().eq("id", tableId).eq("del_flag", 1).one();
                tableVO.tableName = model.appAbbreviation + "_" + po.tableName;
                tableList.add(tableVO);
                qualifiedNames.add(hostname + "_" + dbName + "_" + po.getId());
            }


            // 删除应用下面的所有表及表结构
            accessList.forEach(po -> {
                tableAccessMapper.deleteByIdWithFill(po);

                // 封装要删除的api参数
                DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
                deleteTableDetailDto.appId = String.valueOf(id);
                deleteTableDetailDto.tableId = String.valueOf(po.id);
                deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DATALAKE_TASK;
                deleteTableDetailDtoList.add(deleteTableDetailDto);
            });
            // 先遍历accessList,取出每个对象中的id,再去tb_table_fields表中查询相应数据,将查询到的对象删除
            accessList.stream().map(po -> tableFieldsImpl.query().eq("table_access_id", po.id).eq("del_flag", 1).list()).flatMap(Collection::stream).forEachOrdered(po -> tableFieldsMapper.deleteByIdWithFill(po));
        }

        // 删除factory-dispatch对应的表配置
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(deleteTableDetailDtoList)) {
            dataFactoryClient.editByDeleteTable(deleteTableDetailDtoList);
        }

        /*
          将方法的返回值封装
         */
        vo.userId = userInfo.id;
        vo.appId = String.valueOf(model.id);
        vo.tableIdList = tableIdList;
        // atlas物理表信息
        vo.tableList = tableList;
        vo.qualifiedNames = qualifiedNames;
        vo.classifications = model.appName;
        vo.appAbbreviation = appById.getAppAbbreviation();
        log.info("删除的应用信息,{}", vo);

        if (openMetadata) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // 删除元数据实体
                        if (!CollectionUtils.isEmpty(vo.qualifiedNames)) {
                            MetaDataDeleteAttributeDTO metaDataDeleteAttributeDto = new MetaDataDeleteAttributeDTO();
                            metaDataDeleteAttributeDto.setQualifiedNames(vo.getQualifiedNames());
                            metaDataDeleteAttributeDto.classifications = vo.classifications;
                            dataManageClient.deleteMetaData(metaDataDeleteAttributeDto);
                        }

                        // 删除业务分类
                        ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
                        classificationInfoDto.setName(vo.classifications);
                        classificationInfoDto.setDescription(model.appDes);
                        classificationInfoDto.setSourceType(ClassificationTypeEnum.DATA_ACCESS);
                        classificationInfoDto.setDelete(true);

                        dataManageClient.appSynchronousClassification(classificationInfoDto);
                    } catch (Exception e) {
                        // 不同场景下，元数据可能不会部署，在这里只做日志记录，不影响正常流程
                        log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
                    }
                }
            }).start();
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);
    }

    @Override
    public List<AppNameDTO> queryAppName() {

        // 查询所有应用名称
        List<AppRegistrationPO> list = this.query().eq("del_flag", 1).orderByDesc("create_time").list();
        List<AppNameDTO> listAppName = new ArrayList<>();
        for (AppRegistrationPO po : list) {

            AppNameDTO appNameDTO = new AppNameDTO();
            String appName = po.getAppName();
            appNameDTO.setId(po.id);
            appNameDTO.setAppName(appName);
            appNameDTO.setAppType((byte) po.getAppType());

            listAppName.add(appNameDTO);
        }
        return listAppName;
    }

    @Override
    public AppRegistrationDTO getData(long id) {

        AppRegistrationPO modelReg = this.query().eq("id", id).eq("del_flag", 1).one();
        AppRegistrationDTO appRegistrationDTO = AppRegistrationMap.INSTANCES.poToDto(modelReg);

        List<AppDataSourcePO> modelDataSource = appDataSourceImpl.query().eq("app_id", id).eq("del_flag", 1).list();
        List<AppDataSourceDTO> appDataSourceDTO = AppDataSourceMap.INSTANCES.listPoToDto(modelDataSource);

        appDataSourceDTO.stream().map(e -> {
            // 数据库密码不展示
            e.connectPwd = "";
            //jwt类型展示返回结果json配置
            if (e.authenticationMethod != null && e.authenticationMethod == 3) {
                e.apiResultConfigDtoList = apiResultConfig.getApiResultConfig(e.id);
            }
            return e;
        });

        appRegistrationDTO.setAppDatasourceDTO(appDataSourceDTO);

        return appRegistrationDTO;
    }

    @Override
    public List<AppRegistrationDTO> getDescDate() {

        // 按时间倒叙,查询top10的数据
        List<AppRegistrationPO> descDate = baseMapper.getDescDate();

        return AppRegistrationMap.INSTANCES.listPoToDto(descDate);
    }

    @Override
    public List<AppNameDTO> queryNoneRealTimeAppName() {

        List<AppRegistrationPO> list = this.query().eq("del_flag", 1).eq("app_type", 1).list();
        List<AppNameDTO> listAppName = new ArrayList<>();
        for (AppRegistrationPO po : list) {

            AppNameDTO appNameDTO = new AppNameDTO();
            String appName = po.getAppName();
            appNameDTO.setAppName(appName);
            appNameDTO.setAppType((byte) 1);

            listAppName.add(appNameDTO);
        }
        return listAppName;
    }

    @Override
    public List<AppDriveTypeDTO> getDriveType() {

        List<AppDriveTypePO> list = appDriveTypeMapper.listData();
        return AppDriveTypeDTO.convertEntityList(list);
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_ENTITY)
    @Override
    public AtlasEntityDTO getAtlasEntity(long id) {

        AtlasEntityDTO dto;
        try {
            dto = new AtlasEntityDTO();

            AppRegistrationPO modelReg = this.query().eq("id", id).eq("del_flag", 1).one();

            AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", id).eq("del_flag", 1).one();

            dto.sendTime = LocalDateTime.now();
            dto.appName = modelReg.appName;
            dto.createUser = modelReg.getCreateUser();
            dto.appDes = modelReg.getAppDes();

            String driveType = "mysql";
            if (driveType.equalsIgnoreCase(modelDataSource.getDriveType())) {
                dto.driveType = "MySQL";
            } else {
                dto.driveType = modelDataSource.getDriveType();
            }
            dto.host = modelDataSource.getHost();
            dto.port = modelDataSource.getPort();
            dto.dbName = modelDataSource.getDbName();

        } catch (Exception e) {
            log.error("方法执行失败:", e);
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAtlasInstanceIdAndDbId(long appid, String atlasInstanceId, String atlasDbId) {

        AppRegistrationPO modelReg = this.query().eq("id", appid).eq("del_flag", 1).one();
        if (modelReg == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 保存tb_app_registration
        boolean update = this.updateById(modelReg);
        if (!update) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        AppDataSourcePO modelData = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        if (modelData == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        // 保存tb_app_datasource
        boolean updateById = appDataSourceImpl.updateById(modelData);

        return updateById ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<AppRegistrationVO> listData(AppRegistrationQueryDTO query) {

        StringBuilder querySql = new StringBuilder();
        if (query.key != null && query.key.length() > 0) {
            querySql.append(" and app_name like concat('%', " + "'" + query.key + "'" + ", '%') ");
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        AppRegistrationPageDTO data = new AppRegistrationPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();
        Page<AppRegistrationVO> filter = baseMapper.filter(query.page, data);
        // 查询驱动类型
        List<AppRegistrationVO> appRegistrationVOList = filter.getRecords();
        if (!CollectionUtils.isEmpty(appRegistrationVOList)) {
            List<Long> appIds = appRegistrationVOList.stream().map(AppRegistrationVO::getId).collect(Collectors.toList());
            QueryWrapper<AppDataSourcePO> qw = new QueryWrapper<>();
            qw.in("app_id", appIds);
            List<AppDataSourcePO> driveTypePOList = appDataSourceMapper.selectList(qw);
            if (driveTypePOList != null) {
                for (AppRegistrationVO item : appRegistrationVOList) {
                    String driveType = Objects.requireNonNull(driveTypePOList.stream().filter(e -> e.getAppId() == item.getId()).findFirst().orElse(null)).getDriveType();
                    log.info("应用驱动类型：" + driveType);
                    item.setDriveType(driveType);
                    //实时和非实时计数方式不同
                    if (item.appType == 0) {
                        if (DbTypeEnum.doris_catalog.getName().equalsIgnoreCase(driveType)) {
                            item.setTblCount(tableAccessImpl.countTblByApp((int) item.id));
                            continue;
                        }
                        item.setTblCount(apiConfigImpl.countTblByAppForApi((int) item.id));
                    } else {
                        item.setTblCount(tableAccessImpl.countTblByApp((int) item.id));
                    }
                }
            }
            filter.setRecords(appRegistrationVOList);
        }
        return filter;
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_app_registration";
        dto.filterSql = FilterSqlConstants.APP_REGISTRATION_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public List<AppNameDTO> getDataList() {

        return baseMapper.getDataList();
    }

    @SneakyThrows
    @Override
    public List<DbNameDTO> connectDb(DbConnectionDTO dto) {
        if (StringUtils.isBlank(dto.driveType)) {
            throw new FkException(ResultEnum.DRIVETYPE_IS_NULL);
        }

        // jdbc连接信息
        String url = null;
        MyDestinationDataProvider myProvider = null;
        MongoClient mongoClient = null;
        List<String> allDatabases = new ArrayList<>();

        DataSourceTypeEnum driveType = DataSourceTypeEnum.getValue(dto.driveType);
        try {
            Connection conn = null;
            OracleUtils oracleUtils = new OracleUtils();
            switch (Objects.requireNonNull(driveType)) {
                case MYSQL:
                    MysqlConUtils mysqlConUtils = new MysqlConUtils();
//                    url = "jdbc:mysql://" + dto.host + ":" + dto.port;
                    url = dto.connectStr;
                    conn = DbConnectionHelper.connection(url, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
                    allDatabases.addAll(mysqlConUtils.getAllDatabases(conn));
                    break;
                case POSTGRESQL:
                    url = "jdbc:postgresql://" + dto.host + ":" + dto.port + "/postgres";

                    conn = DbConnectionHelper.connection(url, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.POSTGRESQL);
                    allDatabases.addAll(pgsqlUtils.getPgDatabases(conn));
                    break;
                case SQLSERVER:
                    url = "jdbc:sqlserver://" + dto.host + ":" + dto.port;
                    SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                    conn = DbConnectionHelper.connection(url, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER);
                    allDatabases.addAll(sqlServerPlusUtils.getAllDatabases(conn));
                    break;
                case ORACLE:
                    Class.forName(DriverTypeEnum.ORACLE.getName());
                    Connection connection = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    allDatabases.addAll(oracleUtils.getAllDatabases(connection));
                    break;
                case ORACLE_CDC:
                    conn = DbConnectionHelper.connection(dto.connectStr, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE);
                    allDatabases.addAll(oracleUtils.getAllDatabases(conn));
                    break;
                case OPENEDGE:
                    log.info("注册OpenEdge驱动程序前...");
//                    // 注册OpenEdge驱动程序
//                    DriverManager.registerDriver(new OpenEdgeDriver());
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.OPENEDGE.getDriverName());
                    log.info("注册OpenEdge驱动程序后...");
                    conn = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    allDatabases.addAll(OpenEdgeUtils.getAllDatabases(conn));
                case SAPBW:
                    ProviderAndDestination providerAndDestination = DbConnectionHelper.myDestination(dto.host, dto.sysNr, dto.port, dto.connectAccount, dto.connectPwd, dto.lang);
                    JCoDestination destination = providerAndDestination.getDestination();
                    myProvider = providerAndDestination.getMyProvider();
                    // 测试连接
                    destination.ping();
                    log.info("注册SAPBW驱动程序后...");
                    CubesAndCats allCubes = SapBwUtils.getAllCubes(destination, myProvider);
                    // cube Names
                    List<String> cubeNames = allCubes.getCubeNames();
                    // cat Names
                    List<String> catNames = allCubes.getCatNames();
                    // 只返回cubeNames
                    allDatabases.addAll(cubeNames);
                case DORIS_CATALOG:
                    log.info("注册HIVE驱动程序前...");
                    // 加载Hive驱动
                    Class.forName("org.apache.hive.jdbc.HiveDriver");

                    // 建立Hive连接
//                    Connection con = DriverManager.getConnection("jdbc:hive2://192.168.11.136:10001/default", "root", "root123");
                    conn = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    log.info("注册HIVE驱动程序后...");
                    allDatabases.addAll(hiveUtils.getAllDatabases(conn));
                    // 达梦数据库
                case DM8:
                    log.info("注册达梦数据库驱动程序前...");
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.DM8.getDriverName());
                    log.info("注册达梦数据库驱动程序后...");
                    conn = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    log.info("连接达梦数据库成功...");
                    allDatabases.addAll(dm8Utils.getAllDatabases(conn));
                    // todo:强生入仓配置 hudi的测试连接先不做
                case HUDI:
                    allDatabases.addAll(new ArrayList<>());
                case MONGODB:
                    ServerAddress serverAddress = new ServerAddress(dto.host, Integer.parseInt(dto.port));
                    List<ServerAddress> serverAddresses = new ArrayList<>();
                    serverAddresses.add(serverAddress);

                    //账号 验证数据库名 密码
                    MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential(dto.connectAccount, dto.sysNr, dto.connectPwd.toCharArray());
                    List<MongoCredential> mongoCredentials = new ArrayList<>();
                    mongoCredentials.add(scramSha1Credential);

                    mongoClient = new MongoClient(serverAddresses, mongoCredentials);
                    allDatabases.addAll(new ArrayList<>());
                default:
                    break;
            }
        } catch (Exception e) {
            //数据库账号或密码不正确
            ResultEnum resultEnum = ((FkException) e).getResultEnum();
            if (resultEnum.getCode() == 4001) {
                log.error("测试连接用户名或密码不正确:{}", e);
                throw new FkException(ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR);
            }
            log.error("测试连接失败:{}", e);
            throw new FkException(ResultEnum.DATAACCESS_CONNECTDB_ERROR);
        } finally {
            if (myProvider != null) {
                Environment.unregisterDestinationDataProvider(myProvider);
            }
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        final int[] count = {1};

        return allDatabases.stream().filter(Objects::nonNull).map(e -> {
            DbNameDTO dbname = new DbNameDTO();
            dbname.setId(count[0]);
            count[0]++;
            dbname.setDbName(e);
            return dbname;
        }).collect(Collectors.toList());
    }

    @Override
    public ResultEntity<Object> getRepeatAppName(String appName) {

        List<String> appNameList = baseMapper.getAppName();

        return appNameList.contains(appName) ? ResultEntityBuild.build(ResultEnum.DATAACCESS_APPNAME_ERROR) : ResultEntityBuild.build(ResultEnum.DATAACCESS_APPNAME_SUCCESS);
    }

    @Override
    public ResultEntity<Object> getRepeatAppAbbreviation(String appAbbreviation, boolean whetherSchema) {

        if (whetherSchema) {
            return ResultEntityBuild.build(ResultEnum.DATAACCESS_APPABBREVIATION_SUCCESS);
        }

        List<String> appAbbreviationList = baseMapper.getAppAbbreviation();

        return appAbbreviationList.contains(appAbbreviation) ? ResultEntityBuild.build(ResultEnum.DATAACCESS_APPABBREVIATION_ERROR) : ResultEntityBuild.build(ResultEnum.DATAACCESS_APPABBREVIATION_SUCCESS);
    }

    @Override
    public DataAccessNumDTO getDataAccessNum() {
        DataAccessNumDTO dto = new DataAccessNumDTO();
        dto.num = query().list().size();
        return dto;
    }

    @Override
    public Page<PipelineTableLogVO> logMessageFilter(PipelineTableQueryDTO dto) {

        AppDataSourcePO appDataSourcePo = appDataSourceImpl.query().eq("app_id", dto.appId).one();
        if (appDataSourcePo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        List<LogMessageFilterVO> sourcePage = new ArrayList<>();

        // 实时api
        if (DbTypeEnum.RestfulAPI.getName().equalsIgnoreCase(appDataSourcePo.driveType) || DbTypeEnum.webservice.getName().equalsIgnoreCase(appDataSourcePo.driveType)) {
            sourcePage = baseMapper.logMessageFilterByRestApi(Long.valueOf(dto.appId), dto.keyword, null);
            // 非实时api
        } else if (DbTypeEnum.api.getName().equalsIgnoreCase(appDataSourcePo.driveType)) {
            sourcePage = baseMapper.logMessageFilterByApi(Long.valueOf(dto.appId), dto.keyword, null);
            // 物理表
        } else {
            sourcePage = baseMapper.logMessageFilterByTable(Long.valueOf(dto.appId), dto.keyword, null);
        }

        log.info("接入库中的查询数据: " + JSON.toJSONString(sourcePage));

        Page<PipelineTableLogVO> targetPage = new Page<>();
        List<LogMessageFilterVO> records = sourcePage;
        List<PipelineTableLogVO> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(records)) {
            for (LogMessageFilterVO record : records) {
                PipelineTableLogVO vo = new PipelineTableLogVO();
                vo.appId = record.appId;
                vo.tableId = record.tableId == null ? record.apiId : record.tableId;
                vo.tableName = record.tableName == null ? record.apiName : record.tableName;
                if (record.appType == 0) {
                    vo.tableType = OlapTableEnum.PHYSICS_RESTAPI;
                } else if (record.appType == 1 && record.apiId != null) {
                    vo.tableType = OlapTableEnum.PHYSICS_API;
                } else if (record.appType == 1 && record.tableId != null) {
                    vo.tableType = OlapTableEnum.PHYSICS;
                }
                list.add(vo);
            }
        }

        log.info("接入组装后的数据: " + JSON.toJSONString(list));
        // 接入日志完善 对list进行改造,添加task日志信息
        try {
            ResultEntity<List<PipelineTableLogVO>> pipelineTableLogs = publishTaskClient.getPipelineTableLog(JSON.toJSONString(list), JSON.toJSONString(dto));
            List<PipelineTableLogVO> data = pipelineTableLogs.data;

            // 每页条数
            targetPage.setSize(dto.page.getSize());
            // 当前页
            targetPage.setCurrent(dto.page.getCurrent());
            // 总条数
            targetPage.setTotal(data.size());
            // steam流给list分页
            targetPage.setRecords(data.stream().skip((targetPage.getCurrent() - 1) * targetPage.getSize()).limit(targetPage.getSize()).collect(Collectors.toList()));
        } catch (Exception e) {
            targetPage.setRecords(null);
            targetPage.setTotal(0);
        }
        return targetPage;
    }

    @Override
    public List<LogMessageFilterVO> getTableNameListByAppIdAndApiId(PipelineTableQueryDTO dto) {

        AppDataSourcePO appDataSourcePo = appDataSourceImpl.query().eq("app_id", dto.appId).one();
        if (appDataSourcePo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        List<LogMessageFilterVO> sourcePage = new ArrayList<>();

        // 实时api
        if (DbTypeEnum.RestfulAPI.getName().equalsIgnoreCase(appDataSourcePo.driveType)) {
            // 非实时api
        } else if (DbTypeEnum.api.getName().equalsIgnoreCase(appDataSourcePo.driveType)) {
            sourcePage = baseMapper.logMessageFilterByApi(Long.valueOf(dto.appId), dto.keyword, dto.apiId);
            // 物理表
        } else {
            sourcePage = baseMapper.logMessageFilterByTable(Long.valueOf(dto.appId), dto.keyword, dto.apiId);
        }

        log.info("接入库中的查询数据: " + JSON.toJSONString(sourcePage));

        return sourcePage;
    }

    @Override
    public List<DispatchRedirectDTO> redirect(AccessRedirectDTO dto) {

        NifiCustomWorkflowDetailDTO detailDto = new NifiCustomWorkflowDetailDTO();
        detailDto.appId = String.valueOf(dto.getAppId());

        // 根据driveType对应管道的具体组件
        // 物理表
        if (dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.MYSQL.getName()) || dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName()) || dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.ORACLE.getName())) {
            detailDto.componentType = ChannelDataEnum.DATALAKE_TASK.getName();
            detailDto.tableId = String.valueOf(dto.getTableId());
            // 非实时api
        } else if (dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.API.getName())) {
            detailDto.componentType = ChannelDataEnum.DATALAKE_API_TASK.getName();
            detailDto.tableId = String.valueOf(dto.getApiId());
            // ftp
        } else if (dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.FTP.getName())) {
            detailDto.componentType = ChannelDataEnum.DATALAKE_FTP_TASK.getName();
            detailDto.tableId = String.valueOf(dto.getApiId());
        }

        try {
            ResultEntity<List<DispatchRedirectDTO>> result = dataFactoryClient.redirect(detailDto);
            if (result.code == ResultEnum.SUCCESS.getCode()) {
                return result.data;
            }
        } catch (Exception e) {
            log.error("远程调用失败,方法名: 【data-factory:redirect】");
            return null;
        }
        return null;
    }

    @Override
    public List<FiDataMetaDataDTO> getDataAccessStructure(FiDataMetaDataReqDTO reqDto) {

        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId));
        if (!flag) {
            // 将数据接入结构存入redis
            setDataAccessStructure(reqDto);
        }
        List<FiDataMetaDataDTO> list = null;
        String dataAccessStructure = redisUtil.get(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId)).toString();
        if (StringUtils.isNotBlank(dataAccessStructure)) {
            list = JSONObject.parseArray(dataAccessStructure, FiDataMetaDataDTO.class);
        }
        return list;
    }

    @Override
    public List<FiDataMetaDataTreeDTO> getDataAccessTableStructure(FiDataMetaDataReqDTO reqDto) {

        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId));
        if (!flag) {
            // 将数据接入结构存入redis
            setDataAccessStructure(reqDto);
        }
        List<FiDataMetaDataTreeDTO> list = null;
        String dataAccessStructure = redisUtil.get(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId)).toString();
        if (StringUtils.isNotBlank(dataAccessStructure)) {
            list = JSONObject.parseArray(dataAccessStructure, FiDataMetaDataTreeDTO.class);
        }
        return list;
    }

    @Override
    public boolean setDataAccessStructure(FiDataMetaDataReqDTO reqDto) {

        List<FiDataMetaDataDTO> list = new ArrayList<>();
        FiDataMetaDataDTO dto = new FiDataMetaDataDTO();
        // FiData数据源id: 数据资产自定义
        dto.setDataSourceId(Integer.parseInt(reqDto.dataSourceId));

        // 第一层id
        List<FiDataMetaDataTreeDTO> dataTreeList = new ArrayList<>();
        FiDataMetaDataTreeDTO dataTree = new FiDataMetaDataTreeDTO();
        dataTree.setId(reqDto.dataSourceId);
        dataTree.setParentId("-10");
        dataTree.setLabel(reqDto.dataSourceName);
        dataTree.setLabelAlias(reqDto.dataSourceName);
        dataTree.setLevelType(LevelTypeEnum.DATABASE);
        dataTree.setSourceType(1);
        dataTree.setSourceId(Integer.parseInt(reqDto.dataSourceId));

        // 封装data-access所有结构数据
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = buildChildren(reqDto.dataSourceId);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> next = hashMap.entrySet().iterator().next();
        dataTree.setChildren(next.getValue());
        dataTreeList.add(dataTree);

        dto.setChildren(dataTreeList);
        list.add(dto);

        if (!CollectionUtils.isEmpty(list)) {
            redisUtil.set(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId), JSON.toJSONString(list));
        }
        List<FiDataMetaDataTreeDTO> key = next.getKey();
        if (!CollectionUtils.isEmpty(key)) {
            String s = JSON.toJSONString(key);
            redisUtil.set(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId), s);
        }

        return true;
    }

    @Override
    public TableRuleInfoDTO buildTableRuleInfo(TableRuleParameterDTO dto) {

        TableRuleInfoDTO tableRuleInfoDto = new TableRuleInfoDTO();
        List<TableRuleInfoDTO> fieldRules = new ArrayList<>();

        TableAccessNonDTO data = tableAccessImpl.getData(dto.getTableId());
        if (data == null) {
            return null;
        }
        AppRegistrationDTO appRegistrationDto = this.getData(data.appId);
        if (appRegistrationDto == null) {
            return null;
        }

        // 应用名称
        tableRuleInfoDto.businessName = appRegistrationDto.appName;
        // 应用负责人
        tableRuleInfoDto.dataResponsiblePerson = appRegistrationDto.appPrincipal;
        // 表名
        tableRuleInfoDto.name = TableNameGenerateUtils.buildOdsTableName(data.tableName, appRegistrationDto.appAbbreviation, appRegistrationDto.whetherSchema);
        // 类型 1: 表
        tableRuleInfoDto.type = 1;

        if (!CollectionUtils.isEmpty(data.list)) {
            StringBuilder transformationRules = new StringBuilder();
            data.list.stream().filter(Objects::nonNull).forEach(e -> {
                TableRuleInfoDTO tableRuleInfoDtoByField = new TableRuleInfoDTO();
                // 应用名称
                tableRuleInfoDtoByField.businessName = appRegistrationDto.appName;
                // 应用负责人
                tableRuleInfoDtoByField.dataResponsiblePerson = appRegistrationDto.appPrincipal;
                // 字段名称
                tableRuleInfoDtoByField.name = e.fieldName;
                // 类型 2: 字段
                tableRuleInfoDtoByField.type = 2;
                if (!e.fieldName.equalsIgnoreCase(e.sourceFieldName)) {
                    transformationRules.append(e.sourceFieldName).append("转换").append(e.fieldName).append(",");
                    // 转换规则
                    tableRuleInfoDtoByField.transformationRules = transformationRules.deleteCharAt(transformationRules.length() - 1).toString();
                }
                fieldRules.add(tableRuleInfoDtoByField);
            });
        }

        // 表字段规则
        tableRuleInfoDto.fieldRules = fieldRules;

        return tableRuleInfoDto;
    }

    @Override
    public List<FiDataTableMetaDataDTO> getFiDataTableMetaData(FiDataTableMetaDataReqDTO dto) {

        if (CollectionUtils.isEmpty(dto.getTableUniques())) {
            return null;
        }

        return dto.getTableUniques().keySet().stream().map(e -> {
            // 表信息
            FiDataTableMetaDataDTO tableMetaDataDto = new FiDataTableMetaDataDTO();
            TableAccessNonDTO data = tableAccessImpl.getData(Long.parseLong(e));
            if (data == null || CollectionUtils.isEmpty(data.list)) {
                return null;
            }
            AppRegistrationPO app = this.query().eq("id", data.appId).select("app_abbreviation").one();
            tableMetaDataDto.id = e;
            tableMetaDataDto.name = TableNameGenerateUtils.buildOdsTableName(data.tableName, app.appAbbreviation, app.whetherSchema);
            tableMetaDataDto.nameAlias = data.tableName;

            // 字段信息
            tableMetaDataDto.fieldList = data.list.stream().filter(Objects::nonNull).map(field -> {
                FiDataTableMetaDataDTO fieldMetaDataDto = new FiDataTableMetaDataDTO();
                fieldMetaDataDto.id = String.valueOf(field.id);
                fieldMetaDataDto.name = field.fieldName;
                fieldMetaDataDto.nameAlias = field.fieldName;
                return fieldMetaDataDto;
            }).collect(Collectors.toList());

            return tableMetaDataDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AppBusinessInfoDTO> getAppList() {
        return AppRegistrationMap.INSTANCES.listDtoToAppBusinessInfoDto(baseMapper.getDataList());
    }

    /**
     * 获取所有不使用简称作为架构名的应用信息
     *
     * @return
     */
    @Override
    public List<AppRegistrationPO> getAppListWithNoSchema() {
        LambdaQueryWrapper<AppRegistrationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppRegistrationPO::getWhetherSchema, 0);
        return list(wrapper);
    }

    @Override
    public String getApiToken(AppDataSourceDTO dto) {
        Optional<ApiResultConfigDTO> first = dto.apiResultConfigDtoList.stream().filter(e -> e.checked == true).findFirst();
        if (!first.isPresent()) {
            throw new FkException(ResultEnum.RETURN_RESULT_DEFINITION);
        }
        try {
            // jwt身份验证方式对象
            ApiHttpRequestDTO apiHttpRequestDto = new ApiHttpRequestDTO();
            apiHttpRequestDto.httpRequestEnum = POST;
            // 身份验证地址
            apiHttpRequestDto.uri = dto.connectStr;
            // jwt账号&密码
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(dto.accountKey, dto.connectAccount);
            jsonObj.put(dto.pwdKey, dto.connectPwd);

            String result = buildHttpRequest.sendPostRequest(apiHttpRequestDto, jsonObj.toJSONString());

            JSONObject jsonObject = JSONObject.parseObject(result);
            String token = (String) jsonObject.get(first.get().name);
            if (StringUtils.isEmpty(token)) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            //token存Redis
            redisTemplate.opsForValue().set("ApiConfig:" + dto.id, token, dto.expirationTime, TimeUnit.MINUTES);
            return token;
        } catch (Exception e) {
            log.error("getApiToken ex:", e);
            throw new FkException(ResultEnum.AUTH_TOKEN_PARSER_ERROR);
        }
    }

    @Override
    public CdcJobScriptDTO buildCdcJobScript(CdcJobParameterDTO dto) {

        AppDataSourceDTO dataSourceData = appDataSourceImpl.getDataSourceByAppId(dto.appId);

        TbTableAccessDTO tableAccessData = tableAccessImpl.getTableAccessData(dto.tableAccessId);

        AppRegistrationPO registrationPo = mapper.selectById(dto.appId);

        if (tableAccessData == null || registrationPo == null) {
            throw new FkException(ResultEnum.TASK_TABLE_NOT_EXIST);
        }
        //拼接ods表名
        if (!tableAccessData.useExistTable) {
            tableAccessData.tableName = TableNameGenerateUtils.buildOdsTableName(tableAccessData.tableName, registrationPo.appAbbreviation, registrationPo.whetherSchema);
        }

        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(registrationPo.targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return oracleCdcUtils.createCdcJobScript(dto, dataSourceData, dataSourceConfig.data, tableAccessData);
    }

    @Override
    public JSONObject dataTypeList(Integer appId) {

        AppRegistrationPO po = baseMapper.selectById(appId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ResultEntity<DataSourceDTO> fiDataDataSourceById = userClient.getFiDataDataSourceById(po.targetDbId);
        if (fiDataDataSourceById == null) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum conType = fiDataDataSourceById.data.conType;
        //如果是hudi ods :则是入参配置，就去获取应用引用数据源的id
        if (DataSourceTypeEnum.HUDI.getName().equalsIgnoreCase(conType.getName())) {
            List<AppDataSourceDTO> appSourcesByAppId = iAppDataSource.getAppSourcesByAppId(appId);

            String driveType = appSourcesByAppId.get(0).getDriveType();
            conType = com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.getEnum(driveType.toUpperCase());
        }

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(conType);
        return command.dataTypeList();

    }

    /**
     * 构建data-access子集树
     *
     * @param id FiData数据源id
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/15 17:46
     */
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> buildChildren(String id) {

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();

        List<FiDataMetaDataTreeDTO> appTypeTreeList = new ArrayList<>();

        FiDataMetaDataTreeDTO appTreeByRealTime = new FiDataMetaDataTreeDTO();
        String appTreeByRealTimeGuid = UUID.randomUUID().toString();
        appTreeByRealTime.setId(appTreeByRealTimeGuid);
        appTreeByRealTime.setParentId(id);
        appTreeByRealTime.setLabel("实时应用");
        appTreeByRealTime.setLabelAlias("实时应用");
        appTreeByRealTime.setLevelType(LevelTypeEnum.FOLDER);
        appTreeByRealTime.setSourceType(1);
        appTreeByRealTime.setSourceId(Integer.parseInt(id));

        FiDataMetaDataTreeDTO appTreeByNonRealTime = new FiDataMetaDataTreeDTO();
        String appTreeByNonRealTimeGuid = UUID.randomUUID().toString();
        appTreeByNonRealTime.setId(appTreeByNonRealTimeGuid);
        appTreeByNonRealTime.setParentId(id);
        appTreeByNonRealTime.setLabel("非实时应用");
        appTreeByNonRealTime.setLabelAlias("非实时应用");
        appTreeByNonRealTime.setLevelType(LevelTypeEnum.FOLDER);
        appTreeByNonRealTime.setSourceType(1);
        appTreeByNonRealTime.setSourceId(Integer.parseInt(id));

        // 所有应用
        List<AppRegistrationPO> appPoList = this.query().orderByDesc("create_time").list();
        // 所有应用下表字段信息
        List<FiDataMetaDataTreeDTO> tableFieldList = new ArrayList<>();

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTreeByRealTime = getFiDataMetaDataTreeByRealTime(appTreeByRealTimeGuid, id, appPoList);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTreeByRealTime = fiDataMetaDataTreeByRealTime.entrySet().iterator().next();
        appTreeByRealTime.setChildren(nextTreeByRealTime.getValue());
        tableFieldList.addAll(nextTreeByRealTime.getKey());

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTreeByNonRealTime = getFiDataMetaDataTreeByNonRealTime(appTreeByNonRealTimeGuid, id, appPoList);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTreeByNonRealTime = fiDataMetaDataTreeByNonRealTime.entrySet().iterator().next();
        appTreeByNonRealTime.setChildren(nextTreeByNonRealTime.getValue());
        tableFieldList.addAll(nextTreeByNonRealTime.getKey());

        appTypeTreeList.add(appTreeByRealTime);
        appTypeTreeList.add(appTreeByNonRealTime);

        // key是表字段 value是tree
        hashMap.put(tableFieldList, appTypeTreeList);
        return hashMap;
    }

    /**
     * 获取实时应用结构
     *
     * @param appPoList 所有的应用实体对象
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/16 15:21
     * @params id FiData数据源id
     */
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> getFiDataMetaDataTreeByRealTime(String appTreeByRealTimeGuid, String id, List<AppRegistrationPO> appPoList) {
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = appPoList.stream().filter(Objects::nonNull)
                // 实时应用
                .filter(e -> e.appType == 0).map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    // 当前层默认生成的uuid
                    String uuid_appId = UUID.randomUUID().toString().replace("-", "");
                    appDtoTree.setId(uuid_appId); //String.valueOf(app.id)
                    // 上一级的id
                    appDtoTree.setSourceType(1);
                    appDtoTree.setSourceId(Integer.parseInt(id));
                    appDtoTree.setParentId(appTreeByRealTimeGuid);
                    appDtoTree.setLabel(app.appName);
                    appDtoTree.setLabelAlias(app.appAbbreviation);
                    appDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                    appDtoTree.setLabelDesc(app.appDes);

                    // 第二层: api层
                    // 查询驱动类型
//                    AppDataSourcePO dataSourcePo = this.appDataSourceImpl.query().eq("app_id", app.id).one();
//                    DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.getValue(dataSourcePo.driveType);
                    String type = "dataBase";
                    QueryWrapper<AppDataSourcePO> appDataSourcePOQueryWrapper = new QueryWrapper<>();
                    appDataSourcePOQueryWrapper.lambda().eq(AppDataSourcePO::getAppId, app.id).eq(AppDataSourcePO::getDelFlag, 1).eq(AppDataSourcePO::getDriveType, DataSourceTypeEnum.RestfulAPI.getName());
                    List<AppDataSourcePO> appDataSourcePOS = appDataSourceMapper.selectList(appDataSourcePOQueryWrapper);
                    if (!CollectionUtils.isEmpty(appDataSourcePOS)) {
                        type = "restfulapi";
                    }
                    // 根据驱动类型封装不同的子级
                    switch (type) {
                        case "restfulapi":
                            // 当前app下的所有api
                            List<FiDataMetaDataTreeDTO> apiTreeList = this.apiConfigImpl.query().eq("app_id", app.id).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(api -> {
                                FiDataMetaDataTreeDTO apiDtoTree = new FiDataMetaDataTreeDTO();
                                String uuid_apiId = UUID.randomUUID().toString().replace("-", "");
                                apiDtoTree.setId(uuid_apiId);// String.valueOf(api.id)
                                apiDtoTree.setParentId(uuid_appId); // String.valueOf(app.id)
                                apiDtoTree.setLabel(api.apiName);
                                apiDtoTree.setLabelAlias(api.apiName);
                                apiDtoTree.setSourceType(1);
                                apiDtoTree.setSourceId(Integer.parseInt(id));
                                apiDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                                // 不是已发布的都当作未发布处理
                                if (api.publish == null) {
                                    apiDtoTree.setPublishState("0");
                                } else {
                                    apiDtoTree.setPublishState(String.valueOf(api.publish != 1 ? 0 : 1));
                                }
                                apiDtoTree.setLabelDesc(api.apiDes);

                                // 第三层: table层
                                List<FiDataMetaDataTreeDTO> tableTreeList = this.tableAccessImpl.query().eq("api_id", api.id).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(table -> {
                                    FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                                    tableDtoTree.setId(String.valueOf(table.id));
                                    tableDtoTree.setParentId(uuid_apiId); // String.valueOf(api.id)
                                    tableDtoTree.setLabel(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                    tableDtoTree.setLabelAlias(table.tableName);
                                    tableDtoTree.setLabelRelName(TableNameGenerateUtils.buildOdsTableRelName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                    tableDtoTree.setLabelFramework(TableNameGenerateUtils.buildOdsSchemaName(app.appAbbreviation, app.whetherSchema));
                                    tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                    tableDtoTree.setSourceType(1);
                                    tableDtoTree.setSourceId(Integer.parseInt(id));
                                    if (table.publish == null) {
                                        tableDtoTree.setPublishState("0");
                                        table.publish = 0;
                                    }
                                    tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                    // 实时API应用表描述不存在，取api的描述
                                    tableDtoTree.setLabelDesc(api.apiDes);
                                    tableDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());

                                    // 第四层: field层
                                    List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query().eq("table_access_id", table.id).list().stream().filter(Objects::nonNull).map(field -> {

                                        FiDataMetaDataTreeDTO fieldDtoTree = new FiDataMetaDataTreeDTO();
                                        fieldDtoTree.setId(String.valueOf(field.id));
                                        fieldDtoTree.setParentId(String.valueOf(table.id));
                                        fieldDtoTree.setLabel(field.fieldName);
                                        fieldDtoTree.setLabelAlias(field.fieldName);
                                        fieldDtoTree.setLevelType(LevelTypeEnum.FIELD);
                                        fieldDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                        fieldDtoTree.setLabelLength(String.valueOf(field.fieldLength));
                                        fieldDtoTree.setLabelType(field.fieldType);
                                        fieldDtoTree.setLabelDesc(field.fieldDes);
                                        fieldDtoTree.setSourceType(1);
                                        fieldDtoTree.setSourceId(Integer.parseInt(id));
                                        fieldDtoTree.setParentName(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                        fieldDtoTree.setParentNameAlias(table.tableName);
                                        fieldDtoTree.setParentLabelRelName(TableNameGenerateUtils.buildOdsTableRelName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                        fieldDtoTree.setParentLabelFramework(TableNameGenerateUtils.buildOdsSchemaName(app.appAbbreviation, app.whetherSchema));
                                        fieldDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                        return fieldDtoTree;
                                    }).collect(Collectors.toList());

                                    // table的子级
                                    tableDtoTree.setChildren(fieldTreeList);
                                    return tableDtoTree;
                                }).collect(Collectors.toList());

                                // api的子级
                                apiDtoTree.setChildren(tableTreeList);
                                // 表字段信息单独再保存一份
                                if (!CollectionUtils.isEmpty(tableTreeList)) {
                                    key.addAll(tableTreeList);
                                }
                                return apiDtoTree;
                            }).collect(Collectors.toList());

                            // app的子级
                            appDtoTree.setChildren(apiTreeList);
                            break;
//                        case API:
//                        case MYSQL:
//                        case SQLSERVER:
//                        case ORACLE:
//                        case POSTGRESQL:
                        default:
                            break;
                    }
                    return appDtoTree;
                }).collect(Collectors.toList());
        hashMap.put(key, value);
        return hashMap;
    }

    /**
     * 获取非实时应用结构
     *
     * @param id        guid
     * @param appPoList 所有的应用实体对象
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/16 15:21
     */
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> getFiDataMetaDataTreeByNonRealTime(String appTreeByNonRealTimeGuid, String id, List<AppRegistrationPO> appPoList) {
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = appPoList.stream().filter(Objects::nonNull)
                // 非实时应用
                .filter(e -> e.appType == 1).map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    String uuid_appId = UUID.randomUUID().toString().replace("-", "");
                    appDtoTree.setId(uuid_appId); //String.valueOf(app.id)
                    // 上一级的id
                    appDtoTree.setParentId(appTreeByNonRealTimeGuid);
                    appDtoTree.setLabel(app.appName);
                    appDtoTree.setLabelAlias(app.appAbbreviation);
                    appDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                    appDtoTree.setSourceType(1);
                    appDtoTree.setSourceId(Integer.parseInt(id));
                    appDtoTree.setLabelDesc(app.appDes);

                    // 查询驱动类型
                    String type = "dataBase";
                    QueryWrapper<AppDataSourcePO> appDataSourcePOQueryWrapper = new QueryWrapper<>();
                    appDataSourcePOQueryWrapper.lambda().eq(AppDataSourcePO::getAppId, app.id).eq(AppDataSourcePO::getDelFlag, 1).eq(AppDataSourcePO::getDriveType, DataSourceTypeEnum.API.getName());
                    List<AppDataSourcePO> appDataSourcePOS = appDataSourceMapper.selectList(appDataSourcePOQueryWrapper);
                    if (!CollectionUtils.isEmpty(appDataSourcePOS)) {
                        type = "api";
                    }
                    // 根据驱动类型封装不同的子级
                    switch (type) {
                        // 第二层: api层
                        case "api":
                            // 当前app下的所有api
                            List<FiDataMetaDataTreeDTO> apiTreeList = this.apiConfigImpl.query().eq("app_id", app.id).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(api -> {
                                FiDataMetaDataTreeDTO apiDtoTree = new FiDataMetaDataTreeDTO();

                                String uuid_apiId = UUID.randomUUID().toString().replace("-", "");
                                apiDtoTree.setId(uuid_apiId); // String.valueOf(api.id)
                                apiDtoTree.setParentId(uuid_appId); // String.valueOf(app.id)
                                apiDtoTree.setLabel(api.apiName);
                                apiDtoTree.setLabelAlias(api.apiName);
                                apiDtoTree.setSourceType(1);
                                apiDtoTree.setSourceId(Integer.parseInt(id));
                                apiDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                                // 不是已发布的都当作未发布处理
                                if (api.publish == null) {
                                    apiDtoTree.setPublishState("0");
                                } else {
                                    apiDtoTree.setPublishState(String.valueOf(api.publish != 1 ? 0 : 1));
                                }
                                apiDtoTree.setLabelDesc(api.apiDes);

                                // 第三层: table层
                                List<FiDataMetaDataTreeDTO> tableTreeList = this.tableAccessImpl.query().eq("api_id", api.id).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(table -> {
                                    FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                                    tableDtoTree.setId(String.valueOf(table.id));
                                    tableDtoTree.setParentId(uuid_apiId); // String.valueOf(api.id)
                                    tableDtoTree.setLabel(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                    tableDtoTree.setLabelAlias(table.tableName);
                                    tableDtoTree.setLabelRelName(TableNameGenerateUtils.buildOdsTableRelName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                    tableDtoTree.setLabelFramework(TableNameGenerateUtils.buildOdsSchemaName(app.appAbbreviation, app.whetherSchema));
                                    tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                    tableDtoTree.setSourceType(1);
                                    tableDtoTree.setSourceId(Integer.parseInt(id));
                                    if (table.publish == null) {
                                        tableDtoTree.setPublishState("0");
                                        table.publish = 0;
                                    } else {
                                        tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                    }
                                    tableDtoTree.setLabelDesc(table.tableDes);
                                    tableDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());

                                    // 第四层: field层
                                    List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query().eq("table_access_id", table.id).list().stream().filter(Objects::nonNull).map(field -> {

                                        FiDataMetaDataTreeDTO fieldDtoTree = new FiDataMetaDataTreeDTO();
                                        fieldDtoTree.setId(String.valueOf(field.id));
                                        fieldDtoTree.setParentId(String.valueOf(table.id));
                                        fieldDtoTree.setLabel(field.fieldName);
                                        fieldDtoTree.setLabelAlias(field.fieldName);
                                        fieldDtoTree.setLevelType(LevelTypeEnum.FIELD);
                                        fieldDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                        fieldDtoTree.setLabelLength(String.valueOf(field.fieldLength));
                                        fieldDtoTree.setLabelType(field.fieldType);
                                        fieldDtoTree.setLabelDesc(field.fieldDes);
                                        fieldDtoTree.setSourceType(1);
                                        fieldDtoTree.setSourceId(Integer.parseInt(id));
                                        fieldDtoTree.setParentName(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                        fieldDtoTree.setParentNameAlias(table.tableName);
                                        fieldDtoTree.setParentLabelRelName(TableNameGenerateUtils.buildOdsTableRelName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                        fieldDtoTree.setParentLabelFramework(TableNameGenerateUtils.buildOdsSchemaName(app.appAbbreviation, app.whetherSchema));
                                        fieldDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                        return fieldDtoTree;
                                    }).collect(Collectors.toList());

                                    // table的子级
                                    tableDtoTree.setChildren(fieldTreeList);
                                    return tableDtoTree;
                                }).collect(Collectors.toList());

                                // api的子级
                                apiDtoTree.setChildren(tableTreeList);
                                // 表字段信息单独再保存一份
                                if (!CollectionUtils.isEmpty(tableTreeList)) {
                                    key.addAll(tableTreeList);
                                }
                                return apiDtoTree;
                            }).collect(Collectors.toList());

                            // app的子级
                            appDtoTree.setChildren(apiTreeList);
                            break;
                        // 第二层: table层
                        case "dataBase":
                            List<FiDataMetaDataTreeDTO> tableTreeList = this.tableAccessImpl.query().eq("app_id", app.id).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(table -> {
                                FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                                tableDtoTree.setId(String.valueOf(table.id));
                                tableDtoTree.setParentId(uuid_appId); // String.valueOf(app.id)
                                tableDtoTree.setLabel(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                tableDtoTree.setLabelAlias(table.tableName);
                                tableDtoTree.setLabelRelName(TableNameGenerateUtils.buildOdsTableRelName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                tableDtoTree.setLabelFramework(TableNameGenerateUtils.buildOdsSchemaName(app.appAbbreviation, app.whetherSchema));
                                tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                tableDtoTree.setSourceType(1);
                                tableDtoTree.setSourceId(Integer.parseInt(id));
                                if (table.publish == null) {
                                    tableDtoTree.setPublishState("0");
                                    table.publish = 0;
                                } else {
                                    tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                }
                                tableDtoTree.setLabelDesc(table.tableDes);
                                tableDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());

                                // 第四层: field层
                                List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query().eq("table_access_id", table.id).list().stream().filter(Objects::nonNull).map(field -> {

                                    FiDataMetaDataTreeDTO fieldDtoTree = new FiDataMetaDataTreeDTO();
                                    fieldDtoTree.setId(String.valueOf(field.id));
                                    fieldDtoTree.setParentId(String.valueOf(table.id));
                                    fieldDtoTree.setLabel(field.fieldName);
                                    fieldDtoTree.setLabelAlias(field.fieldName);
                                    fieldDtoTree.setLevelType(LevelTypeEnum.FIELD);
                                    fieldDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                    fieldDtoTree.setLabelLength(String.valueOf(field.fieldLength));
                                    fieldDtoTree.setLabelType(field.fieldType);
                                    fieldDtoTree.setLabelDesc(field.fieldDes);
                                    fieldDtoTree.setSourceType(1);
                                    fieldDtoTree.setSourceId(Integer.parseInt(id));
                                    fieldDtoTree.setParentName(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                    fieldDtoTree.setParentNameAlias(table.tableName);
                                    fieldDtoTree.setParentLabelRelName(TableNameGenerateUtils.buildOdsTableRelName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                    fieldDtoTree.setParentLabelFramework(TableNameGenerateUtils.buildOdsSchemaName(app.appAbbreviation, app.whetherSchema));
                                    fieldDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                    return fieldDtoTree;
                                }).collect(Collectors.toList());

                                // table的子级
                                tableDtoTree.setChildren(fieldTreeList);
                                return tableDtoTree;
                            }).collect(Collectors.toList());

                            appDtoTree.setChildren(tableTreeList);
                            // 表字段信息单独再保存一份
                            if (!CollectionUtils.isEmpty(tableTreeList)) {
                                key.addAll(tableTreeList);
                            }
                            break;
//                            case RestfulAPI:
                        default:
                            break;
                    }
                    return appDtoTree;
                }).collect(Collectors.toList());
        hashMap.put(key, value);
        return hashMap;
    }

    /**
     * 校验schema
     *
     * @param schemaName
     * @param targetDbId
     */
    public void VerifySchema(String schemaName, Integer targetDbId) {
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
        Connection connection = helper.connection(dataSourceConfig.data.conStr, dataSourceConfig.data.conAccount, dataSourceConfig.data.conPassword, dataSourceConfig.data.conType);
        CreateSchemaSqlUtils.buildSchemaSql(connection, schemaName, dataSourceConfig.data.conType);
    }

    @Override
    public List<ExternalDataSourceDTO> getFiDataDataSource() {
        ResultEntity<List<DataSourceDTO>> allExternalDataSource = userClient.getAllFiDataDataSource();
        if (allExternalDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        List<DataSourceDTO> collect = allExternalDataSource.data.stream().filter(e -> SourceBusinessTypeEnum.ODS.getName().equals(e.sourceBusinessType.getName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return new ArrayList<>();
        }
        List<ExternalDataSourceDTO> list = new ArrayList<>();
        for (DataSourceDTO item : collect) {
            ExternalDataSourceDTO data = new ExternalDataSourceDTO();
            data.id = item.id;
            data.name = item.name;
            data.dbType = item.conType.getName().toLowerCase();
            list.add(data);
        }
        return list;
    }

    @Override
    public List<MetaDataInstanceAttributeDTO> synchronizationAppRegistration() {
        List<AppRegistrationPO> appRegistrationList = this.query().list();
        if (CollectionUtils.isEmpty(appRegistrationList)) {
            return new ArrayList<>();
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();

        for (AppRegistrationPO appRegistration : appRegistrationList) {
            List<AppDataSourcePO> one = appDataSourceImpl.query().eq("app_id", appRegistration.id).list();
            for (AppDataSourcePO appDataSourcePO : one) {
                if (one == null) {
                    continue;
                }
                List<MetaDataInstanceAttributeDTO> data = addDataSourceMetaData(appRegistration, appDataSourcePO);
                if (CollectionUtils.isEmpty(data)) {
                    continue;
                }

                list.addAll(data);
            }
        }
        return list;
    }

    @Override
    public List<MetaDataInstanceAttributeDTO> synchronizationAccessTable() {
        List<AppRegistrationPO> appRegistrationList = this.query().list();
        if (CollectionUtils.isEmpty(appRegistrationList)) {
            return new ArrayList<>();
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();

        for (AppRegistrationPO appRegistrationPo : appRegistrationList) {

            MetaDataInstanceAttributeDTO metaDataInstance = getMetaDataInstance(appRegistrationPo);

            List<TableAccessPO> tableAccessPoList = tableAccessImpl.query().eq("app_id", appRegistrationPo.id).list();
            if (CollectionUtils.isEmpty(tableAccessPoList)) {
                continue;
            }
            List<MetaDataTableAttributeDTO> metaDataTable = new ArrayList<>();
            for (TableAccessPO tableAccessPo : tableAccessPoList) {
                metaDataTable.addAll(getAccessTableMetaData(appRegistrationPo, tableAccessPo.id, metaDataInstance.dbList.get(0).qualifiedName));
            }
            metaDataInstance.dbList.get(0).tableList = metaDataTable;
            list.add(metaDataInstance);
        }
        return list;
    }

    /**
     * 元数据同步单个接入表
     *
     * @return
     */
    @Override
    public List<MetaDataInstanceAttributeDTO> synchronizationAccessOneTable(Long appId) {
        AppRegistrationPO one = getOne(new LambdaQueryWrapper<AppRegistrationPO>().eq(AppRegistrationPO::getId, appId));
        if (one == null) {
            return new ArrayList<>();
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();

        MetaDataInstanceAttributeDTO metaDataInstance = getMetaDataInstance(one);

        List<TableAccessPO> tableAccessPoList = tableAccessImpl.query().eq("app_id", one.id).list();
        List<MetaDataTableAttributeDTO> metaDataTable = new ArrayList<>();
        for (TableAccessPO tableAccessPo : tableAccessPoList) {
            metaDataTable.addAll(getAccessTableMetaData(one, tableAccessPo.id, metaDataInstance.dbList.get(0).qualifiedName));
        }
        metaDataInstance.dbList.get(0).tableList = metaDataTable;
        list.add(metaDataInstance);

        return list;
    }

    /**
     * 依据应用id集合查询应用对应的目标源id集合
     *
     * @param appIds 应用id集合
     * @return
     */
    @Override
    public List<AppRegistrationInfoDTO> getBatchTargetDbIdByAppIds(List<Integer> appIds) {
        if (CollectionUtils.isEmpty(appIds)) {
            return null;
        }

        List<AppRegistrationInfoDTO> idList = new ArrayList<>();

        List<AppRegistrationPO> appRegistrationPOList = mapper.selectBatchIds(appIds);
        if (!CollectionUtils.isEmpty(appRegistrationPOList)) {
            idList = appRegistrationPOList.stream().map(e -> {
                AppRegistrationInfoDTO item = new AppRegistrationInfoDTO();
                item.setAppId(e.id);
                item.setTargetDbId(e.targetDbId);
                return item;
            }).collect(Collectors.toList());
        }
        return idList;
    }

    @Override
    public SyncTableCountVO getSyncTableCount(Integer appId) {
        if (appId == 0) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }

        List<SyncTableCountPO> list = tableAccessMapper.getSyncTableCount(appId, DelFlagEnum.NORMAL_FLAG.getValue());
        SyncTableCountVO model = new SyncTableCountVO();
        if (!CollectionUtils.isEmpty(list)) {
            Map<Integer, Integer> map = list.stream().collect(Collectors.toMap(SyncTableCountPO::getSyncMode, SyncTableCountPO::getCount));
            log.info("数据{}", JSON.toJSONString(map));
            int appendCount = map.get(SyncModeEnum.FULL_AMOUNT.getValue()) == null ? 0 : map.get(SyncModeEnum.FULL_AMOUNT.getValue());
            int fullCount = map.get(SyncModeEnum.INCREMENTAL.getValue()) == null ? 0 : map.get(SyncModeEnum.INCREMENTAL.getValue());
            int timeCount = map.get(SyncModeEnum.CUSTOM_OVERRIDE.getValue()) == null ? 0 : map.get(SyncModeEnum.CUSTOM_OVERRIDE.getValue());
            int delKey = map.get(SyncModeEnum.DELETE_INSERT.getValue()) == null ? 0 : map.get(SyncModeEnum.DELETE_INSERT.getValue());
            int key = map.get(SyncModeEnum.TIME_COVER.getValue()) == null ? 0 : map.get(SyncModeEnum.TIME_COVER.getValue());
            model.setAppendCoverCount(appendCount);
            model.setFullCoverCount(fullCount);
            model.setBusinessTimeCoverCount(timeCount);
            model.setBusinessKeyCoverCount(delKey + key);
            model.setTotalCount(appendCount + fullCount + timeCount + delKey + key);
        }
        return model;
    }

    @Override
    public List<AppDriveTypeDTO> getDriveTypeByAppId(Long appid) {

        //先获取到所有的数据源驱动类型
        List<AppDriveTypePO> list = appDriveTypeMapper.listData();
        //PO --> DTO
        List<AppDriveTypeDTO> appDriveTypeDTOS = AppDriveTypeDTO.convertEntityList(list);
        //appid==0表示该应用是第一次接入数据源，因此返回全部的数据源类型给前端
        if (appid == 0) {
            return appDriveTypeDTOS;
        }
        //获取当前应用所拥有的数据源类型
        List<AppDataSourcePO> data = iAppDataSource.getDataSourceDrivesTypeByAppId(appid);
        if (CollectionUtils.isEmpty(data)) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS);
        }
//        遇到数据量较大的情况，下面这种方式去重效率不高。
//        List<AppDriveTypeDTO> result = chooseDriveType(data, appDriveTypeDTOS);
//        return result.stream()
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AppDriveTypeDTO::getName))),
//                        ArrayList::new));
        //调用封装的筛选方法
        return chooseDriveType(data, appDriveTypeDTOS);
    }

    /**
     * 根据appId获取app应用名称
     *
     * @param id
     * @return
     */
    @Override
    public AppRegistrationDTO getAppNameById(Long id) {
        QueryWrapper<AppRegistrationPO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).select("app_name");
        AppRegistrationPO one = getOne(wrapper);
        return AppRegistrationMap.INSTANCES.poToDto(one);
    }

    /**
     * 根据应用名称获取单个应用详情
     *
     * @param id
     * @return
     */
    @Override
    public AppRegistrationDTO getAppById(Long id) {
        QueryWrapper<AppRegistrationPO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        AppRegistrationPO one = getOne(wrapper);
        return AppRegistrationMap.INSTANCES.poToDto(one);
    }

    /**
     * 根据应用名称获取单个应用详情
     *
     * @param appName
     * @return
     */
    @Override
    public AppRegistrationDTO getAppByAppName(String appName) {
        QueryWrapper<AppRegistrationPO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AppRegistrationPO::getAppName, appName);
        AppRegistrationPO one = getOne(wrapper);
        return AppRegistrationMap.INSTANCES.poToDto(one);
    }

    /**
     * 数据接入--应用级别修改应用下的接口是否允许推送数据
     *
     * @param appId
     * @return
     */
    @Override
    public Object appIfAllowDataTransfer(Long appId) {

        // 1.1非空判断
        AppRegistrationPO model = this.getById(appId);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        Integer mark = model.ifAllowDatatransfer;
        if (mark == null) {
            mark = 1;
        } else {
            if (mark == 0) {
                mark = 1;
            } else {
                mark = 0;
            }
        }

        model.setIfAllowDatatransfer(mark);
        return updateById(model);
    }

    /**
     * hudi入仓配置 -- 新增表时 获取表名
     *
     * @param dbId
     * @return
     */
    @Override
    public List<TablePyhNameDTO> getHudiConfigFromDb(Integer dbId) {

        log.info("hudi入仓配置 -- 新增表时 获取表名");
        ResultEntity<DataSourceDTO> datasource = userClient.getFiDataDataSourceById(dbId);
        DataSourceDTO dto = datasource.getData();
        if (dto == null) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        Connection conn = null;

        //获取到所有表名
        List<TablePyhNameDTO> tableNames = new ArrayList<>();

        log.info("引用的数据源类型" + dto.conType);
        try {
            switch (dto.conType) {
                case MYSQL:
                    MysqlConUtils mysqlConUtils = new MysqlConUtils();
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tableNames = mysqlConUtils.getTableNameAndColumns(conn);
                    break;
                case SQLSERVER:
                    SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tableNames = sqlServerPlusUtils.getTableNameAndColumnsPlus(conn, dto.conDbname);
                    break;
                case ORACLE:
                    OracleUtils oracleUtils = new OracleUtils();
                    log.info("ORACLE驱动开始加载");
                    log.info("ORACLE驱动基本信息：" + com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                    Class.forName(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE.getDriverName());
                    log.info("ORACLE驱动加载完毕");
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    tableNames = oracleUtils.getTableNameList(conn, dto.conDbname);
                    break;
                default:
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
            }
        } catch (Exception e) {
            log.error("hudi入仓配置 -- 新增表时 获取表名：" + e);
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return tableNames;
    }

    /**
     * hudi入仓配置 -- 配置单张表
     *
     * @param dto
     * @return
     */
    @Override
    public Object syncOneTblForHudi(SyncOneTblForHudiDTO dto) {
        log.info("hudi 入仓配置 - 开始同步所有表-------------------------------");
        try {
            long appId = dto.getAppId();
            List<AppDataSourceDTO> appSourcesByAppId = appDataSourceImpl.getAppSourcesByAppId(appId);
            //获取来源数据源id
            Integer systemDataSourceId = appSourcesByAppId.get(0).getSystemDataSourceId();
            int appDatasourceId = Math.toIntExact(appSourcesByAppId.get(0).getId());
            //获取来源数据源id
            //hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
            List<String> tblNames = dto.getTblNames();
            for (String tblName : tblNames) {
                hudiSyncOneTableToFidataConfig(systemDataSourceId, appDatasourceId, appId, dto.getAppName(), tblName);
            }
        } catch (Exception e) {
            log.error("hudi 入仓配置 - 开始同步所有表失败：" + e);
            throw new FkException(ResultEnum.ERROR, e);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 获取cdc类型所有应用及表名
     *
     * @return
     */
    @Override
    public List<CDCAppNameAndTableVO> getCDCAppNameAndTables(Integer appId) {
        return baseMapper.getCDCAppNameAndTables(appId);
    }

    @Override
    public List<CDCAppNameVO> getAllCDCAppName() {
        List<AppRegistrationPO> allCDCAppName = baseMapper.getAllCDCAppName();
        List<CDCAppNameVO> cdcAppNameVOS = allCDCAppName.stream().map(i -> {
            CDCAppNameVO cdcAppNameVO = new CDCAppNameVO();
            cdcAppNameVO.setId((int) i.getId());
            cdcAppNameVO.setAppName(i.getAppName());
            return cdcAppNameVO;
        }).collect(Collectors.toList());
        return cdcAppNameVOS;
    }

    /**
     * 获取数据接入所有应用和应用下的所有物理表
     *
     * @return
     */
    @Override
    public List<AccessAndModelAppDTO> getAllAppAndTables() {
        //先查询所有非入仓配置的应用
        QueryWrapper<AppRegistrationPO> w = new QueryWrapper<>();
        w.select("id", "app_name")
                .lambda()
                .isNull(AppRegistrationPO::getIfSyncAllTables);
        List<AppRegistrationPO> appPOS = this.list(w);
        List<AccessAndModelAppDTO> appList = new ArrayList<>();

        for (AppRegistrationPO appRegistrationPO : appPOS) {
            AccessAndModelAppDTO accessAndModelAppDTO = new AccessAndModelAppDTO();
            accessAndModelAppDTO.setAppId((int) appRegistrationPO.getId());
            accessAndModelAppDTO.setAppName(appRegistrationPO.getAppName());
            accessAndModelAppDTO.setServerType(ServerTypeEnum.ACCESS.getValue());

            List<AccessAndModelTableDTO> accessAndModelTableDTOS = new ArrayList<>();

            //获取获取应用下的所有物理表
            LambdaQueryWrapper<TableAccessPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.select(TableAccessPO::getTableName, TableAccessPO::getId, TableAccessPO::getDisplayName)
                    .eq(TableAccessPO::getAppId, appRegistrationPO.getId());
            List<TableAccessPO> tableAccessPOS = tableAccessImpl.list(wrapper1);
            for (TableAccessPO dimensionPO : tableAccessPOS) {
                AccessAndModelTableDTO dimTable = new AccessAndModelTableDTO();
                dimTable.setTblId((int) dimensionPO.getId());
                dimTable.setTableName(dimensionPO.getTableName());
                dimTable.setDisplayTableName(dimensionPO.getDisplayName());
                dimTable.setTableType(AccessAndModelTableTypeEnum.PHYSICS.getValue());
                accessAndModelTableDTOS.add(dimTable);
            }

            accessAndModelAppDTO.setTables(accessAndModelTableDTOS);
            appList.add(accessAndModelAppDTO);
        }

        return appList;
    }

    /**
     * 通过物理表id获取应用详情
     *
     * @param tblId
     * @return
     */
    @Override
    public AppRegistrationDTO getAppByTableAccessId(Integer tblId) {
        TableAccessDTO tableAccess = tableAccessImpl.getTableAccess(tblId);
        LambdaQueryWrapper<AppRegistrationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppRegistrationPO::getId, tableAccess.getAppId());
        AppRegistrationPO one = this.getOne(wrapper);
        return AppRegistrationMap.INSTANCES.poToDto(one);
    }

    /**
     * 封装筛选方法：要么是四种数据库类型，要么只能是restfulapi,api,sftp,ftp中单一的一种
     *
     * @param transFormData
     * @param originDriveType
     * @return
     */
    public List<AppDriveTypeDTO> chooseDriveType(List<AppDataSourcePO> transFormData, List<AppDriveTypeDTO> originDriveType) {
        //新建集合预装载合格驱动类型
        List<AppDriveTypeDTO> result = new ArrayList<>();
        transFormData.stream().anyMatch(e -> {
            String driveType = e.driveType;
            if (AppDriveTypeEnum.MYSQL.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.SQLSERVER.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.ORACLE.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(driveType) || AppDriveTypeEnum.OPENEDGE.getName().equalsIgnoreCase(driveType)) {
                originDriveType.forEach(d -> {
                    if (AppDriveTypeEnum.MYSQL.getName().equalsIgnoreCase(d.name) || AppDriveTypeEnum.SQLSERVER.getName().equalsIgnoreCase(d.name) || AppDriveTypeEnum.ORACLE.getName().equalsIgnoreCase(d.name) || AppDriveTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(d.name) || AppDriveTypeEnum.OPENEDGE.getName().equals(d.name)) {
                        result.add(d);
                    }
                });
                return true;
            } else {
                originDriveType.forEach(f -> {
                    if (driveType.equalsIgnoreCase(f.name)) {
                        result.add(f);
                    }
                });
            }
            return false;
        });
        return result;
    }

    /**
     * 获取实例元数据
     *
     * @param app
     * @return
     */
    public MetaDataInstanceAttributeDTO getMetaDataInstance(AppRegistrationPO app) {
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(app.targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        String rdbmsType = dataSourceConfig.data.conType.getName();
        String platform = dataSourceConfig.data.platform;
        String hostname = dataSourceConfig.data.conIp;
        String port = dataSourceConfig.data.conPort.toString();
        String protocol = dataSourceConfig.data.protocol;
        String dbName = dataSourceConfig.data.conDbname;
        // 实例
        MetaDataInstanceAttributeDTO instance = new MetaDataInstanceAttributeDTO();
        instance.setRdbms_type(rdbmsType);
        instance.setPlatform(platform);
        instance.setHostname(hostname);
        instance.setPort(port);
        instance.setProtocol(protocol);
        instance.setQualifiedName(hostname);
        instance.setName(hostname);
        instance.setContact_info(app.getAppPrincipal());
        instance.setDescription(app.getAppDes());
        instance.setComment(app.getAppDes());
        instance.setOwner(app.createUser);
        instance.setDisplayName(hostname);

        // 库
        List<MetaDataDbAttributeDTO> dbList = new ArrayList<>();
        MetaDataDbAttributeDTO db = new MetaDataDbAttributeDTO();
        db.setQualifiedName(hostname + "_" + dbName);
        db.setName(dbName);
        db.setContact_info(app.getAppPrincipal());
        db.setDescription(app.getAppDes());
        db.setComment(app.getAppDes());
        db.setOwner(app.createUser);
        db.setDisplayName(dbName);

        dbList.add(db);
        instance.setDbList(dbList);

        return instance;
    }

    /**
     * 获取应用下所有表元数据
     *
     * @param app
     * @param accessId
     * @param qualifiedName
     * @return
     */
    public List<MetaDataTableAttributeDTO> getAccessTableMetaData(AppRegistrationPO app, long accessId, String qualifiedName) {

        TableAccessPO tableAccess = tableAccessImpl.query().eq("id", accessId).one();
        if (tableAccess == null) {
            return new ArrayList<>();
        }

        // 表
        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
        table.setQualifiedName(qualifiedName + "_" + tableAccess.getId());
        table.setName(TableNameGenerateUtils.buildOdsTableName(tableAccess.getTableName(), app.appAbbreviation, app.whetherSchema));
        table.setContact_info(app.getAppPrincipal());
        table.setDescription(tableAccess.getTableDes());
        table.setComment(String.valueOf(app.getId()));
        table.setDisplayName(tableAccess.displayName);
        table.setOwner(app.createUser);

        // 字段
        List<MetaDataColumnAttributeDTO> columnList = tableFieldsImpl.query().eq("table_access_id", tableAccess.id).list().stream().filter(Objects::nonNull).map(e -> {
            MetaDataColumnAttributeDTO field = new MetaDataColumnAttributeDTO();
            field.setQualifiedName(table.qualifiedName + "_" + e.getId());
            field.setName(e.getFieldName());
            field.setContact_info(app.getAppPrincipal());
            field.setDescription(e.getFieldDes());
            field.setComment(e.getDisplayName());
            field.setDataType(e.fieldType);
            field.setDisplayName(e.displayName);
            field.setOwner(table.owner);
            field.setLength(String.valueOf(e.fieldLength));
            return field;
        }).collect(Collectors.toList());

        table.setColumnList(columnList);
        tableList.add(table);

        return tableList;
    }

    /**
     * 通过表名（带架构）获取表信息
     *
     * @param schemaName
     * @return
     */
    public AppRegistrationPO getAppBySchemaName(String schemaName) {
        LambdaQueryWrapper<AppRegistrationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppRegistrationPO::getAppAbbreviation, schemaName);
        return getOne(wrapper);
    }


}
