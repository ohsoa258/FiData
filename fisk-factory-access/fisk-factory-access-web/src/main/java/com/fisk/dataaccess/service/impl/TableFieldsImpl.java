package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.flink.UploadWayEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.flinkupload.FlinkFactoryHelper;
import com.fisk.common.service.flinkupload.IFlinkJobUpload;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.access.OperateMsgDTO;
import com.fisk.dataaccess.dto.access.OperateTableDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewPageDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.dto.flink.FlinkConfigDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.dto.table.TableSyncmodeDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.map.FlinkParameterMap;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.utils.files.FileTxtUtils;
import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import com.fisk.dataaccess.utils.sql.OracleCdcUtils;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.generic.RET;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableFieldsImpl extends ServiceImpl<TableFieldsMapper, TableFieldsPO> implements ITableFields {
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private ITableAccess iTableAccess;
    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private TableAccessMapper tableAccessMapper;
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

    @Resource
    private RedisTemplate redisTemplate;

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

        List<TableFieldsDTO> listDto = dto.list;
        TableSyncmodeDTO syncmodeDto = dto.tableSyncmodeDTO;
        TableBusinessDTO businessDto = dto.businessDTO;
        if (CollectionUtils.isEmpty(listDto) || syncmodeDto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // list: dto -> po
        List<TableFieldsPO> listPo = TableFieldsMap.INSTANCES.listDtoToPo(listDto);

        // 字段名称不可重复(前端没有提示功能,后端强制去重)
        List<TableFieldsPO> distinctFields = listPo.stream().filter(RegexUtils.distinctByKey(po -> po.fieldName)).collect(Collectors.toList());

        boolean success;
        // 批量添加tb_table_fields
        success = this.saveBatch(distinctFields);
        if (!success) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        int businessMode = 4;
        if (syncmodeDto.syncMode == businessMode && businessDto != null) {
            success = businessImpl.save(TableBusinessMap.INSTANCES.dtoToPo(businessDto));
            if (!success) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        // 添加tb_table_syncmode
        Long tableAccessId = distinctFields.get(0).tableAccessId;
        TableSyncmodePO syncmodePo = syncmodeDto.toEntity(TableSyncmodePO.class);
        syncmodePo.id = tableAccessId;
        success = syncmodeImpl.saveOrUpdate(syncmodePo);

        TableAccessPO accessPo = tableAccessImpl.query().eq("id", tableAccessId).one();
        if (accessPo == null) {
            return ResultEnum.TABLE_NOT_EXIST;
        }

        tableAccessImpl.updateById(accessPo);

        //系统变量
        if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(tableAccessId, dto.deltaTimes);
        }

        // 发布
        publish(success, accessPo.appId, accessPo.id, accessPo.tableName, dto.flag, dto.openTransmission, null, false, dto.deltaTimes);

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
        success = syncmodeImpl.saveOrUpdate(modelSync);

        // 修改发布状态
        model.publish = 0;
        tableAccessImpl.updateById(model);

        //系统变量
        if (!CollectionUtils.isEmpty(dto.deltaTimes)) {
            systemVariables.addSystemVariables(dto.id, dto.deltaTimes);
        }

        // 发布
        publish(success, model.appId, model.id, model.tableName, dto.flag, dto.openTransmission, null, false, dto.deltaTimes);

        return success ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
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

    /**
     * 调用发布和存储过程
     *
     * @param success   true: 保存成功,执行发布
     * @param appId     应用id
     * @param accessId  物理表id
     * @param tableName 物理表tableName
     * @param flag      0: 保存;  1: 发布
     */
    private void publish(boolean success,
                         long appId,
                         long accessId,
                         String tableName,
                         int flag,
                         boolean openTransmission,
                         CdcJobScriptDTO cdcDto,
                         boolean useExistTable,
                         List<DeltaTimeDTO> deltaTimes) {
        AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("app_id", appId).one();
        if (dataSourcePo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        if (success && flag == 1 && !useExistTable) {
            UserInfo userInfo = userHelper.getLoginUserInfo();
            ResultEntity<BuildPhysicalTableDTO> result = tableAccessImpl.getBuildPhysicalTableDTO(accessId, appId);
            BuildPhysicalTableDTO data = result.data;
            data.appId = String.valueOf(appId);
            data.dbId = String.valueOf(accessId);
            data.userId = userInfo.id;
            data.openTransmission = openTransmission;
            data.deltaTimes = deltaTimes;

            // 版本号入库、调用存储存储过程  
            List<TableFieldsPO> list = this.query().eq("table_access_id", accessId).list();
            AppRegistrationPO registration = iAppRegistration.getById(appId);

            //拼接ods表名
            String odsTableName = TableNameGenerateUtils.buildOdsTableName(tableName, registration.appAbbreviation, registration.whetherSchema);
            data.modelPublishTableDTO = getModelPublishTableDTO(accessId, odsTableName, 3, list);
            data.whetherSchema = registration.whetherSchema;

            // 执行发布
            try {
                // 实时--RestfulAPI类型  or  非实时--api类型
                if ((registration.appType == 0 && DataSourceTypeEnum.RestfulAPI.getName().equals(dataSourcePo.driveType)) || (registration.appType == 1 && DataSourceTypeEnum.API.getName().equals(dataSourcePo.driveType))) {
                    // 传入apiId和api下所有表
                    TableAccessPO accessPo = tableAccessImpl.query().eq("id", accessId).one();
                    List<TableAccessPO> tablePoList = tableAccessImpl.query().eq("api_id", accessPo.apiId).list();
                    // api下所有表
                    data.apiTableNames = tablePoList.stream().map(e -> e.tableName).collect(Collectors.toList());
                    data.appType = registration.appType;
                    data.apiId = accessPo.apiId;
                    // 创建表流程
                    publishTaskClient.publishBuildPhysicsTableTask(data);
                    // 构建元数据实时同步数据对象
                    buildMetaDataInstanceAttribute(registration, accessId, 1);
                } else if (registration.appType == 1) {
                    // 非实时物理表发布
                    // 创建表流程
                    publishTaskClient.publishBuildPhysicsTableTask(data);
                    if (DataSourceTypeEnum.FTP.getName().equals(dataSourcePo.driveType)) {
                        data.excelFlow = true;
                    }
                    // 生成nifi流程
                    //log.info(JSON.toJSONString(data));
                    //publishTaskClient.publishBuildAtlasTableTask(data);
                    // 构建元数据实时同步数据对象
                    buildMetaDataInstanceAttribute(registration, accessId, 2);
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

    @Override
    public void test() {
        CdcJobScriptDTO cdcJobScript = new CdcJobScriptDTO();
        long accessId = 4112;
        cdcScriptUploadFlink(cdcJobScript, accessId);
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
    private void buildMetaDataInstanceAttribute(AppRegistrationPO app, long accessId, int flag) {

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

        // 库
        List<MetaDataDbAttributeDTO> dbList = new ArrayList<>();
        MetaDataDbAttributeDTO db = new MetaDataDbAttributeDTO();
        db.setQualifiedName(hostname + "_" + dbName);
        db.setName(dbName);
        db.setContact_info(app.getAppPrincipal());
        db.setDescription(app.getAppDes());
        db.setComment(app.getAppDes());

        TableAccessPO tableAccess = tableAccessImpl.query().eq("id", accessId).one();
        if (tableAccess == null) {
            return;
        }
        if (flag == tableType) {
            // 表
            List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();
            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.setQualifiedName(hostname + "_" + dbName + "_" + tableAccess.getId());
            table.setName(TableNameGenerateUtils.buildOdsTableName(tableAccess.getTableName(),
                    app.appAbbreviation,
                    app.whetherSchema));
            table.setContact_info(app.getAppPrincipal());
            table.setDescription(tableAccess.getTableDes());
            table.setComment(String.valueOf(app.getId()));

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
                        field.setComment(e.getFieldDes());
                        field.setDataType("VARCHAR".equalsIgnoreCase(e.fieldType) ? e.fieldType + "(" + e.fieldLength + ")" : e.fieldType);
                        return field;
                    }).collect(Collectors.toList());

            table.setColumnList(columnList);
            tableList.add(table);
            db.setTableList(tableList);
            dbList.add(db);
            instance.setDbList(dbList);
        } else if (flag == apiType) {

            List<MetaDataTableAttributeDTO> tableList = tableAccessImpl.query().eq("api_id", tableAccess.apiId).list()
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
                                    field.setComment(e.getFieldDes());
                                    field.setDataType("VARCHAR".equalsIgnoreCase(e.fieldType) ? e.fieldType + "(" + e.fieldLength + ")" : e.fieldType);
                                    return field;
                                }).collect(Collectors.toList());

                        table.setColumnList(columnList);
                        return table;
                    }).collect(Collectors.toList());
            db.setTableList(tableList);
            dbList.add(db);
            instance.setDbList(dbList);
        }

        list.add(instance);

        try {
            MetaDataAttributeDTO data = new MetaDataAttributeDTO();
            data.instanceList = list;
            data.userId = userHelper.getLoginUserInfo().id;
            // 更新元数据内容
            log.info("构建元数据实时同步数据对象开始.........:  参数为: {}", JSON.toJSONString(list));
            dataManageClient.metaData(data);
        } catch (Exception e) {
            log.error("【dataManageClient.MetaData()】方法报错,ex", e);
        }
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
            fieldDTO.fieldLength = Math.toIntExact(po.fieldLength);
            fieldDTO.isPrimaryKey = po.isPrimarykey;
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
    private String createVersionSql(TableSyncmodeDTO tableSyncmodeDto, String tableName, int targetDbId) {
        String versionSql = "";
        // 非全量模式 || 未启用版本功能 || 保留0天 || 表名称为空 || 数据源为空
        if (tableSyncmodeDto.getSyncMode() != 1 || tableSyncmodeDto.getRetainHistoryData() != 1
                || tableSyncmodeDto.getRetainTime() == 0 || StringUtils.isEmpty(tableName) || targetDbId == 0) {
            return versionSql;
        }
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            return versionSql;
        }
        DataSourceDTO dataSourceDTO = dataSourceConfig.getData();
        // 版本设置是否变更，如变更则需要清空ODS数据重新按照版本规则生成版本数据
        TableSyncmodePO tableSyncmodePO = tableAccessImpl.getTableSyncmode(tableSyncmodeDto.getId());
        if (tableSyncmodePO != null) {
            if (!tableSyncmodeDto.getVersionUnit().equals(tableSyncmodePO.getVersionUnit())) {
                Connection conn = DbConnectionHelper.connection(dataSourceDTO.getConStr(), dataSourceDTO.getConAccount(), dataSourceDTO.getConPassword(), dataSourceDTO.getConType());
                String sql = String.format("TRUNCATE TABLE %s", tableName);
                AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
                try {
                    dbHelper.executeSql(sql, conn);
                } catch (SQLException ex) {
                    log.error("【createVersionSql】版本变更执行清空ods异常：" + ex);
                }
            }
        }
        IBuildAccessSqlCommand dbCommand = BuildFactoryAccessHelper.getDBCommand(dataSourceDTO.getConType());
        versionSql = dbCommand.buildVersionSql(tableSyncmodeDto.getVersionUnit(), tableSyncmodeDto.getVersionCustomRule());
        return versionSql;
    }

    /**
     * @return com.fisk.common.core.response.ResultEnum
     * @description 检查版本数据
     * @author dick
     * @date 2022/11/2 11:35
     * @version v1.0
     * @params tableId
     * @params tableType
     */
    public void checkVersionData(String keyStr) {
        log.info("【checkVersionData】请求参数：" + keyStr);
        if (StringUtils.isEmpty(keyStr)) {
            return;
        }
        String[] keyList = keyStr.split("\\.");
        if (keyList == null || keyList.length == 0) {
            return;
        }
        // tb_table_access id
        String tableId = keyList[keyList.length - 1];
        // 应用id
        String appId = keyList[keyList.length - 2];
        // 表类型 属于接入还是建模
        String tableType = keyList[keyList.length - 3];
        // 表名称
        String tableName = "";
        OlapTableEnum tableTypeEnum = OlapTableEnum.getNameByValue(Integer.parseInt(tableType));
        if (StringUtils.isEmpty(tableId) || StringUtils.isEmpty(appId)
                || (tableTypeEnum != OlapTableEnum.PHYSICS && tableTypeEnum != OlapTableEnum.PHYSICS_API && tableTypeEnum != OlapTableEnum.PHYSICS_RESTAPI)) {
            return;
        }
        TableSyncmodePO tableSyncmodePO = tableAccessImpl.getTableSyncmode(Long.parseLong(tableId));
        if (tableSyncmodePO == null || tableSyncmodePO.getRetainTime() == 0) {
            return;
        }
        int targetDbId = 0;
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            return;
        }
        DataSourceDTO dataSourceDTO = dataSourceConfig.getData();

        // 生成删除SQL语句
        Connection conn = DbConnectionHelper.connection(dataSourceDTO.getConStr(), dataSourceDTO.getConAccount(), dataSourceDTO.getConPassword(), dataSourceDTO.getConType());
        AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
        String retainUnit = tableSyncmodePO.getRetainUnit();
        int retainTime = tableSyncmodePO.getRetainTime();
        Calendar calendar = Calendar.getInstance();
        String sql = String.format("DELETE FROM %s WHERE version NOT IN", tableName);
        List<String> sqlConditions = new ArrayList<>();
        int i = 1;
        if (retainUnit.equals("年")) {
            int year = calendar.get(Calendar.YEAR);
            while (i <= retainTime) {
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
            while (i <= retainTime) {
                calendar.add(Calendar.MONTH, -3);
                month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);
                int quarter = month % 3 == 0 ? month / 3 : month / 3 + 1;
                sqlConditions.add(String.format("'%s/Q0%s'", year, quarter));
                i++;
            }
        } else if (retainUnit.equals("月")) {
            while (i <= retainTime) {
                calendar.add(Calendar.MONTH, -1);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                sqlConditions.add(String.format("'%s/Q0%s'", year, month));
                i++;
            }
        } else if (retainUnit.equals("周")) {
            // pg和sqlserver获取周时不一样，因此要通过数据库查询
            IBuildAccessSqlCommand dbCommand = BuildFactoryAccessHelper.getDBCommand(dataSourceDTO.getConType());
            String versionSql = dbCommand.buildWeekSql();
            List<Map<String, Object>> data = dbHelper.batchExecQueryResultMaps(versionSql, conn);
            if (CollectionUtils.isEmpty(data)) {
                return;
            }
            Object weekValueObj = data.get(0).get("WeekValue");
            if (weekValueObj == null || weekValueObj == "") {
                return;
            }
            int weekValue = Integer.parseInt(weekValueObj.toString());
            while (i <= retainTime) {
                calendar.add(Calendar.DATE, -7);
                int year = calendar.get(Calendar.YEAR);
                sqlConditions.add(String.format("'%s/W%s'", year, retainTime - i));
                i++;
            }
        } else if (retainUnit.equals("日")) {
            while (i <= retainTime) {
                calendar.add(Calendar.DATE, -1);
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String curDate = s.format(calendar.getTime());
                sqlConditions.add(String.format("'%s'", curDate));
                i++;
            }
        }
    }
}
