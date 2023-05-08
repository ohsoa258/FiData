package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.enums.flink.UploadWayEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndTask.FactoryCodePreviewSqlHelper;
import com.fisk.common.service.accessAndTask.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.accessAndTask.factorycodepreviewdto.PublishFieldDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.flinkupload.FlinkFactoryHelper;
import com.fisk.common.service.flinkupload.IFlinkJobUpload;
import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.access.OperateMsgDTO;
import com.fisk.dataaccess.dto.access.OperateTableDTO;
import com.fisk.dataaccess.dto.access.OverlayCodePreviewAccessDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewPageDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.dto.factorycodepreviewdto.AccessOverlayCodePreviewDTO;
import com.fisk.dataaccess.dto.factorycodepreviewdto.AccessPublishFieldDTO;
import com.fisk.dataaccess.dto.flink.FlinkConfigDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;
import com.fisk.dataaccess.dto.table.*;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.map.FlinkParameterMap;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.map.TableSyncModeMap;
import com.fisk.dataaccess.map.codepreview.AccessCodePreviewMapper;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.*;
import com.fisk.dataaccess.service.strategy.BuildSqlStrategy;
import com.fisk.dataaccess.utils.files.FileTxtUtils;
import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import com.fisk.dataaccess.utils.sql.OracleCdcUtils;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.metadatafield.MetaDataFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.POSTGRESQL;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableFieldsImpl
        extends ServiceImpl<TableFieldsMapper, TableFieldsPO>
        implements ITableFields {

    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private ITableAccess iTableAccess;
    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private TableAccessMapper tableAccessMapper;
    @Resource
    private AppRegistrationMapper appRegistrationMapper;
    @Resource
    private AppDataSourceMapper appDataSourceMapper;
    @Resource
    private IAppRegistration iAppRegistration;
    @Resource
    private TableBusinessImpl businessImpl;
    @Resource
    private TableSyncmodeImpl syncmodeImpl;
    @Resource
    private AppDataSourceImpl dataSourceImpl;
    @Resource
    SystemVariablesImpl systemVariables;
    @Resource
    SavepointHistoryImpl savepointHistory;
    @Resource
    AppRegistrationImpl appRegistration;
    @Resource
    FlinkApiImpl flinkApi;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private DataManageClient dataManageClient;
    @Resource
    private UserClient userClient;

    @Resource
    FlinkConfigDTO flinkConfig;

    @Value("${spring.open-metadata}")
    private Boolean openMetadata;


    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    ITableHistory iTableHistory;

    private static Integer fetchSize = 10000;
    private static Integer maxRowsPerFlowFile = 10000;

    @Override
    public Page<DataReviewVO> listData(DataReviewQueryDTO query) {

        StringBuilder querySql = new StringBuilder();
        querySql.append(generateCondition.getCondition(query.dto));
        DataReviewPageDTO data = new DataReviewPageDTO();
        data.page = query.page;
        data.where = querySql.toString();
        return baseMapper.filter(query.page, data);
    }

    @Override
    public TableFieldsDTO getTableField(int id) {
        //name 类别  简称
        TableFieldsPO po = baseMapper.selectById(id);
        TableAccessNonDTO data = iTableAccess.getData(po.tableAccessId);
        AppRegistrationDTO data1 = iAppRegistration.getData(data.appId);
        TableFieldsDTO tableFieldsDTO = new TableFieldsDTO();
        tableFieldsDTO.appbAbreviation = data1.appAbbreviation;
        tableFieldsDTO.fieldName = po.fieldName;
        tableFieldsDTO.fieldType = po.fieldType;
        tableFieldsDTO.originalTableName = data.tableName;
        return tableFieldsDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addData(TableAccessNonDTO dto) {
        //获取物理表字段集合
        List<TableFieldsDTO> listDto = dto.list;
        //获取物理表同步方式
        TableSyncmodeDTO syncmodeDto = dto.tableSyncmodeDTO;
        //获取业务时间覆盖方式选择的 时间逻辑
        TableBusinessDTO businessDto = dto.businessDTO;
        if (CollectionUtils.isEmpty(listDto) || syncmodeDto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // list: dto -> po   字段集合转为po
        List<TableFieldsPO> listPo = TableFieldsMap.INSTANCES.listDtoToPo(listDto);

        // 字段名称不可重复(前端没有提示功能,后端强制去重)
        List<TableFieldsPO> distinctFields = listPo.stream().filter(RegexUtils.distinctByKey(po -> po.fieldName)).collect(Collectors.toList());

        boolean success;
        // 批量添加tb_table_fields
        success = this.saveBatch(distinctFields);
        if (!success) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //如果是业务时间覆盖方式
        int businessMode = 4;
        if (syncmodeDto.syncMode == businessMode && businessDto != null) {
            //保存所选的业务逻辑(时间逻辑)
            success = businessImpl.save(TableBusinessMap.INSTANCES.dtoToPo(businessDto));
            if (!success) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        //添加tb_table_syncmode
        //获取物理表id
        Long tableAccessId = distinctFields.get(0).tableAccessId;
        //同步方式dto -> po
        TableSyncmodePO syncmodePo = syncmodeDto.toEntity(TableSyncmodePO.class);
        //将该物理表id赋予给 它本身待存储的同步方式
        syncmodePo.id = tableAccessId;
        //执行更新或插入
        success = syncmodeImpl.saveOrUpdate(syncmodePo);

        //通过物理表id,获取物理表
        TableAccessPO accessPo = tableAccessImpl.query().eq("id", tableAccessId).one();
        if (accessPo == null) {
            return ResultEnum.TABLE_NOT_EXIST;
        }
        //excel sheet页名称
        accessPo.sheet = dto.sheet;
        //excel中指定sheet开始读数据的起始行
        accessPo.startLine = dto.startLine;
        //stgToOds覆盖SQL语句脚本
        accessPo.coverScript = dto.coverScript;
        //获取数据的sql脚本
        accessPo.sqlScript = dto.sqlScript;
        //判断where条件是否传递
        int syncType = dto.tableSyncmodeDTO.syncMode;
        log.info("syncType类型，{}", syncType);
        if (syncType == SyncModeEnum.CUSTOM_OVERRIDE.getValue()) {
            // 获取脚本
            String whereScript = (String) previewCoverCondition(dto.businessDTO);
            log.info("where条件数据{}", whereScript);
            if (!whereScript.contains("WHERE")) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR, "获取业务时间覆盖where条件失败");
            }
            accessPo.whereScript = whereScript;
        }
        log.info("业务时间覆盖where条件语句, {}", accessPo.whereScript);
        //更新物理表自身的信息
        tableAccessImpl.updateById(accessPo);

        //系统变量
        if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(tableAccessId, dto.deltaTimes);
        }

        // 版本语句
        String versionSql = getVersionSql(syncmodePo);

        if (openMetadata) {
            //新增元数据信息
            odsMetaDataInfo(dto.appDataSourceId, dto.sqlScript);
        }

        // 发布
        publish(success, accessPo.appId, accessPo.id, accessPo.tableName, dto.flag, dto.openTransmission, null, false, dto.deltaTimes, versionSql, dto.tableSyncmodeDTO, dto.appDataSourceId, dto.tableHistorys, null);

        return success ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateData(TableAccessNonDTO dto) {

        // 字段名称不可重复(前端没有提示功能,后端强制去重)
        List<TableFieldsDTO> list = dto.list.stream().filter(RegexUtils.distinctByKey(po -> po.fieldName)).collect(Collectors.toList());

        List<TableFieldsPO> originalDataList = list(Wrappers.<TableFieldsPO>lambdaQuery()
                .eq(TableFieldsPO::getTableAccessId, list.get(0).tableAccessId)
                .select(TableFieldsPO::getId));

        TableAccessPO model = tableAccessImpl.getById(dto.id);

        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        if (CollectionUtils.isEmpty(dto.list) || tableSyncmodeDTO == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 保存tb_table_fields
        boolean success;

        success = this.saveOrUpdateBatch(TableFieldsMap.INSTANCES.listDtoToPo(list));
        if (!success) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 删除字段
        List<TableFieldsPO> webData = TableFieldsMap.INSTANCES.listDtoToPo(list);
        List<TableFieldsPO> webData1 = webData.stream().filter(e -> StringUtils.isNotEmpty(String.valueOf(e.id))).collect(Collectors.toList());
        List<TableFieldsPO> webDataList = webData1.stream().map(e -> e.id).collect(Collectors.toList()).stream()
                .map(e -> {
                    TableFieldsPO fieldsPo = new TableFieldsPO();
                    fieldsPo.setId(e);
                    return fieldsPo;
                }).collect(Collectors.toList());
        List<TableFieldsPO> collect = originalDataList.stream().filter(item -> !webDataList.contains(item)).collect(Collectors.toList());
        System.out.println("collect = " + collect);
        try {
            collect.stream().map(e -> baseMapper.deleteByIdWithFill(e)).collect(Collectors.toList());
        } catch (Exception e) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        TableBusinessDTO businessDto = dto.businessDTO;
        // 保存tb_table_business
        int businessMode = 4;
        if (tableSyncmodeDTO.syncMode == businessMode && businessDto != null) {

            TableBusinessPO businessPo = TableBusinessMap.INSTANCES.dtoToPo(businessDto);
            success = businessImpl.saveOrUpdate(businessPo);
            if (!success) {
                return ResultEnum.UPDATE_DATA_ERROR;
            }
        }
        // 保存tb_table_syncmode
        TableSyncmodePO modelSync = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);

        // 版本语句。位置不要调整，在保存之前与历史数据做对比
        String versionSql = getVersionSql(modelSync);

        success = syncmodeImpl.saveOrUpdate(modelSync);

        // 修改发布状态
        model.publish = 0;
        model.sheet = dto.sheet;
        model.startLine = dto.startLine;
        //stgToOds覆盖SQL语句脚本
        model.coverScript = dto.coverScript;
        //获取数据的sql脚本
        model.sqlScript = dto.sqlScript;

        // 判断where条件是否传递
        int syncType = dto.tableSyncmodeDTO.syncMode;
        log.info("syncType类型，{}", syncType);
        if (syncType == SyncModeEnum.CUSTOM_OVERRIDE.getValue()) {
            // 获取脚本
            String whereScript = (String) previewCoverCondition(dto.businessDTO);
            log.info("where条件数据{}", whereScript);
            if (!whereScript.contains("WHERE")) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR, "获取业务时间覆盖where条件失败");
            }
            model.whereScript = whereScript;
        } else {
            model.whereScript = "";
        }
        log.info("业务时间覆盖where条件语句, {}", model.whereScript);
        tableAccessImpl.updateById(model);

        //系统变量
        if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.id, dto.deltaTimes);
        }

        if (openMetadata) {
            //新增元数据信息
            odsMetaDataInfo(model.appDataSourceId, dto.sqlScript);
        }

        // 发布
        publish(success, model.appId, model.id, model.tableName, dto.flag, dto.openTransmission, null,
                false, dto.deltaTimes, versionSql, dto.tableSyncmodeDTO, model.appDataSourceId, dto.tableHistorys, null);

        return success ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    /**
     * 新增ods元数据信息
     *
     * @param appId
     * @param sql
     */
    public void odsMetaDataInfo(long appId, String sql) {

        if (StringUtils.isEmpty(sql)) {
            return;
        }

        AppDataSourcePO po = appDataSourceMapper.selectById(appId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        AppRegistrationPO registrationPO = appRegistrationMapper.selectById(po.appId);
        if (registrationPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        List<MetaDataInstanceAttributeDTO> list = appRegistration.addDataSourceMetaData(registrationPO, po);
        //解析sql
        List<TableMetaDataObject> res = SqlParserUtils.sqlDriveConversionName(null, po.driveType, sql);
        if (CollectionUtils.isEmpty(res)) {
            return;
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();
        for (TableMetaDataObject item : res) {
            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.setQualifiedName(list.get(0).dbList.get(0).qualifiedName + "_" + item.name);
            table.setName(item.name);
            table.setComment(String.valueOf(appId));
            table.setDisplayName(item.name);
            table.setDescription("stg");
            tableList.add(table);
        }
        //list.get(0).description = "stg";
        list.get(0).dbList.get(0).tableList = tableList;

        log.info("构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
        dataManageClient.consumeMetaData(list);

        /*//修改元数据
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 更新元数据内容
                    log.info("维度表构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
                    dataManageClient.consumeMetaData(list);
                } catch (Exception e) {
                    log.error("【dataManageClient.MetaData()】方法报错,ex", e);
                }
            }
        });*/

    }

    @Override
    public OperateMsgDTO loadDepend(OperateTableDTO dto) {
        OperateMsgDTO operateMsgDTO = new OperateMsgDTO();
        try {
            LoadDependDTO loadDependDTO = new LoadDependDTO();

            loadDependDTO.channelDataEnum = ChannelDataEnum.DATALAKE_TASK;
            loadDependDTO.tableId = dto.tableId;
            loadDependDTO.appId = dto.appId;
            // 判断数据工厂是否有依赖
            boolean loadDepend = dataFactoryClient.loadDepend(loadDependDTO);
            if (loadDepend) {
                switch (dto.operateBehaveTypeEnum) {
                    case UPDATE_APP:
                    case DELETE_APP:
                        operateMsgDTO.operateBehaveMsg = "管道正在使用该应用中的表,请及时更新管道";
                        break;
                    case UPDATE_TABLE:
                    case DELETE_TABLE:
                        operateMsgDTO.operateBehaveMsg = "管道正在使用该表,请及时更新管道";
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new FkException(ResultEnum.DATA_FACTORY_FEIGN_EXCEPTION);
        }
        return operateMsgDTO;
    }

    @Override
    public Object previewCoverCondition(TableBusinessDTO dto) {

        TableAccessPO tableAccess = tableAccessImpl.getById(dto.accessId);
        if (tableAccess == null) {
            throw new FkException(ResultEnum.TASK_TABLE_NOT_EXIST);
        }

        AppRegistrationPO registration = appRegistration.getById(tableAccess.appId);
        if (registration == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        ResultEntity<DataSourceDTO> dataSource = userClient.getFiDataDataSourceById(registration.targetDbId);
        if (dataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }
        //数据库时间
        Integer businessDate = 0;

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(dataSource.data.conType);

        //高级模式
        if (dto.otherLogic != 1) {
            //查询数据库时间sql
            String timeSql = command.buildQueryTimeSql(BusinessTimeEnum.getValue(dto.businessTimeFlag));

            AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
            Connection connection = commonDbHelper.connection(dataSource.data.conStr, dataSource.data.conAccount, dataSource.data.conPassword, dataSource.data.conType);
            businessDate = Integer.parseInt(AbstractCommonDbHelper.executeTotalSql(timeSql, connection, "tmp"));
        }


        String str = command.buildBusinessCoverCondition(TableFieldsMap.INSTANCES.businessTime(dto), businessDate);
        log.info("预览业务时间覆盖,where条件:{}", str);

        return str.toUpperCase();
    }

    /**
     * 调用发布和存储过程
     *
     * @param success          true: 保存成功,执行发布
     * @param appId            应用id
     * @param accessId         物理表id
     * @param tableName        物理表tableName
     * @param flag             0: 保存;  1: 发布
     * @param openTransmission 发布时,是否立即同步数据
     * @param deltaTimes       接入的增量时间参数
     * @param versionSql       版本sql
     * @param syncMode         同步方式
     * @param appDataSourceId  应用数据源id
     * @param dto              发布历史
     * @param currUserName     用户名
     */
    private void publish(boolean success,
                         long appId,
                         long accessId,
                         String tableName,
                         int flag,
                         boolean openTransmission,
                         CdcJobScriptDTO cdcDto,
                         boolean useExistTable,
                         List<DeltaTimeDTO> deltaTimes,
                         String versionSql,
                         TableSyncmodeDTO syncMode,
                         Integer appDataSourceId,
                         List<TableHistoryDTO> dto,
                         String currUserName) {
        //获取应用数据源
        AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("id", appDataSourceId).one();
        //获取不到则抛出异常
        if (dataSourcePo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //获取应用信息
        AppRegistrationPO appRegistrationPo = appRegistration.query().eq("id", appId).one();
        //获取不到则抛出异常
        if (appRegistrationPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        Long tableHistoryId = 0L;
        //如果发布历史不为空
        if (!CollectionUtils.isEmpty(dto)) {
            log.info("开始记录发布日志");
            for (TableHistoryDTO tableHistory : dto) {
                if (tableHistory.tableId.longValue() == accessId) {
                    log.info("记录发布日志的表id:{}", accessId);
                    List<TableHistoryDTO> list = new ArrayList<>();
                    list.add(tableHistory);
                    tableHistoryId = iTableHistory.addTableHistory(list);
                }
            }
        }

        if (success && flag == 1 && !useExistTable) {
            UserInfo userInfo = userHelper.getLoginUserInfo();

            //调用方法：封装参数给nifi
            ResultEntity<BuildPhysicalTableDTO> result = tableAccessImpl.getBuildPhysicalTableDTO(accessId, appDataSourceId);

            BuildPhysicalTableDTO data = result.data;
            //装载应用id
            data.appId = String.valueOf(appId);
            //数据库id为什么传物理表id???????????
            data.dbId = String.valueOf(accessId);
            //用户id
            data.userId = userInfo.id;
            //是否发布
            data.openTransmission = openTransmission;
            //接入的增量时间参数
            data.deltaTimes = deltaTimes;

            //来源和目标数据源id
            data.dataSourceDbId = dataSourcePo.systemDataSourceId;
            //目标ods数据源id
            data.targetDbId = appRegistrationPo.targetDbId;

            // 拼接删除ods的sql
            String tbName = TableNameGenerateUtils.buildOdsTableName(data.tableName, appRegistrationPo.appAbbreviation, appRegistrationPo.whetherSchema);

            // pg库则将表名转换为小写
            //调用方法，获取数据源
            boolean typeFlag = getTargetDbType(data.targetDbId);
            //如果可以获取到
            if (typeFlag) {
                //表名小写
                data.tableName = data.tableName.toLowerCase();
                // 将字段集合转换为小写
                List<TableFieldsDTO> tableFieldsDTOList = data.tableFieldsDTOS;
                data.tableFieldsDTOS = tableFieldsDTOList.stream().map(item -> {
                    item.fieldName = item.fieldName.toLowerCase();
                    return item;
                }).collect(Collectors.toList());
                tbName = tbName.toLowerCase();
            }
            //同步方式
            int syncType = syncMode.syncMode;
            log.info("syncType类型，{}，判断拼接删除ods的sql", syncType);

            if (syncType == SyncModeEnum.CUSTOM_OVERRIDE.getValue()) {
                data.whereScript = "DELETE FROM " + tbName + " " + data.whereScript;
                log.info("删除ods表的sql，{}", data.whereScript);
            }

            // 版本号入库、调用存储存储过程
            //获取物理表字段集合
            List<TableFieldsPO> list = this.query().eq("table_access_id", accessId).list();
            //获取应用信息
            AppRegistrationPO registration = iAppRegistration.getById(appId);

            //拼接ods表名
            String odsTableName = TableNameGenerateUtils.buildOdsTableName(tableName, registration.appAbbreviation, registration.whetherSchema);

            //调用方法 封装版本号和修改表结构的参数
            data.modelPublishTableDTO = getModelPublishTableDTO(accessId, odsTableName, 3, list);
            //是否使用应用简称
            data.whetherSchema = registration.whetherSchema;
            //版本sql
            data.generateVersionSql = versionSql;

            data.maxRowsPerFlowFile = syncMode.maxRowsPerFlowFile == null ? maxRowsPerFlowFile : syncMode.maxRowsPerFlowFile;
            data.fetchSize = syncMode.fetchSize == null ? fetchSize : syncMode.fetchSize;
            data.sftpFlow = DataSourceTypeEnum.SFTP.getName().equals(dataSourcePo.driveType);
            data.tableHistoryId = tableHistoryId;

            // 执行发布
            try {
                //获取指定id的物理表信息
                TableAccessPO accessPo = tableAccessImpl.query().eq("id", accessId).one();
                data.sheetName = accessPo.sheet;
                //覆盖sql脚本
                data.syncStgToOdsSql = accessPo.coverScript;
                data.coverScript = accessPo.coverScript;
                //todo:当Keep_number 配置天数后，这里保存删除stg表的数据的脚本语句 默认5day
                data.deleteStgScript = accessPo.deleteStgScript;

                List<MetaDataInstanceAttributeDTO> metaDataList = new ArrayList<>();
                // 实时--RestfulAPI类型  or  非实时--api类型
                //0实时
                if ((registration.appType == 0
                        && DataSourceTypeEnum.RestfulAPI.getName().equals(dataSourcePo.driveType))
                        || (registration.appType == 1
                        && DataSourceTypeEnum.API.getName().equals(dataSourcePo.driveType))) {
                    // 传入apiId和api下所有表
                    List<TableAccessPO> tablePoList = tableAccessImpl.query().eq("api_id", accessPo.apiId).list();
                    // api下所有表
                    data.apiTableNames = tablePoList.stream().map(e -> e.tableName).collect(Collectors.toList());
                    data.appType = registration.appType;
                    data.apiId = accessPo.apiId;
                    // 创建表流程
                    publishTaskClient.publishBuildPhysicsTableTask(data);
                    // 构建元数据实时同步数据对象
                    metaDataList = buildMetaDataInstanceAttribute(registration, accessId, 1, currUserName);
                } else if (registration.appType == 1) {
                    if (DataSourceTypeEnum.FTP.getName().equals(dataSourcePo.driveType) || data.sftpFlow) {
                        data.excelFlow = true;
                    }
                    // 非实时物理表发布
                    // 创建表流程
                    data.popout = true;
                    publishTaskClient.publishBuildPhysicsTableTask(data);
                    // 生成nifi流程
                    //log.info(JSON.toJSONString(data));
                    //publishTaskClient.publishBuildAtlasTableTask(data);
                    //构建元数据实时同步数据对象
                    metaDataList = buildMetaDataInstanceAttribute(registration, accessId, 2, currUserName);
                }
                if (openMetadata) { //
                    //同步元数据
                    consumeMetaData(metaDataList);
                }

            } catch (Exception e) {
                log.info("发布失败", e);
                log.info("发布失败,{}", ResultEnum.TASK_EXEC_FAILURE.getMsg());
                throw new FkException(ResultEnum.TASK_EXEC_FAILURE);
            }
        }

        //oracle-cdc类型需要上传脚本
        if (dataSourcePo.driveType.equalsIgnoreCase("ORACLE-CDC")) {
            cdcScriptUploadFlink(cdcDto, accessId);
        }
    }

    private boolean getTargetDbType(Integer dbId) {
        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(dbId);
        if (result.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = result.getData();
            log.info("数据源连接类型，{}, 枚举中PG类型，{}", dataSource.getConType().getValue(), POSTGRESQL.getValue());
            return POSTGRESQL.getValue() == dataSource.getConType().getValue();
        }
        return false;
    }

    /**
     * cdc脚本上传flink
     *
     * @param cdcJobScript
     * @param accessId
     */
    public void cdcScriptUploadFlink(CdcJobScriptDTO cdcJobScript,
                                     long accessId) {
        TableAccessPO accessPo = tableAccessImpl.query().eq("id", accessId).one();
        if (accessPo == null) {
            throw new FkException(ResultEnum.TASK_TABLE_NOT_EXIST);
        }
        String fileName = accessPo.pipelineName;
        //替换脚本
        Boolean exist = redisTemplate.hasKey(OracleCdcUtils.redisPrefix + ":" + accessId);
        if (!exist) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_CHECKRESULT_EXISTS);
        }
        cdcJobScript.jobScript = redisTemplate.opsForValue().get(OracleCdcUtils.redisPrefix + ":" + accessId).toString();

        //先根据job id,先停止任务
        if (!StringUtils.isEmpty(accessPo.jobId)) {
            cancelJob(accessPo.jobId, accessPo.id);
        }

        //上传文件
        FileTxtUtils.setFiles(flinkConfig.uploadPath, fileName, cdcJobScript.jobScript);
        //ssh上传,需要上传至远程服务器
        if (flinkConfig.uploadWay.getValue() == UploadWayEnum.SSH.getValue()) {
            try {
                InputStream client_fileInput = new FileInputStream(flinkConfig.uploadPath + fileName);
                FileTxtUtils.uploadFile(flinkConfig.host, flinkConfig.port, flinkConfig.user, flinkConfig.password, flinkConfig.uploadPath, fileName, client_fileInput);
            } catch (FileNotFoundException e) {
                log.error("uploadFile remote ex:", e);
                throw new FkException(ResultEnum.UPLOADFILE_REMOTE_ERROR);
            }
        }
        log.info("创建任务前jobId:" + accessPo.jobId);
        IFlinkJobUpload upload = FlinkFactoryHelper.flinkUpload(flinkConfig.uploadWay);
        flinkConfig.fileName = fileName;
        String jobId = upload.submitJob(FlinkParameterMap.INSTANCES.dtoToDto(flinkConfig));
        log.info("创建任务返回jobId:" + jobId);
        if (StringUtils.isEmpty(jobId)) {
            throw new FkException(ResultEnum.CREATE_JOB_ERROR);
        }
        accessPo.jobId = jobId;
        log.info("修改AccessPO参数:{}", JSON.toJSON(accessPo));
        tableAccessMapper.updateJobId(accessId, jobId);
    }

    /**
     * 取消job,并创建检查点
     *
     * @param jobId
     * @param accessId
     */
    public void cancelJob(String jobId, long accessId) {
        String triggerId = flinkApi.savePoints(jobId, String.valueOf(accessId));
        boolean flat = true;
        long startTime = System.currentTimeMillis();
        String savePointsPath = null;
        while (flat || (System.currentTimeMillis() - startTime) < 10000) {
            savePointsPath = flinkApi.savePointsStatus(jobId, triggerId);
            if (!StringUtils.isEmpty(savePointsPath)) {
                flat = false;
                break;
            }
        }
        if (flat) {
            throw new FkException(ResultEnum.SAVE_POINTS_UPDATE_ERROR);
        }
        //保存到检查点历史表
        SavepointHistoryDTO dto = new SavepointHistoryDTO();
        dto.savepointPath = savePointsPath;
        dto.tableAccessId = accessId;
        dto.savepointDate = LocalDateTime.now();
        savepointHistory.addSavepointHistory(dto);
    }

    /**
     * 构建元数据实时同步数据对象
     *
     * @param app      应用
     * @param accessId 表id
     * @param flag     1: api 2:table
     * @author Lock
     * @date 2022/7/5 16:51
     */
    public List<MetaDataInstanceAttributeDTO> buildMetaDataInstanceAttribute(AppRegistrationPO app, long accessId, int flag, String currUserName) {

        int apiType = 1;
        int tableType = 2;

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
        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
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
        instance.setCurrUserName(currUserName);

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

        TableAccessPO tableAccess = tableAccessImpl.query().eq("id", accessId).one();
        if (tableAccess == null) {
            return new ArrayList<>();
        }
        List<Long> userIds = new ArrayList<>();
        // 表
        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();
        if (flag == tableType) {
            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.setQualifiedName(hostname + "_" + dbName + "_" + tableAccess.getId());
            table.setName(TableNameGenerateUtils.buildOdsTableName(tableAccess.getTableName(),
                    app.appAbbreviation,
                    app.whetherSchema));
            table.setContact_info(app.getAppPrincipal());
            table.setDescription(tableAccess.getTableDes());
            table.setComment(String.valueOf(app.getId()));
            table.setDisplayName(tableAccess.displayName);
            table.setOwner(app.createUser);

            //所属人
            /*userIds.add(Long.parseLong(tableAccess.createUser));
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
                table.setOwner(userListByIds.data.get(0).getUsername());
            }*/


            // 字段
            List<MetaDataColumnAttributeDTO> columnList = this.query().eq("table_access_id", tableAccess.id).list()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(e -> {
                        MetaDataColumnAttributeDTO field = new MetaDataColumnAttributeDTO();
                        field.setQualifiedName(hostname + "_" + dbName + "_" + tableAccess.getId() + "_" + e.getId());
                        field.setName(e.getFieldName());
                        field.setContact_info(app.getAppPrincipal());
                        field.setDescription(e.getFieldDes());
                        field.setComment(e.getDisplayName());
                        field.setDataType(e.fieldType);
                        field.setDisplayName(e.displayName);
                        field.setOwner(table.owner);
                        return field;
                    }).collect(Collectors.toList());

            table.setColumnList(columnList);
            tableList.add(0, table);

            db.setTableList(tableList);
            dbList.add(db);
            instance.setDbList(dbList);
        } else if (flag == apiType) {
            tableList = tableAccessImpl.query().eq("api_id", tableAccess.apiId).list()
                    .stream().filter(Objects::nonNull)
                    .map(tb -> {
                        // 表
                        MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
                        table.setQualifiedName(hostname + "_" + dbName + "_" + tb.getId());
                        table.setName(TableNameGenerateUtils.buildOdsTableName(tb.getTableName(),
                                app.appAbbreviation,
                                app.whetherSchema));
                        table.setContact_info(app.getAppPrincipal());
                        table.setDescription(tb.getTableDes());
                        table.setComment(String.valueOf(app.getId()));
                        table.setOwner(app.appPrincipal);
                        //userIds.add(tb.id);
                        // 字段
                        List<MetaDataColumnAttributeDTO> columnList = this.query()
                                .eq("table_access_id", tb.id)
                                .list()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(e -> {
                                    MetaDataColumnAttributeDTO field = new MetaDataColumnAttributeDTO();
                                    field.setQualifiedName(hostname + "_" + dbName + "_" + tb.getId() + "_" + e.getId());
                                    field.setName(e.getFieldName());
                                    field.setContact_info(app.getAppPrincipal());
                                    field.setDescription(e.getFieldDes());
                                    field.setComment(e.getDisplayName());
                                    field.setDataType(e.fieldType);
                                    field.setDisplayName(e.displayName);
                                    field.setOwner(table.owner);
                                    return field;
                                }).collect(Collectors.toList());

                        table.setColumnList(columnList);
                        return table;
                    }).collect(Collectors.toList());
            /*ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
                tableList.stream().forEach(e -> {
                    Optional<UserDTO> first = userListByIds.data.stream().filter(p -> p.id == Long.parseLong(e.owner)).findFirst();
                    if (first.isPresent()) {
                        e.setOwner(first.get().getUsername());
                        e.columnList.stream().map(p -> p.owner = e.owner).collect(Collectors.toList());
                    }
                });
            }*/
            db.setTableList(tableList);
            dbList.add(db);
            instance.setDbList(dbList);
        }

        list.add(instance);

        return list;
    }

    /**
     * 调用元数据
     *
     * @param list
     */
    public void consumeMetaData(List<MetaDataInstanceAttributeDTO> list) {
        log.info("构建元数据实时同步数据对象开始.........:  参数为: {}", JSON.toJSONString(list));
        dataManageClient.consumeMetaData(list);
        /*ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("构建元数据实时同步数据对象开始.........:  参数为: {}", JSON.toJSONString(list));
                    dataManageClient.consumeMetaData(list);
                } catch (Exception e) {
                    // 不同场景下，元数据可能不会部署，在这里只做日志记录，不影响正常流程
                    log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
                }
            }
        });*/
    }

    /**
     * 封装版本号和修改表结构的参数
     *
     * @param accessId     物理表名
     * @param odsTableName ods表名
     * @param createType   3: 代表数据接入
     * @param list         表字段
     * @return dto
     */
    private ModelPublishTableDTO getModelPublishTableDTO(long accessId, String odsTableName, int createType, List<TableFieldsPO> list) {
        ModelPublishTableDTO dto = new ModelPublishTableDTO();
        List<ModelPublishFieldDTO> fieldList = new ArrayList<>();

        list.forEach(po -> {
            ModelPublishFieldDTO fieldDTO = new ModelPublishFieldDTO();
            fieldDTO.fieldId = po.id;
            fieldDTO.fieldEnName = po.fieldName;
            fieldDTO.fieldType = po.fieldType;
            fieldDTO.fieldLength = po.fieldLength == null ? 0 : Math.toIntExact(po.fieldLength);
            fieldDTO.isPrimaryKey = po.isPrimarykey;
            fieldDTO.fieldPrecision = po.fieldPrecision;
            fieldList.add(fieldDTO);
        });

        dto.createType = createType;
        dto.tableId = accessId;
        dto.tableName = odsTableName;
        dto.fieldList = fieldList;
        return dto;
    }

    /**
     * @return java.lang.String
     * @description 创建生成版本SQL
     * @author dick
     * @date 2022/11/1 17:02
     * @version v1.0
     * @params dto
     */
    public String getVersionSql(TableSyncmodePO tableSyncmodeDto) {
        String versionSql = "";
        Connection conn = null;
        AbstractCommonDbHelper dbHelper = null;
        try {
            if (tableSyncmodeDto == null || tableSyncmodeDto.getId() == 0) {
                log.info("【getVersionSql】参数为空异常");
                return versionSql;
            }
            long tableId = tableSyncmodeDto.getId();
            // 非全量模式 || 未启用版本功能 || 保留0天 || 表名称为空 || 数据源为空
            if (tableSyncmodeDto.getSyncMode() != 1 || tableSyncmodeDto.getRetainHistoryData() != 1 || tableSyncmodeDto.getRetainTime() == 0) {
                log.info("【getVersionSql】参数值异常");
                return versionSql;
            }
            TableAccessPO tableAccessPO = tableAccessImpl.getById(tableId);
            if (tableAccessPO == null || StringUtils.isEmpty(tableAccessPO.getTableName())) {
                log.info("【getVersionSql】tableAccess配置不存在");
                return versionSql;
            }
            String appId = String.valueOf(tableAccessPO.getAppId());
            AppRegistrationPO appRegistrationPO = iAppRegistration.getById(appId);
            if (appRegistrationPO == null) {
                log.info("【getVersionSql】appRegistration配置不存在");
                return versionSql;
            }
            String tableName = TableNameGenerateUtils.buildOdsTableName(tableAccessPO.getTableName(), appRegistrationPO.getAppAbbreviation(), appRegistrationPO.getWhetherSchema());
            int targetDbId = appRegistrationPO.getTargetDbId();
            ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode() || dataSourceConfig.getData() == null) {
                log.info("【getVersionSql】DataSource配置不存在");
                return versionSql;
            }
            DataSourceDTO dataSourceDTO = dataSourceConfig.getData();
            TableSyncmodePO tableSyncmodePO = tableAccessImpl.getTableSyncmode(tableSyncmodeDto.getId());
            if (tableSyncmodePO == null) {
                log.info("【getVersionSql】tableSyncmode配置不存在");
                return versionSql;
            }
            // 版本设置是否变更，如变更则需要清空ODS数据重新按照版本规则生成版本数据
            IBuildAccessSqlCommand dbCommand = BuildFactoryAccessHelper.getDBCommand(dataSourceDTO.getConType());
            if (!tableSyncmodeDto.getVersionUnit().equals(tableSyncmodePO.getVersionUnit())) {
                log.info("【getVersionSql】配置发生变更，清空表数据");
                conn = DbConnectionHelper.connection(dataSourceDTO.getConStr(), dataSourceDTO.getConAccount(), dataSourceDTO.getConPassword(), dataSourceDTO.getConType());
                // 检查表是否存在
                dbHelper = new AbstractCommonDbHelper();
                String existTableSql = dbCommand.buildExistTableSql(tableName);
                List<Map<String, Object>> maps = dbHelper.batchExecQueryResultMaps_noClose(existTableSql, conn);
                boolean isExists = !maps.get(0).get("isExists").toString().equals("0");
                if (isExists) {
                    String sql = String.format("TRUNCATE TABLE %s", tableName);
                    dbHelper.executeSql(sql, conn);
                }
            }
            versionSql = dbCommand.buildVersionSql(tableSyncmodeDto.getVersionUnit(), tableSyncmodeDto.getVersionCustomRule());
        } catch (Exception ex) {
            versionSql = "";
            log.error("【getVersionSql】触发异常：" + ex);
        } finally {
            if (conn != null) {
                dbHelper.closeConnection(conn);
            }
        }
        log.info("【getVersionSql】versionSql：" + versionSql);
        return versionSql;
    }

    /**
     * @return com.fisk.common.core.response.ResultEnum
     * @description 删除版本数据
     * @author dick
     * @date 2022/11/2 11:35
     * @version v1.0
     * @params tableId
     * @params tableType
     */
    @Override
    public ResultEnum delVersionData(String keyStr) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        Connection conn = null;
        AbstractCommonDbHelper dbHelper = null;
        String deleteSql = "";
        try {
            if (StringUtils.isEmpty(keyStr)) {
                return ResultEnum.PARAMTER_ERROR;
            }
            log.info("【delVersionData】请求参数：" + keyStr);
            String[] keyList = keyStr.split("\\.");
            if (keyList == null || keyList.length == 0) {
                return ResultEnum.VERSION_PARAMS_SPLIT_ERROR;
            }
            // tb_table_access id
            String tableId = keyList[keyList.length - 1];
            // 应用id
            String appId = keyList[keyList.length - 2];
            // 表类型 属于接入还是建模
//            String tableType = keyList[keyList.length - 3];
            // 表名称
            String tableName = "";
            // 数据源ID dmp_ods
            int targetDbId = 0;

//            OlapTableEnum tableTypeEnum = OlapTableEnum.getNameByValue(Integer.parseInt(tableType));
//            if (StringUtils.isEmpty(tableId) ||
//                    (tableTypeEnum != OlapTableEnum.PHYSICS
//                            && tableTypeEnum != OlapTableEnum.PHYSICS_API
//                            && tableTypeEnum != OlapTableEnum.PHYSICS_RESTAPI)) {
//                return ResultEnum.VERSION_TABLE_TYPE_ERROR;
//            }

            TableAccessPO tableAccessPO = tableAccessImpl.getById(tableId);
            if (tableAccessPO == null || StringUtils.isEmpty(tableAccessPO.getTableName())) {
                return ResultEnum.VERSION_TABLE_NOT_EXISTS;
            }
            appId = StringUtils.isEmpty(appId) ? String.valueOf(tableAccessPO.getAppId()) : appId;
            AppRegistrationPO appRegistrationPO = iAppRegistration.getById(appId);
            if (appRegistrationPO == null) {
                return ResultEnum.VERSION_APP_NOT_EXISTS;
            }
            tableName = TableNameGenerateUtils.buildOdsTableName(tableAccessPO.getTableName(), appRegistrationPO.getAppAbbreviation(), appRegistrationPO.getWhetherSchema());
            targetDbId = appRegistrationPO.getTargetDbId();
            log.info("【delVersionData】表名称：" + tableName);
            log.info("【delVersionData】数据源ID：" + targetDbId);

            TableSyncmodePO tableSyncmodePO = tableAccessImpl.getTableSyncmode(Long.parseLong(tableId));
            if (tableSyncmodePO == null) {
                return ResultEnum.VERSION_TABLE_SYNC_NOT_EXISTS;
            }
            if (tableSyncmodePO.getSyncMode() != 1) {
                return ResultEnum.VERSION_TABLE_SYNC_NOT_ALL;
            }

            ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode() || dataSourceConfig.getData() == null) {
                return ResultEnum.DATA_SOURCE_ERROR;
            }
            DataSourceDTO dataSourceDTO = dataSourceConfig.getData();
            conn = DbConnectionHelper.connection(dataSourceDTO.getConStr(), dataSourceDTO.getConAccount(), dataSourceDTO.getConPassword(), dataSourceDTO.getConType());
            dbHelper = new AbstractCommonDbHelper();
            IBuildAccessSqlCommand dbCommand = BuildFactoryAccessHelper.getDBCommand(dataSourceDTO.getConType());

            if (tableSyncmodePO.getRetainHistoryData() != 1) {
                log.info("【delVersionData】未启用版本设置，不加版本条件直接全量删除");
                deleteSql = String.format("TRUNCATE TABLE %s", tableName);
            } else {
                log.info("【delVersionData】启用版本设置，加版本条件删除");
                if (tableSyncmodePO.getRetainTime() == 0) {
                    return ResultEnum.VERSION_SAVE_DAY_ERROR;
                }
                String retainUnit = tableSyncmodePO.getRetainUnit();
                int retainTime = tableSyncmodePO.getRetainTime();
                String versionUnit = tableSyncmodePO.getVersionUnit();
                String versionCustomRule = tableSyncmodePO.getVersionCustomRule();

                // 日期操作函数
                Calendar calendar = Calendar.getInstance();
                deleteSql = dbCommand.buildVersionDeleteSql(tableName);
                List<String> sqlConditions = new ArrayList<>();
                int i = 1;
                // 因为版本保留需要含当月，所以当仅保留一个月时，i要等于0
                if (retainTime == 1) {
                    i = 0;
                }

                // 如果版本规则是按照自定义模式生成的，则需要根据保留规则和版本规则生成条件语句
                if (versionUnit.equals("自定义")) {
                    log.info("【delVersionData】自定义模式");
                    if (StringUtils.isEmpty(versionCustomRule)) {
                        return ResultEnum.VERSION_CUSTOM_SQL_IS_NULL;
                    }
                    versionCustomRule = String.format("SELECT (%s) AS fi_version", versionCustomRule);
                    log.info("【delVersionData】自定义模式下自定义规则：" + versionCustomRule);
                    List<Map<String, Object>> data = dbHelper.batchExecQueryResultMaps_noClose(versionCustomRule, conn);
                    Object versionObj = data.get(0).get("fi_version");
                    if (versionObj == null || versionObj == "") {
                        return ResultEnum.VERSION_CUSTOM_SQL_RESULT_IS_NULL;
                    }
                    if (retainUnit.equals("年")) {
                        // 2022、2023、2024
                        int year = Integer.parseInt(versionObj.toString());
                        while (i < retainTime) {
                            sqlConditions.add(String.format("'%s'", year - i));
                            i++;
                        }
                    } else if (retainUnit.equals("季")) {
                        // 2022/Q01、2022/Q02、2022/Q03、2022/Q04
                        String[] split = versionObj.toString().split("/");
                        String yearStr = split[0];
                        String quarterStr = split[1];
                        String dateStr = "";
                        if (quarterStr.equals("Q01")) {
                            dateStr = yearStr + "-01-01";
                        } else if (quarterStr.equals("Q02")) {
                            dateStr = yearStr + "-04-01";
                        } else if (quarterStr.equals("Q03")) {
                            dateStr = yearStr + "-07-01";
                        } else if (quarterStr.equals("Q04")) {
                            dateStr = yearStr + "-10-01";
                        }
                        String str = "yyyy-MM-dd";
                        SimpleDateFormat format = new SimpleDateFormat(str);
                        Date date = format.parse(dateStr);
                        calendar.setTime(date);
                        while (i < retainTime) {
                            calendar.add(Calendar.MONTH, -3);
                            // 注意：java取时间函数的月份时要+1，否则取的是上个月
                            int month = calendar.get(Calendar.MONTH) + 1;
                            int year = calendar.get(Calendar.YEAR);
                            int quarter = month % 3 == 0 ? month / 3 : month / 3 + 1;
                            sqlConditions.add(String.format("'%s/Q0%s'", year, quarter));
                            i++;
                        }
                    } else if (retainUnit.equals("月")) {
                        //2022/1、2022/2
                        String str = "yyyy/MM";
                        SimpleDateFormat format = new SimpleDateFormat(str);
                        Date date = format.parse(versionObj.toString());
                        calendar.setTime(date);
                        while (i < retainTime) {
                            calendar.add(Calendar.MONTH, -1);
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            sqlConditions.add(String.format("'%s/%s'", year, month));
                            i++;
                        }
                    } else if (retainUnit.equals("周")) {
                        // 2022/11/01
                        String str = "yyyy/MM/dd";
                        SimpleDateFormat format = new SimpleDateFormat(str);
                        Date date = format.parse(versionObj.toString());
                        calendar.setTime(date);
                        // pg和sqlserver获取周时不一样，因此要通过数据库查询
                        while (i < retainTime) {
                            calendar.add(Calendar.DATE, -7);
                            // 查询该时间是当年的第几周
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            String dataStr = year + "/" + month + "/" + day;
                            String versionSql = dbCommand.buildWeekSql(dataStr);
                            List<Map<String, Object>> weekData = dbHelper.batchExecQueryResultMaps_noClose(versionSql, conn);
                            int weekValue = Integer.parseInt(weekData.get(0).get("WeekValue").toString());
                            sqlConditions.add(String.format("'%s/W%s'", year, weekValue));
                            i++;
                        }
                    } else if (retainUnit.equals("日")) {
                        // 2022/11/01
                        String str = "yyyy/MM/dd";
                        SimpleDateFormat format = new SimpleDateFormat(str);
                        Date date = format.parse(versionObj.toString());
                        calendar.setTime(date);
                        while (i < retainTime) {
                            calendar.add(Calendar.DATE, -1);
                            SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd");
                            String curDate = s.format(calendar.getTime());
                            sqlConditions.add(String.format("'%s'", curDate));
                            i++;
                        }
                    }
                } else {
                    log.info("【delVersionData】系统模式");
                    if (retainUnit.equals("年")) {
                        int year = calendar.get(Calendar.YEAR);
                        while (i < retainTime) {
                            sqlConditions.add(String.format("'%s'", year - i));
                            i++;
                        }
                    } else if (retainUnit.equals("季")) {
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int currentQuarter = month % 3 == 0 ? month / 3 : month / 3 + 1;
                        if (currentQuarter == 1) {
                            calendar.set(Calendar.MONTH, 0);
                        } else if (currentQuarter == 2) {
                            calendar.set(Calendar.MONTH, 3);
                        } else if (currentQuarter == 3) {
                            calendar.set(Calendar.MONTH, 6);
                        } else {
                            calendar.set(Calendar.MONTH, 9);
                        }
                        while (i < retainTime) {
                            calendar.add(Calendar.MONTH, -3);
                            month = calendar.get(Calendar.MONTH) + 1;
                            int year = calendar.get(Calendar.YEAR);
                            int quarter = month % 3 == 0 ? month / 3 : month / 3 + 1;
                            sqlConditions.add(String.format("'%s/Q0%s'", year, quarter));
                            i++;
                        }
                    } else if (retainUnit.equals("月")) {
                        while (i < retainTime) {
                            calendar.add(Calendar.MONTH, -1);
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            sqlConditions.add(String.format("'%s/%s'", year, month));
                            i++;
                        }
                    } else if (retainUnit.equals("周")) {
                        // pg和sqlserver获取周时不一样，因此要通过数据库查询
                        while (i < retainTime) {
                            calendar.add(Calendar.DATE, -7);
                            // 查询该时间是当年的第几周
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            String dataStr = year + "/" + month + "/" + day;
                            String versionSql = dbCommand.buildWeekSql(dataStr);
                            List<Map<String, Object>> data = dbHelper.batchExecQueryResultMaps_noClose(versionSql, conn);
                            int weekValue = Integer.parseInt(data.get(0).get("WeekValue").toString());
                            sqlConditions.add(String.format("'%s/W%s'", year, weekValue));
                            i++;
                        }
                    } else if (retainUnit.equals("日")) {
                        while (i < retainTime) {
                            calendar.add(Calendar.DATE, -1);
                            SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd");
                            String curDate = s.format(calendar.getTime());
                            sqlConditions.add(String.format("'%s'", curDate));
                            i++;
                        }
                    }
                }
                if (CollectionUtils.isEmpty(sqlConditions)) {
                    return ResultEnum.SQL_ERROR;
                }
                String sqlConditionStr = Joiner.on(",").join(sqlConditions);
                deleteSql += String.format("(%s)", sqlConditionStr);
            }
            log.info("【delVersionData】删除语句：" + deleteSql);
            String existTableSql = dbCommand.buildExistTableSql(tableName);
            List<Map<String, Object>> maps = dbHelper.batchExecQueryResultMaps_noClose(existTableSql, conn);
            boolean isExists = !maps.get(0).get("isExists").toString().equals("0");
            if (isExists) {
                dbHelper.executeSql(deleteSql, conn);
            } else {
                log.info("【delVersionData】删除的表不存在，表名称：" + tableName);
            }
        } catch (Exception ex) {
            log.error("【delVersionData】触发异常：" + ex);
            return ResultEnum.ERROR;
        } finally {
            if (conn != null) {
                dbHelper.closeConnection(conn);
            }
        }
        return resultEnum;
    }

    @Override
    public List<FieldNameDTO> getTableFileInfo(long tableAccessId) {
        List<TableFieldsPO> list = this.query().eq("table_access_id", tableAccessId).list();
        return TableFieldsMap.INSTANCES.poListToDtoFileList(list);
    }

    @Override
    public ResultEnum batchPublish(BatchPublishDTO dto) {
        for (Long id : dto.ids) {
            TableAccessPO accessPo = tableAccessImpl.query().eq("id", id).one();
            if (accessPo == null) {
                log.error("【物理表为空】,id={}", id);
                continue;
            }

            TableSyncmodePO tableSyncmodePo = syncmodeImpl.query().eq("id", id).one();
            if (tableSyncmodePo == null) {
                log.error("【物理表同步配置为空】,id={}", id);
                continue;
            }
            String versionSql = getVersionSql(tableSyncmodePo);

            List<DeltaTimeDTO> systemVariable = systemVariables.getSystemVariable(id);

            publish(true,
                    accessPo.appId,
                    accessPo.id,
                    accessPo.tableName,
                    1,
                    dto.openTransmission,
                    null,
                    false,
                    systemVariable,
                    versionSql,
                    TableSyncModeMap.INSTANCES.poToDto(tableSyncmodePo),
                    accessPo.appDataSourceId,
                    dto.tableHistorys,
                    dto.currUserName);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addFile(TableFieldsDTO dto) {
        TableFieldsPO po = this.query().eq("table_access_id", dto.tableAccessId)
                .eq("field_name", dto.fieldName).one();
        if (po != null) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }

        boolean flat = this.save(TableFieldsMap.INSTANCES.dtoToPo(dto));
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delFile(long id, long tableId, long userId) {
        TableFieldsPO po = this.query().eq("id", id).select("id").one();

        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        int flat = baseMapper.deleteByIdWithFill(po);

        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //异步删除元数据字段
        MetaDataFieldDTO metaDataFieldDTO = new MetaDataFieldDTO();
        metaDataFieldDTO.setFieldId((int) id);
        metaDataFieldDTO.setTableId((int) tableId);
        metaDataFieldDTO.setUserId(userId);
        publishTaskClient.fieldDelete(metaDataFieldDTO);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateFile(TableFieldsDTO dto) {
        TableFieldsPO po = this.query().eq("id", dto.id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        po.fieldName = dto.fieldName;
        po.fieldLength = dto.fieldLength;
        po.fieldType = dto.fieldType;
        po.fieldDes = dto.fieldDes;
        po.displayName = dto.displayName;
        po.fieldPrecision = dto.fieldPrecision;

        int flat = baseMapper.updateById(po);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

//    //   修改...
//    public Object overlayCodePreview(OverlayCodePreviewAccessDTO dto) {
//        log.info("数据接入预览SQL参数{}", JSON.toJSONString(dto));
//        // 查询表数据
//        //从tb_table_access表获取物理表信息
//        TableAccessPO tableAccessPO = tableAccessMapper.selectById(dto.id);
//        //获取不到，抛出异常
//        if (Objects.isNull(tableAccessPO)) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，表信息不存在");
//        }
//        log.info("数据接入表数据：{}", JSON.toJSONString(tableAccessPO));
//
//        // 查询app应用信息
//        AppRegistrationPO appRegistrationPO = appRegistrationMapper.selectById(tableAccessPO.appId);
//        //获取不到应用信息，则抛出异常
//        if (Objects.isNull(appRegistrationPO)) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，应用不存在");
//        }
//        //获取应用下的数据源信息
//        DataSourceDTO dataSourceDTO = getTargetDbInfo(appRegistrationPO.getTargetDbId());
//        log.info("数据接入数据源：{}", JSON.toJSONString(dataSourceDTO));
//
//        // 处理不同架构下的表名称
//        String targetTableName = "";
//        if (appRegistrationPO.whetherSchema) {
//            targetTableName = tableAccessPO.tableName;
//        } else {
//            targetTableName = "ods_" + appRegistrationPO.getAppAbbreviation() + "_" + tableAccessPO;
//        }
//
//        // 获取预览SQL
//        return getBuildSql(dataSourceDTO, dto, tableAccessPO, appRegistrationPO, targetTableName);
//    }
//
//    private Object getBuildSql(DataSourceDTO dataSourceDTO, OverlayCodePreviewAccessDTO dto, TableAccessPO tableAccessPO, AppRegistrationPO appRegistrationPO, String targetTableName) {
//        IBuildOverlaySqlPreview service = BuildSqlStrategy.getService(dataSourceDTO.conType.getName().toUpperCase());
//        return service.buildStgToOdsSql(dataSourceDTO, dto, tableAccessPO, appRegistrationPO, targetTableName);
//    }
////


////////////////////////////////////////////////////////////////////////////////////

    /**
     * 覆盖方式代码预览
     *
     * @return
     */
    @Override
    public Object accessOverlayCodePreview(OverlayCodePreviewAccessDTO dto) {

        //获取物理表id
        Integer id = dto.id;
        if (id == -1) {
            log.info("表信息不存在...");
            return "";
        }

        // 查询物理表数据
        //从tb_table_access表获取物理表信息
        TableAccessPO tableAccessPO = tableAccessMapper.selectById(dto.id);
        //获取不到，抛出异常
        if (Objects.isNull(tableAccessPO)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，表信息不存在");
        }

        // 查询app应用信息
        AppRegistrationPO appRegistrationPO = appRegistrationMapper.selectById(tableAccessPO.appId);
        //获取不到应用信息，则抛出异常
        if (Objects.isNull(appRegistrationPO)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "预览SQL失败，应用不存在");
        }
        //获取应用下的数据源信息
        DataSourceDTO dataSourceDTO = getTargetDbInfo(appRegistrationPO.getTargetDbId());
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum conType = dataSourceDTO.getConType();

        // 处理不同架构下的表名称
        String targetTableName = "";

        /*appRegistrationPO.whetherSchema
         * 是否将应用简称作为schema使用
         * 否：0  false
         * 是：1  true
         */
        if (appRegistrationPO.whetherSchema) {
            targetTableName = tableAccessPO.tableName;
        } else {
            targetTableName = "ods_" + appRegistrationPO.getAppAbbreviation() + "_" + tableAccessPO.getTableName();
        }

        List<String> stgAndTableName = getStgAndTableName(targetTableName, appRegistrationPO);

        //临时表
        String stgTableName = "";
        //目标表
        String odsTableName = "";
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                stgTableName = stgAndTableName.get(i);
            } else {
                odsTableName = stgAndTableName.get(i);
            }
        }

        if (appRegistrationPO.whetherSchema) {
            stgTableName = appRegistrationPO.appAbbreviation + "." + stgTableName;
            odsTableName = appRegistrationPO.appAbbreviation + "." + odsTableName;
        } else {
            stgTableName = "dbo."+stgTableName;
            odsTableName = "dbo."+odsTableName;
        }

        //List<ModelPublishFieldDTO> ==> List<AccessPublishFieldDTO>
        List<TableFieldsDTO> dtoList = dto.modelPublishFieldDTOList;

        //新建集合预装载转换后的字段数据
        List<AccessPublishFieldDTO> accessList = new ArrayList<>();
        //遍历==>手动转换，属性不多，并未使用mapStruct
        for (TableFieldsDTO m : dtoList) {
            AccessPublishFieldDTO a = new AccessPublishFieldDTO();
            a.sourceFieldName = m.sourceFieldName;
            a.fieldLength = Math.toIntExact(m.fieldLength);
            a.fieldType = m.fieldType;
            a.isBusinessKey = m.isPrimarykey;
            accessList.add(a);
        }

        //新建AccessOverlayCodePreviewDTO对象，参数赋值
        AccessOverlayCodePreviewDTO previewDTO = new AccessOverlayCodePreviewDTO();
        //业务时间覆盖所需的业务逻辑
        previewDTO.tableBusiness = dto.tableBusiness;
        //物理表id
        previewDTO.id = dto.id;
        //同步方式
        previewDTO.syncMode = dto.syncMode;
        //字段集合
        previewDTO.modelPublishFieldDTOList = accessList;

        //调用方法，获取sql语句
        String finalSql = codePreviewBySyncMode(stgTableName, odsTableName, previewDTO,conType);

        //判断是否是全量覆盖方式
        if (dto.syncMode==1){
            //判断全量覆盖方式是否生成快照  1使用  0不使用
            int snapshotFlag = dto.snapshotDTO.ifEnableSnapshot;
            if (snapshotFlag == 1){
                String fullVolumeSql = finalSql.substring(finalSql.indexOf(";"));
            }else {
                log.info("全量覆盖未选择生成版本快照...");
            }
        }

        //返回最终拼接好的sql
        return finalSql;
    }

    /**
     * 数据建模覆盖方式预览sql
     *
     * @param stgAndTableName
     * @param odsTableName
     * @param dto
     * @param conType
     * @return
     * @author lishiji
     */
    private String codePreviewBySyncMode(String stgAndTableName, String odsTableName, AccessOverlayCodePreviewDTO dto, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum conType) {
        //获取业务时间覆盖所需的逻辑
        TableBusinessDTO tableBusiness = dto.tableBusiness;
        //TableBusinessDTO ==> PreviewTableBusinessDTO
        PreviewTableBusinessDTO previewTableBusinessDTO = AccessCodePreviewMapper.INSTANCES.dtoToDto(tableBusiness);

        //获取数据源类型  //todo：目前未从前端传参，暂时认定只有SqlServer
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum sourceType = conType;
        //获取同步方式
        int syncMode = dto.syncMode;
        //获取表名
        //代码可读性，冗余就冗余吧
        String tableName = odsTableName;
        //获取临时表名称
        String tempTableName = stgAndTableName;
        //获取前端传递的表字段集合
        List<AccessPublishFieldDTO> fields = dto.modelPublishFieldDTOList;
        //List<AccessPublishFieldDTO>   ==>   List<PublishFieldDTO>
        List<PublishFieldDTO> fieldList = AccessCodePreviewMapper.INSTANCES.tableFieldsToPublishFields(fields);

        //根据覆盖方式决定返回的sql
        switch (sourceType) {
            case SQLSERVER:
                switch (syncMode) {
                    //如果是0的话，不拼接任何sql
                    case 0:
                        return "";
                    //全量
                    case 1:
                        //调用封装的全量覆盖方式拼接sql方法并返回
                        return FactoryCodePreviewSqlHelper.fullVolumeSql(tableName, tempTableName, fieldList);
                    //追加
                    case 2:
                        //调用封装的追加覆盖方式拼接sql方法并返回
                        return FactoryCodePreviewSqlHelper.insertAndSelectSql(tableName, tempTableName, fieldList);
                    //业务标识覆盖（业务主键覆盖）---merge覆盖
                    case 3:
                        //调用封装的业务标识覆盖方式--merge覆盖(业务标识可以作为业务主键)拼接sql方法并返回
                        return FactoryCodePreviewSqlHelper.merge(tableName, tempTableName, fieldList);
                    //业务时间覆盖
                    case 4:
                        //调用封装的业务时间覆盖方式的拼接sql方法并返回
                        return FactoryCodePreviewSqlHelper.businessTimeOverLay(tableName, tempTableName, fieldList, previewTableBusinessDTO);
                    //业务标识覆盖（业务主键覆盖）--- delete insert 删除插入
                    case 5:
                        //调用封装的业务标识覆盖方式--删除插入(按照业务主键删除，再重新插入)拼接sql方法并返回
                        return FactoryCodePreviewSqlHelper.delAndInsert(tableName, tempTableName, fieldList);
                    default:
                        throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
                }
                //todo:暂时搁置
            case POSTGRESQL:

            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * getStgAndTableName
     *
     * @param tableName
     * @param appRegistrationPO
     * @return
     */
    public List<String> getStgAndTableName(String tableName, AppRegistrationPO appRegistrationPO) {
        log.info("获取stgAndTableName参数：{}", tableName);
        String stgTableName = "";
        String odsTableName = "";
        List<String> tableNames = new ArrayList<>();
        if (tableName.contains("ods_")) {
            String[] split = tableName.split("ods_");
            stgTableName = "stg_" + split[1];
            odsTableName = tableName;
            tableNames.add(stgTableName);
            tableNames.add(odsTableName);
        } else {
            stgTableName = "stg_" + tableName;
            odsTableName = tableName;
            tableNames.add(stgTableName);
            tableNames.add(odsTableName);
        }
        log.info("getStgTableName的表名称{}", tableName);
        return tableNames;
    }

    /**
     * 获取数据源信息
     *
     * @param id
     * @return
     */
    public DataSourceDTO getTargetDbInfo(Integer id) {
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try {
            dataSourceConfig = userClient.getFiDataDataSourceById(id);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            if (Objects.isNull(dataSourceConfig.data)) {
                throw new FkException(ResultEnum.DATA_QUALITY_DATASOURCE_ONTEXISTS);
            }
        } catch (Exception e) {
            log.error("调用userClient服务获取数据源失败,", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return dataSourceConfig.data;
    }

}
