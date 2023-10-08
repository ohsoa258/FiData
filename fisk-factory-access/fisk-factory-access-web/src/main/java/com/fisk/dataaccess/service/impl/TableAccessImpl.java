package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.dbdatatype.FiDataDataTypeEnum;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.server.datasource.ExternalDataSourceDTO;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.AccessMainPageVO;
import com.fisk.dataaccess.dto.GetConfigDTO;
import com.fisk.dataaccess.dto.access.DataAccessTreeDTO;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.api.ApiColumnInfoDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datamodel.AppAllRegistrationDataDTO;
import com.fisk.dataaccess.dto.datamodel.AppRegistrationDataDTO;
import com.fisk.dataaccess.dto.datamodel.TableAccessDataDTO;
import com.fisk.dataaccess.dto.datamodel.TableQueryDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcHeadConfigDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.sapbw.ProviderAndDestination;
import com.fisk.dataaccess.dto.table.*;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessQueryDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.map.TableAccessMap;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.utils.dbdatasize.IBuildFactoryDbDataSizeCount;
import com.fisk.dataaccess.utils.dbdatasize.impl.DbDataSizeCountHelper;
import com.fisk.dataaccess.utils.keepnumberfactory.IBuildKeepNumber;
import com.fisk.dataaccess.utils.keepnumberfactory.impl.BuildKeepNumberSqlHelper;
import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import com.fisk.dataaccess.utils.sql.MysqlConUtils;
import com.fisk.dataaccess.utils.sql.SapBwUtils;
import com.fisk.dataaccess.utils.sql.SqlServerConUtils;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.TableNameVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.dataaccess.vo.table.PhyTblAndApiTblVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.check.CheckPhyDimFactTableIfExistsDTO;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.AccessDataSuccessAndFailCountDTO;
import com.fisk.task.dto.atlas.AtlasEntityColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.*;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OdsDataSyncTypeEnum;
import com.sap.conn.jco.JCoDestination;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements ITableAccess {

    @Resource
    private TableFieldsImpl tableFieldsImpl;
    @Resource
    private TableAccessMapper accessMapper;
    @Resource
    private TableFieldsMapper fieldsMapper;
    @Resource
    private AppRegistrationMapper appRegistrationMapper;
    @Resource
    private AppRegistrationImpl appRegistrationImpl;
    @Resource
    private AppRegistrationMapper registrationMapper;
    @Resource
    private AppDataSourceMapper appDataSourceMapper;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private TableSyncmodeImpl syncmodeImpl;
    @Resource
    private SystemVariablesImpl systemVariables;
    @Resource
    private TableSyncmodeMapper syncmodeMapper;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserClient userClient;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private TableBusinessImpl businessImpl;
    @Resource
    private TableBusinessMapper businessMapper;
    @Resource
    private ApiConfigImpl apiConfigImpl;
    @Value("${spring.datasource.dynamic.datasource.taskdb.url}")
    private String jdbcStr;
    @Value("${spring.datasource.dynamic.datasource.taskdb.username}")
    private String user;
    @Value("${spring.datasource.dynamic.datasource.taskdb.password}")
    private String password;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;
    @Resource
    private TableSyncmodeImpl tableSyncmodeImpl;
    @Resource
    private FtpImpl ftpImpl;
    @Resource
    private DataManageClient dataManageClient;
    @Resource
    GetConfigDTO getConfig;

    @Value("${sftp.nifi-file-path}")
    private String nifiFilePath;
    @Value("${spring.open-metadata}")
    private Boolean openMetadata;

    @Value("${config-url}")
    private String accessConfigDbURL;
    @Value("${config-username}")
    private String username;
    @Value("${config-password}")
    private String pwd;
    @Value("${config-driverType}")
    private String dbType;
    @Value("${config-tb-name}")
    private String tbName;

    @Resource
    TableHistoryMapper tableHistoryMapper;
    @Resource
    private AppRegistrationImpl appRegistration;

    /**
     * 数据库连接
     *
     * @param dto
     * @return
     */
    public Connection getConnection(DataSourceDTO dto) {
        AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
        Connection connection = dbHelper.connection(dto.conStr, dto.conAccount,
                dto.conPassword, dto.conType);
        return connection;
    }

    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO 请求参数
     * @return 返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addRealTimeData(TableAccessDTO tableAccessDTO) {

        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;


        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        // 数据保存: 添加应用的时候,相同的表名不可以再次添加
        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        String tableName = modelAccess.getTableName();

        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
        tableNameVO.appId = tableAccessDTO.appId;
        tableNameVO.tableName = tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            return ResultEnum.TABLE_NAME_EXISTS;
        }

        if (tableAccessDTO.appId < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        modelAccess.setCreateUser(String.valueOf(userId));
        modelAccess.appId = tableAccessDTO.appId;

        // 0是实时物理表，1是非实时物理表
        modelAccess.setSyncSrc(tableAccessDTO.getSyncSrc());
        modelAccess.setIsRealtime(0);

        // 2.保存tb_table_access数据
        boolean saveAccess = this.save(modelAccess);

        if (!saveAccess) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 保存tb_table_fields数据
        boolean saveFields = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessDTO.getList();

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setTableAccessId(modelAccess.getId());

            modelField.setCreateUser(String.valueOf(userId));

            // 1是实时物理表的字段，0是非实时物理表的字段
            modelField.setIsRealtime(1);

            saveFields = tableFieldsImpl.save(modelField);
        }

        if (!saveFields) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return saveFields ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     *
     * @param tableAccessNonDTO dto
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<AtlasIdsVO> addNonRealTimeData(TableAccessNonDTO tableAccessNonDTO) {
        // 当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 判断table_name是否已存在(不同应用注册下,名称可以相同)
        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessNonDTO.toEntity(TableAccessPO.class);

        // TODO: tableName 物理表名称
        String tableName = modelAccess.getTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
//        tableNameVO.appId = modelReg.id;
        tableNameVO.appId = tableAccessNonDTO.appId;
        tableNameVO.tableName = tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            return ResultEntityBuild.build(ResultEnum.TABLE_NAME_EXISTS);
        }

        if (tableAccessNonDTO.appId < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }
        modelAccess.setAppId(tableAccessNonDTO.appId);
        // 0是实时物理表，1是非实时物理表
        modelAccess.setSyncSrc(tableAccessNonDTO.getSyncSrc());
        // 非实时
        modelAccess.setIsRealtime(1);
        // 当前登录人
        modelAccess.setCreateUser(String.valueOf(userId));
        // 2.保存tb_table_access数据
        boolean saveAccess = this.save(modelAccess);
        if (!saveAccess) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存tb_table_fields数据
        boolean saveField = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessNonDTO.getList();

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setTableAccessId(modelAccess.getId());

            // 1是实时物理表的字段，0是非实时物理表的字段
            modelField.setIsRealtime(1);
            modelField.setCreateUser(String.valueOf(userId));

            saveField = tableFieldsImpl.save(modelField);
        }

        if (!saveField) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // TODO 新增tb_table_business业务时间表
        // 保存tb_table_business数据
        TableBusinessDTO businessDTO = tableAccessNonDTO.getBusinessDTO();
        TableBusinessPO modelBusiness = TableBusinessMap.INSTANCES.dtoToPo(businessDTO);
        modelBusiness.setAccessId(modelAccess.getId());
        boolean saveBusiness = this.businessImpl.save(modelBusiness);
        if (!saveBusiness) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存tb_table_syncmode数据
        TableSyncmodeDTO syncmodeDTO = tableAccessNonDTO.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = syncmodeDTO.toEntity(TableSyncmodePO.class);
        modelSync.setId(modelAccess.getId());

        boolean saveSync = syncmodeImpl.save(modelSync);
        if (!saveSync) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        AtlasIdsVO atlasIdsVO = new AtlasIdsVO();
        atlasIdsVO.userId = userId;
        // 应用注册id
        atlasIdsVO.appId = String.valueOf(tableAccessNonDTO.appId);
        atlasIdsVO.dbId = String.valueOf(modelAccess.getId());
        atlasIdsVO.tableName = tableName;

        return ResultEntityBuild.build(ResultEnum.SUCCESS, atlasIdsVO);
    }

    /**
     * 校验相同schema,不同应用是否存在表名重复问题
     *
     * @param appId
     * @param tableName
     */
    public void verifySchemaTable(Long appId, String tableName) {
        if (appId == null || StringUtils.isEmpty(tableName)) {
            return;
        }
        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", appId).one();
        if (registrationPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //是否使用schema,判断相同schema，不同应用是否存在相同表名问题
        if (!registrationPo.whetherSchema) {
            return;
        }

        //存在应用换ods数据源情况
        appRegistrationImpl.VerifySchema(registrationPo.appAbbreviation, registrationPo.targetDbId);

        List<AppRegistrationPO> idList = appRegistrationImpl.query()
                .select("id")
                .ne("id", appId)
                .eq("whether_schema", true)
                .eq("app_abbreviation", registrationPo.appAbbreviation)
                .list();
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        List<Long> collect1 = idList.stream().map(e -> e.id).collect(Collectors.toList());
        List<TableNameVO> tableNameVoList = this.baseMapper.getAppIdAndTableName();
        List<TableNameVO> collect = tableNameVoList.stream()
                .filter(e -> collect1.contains(e.appId) && e.tableName.equals(tableName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            throw new FkException(ResultEnum.SCHEMA_TABLE_REPEAT);
        }
    }

    /**
     * 修改物理表(实时)
     *
     * @param tableAccessDTO dto
     * @return 执行结果
     */
    @Override
    public ResultEnum updateRealTimeData(TableAccessDTO tableAccessDTO) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        modelAccess.setUpdateUser(String.valueOf(userId));

        // 2.保存tb_table_access数据
        boolean updateAccess = this.updateById(modelAccess);

        if (!updateAccess) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 保存tb_table_fields数据: 分为更新和添加数据
        boolean updateFields = true;
        boolean saveField = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessDTO.getList();

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            // 0: 旧数据不操作  1: 修改  2: 新增
            int funcType = tableFieldsDTO.getFuncType();
            // 修改
            if (funcType == 1) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.setUpdateUser(String.valueOf(userId));
                updateFields = tableFieldsImpl.updateById(modelField);
                // 新增
            } else if (funcType == 2) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                // 还要绑定tb_table_access id
                modelField.setTableAccessId(modelAccess.getId());
                modelField.setUpdateUser(String.valueOf(userId));
                saveField = tableFieldsImpl.save(modelField);
            }
        }

        if (!updateFields || !saveField) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        return updateFields ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 修改物理表(非实时)
     *
     * @param dto dto
     * @return 执行结果
     */
    @Override
    public ResultEnum updateNonRealTimeData(TableAccessNonDTO dto) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // dto->po
        TableAccessPO modelAccess = dto.toEntity(TableAccessPO.class);

        modelAccess.setUpdateUser(String.valueOf(userId));

        // 2.保存tb_table_access数据
        boolean updateAccess = this.updateById(modelAccess);

        if (!updateAccess) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 3.保存tb_table_fields数据
        boolean updateField = true;
        boolean saveField = true;
        List<TableFieldsDTO> list = dto.getList();

        for (TableFieldsDTO tableFieldsDTO : list) {
            // 0: 未操作的数据  1: 新增  2: 编辑
            int funcType = tableFieldsDTO.getFuncType();
            if (funcType == 2) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.setUpdateUser(String.valueOf(userId));
                updateField = tableFieldsImpl.updateById(modelField);
            } else if (funcType == 1) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.setTableAccessId(modelAccess.getId());
                modelField.setUpdateUser(String.valueOf(userId));
                saveField = tableFieldsImpl.save(modelField);
            }
        }

        List<TableFieldsPO> originalData = tableFieldsImpl.query().eq("table_access_id", list.get(0).tableAccessId).list();
        List<TableFieldsPO> webData = TableFieldsMap.INSTANCES.listDtoToPo(list);
        List<TableFieldsPO> collect = originalData.stream().filter(item -> !webData.contains(item)).collect(Collectors.toList());

        try {
            collect.stream().map(e -> {
                return fieldsMapper.deleteByIdWithFill(e);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        if (!updateField) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        if (!saveField) {
            return ResultEnum.DATAACCESS_SAVEFIELD_ERROR;
        }

        // TODO 新增tb_table_business业务时间表
        // 4.保存tb_table_business数据
        if (dto.getBusinessDTO() != null) {
            TableBusinessDTO businessDTO = dto.getBusinessDTO();
            TableBusinessPO modelBusiness = TableBusinessMap.INSTANCES.dtoToPo(businessDTO);
            boolean updateBusiness = this.businessImpl.updateById(modelBusiness);
            if (!updateBusiness) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        // 5.保存tb_table_syncmode数据
        boolean updateSync;
        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        updateSync = syncmodeImpl.updateById(modelSync);

        return updateSync ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 物理表接口首页分页查询
     *
     * @param key  key
     * @param page page
     * @param rows rows
     * @return 查询结果
     */
    @Override
    public Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows) {
        // 新建分页
        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    public static OdsResultDTO resultSetToJsonArrayDataAccess(ResultSet rs) throws SQLException, JSONException {
        OdsResultDTO data = new OdsResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldNameDTO> fieldNameDTOList = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        int count = 1;
        // 预览展示10行
        int row = 10;
        while (rs.next() && count <= row) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(columnName) || tableName.equals("ods_" + columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }
        //获取列名
        for (int i = 1; i <= columnCount; i++) {
            FieldNameDTO dto = new FieldNameDTO();
            dto.sourceTableName = metaData.getTableName(i);
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.sourceFieldType = metaData.getColumnTypeName(i).toUpperCase();
            dto.sourceFieldPrecision = metaData.getScale(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName = metaData.getTableName(i) + "key";
            if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(dto.fieldName)
                    || tableName.equals("ods_" + dto.fieldName)) {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toLowerCase();
            dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i))) ? "255" : String.valueOf(metaData.getColumnDisplaySize(i));
            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    /**
     * @return com.fisk.dataaccess.entity.TableSyncmodePO
     * @description 查询单条TableSyncmode信息
     * @author dick
     * @date 2022/11/1 17:13
     * @version v1.0
     * @params
     */
    public TableSyncmodePO getTableSyncmode(long id) {
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        return modelSync;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.table.TableFieldsDTO>
     * @description sql语句更新后，过滤已删除的源字段
     * @author Lock
     * @date 2021/12/28 14:48
     * @version v1.0
     * @params sourceFieldList
     * @params queryDto
     */
    private List<FieldNameDTO> filterSqlFieldList(List<TableFieldsDTO> sourceFieldList, OdsQueryDTO queryDto) {

        queryDto.deltaTimes = systemVariables.getSystemVariable(sourceFieldList.get(0).tableAccessId);

        OdsResultDTO resultDto = getDataAccessQueryList(queryDto);
        // 执行sql
        List<FieldNameDTO> fieldNameDTOList = resultDto.fieldNameDTOList;
        List<TableFieldsDTO> dtoList = new ArrayList<>();
        // 重新组装库中的字段属性
        sourceFieldList.forEach(e -> {
            TableFieldsDTO dto = new TableFieldsDTO();
            dto.id = e.id;
            dto.tableAccessId = e.tableAccessId;
            dto.sourceFieldName = e.sourceFieldName;
            dto.sourceFieldType = e.sourceFieldType;
            dto.fieldName = e.fieldName;
            dto.isPrimarykey = e.isPrimarykey;
            dto.isRealtime = e.isRealtime;
            dto.fieldDes = e.fieldDes;
            dto.fieldType = e.fieldType;
            dto.fieldLength = e.fieldLength;
            dtoList.add(dto);
        });
        // 组装执行sql后的字段属性
        fieldNameDTOList.forEach(e -> dtoList.stream().filter(f -> f.sourceFieldName.equals(e.sourceFieldName)).forEachOrdered(f -> {
            e.id = f.id;
            e.tableAccessId = Math.toIntExact(f.tableAccessId);
            e.sourceFieldName = f.sourceFieldName;
            e.sourceFieldType = f.sourceFieldType;
            e.fieldName = f.fieldName;
            e.isPrimarykey = f.isPrimarykey;
            e.isRealtime = f.isRealtime;
            e.fieldDes = f.fieldDes;
            e.fieldType = f.fieldType;
            e.fieldLength = String.valueOf(f.fieldLength);
        }));
        return fieldNameDTOList;
    }


    /**
     * 根据应用名称,获取物理表名及表对应的字段(非实时)
     *
     * @param appName appName
     * @return 查询结果
     */
    @Override
    public List<TablePyhNameDTO> getTableFields(String appName) {

        // 1.根据应用名称查询表id
        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("app_name", appName)
                .eq("del_flag", 1)
                .one();

        // tb_app_registration表id
        long appid = modelReg.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).one();
        String url = modelDataSource.getConnectStr();
        String user = modelDataSource.getConnectAccount();
        String pwd = modelDataSource.getConnectPwd();
        String dbName = modelDataSource.dbName;

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        List<TablePyhNameDTO> list = new ArrayList<>();
        switch (modelDataSource.driveType) {
            case "mysql":
                break;
            case "sqlserver":
                Connection conn = DbConnectionHelper.connection(url, user, pwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER);
                list = new SqlServerConUtils().getTableNameAndColumns(conn, dbName);
                break;
            default:
                break;
        }

        return list;
    }

    @Override
    public List<TablePyhNameDTO> getTableFieldsByAppId(long appId) {

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appId).one();
        String url = modelDataSource.getConnectStr();
        String user = modelDataSource.getConnectAccount();
        String pwd = modelDataSource.getConnectPwd();
        String dbName = modelDataSource.dbName;

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        List<TablePyhNameDTO> list = new ArrayList<>();
        Connection conn = null;
        switch (modelDataSource.driveType) {
            case "mysql":
            case "oracle":
                // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                conn = DbConnectionHelper.connection(url, user, pwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
                list = mysqlConUtils.getTableNameAndColumns(conn);
                break;
            case "sqlserver":
                conn = DbConnectionHelper.connection(url, user, pwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER);
                list = new SqlServerConUtils().getTableNameAndColumns(conn, dbName);
                break;
            default:
                return null;
        }

        return list;
    }

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<NifiVO> deleteData(long id) {

        // 删除之前检查该物理表是否已经被配置到存在的管道里面：
        // 方式：检查配置库-dmp_factory_db库 tb_nifi_custom_workflow_detail表内是否存在该物理表，
        // 如果存在则不允许删除，给出提示并告知该表被配置到哪个管道里面    tips:数据接入的物理表对应的table type是3 数据湖表任务
        CheckPhyDimFactTableIfExistsDTO dto = new CheckPhyDimFactTableIfExistsDTO();
        dto.setTblId(id);
        dto.setChannelDataEnum(ChannelDataEnum.getName(3));
        ResultEntity<List<NifiCustomWorkflowDetailDTO>> booleanResultEntity = dataFactoryClient.checkPhyTableIfExists(dto);
        if (booleanResultEntity.getCode()!=ResultEnum.SUCCESS.getCode()){
            return ResultEntityBuild.build(ResultEnum.DISPATCH_REMOTE_ERROR);
        }
        List<NifiCustomWorkflowDetailDTO> data = booleanResultEntity.getData();
        if (!CollectionUtils.isEmpty(data)){
            //这里的getWorkflowId 已经被替换为 workflowName
            List<String> collect = data.stream().map(NifiCustomWorkflowDetailDTO::getWorkflowId).collect(Collectors.toList());
            log.info("当前要删除的表存在于以下管道中："+ collect);
            NifiVO nifiVO = new NifiVO();
            nifiVO.setWorkFlowName(collect);
            return ResultEntityBuild.build(ResultEnum.ACCESS_PHYTABLE_EXISTS_IN_DISPATCH,nifiVO);
        }

        UserInfo userInfo = userHelper.getLoginUserInfo();

        // 1.删除tb_table_access数据
        TableAccessPO modelAccess = this.getById(id);
        if (modelAccess == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        int deleteAccess = accessMapper.deleteByIdWithFill(modelAccess);
        if (deleteAccess < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 2.删除tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .list();
        try {
            // 判断是否存在表字段
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(e -> fieldsMapper.deleteByIdWithFill(e));
            }
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        //停止cdc任务
        if (!StringUtils.isEmpty(modelAccess.jobId)) {
            tableFieldsImpl.cancelJob(modelAccess.jobId, modelAccess.id);
        }

        // 查询tb_table_syncmode
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        int businessTimeType = 4;
        if (modelSync != null && modelSync.syncMode == businessTimeType) {
            TableBusinessPO modelBusiness = businessImpl.query().eq("access_id", id).one();
            if (modelBusiness != null) {
                int deleteBusiness = businessMapper.deleteByIdWithFill(modelBusiness);
                if (deleteBusiness < 0) {
                    return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", modelAccess.appId).eq("del_flag", 1).one();

        NifiVO vo = new NifiVO();
        List<TableListVO> voList = new ArrayList<>();
        TableListVO tableListVO = new TableListVO();

        tableListVO.userId = userInfo.id;
        tableListVO.tableName = TableNameGenerateUtils.buildTableName(modelAccess.tableName, registrationPo.appAbbreviation, registrationPo.whetherSchema);
        voList.add(tableListVO);
        List<Long> tableIdList = new ArrayList<>();
        tableIdList.add(id);

        vo.appId = String.valueOf(modelAccess.appId);
        vo.userId = userInfo.id;
        vo.tableList = voList;
        vo.tableIdList = tableIdList;

        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(registrationPo.targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        String hostname = dataSourceConfig.data.conIp;
        String dbName = dataSourceConfig.data.conDbname;

        List<String> qualifiedNames = new ArrayList<>();
        qualifiedNames.add(hostname + "_" + dbName + "_" + id);
        vo.setQualifiedNames(qualifiedNames);
        vo.setClassifications(registrationPo.appName + "_" + registrationPo.appAbbreviation);

        log.info("删除的物理表信息,{}", vo);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);

    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_BUILDTABLE_AND_COLUMN)
    @Override
    public ResultEntity<AtlasEntityDbTableColumnDTO> getAtlasBuildTableAndColumn(long id, long appid) {
        AtlasEntityDbTableColumnDTO dto = null;
        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        TableSyncmodePO modelSync = syncmodeMapper.getData(id);
        if (modelAccess == null || modelDataSource == null || modelSync == null || modelReg == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        dto = new AtlasEntityDbTableColumnDTO();
////        dto.dbId = modelDataSource.getAtlasDbId();
        dto.tableName = modelAccess.getTableName();
        dto.createUser = modelAccess.getCreateUser();
        // TODO:驱动类型(改为枚举类型)
        if (StringUtils.isNotBlank(modelDataSource.driveType)) {
            switch (modelDataSource.driveType) {
                case "sqlserver":
                    dto.dbType = DbTypeEnum.sqlserver;
                    break;
                case "mysql":
                    dto.dbType = DbTypeEnum.mysql;
                    break;
                case "postgresql":
                    dto.dbType = DbTypeEnum.postgresql;
                    break;
                case "oracle":
                    dto.dbType = DbTypeEnum.oracle;
                    break;
                default:
                    break;
            }
        }
        // TODO 新增cron表达式
        dto.cornExpress = modelSync.cornExpression;
        // TODO 新增appAbbreviation syncType syncField
        dto.appAbbreviation = modelReg.appAbbreviation;
        switch (modelSync.syncMode) {
            case 1:
                dto.syncType = OdsDataSyncTypeEnum.full_volume;
                break;
            case 2:
                dto.syncType = OdsDataSyncTypeEnum.timestamp_incremental;
                dto.syncField = modelSync.syncField;
                break;
            case 3:
                dto.syncType = OdsDataSyncTypeEnum.business_time_cover;
                break;
            default:
                break;
        }
        dto.tableId = String.valueOf(modelAccess.getId());
        List<AtlasEntityColumnDTO> columns = new ArrayList<>();
        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();
        if (list.isEmpty()) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        for (TableFieldsPO po : list) {
            AtlasEntityColumnDTO atlasEntityColumnDTO = new AtlasEntityColumnDTO();
            atlasEntityColumnDTO.setColumnId(po.getId());
            atlasEntityColumnDTO.setColumnName(po.getFieldName());
            atlasEntityColumnDTO.setComment(po.getFieldDes());
            if (po.fieldLength == 0) {
                atlasEntityColumnDTO.setDataType(po.getFieldType());
            } else {

                atlasEntityColumnDTO.setDataType(po.getFieldType() + "(" + po.fieldLength + ")");
            }
            atlasEntityColumnDTO.setIsKey("" + po.getIsPrimarykey() + "");
////            atlasEntityColumnDTO.setGuid(po.atlasFieldId);

            columns.add(atlasEntityColumnDTO);
        }
        dto.columns = columns;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_WRITEBACKDATA)
    @Override
    public ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(long appid, long id) {
        AtlasWriteBackDataDTO dto = new AtlasWriteBackDataDTO();
        // 查询tb_app_registration
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        // 查询tb_app_datasource
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        if (modelReg == null || modelDataSource == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
////        dto.appId = modelReg.atlasInstanceId;
////        dto.atlasTableId = modelDataSource.atlasDbId;
        // 查询tb_table_access
        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
////        dto.tableId = modelAccess.atlasTableId;
        AtlasEntityDbTableColumnDTO atlasDTO = new AtlasEntityDbTableColumnDTO();
////        atlasDTO.dbId = modelDataSource.getAtlasDbId();
        atlasDTO.tableName = modelAccess.getTableName();
        atlasDTO.createUser = modelAccess.getCreateUser();
        // TODO:驱动类型
        if (StringUtils.isNotBlank(modelDataSource.driveType)) {
            switch (modelDataSource.driveType) {
                case "sqlserver":
                    atlasDTO.dbType = DbTypeEnum.sqlserver;
                    break;
                case "mysql":
                    atlasDTO.dbType = DbTypeEnum.mysql;
                    break;
                case "postgresql":
                    atlasDTO.dbType = DbTypeEnum.postgresql;
                    break;
                case "oracle":
                    atlasDTO.dbType = DbTypeEnum.oracle;
                    break;
                default:
                    break;
            }
        }
        List<AtlasEntityColumnDTO> columns = new ArrayList<>();

        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();
        if (list.isEmpty()) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        for (TableFieldsPO po : list) {
            AtlasEntityColumnDTO atlasEntityColumnDTO = new AtlasEntityColumnDTO();
            atlasEntityColumnDTO.setColumnId(po.getId());
            atlasEntityColumnDTO.setColumnName(po.getFieldName());
            atlasEntityColumnDTO.setComment(po.getFieldDes());
            if (po.fieldLength == 0) {
                atlasEntityColumnDTO.setDataType(po.getFieldType());
            } else {
                atlasEntityColumnDTO.setDataType(po.getFieldType() + "(" + po.fieldLength + ")");
            }
            atlasEntityColumnDTO.setIsKey("" + po.getIsPrimarykey() + "");
            columns.add(atlasEntityColumnDTO);
        }
        dto.columnsKeys = columns;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAtlasTableIdAndDorisSql(AtlasWriteBackDataDTO dto) {
        // 应用注册id
        long appid = Long.parseLong(dto.appId);
        // 物理表id
        long tableId = Long.parseLong(dto.tableId);
        // 物理表: tb_table_access
        TableAccessPO modelAccess = this.query().eq("id", tableId).eq("app_id", appid).eq("del_flag", 1).one();
        if (modelAccess == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
//        modelAccess.atlasTableId = dto.atlasTableId;
        boolean update = this.updateById(modelAccess);
        if (!update) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<AtlasEntityColumnDTO> list = dto.columnsKeys;
        boolean updateField = true;
        for (AtlasEntityColumnDTO columnDTO : list) {
            // 根据本表id查询tb_table_fields
            TableFieldsPO modelFields = this.tableFieldsImpl.query().eq("id", columnDTO.columnId).one();
            if (modelFields == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            // 回写的字段GUID
////            modelFields.atlasFieldId = columnDTO.getGuid();
            // 更新tb_table_fields表数据
            updateField = this.tableFieldsImpl.updateById(modelFields);
            if (!updateField) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return updateField ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_CONFIG)
    @Override
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(long id, long appid) {
        // 增量
        int syncModel = 4;
        DataAccessConfigDTO dto = new DataAccessConfigDTO();
        // app组配置
        GroupConfig groupConfig = new GroupConfig();
        //任务组配置
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        // 数据源jdbc配置
        DataSourceConfig sourceDsConfig = new DataSourceConfig();
        // 目标源jdbc连接
        DataSourceConfig targetDsConfig = new DataSourceConfig();
        // 增量配置库源jdbc连接
        DataSourceConfig cfgDsConfig = new DataSourceConfig();
        // 表及表sql
        ProcessorConfig processorConfig = new ProcessorConfig();
        // ftp连接信息
        FtpConfig ftpConfig = new FtpConfig();
        // sapBw连接信息
        SapBwConfig sapBwConfig = new SapBwConfig();

        // 1.app组配置
        // select * from tb_app_registration where id=id and del_flag=1;
        AppRegistrationPO modelReg = this.appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        if (modelReg == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        groupConfig.setAppName(modelReg.getAppName());
        groupConfig.setAppDetails(modelReg.getAppDes());
        //3.数据源jdbc配置
        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
        if (modelAccess == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("id", modelAccess.appDataSourceId).eq("del_flag", 1).one();
        if (modelDataSource == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        sourceDsConfig.setJdbcStr(modelDataSource.getConnectStr());

        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.getValue(modelDataSource.driveType);
        switch (dataSourceTypeEnum) {
            case SQLSERVER:
                sourceDsConfig.setType(DriverTypeEnum.SQLSERVER);
                break;
            case MYSQL:
                sourceDsConfig.setType(DriverTypeEnum.MYSQL);
                break;
            case POSTGRESQL:
                sourceDsConfig.setType(DriverTypeEnum.POSTGRESQL);
                break;
            case ORACLE:
                sourceDsConfig.setType(DriverTypeEnum.ORACLE);
                break;
            case FTP:
            case SFTP:
                dto.ftpConfig = buildFtpConfig(ftpConfig, modelDataSource, modelAccess, modelReg);
                break;
            case SAPBW:
                // 设置sapBw配置信息
                sapBwConfig.setAppId(modelDataSource.getAppId());
                sapBwConfig.setDriveType(modelDataSource.getDriveType());
                sapBwConfig.setHost(modelDataSource.getHost());
                sapBwConfig.setPort(modelDataSource.getPort());
                sapBwConfig.setConnectAccount(modelDataSource.getConnectAccount());
                sapBwConfig.setConnectPwd(modelDataSource.getConnectPwd());
                sapBwConfig.setSysNr(modelDataSource.getSysNr());
                sapBwConfig.setLang(modelDataSource.getLang());
                sapBwConfig.setMdxSql(modelAccess.getSqlScript());
                String mdxSqlList = modelAccess.getMdxSqlList();
                //将数据库里存储的json格式的mdx语句集合转为List集合
                List<String> list = JSONArray.parseArray(mdxSqlList, String.class);
                sapBwConfig.setMdxList(list);
                break;
            default:
                break;
        }

        sourceDsConfig.setUser(modelDataSource.getConnectAccount());
        sourceDsConfig.setPassword(modelDataSource.getConnectPwd());
        // 5.表及表sql
        TableSyncmodePO modelSync = syncmodeMapper.getData(id);
        if (modelSync == null) {
            if (modelSync == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
            }
        }
        // TODO: 新增同步方式
        targetDsConfig.syncMode = modelSync.syncMode;
        // 增量的时候,获取业务时间覆盖对象
        if (targetDsConfig.syncMode == syncModel) {
            TableBusinessPO modelBusiness = businessImpl.query().eq("access_id", id).one();
            dto.businessDTO = TableBusinessMap.INSTANCES.poToDto(modelBusiness);
        }
        // 2.任务组配置
        taskGroupConfig.setAppName(modelAccess.getTableName());
        taskGroupConfig.setAppDetails(modelAccess.getTableDes());

        // corn_expression
        processorConfig.scheduleExpression = modelSync.getCornExpression();

        if (modelSync.syncField != null) {
            // 增量字段
            processorConfig.syncField = modelSync.syncField;
        }
        String timerDriver = "Timer driven";
        String corn = "CORN driven";
        if (timerDriver.equalsIgnoreCase(modelSync.timerDriver)) {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.TIMER;
        } else if (corn.equalsIgnoreCase(modelSync.timerDriver)) {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.CRON;
        } else {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.EVENT;
        }
        // TODO  新增: 增量配置库源jdbc连接
        cfgDsConfig.setType(DriverTypeEnum.MYSQL);
        cfgDsConfig.setJdbcStr(jdbcStr);
        cfgDsConfig.setUser(user);
        cfgDsConfig.setPassword(password);
        // TODO: 2021/9/4 nifi流程需要物理表字段
        List<TableFieldsPO> list = this.tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();
        List<TableFieldsDTO> fieldsDTOList = TableFieldsMap.INSTANCES.listPoToDto(list);
        String businessKeyAppend = "";
        if (list != null && !list.isEmpty()) {
            targetDsConfig.tableFieldsList = fieldsDTOList;
            // 封装业务主键
            businessKeyAppend = list.stream().filter(e -> e.isPrimarykey == 1).map(e -> e.fieldName + ",").collect(Collectors.joining());
        }

        if (businessKeyAppend.length() > 0) {
            dto.businessKeyAppend = businessKeyAppend.substring(0, businessKeyAppend.length() - 1);
        }
        dto.groupConfig = groupConfig;
        dto.taskGroupConfig = taskGroupConfig;
        dto.sourceDsConfig = sourceDsConfig;
        dto.targetDsConfig = targetDsConfig;
        dto.processorConfig = processorConfig;
        dto.cfgDsConfig = cfgDsConfig;
        dto.sapBwConfig = sapBwConfig;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    /**
     * 构建ftp数据源信息
     *
     * @param ftpConfig       ftp数据源参数
     * @param modelDataSource 数据源对象
     * @param modelAccess     物理表对象
     * @return com.fisk.task.dto.daconfig.FtpConfig
     * @author Lock
     * @date 2022/4/12 16:47
     */
    private FtpConfig buildFtpConfig(FtpConfig ftpConfig, AppDataSourcePO modelDataSource, TableAccessPO modelAccess, AppRegistrationPO appRegistration) {

        if (StringUtils.isNotBlank(modelAccess.sqlScript)) {
            List<String> list = ftpImpl.encapsulationExcelParam(modelAccess.sqlScript);
            // 去掉最后一位 '/'
            ftpConfig.remotePath = list.get(0).substring(0, list.get(0).length() - 1);
            ftpConfig.fileFilterRegex = list.get(1);
        }

        ftpConfig.hostname = modelDataSource.host;
        ftpConfig.port = modelDataSource.port;
        if (DataSourceTypeEnum.SFTP.getName().equals(modelDataSource.driveType)) {
            if (modelDataSource.serviceType == 1) {
                ftpConfig.linuxPath = nifiFilePath;
                ftpConfig.fileBinary = modelDataSource.fileBinary;
                ftpConfig.fileName = appRegistration.appAbbreviation;
                ftpConfig.password = null;
            } else {
                ftpConfig.password = modelDataSource.connectPwd;
                ftpConfig.linuxPath = null;
            }
        }
        ftpConfig.whetherSftpl = DataSourceTypeEnum.SFTP.getName().equals(modelDataSource.driveType);

        if (DataSourceTypeEnum.FTP.getName().equals(modelDataSource.driveType)) {
            ftpConfig.password = modelDataSource.connectPwd;
        }
        ftpConfig.username = modelDataSource.connectAccount;
        ftpConfig.ftpUseUtf8 = true;
        ftpConfig.sheetName = modelAccess.sheet;
        ftpConfig.startLine = modelAccess.startLine;

        return ftpConfig;
    }

    @Override
    public BuildNifiFlowDTO createPgToDorisConfig(String tableName, String selectSql) {
        return null;
    }

    @Override
    public Page<TableAccessVO> listData(TableAccessQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        if (query.key != null && query.key.length() > 0) {
            querySql.append(" and table_name like concat('%', " + "'").append(query.key).append("'").append(", '%') ");
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        TableAccessPageDTO data = new TableAccessPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        List<FilterFieldDTO> list;
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_table_access";
        dto.tableAlias = "a";
        dto.filterSql = FilterSqlConstants.TABLE_ACCESS_SQL;
        list = getMetadata.getMetadataList(dto);

        dto.tableName = "tb_table_syncmode";
        dto.tableAlias = "b";
        dto.filterSql = FilterSqlConstants.TABLE_SYNCMODE_SQL;
        List<FilterFieldDTO> fieldDTOList = getMetadata.getMetadataList(dto);
        list.addAll(fieldDTOList);
        return list;
    }

    @Override
    public List<TableNameDTO> getDataAccessMeta() {

        List<TableNameDTO> list = new ArrayList<>();

        // 获取所有表
        List<TableNameDTO> listTableName = baseMapper.listTableName();

        for (TableNameDTO tableNameDTO : listTableName) {

            getTableFieldsById(list, tableNameDTO);
        }

        return list;
    }

    /**
     * 封装物理表及字段
     *
     * @param list         list
     * @param tableNameDTO tableNameDTO
     */
    private void getTableFieldsById(List<TableNameDTO> list, TableNameDTO tableNameDTO) {
        TableNameDTO dto = new TableNameDTO();
        List<FieldNameDTO> listFieldName = fieldsMapper.listTableName(tableNameDTO.id);
        dto.id = tableNameDTO.id;
        dto.tableName = tableNameDTO.tableName;
        dto.field = listFieldName;
        list.add(dto);
    }

    @Override
    public List<DataAccessTreeDTO> getTree() {

        List<DataAccessTreeDTO> appTree = registrationMapper.listAppTree();

        return appTree.stream().map(e -> {
            e.setList(baseMapper.listTableNameTree(e.id).stream().map(f -> {
                f.flag = 1;
                f.setPid(e.id);
                f.appType = e.appType;
                return f;
            }).collect(Collectors.toList()));
            e.flag = 1;
            return e;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TableNameDTO> getTableName(long id) {
        List<TableNameDTO> list = new ArrayList<>();

        // 获取物理表
        List<TableNameDTO> listTableName = baseMapper.listTableNameByAppId(id);

        // 根据tb_access_id获取表字段及id
        for (TableNameDTO tableNameDTO : listTableName) {
            getTableFieldsById(list, tableNameDTO);
        }

        return list;
    }

    @Override
    public Map<Integer, String> getTableNames(TableQueryDTO tableQueryDTO) {
        Map<Integer, String> map = new HashMap<>();
        //查询宽表
        QueryWrapper<TableAccessPO> tableAccessPOQueryWrapper = new QueryWrapper<>();
        tableAccessPOQueryWrapper.lambda().in(TableAccessPO::getId, tableQueryDTO.getIds());
        List<TableAccessPO> tableAccessPOList = baseMapper.selectList(tableAccessPOQueryWrapper);
        for (TableAccessPO tableAccessPO : tableAccessPOList) {
            map.put((int) tableAccessPO.getId(), tableAccessPO.getTableName());
        }
        return map;
    }

    @Override
    public TableAccessDTO getTableAccess(int id) {
        TableAccessDTO dto = new TableAccessDTO();
        TableAccessPO po = accessMapper.selectById(id);
        if (po == null) {
            return dto;
        }
        return dto = TableAccessMap.INSTANCES.poToDto(po);
    }

    @Override
    public List<FieldNameDTO> getTableFieldId(int id) {
        QueryWrapper<TableFieldsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(TableFieldsPO::getTableAccessId, id);
        List<FieldNameDTO> list = fieldsMapper.listTableName(id);
        return list;
    }

    @Override
    public List<ChannelDataDTO> getTableId(NifiComponentsDTO nifiComponentsDto) {

        List<AppRegistrationPO> allAppList = appRegistrationImpl.list(Wrappers.<AppRegistrationPO>lambdaQuery()
                .select(AppRegistrationPO::getId, AppRegistrationPO::getAppName)
                .orderByDesc(AppRegistrationPO::getCreateTime));

        List<AppRegistrationPO> list = new ArrayList<>();

        // 要根据不同的类型来加载不同的应用
        ChannelDataEnum channelDataEnum = ChannelDataEnum.getName(Math.toIntExact(nifiComponentsDto.id));
        switch (Objects.requireNonNull(channelDataEnum)) {
            // 数据湖非实时物理表任务(mysql  sqlserver  oracle)
            case DATALAKE_TASK:
                allAppList.forEach(e -> {
                    List<AppDataSourcePO> dataSourcePo = appDataSourceImpl.query().eq("app_id", e.id).list();
                    if (!CollectionUtils.isEmpty(dataSourcePo)) {
                        for (AppDataSourcePO item : dataSourcePo) {
                            if (item.driveType.equalsIgnoreCase(DataSourceTypeEnum.MYSQL.getName()) ||
                                    item.driveType.equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName()) ||
                                    item.driveType.equalsIgnoreCase(DataSourceTypeEnum.ORACLE.getName()) ||
                                    item.driveType.equalsIgnoreCase(DataSourceTypeEnum.OPENEDGE.getName())) {
                                list.add(e);
                            }
                        }
                    }
                });

                return bulidListChannelDataDTOByTableOrFTP(list);
            // 数据湖ftp任务
            case DATALAKE_FTP_TASK:
                allAppList.forEach(e -> {
                    List<AppDataSourcePO> dataSourcePo = appDataSourceImpl.query().eq("app_id", e.id).list();
                    if (!CollectionUtils.isEmpty(dataSourcePo)) {
                        for (AppDataSourcePO item : dataSourcePo) {
                            if (item.driveType.equalsIgnoreCase(DataSourceTypeEnum.FTP.getName())) {
                                list.add(e);
                            }
                        }
                    }
                });
                return bulidListChannelDataDTOByTableOrFTP(list);
            // 数据湖非实时api任务
            case DATALAKE_API_TASK:
                allAppList.forEach(e -> {
                    List<AppDataSourcePO> dataSourcePo = appDataSourceImpl.query().eq("app_id", e.id).list();
                    if (!CollectionUtils.isEmpty(dataSourcePo)) {
                        for (AppDataSourcePO item : dataSourcePo) {
                            if (item.driveType.equalsIgnoreCase(DataSourceTypeEnum.API.getName())) {
                                list.add(e);
                            }
                        }
                    }
                });
                return bulidListChannelDataDTOByAPI(list);
            default:
                break;
        }

        return null;
    }

    @Override
    public List<ChannelDataDTO> getTableId() {

        // 查询所有app_id和app_name
        List<AppRegistrationPO> list = appRegistrationImpl.list(Wrappers.<AppRegistrationPO>lambdaQuery()
                .select(AppRegistrationPO::getId, AppRegistrationPO::getAppName)
                .orderByDesc(AppRegistrationPO::getCreateTime));

        // 封装app_id和appName到新的对象集合中
        List<ChannelDataDTO> channelDataDTOList = AppRegistrationMap.INSTANCES.listPoToChannelDataDto(list);

        for (AppRegistrationPO e : list) {
            List<AppDataSourcePO> appDataSourcePo = appDataSourceImpl.query().eq("app_id", e.id).list();
            if (CollectionUtils.isEmpty(appDataSourcePo)) {
                continue;
            }
            for (AppDataSourcePO item : appDataSourcePo) {
                for (ChannelDataDTO f : channelDataDTOList) {
                    if (item.appId == f.id) {
                        if (item.driveType.equalsIgnoreCase(DbTypeEnum.sqlserver.getName())
                                || item.driveType.equalsIgnoreCase(DbTypeEnum.mysql.getName())
                                || item.driveType.equalsIgnoreCase(DbTypeEnum.oracle.getName())
                                || item.driveType.equalsIgnoreCase(DbTypeEnum.postgresql.getName())
                                || item.driveType.equalsIgnoreCase(DbTypeEnum.sftp.getName())
                                || item.driveType.equalsIgnoreCase(DbTypeEnum.openedge.getName())
                                || item.driveType.equalsIgnoreCase(DbTypeEnum.sapbw.getName())) {
                            f.type = "数据湖表任务";
                        }
                        if (item.driveType.equalsIgnoreCase(DbTypeEnum.api.getName())) {
                            f.type = "数据湖非实时api任务";
                        }
                        if (item.driveType.equalsIgnoreCase(DbTypeEnum.ftp.getName())) {
                            f.type = "数据湖ftp任务";
                        }
                        if (item.driveType.equalsIgnoreCase(DbTypeEnum.RestfulAPI.getName())) {
                            f.type = "数据湖RestfulAPI任务";
                        }
                    }
                }
            }

        }

        // 查询当前应用下面的所有表(type为空的是脏数据)
        channelDataDTOList.stream().filter(e -> StringUtils.isNotBlank(e.type)).collect(Collectors.toList()).forEach(dto -> {

            if (dto.type.equalsIgnoreCase(ChannelDataEnum.DATALAKE_TASK.getName()) || dto.type.equalsIgnoreCase(ChannelDataEnum.DATALAKE_FTP_TASK.getName())) {
                // select id,table_name from tb_table_access where app_id =#{dto.id} and del_flag = 1
                List<TableAccessPO> poList = this.list(Wrappers.<TableAccessPO>lambdaQuery()
//                        .or()
                        .eq(TableAccessPO::getAppId, dto.id)
                        // publish=3: 正在发布 -> 1:发布成功
//                        .eq(TableAccessPO::getPublish, 3)
                        .eq(TableAccessPO::getPublish, 1)
                        .select(TableAccessPO::getId, TableAccessPO::getTableName));
                List<ChannelDataChildDTO> channelDataChildDTOS = TableAccessMap.INSTANCES.listPoToChannelDataDto(poList);
                List<ChannelDataChildDTO> collect = channelDataChildDTOS.stream().map(i -> {
                    i.tableBusinessType = ChannelDataEnum.getValue(dto.type).getValue();
                    return i;
                }).collect(Collectors.toList());
                dto.list = collect;
            } else if (dto.type.equalsIgnoreCase(ChannelDataEnum.DATALAKE_API_TASK.getName())) {
                List<ApiConfigPO> apiConfigPoList = apiConfigImpl.list(Wrappers.<ApiConfigPO>lambdaQuery()
//                        .or()
                        .eq(ApiConfigPO::getAppId, dto.id)
                        // publish=3: 正在发布 -> 1:发布成功
//                        .eq(ApiConfigPO::getPublish, 3)
                        .eq(ApiConfigPO::getPublish, 1)
                        .select(ApiConfigPO::getId, ApiConfigPO::getApiName));
                // list: po->dto 并赋值给dto.list
                List<ChannelDataChildDTO> channelDataChildDTOS = TableAccessMap.INSTANCES.listApiConfigPoToChannelDataChildDTO(apiConfigPoList);
                List<ChannelDataChildDTO> collect = channelDataChildDTOS.stream().map(i -> {
                    i.tableBusinessType = ChannelDataEnum.getValue(dto.type).getValue();
                    return i;
                }).collect(Collectors.toList());
                dto.list = collect;
            }

        });

        return channelDataDTOList;
    }

    /**
     * 封装应用及应用下的物理表
     *
     * @param list list
     * @return java.util.List<com.fisk.datafactory.dto.components.ChannelDataDTO>
     * @author Lock
     * @date 2022/5/1 15:33
     */
    private List<ChannelDataDTO> bulidListChannelDataDTOByTableOrFTP(List<AppRegistrationPO> list) {

        List<ChannelDataDTO> channelDataDTOList = AppRegistrationMap.INSTANCES.listPoToChannelDataDto(list);
        // 查询当前应用下面的所有表
        channelDataDTOList.forEach(dto -> {
            // select id,table_name from tb_table_access where app_id =#{dto.id} and del_flag = 1
            List<TableAccessPO> poList = this.list(Wrappers.<TableAccessPO>lambdaQuery()
                    .eq(TableAccessPO::getAppId, dto.id)
                    // publish=3: 正在发布 -> 1:发布成功
                    .or()
                    .eq(TableAccessPO::getPublish, 3)
                    .eq(TableAccessPO::getPublish, 1)
                    .select(TableAccessPO::getId, TableAccessPO::getTableName));
            // list: po->dto 并赋值给dto.list
            dto.list = TableAccessMap.INSTANCES.listPoToChannelDataDto(poList);
        });

        return channelDataDTOList;
    }

    /**
     * 封装应用及应用下的api
     *
     * @param list list
     * @return java.util.List<com.fisk.datafactory.dto.components.ChannelDataDTO>
     * @author Lock
     * @date 2022/5/1 15:34
     */
    private List<ChannelDataDTO> bulidListChannelDataDTOByAPI(List<AppRegistrationPO> list) {

        List<ChannelDataDTO> channelDataDTOList = AppRegistrationMap.INSTANCES.listPoToChannelDataDto(list);
        // 查询当前应用下面的所有API
        channelDataDTOList.forEach(dto -> {
            // select id,api_name from tb_api_config where app_id =#{dto.id} and del_flag = 1
            List<ApiConfigPO> poList = apiConfigImpl.list(Wrappers.<ApiConfigPO>lambdaQuery()
                    .eq(ApiConfigPO::getAppId, dto.id)
                    // publish=3: 正在发布 -> 1:发布成功
                    .or()
                    .eq(ApiConfigPO::getPublish, 3)
                    .eq(ApiConfigPO::getPublish, 1)
                    .select(ApiConfigPO::getId, ApiConfigPO::getApiName));
            // list: po->dto 并赋值给dto.list
            dto.list = TableAccessMap.INSTANCES.listApiConfigPoToChannelDataChildDTO(poList);
        });

        return channelDataDTOList;
    }

    @Override
    public List<AppRegistrationDataDTO> getDataAppRegistrationMeta() {

        List<AppRegistrationDataDTO> list = new ArrayList<>();

        //获取所有应用注册列表
        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();
        List<AppRegistrationPO> poList = registrationMapper.selectList(queryWrapper);
        if (poList == null || poList.size() == 0) {
            return list;
        }
        list = AppRegistrationMap.INSTANCES.listPoToDtoList(poList);
        //获取所有表配置数据
        QueryWrapper<TableAccessPO> wrapper = new QueryWrapper<>();
        // 只需要发布状态为3: 正在发布 -> 1:发布成功
        wrapper.lambda().eq(TableAccessPO::getPublish, 3).or().eq(TableAccessPO::getPublish, 1);
        List<TableAccessPO> tableAccessList = accessMapper.selectList(wrapper);
        if (tableAccessList == null || tableAccessList.size() == 0) {
            return list;
        }
        //获取表中所有字段配置数据
        QueryWrapper<TableFieldsPO> tableFieldsQueryWrapper = new QueryWrapper<>();
        List<TableFieldsPO> tableFieldsList = fieldsMapper.selectList(tableFieldsQueryWrapper
        );
        for (AppRegistrationDataDTO item : list) {
            item.tableDtoList = TableAccessMap.INSTANCES.poListToDtoList(tableAccessList.stream()
                    .filter(e -> e.appId == item.id).collect(Collectors.toList()));
            item.tableDtoList.stream().map(e -> e.tableName = TableNameGenerateUtils
                    .buildOdsTableName(e.tableName, item.appAbbreviation,
                            item.whetherSchema)).collect(Collectors.toList());
            if (item.tableDtoList.size() == 0 || tableFieldsList == null
                    || tableFieldsList.size() == 0) {
                continue;
            }
            item.tableDtoList.stream().map(e -> e.type = 1).collect(Collectors.toList());
            for (TableAccessDataDTO tableAccessDataDTO : item.tableDtoList) {
                tableAccessDataDTO.fieldDtoList = TableFieldsMap.INSTANCES
                        .poListToDtoList(tableFieldsList.stream()
                                .filter(e -> e.tableAccessId == tableAccessDataDTO.id).collect(Collectors.toList()));
                if (CollectionUtils.isEmpty(tableAccessDataDTO.fieldDtoList)) {
                    continue;
                }
                tableAccessDataDTO.fieldDtoList.stream().map(e -> e.type = 2).collect(Collectors.toList());
            }
        }
        // 反转: 倒序排序
        Collections.reverse(list);
        return list;
    }

    @Override
    public List<AppAllRegistrationDataDTO> getAllDataAppRegistrationMeta() {
        List<AppAllRegistrationDataDTO> root = new ArrayList<>();

        // 获取所有的ods数据源
        List<ExternalDataSourceDTO> fiDataDataSource = appRegistrationImpl.getFiDataDataSource();
        if (CollectionUtils.isEmpty(fiDataDataSource)) {
            return root;
        }
        for (ExternalDataSourceDTO item : fiDataDataSource) {
            AppAllRegistrationDataDTO dto = new AppAllRegistrationDataDTO();
            dto.setId(item.getId());
            dto.setName(item.getName());
            root.add(dto);
        }

        // 获取所有应用注册树
        List<AppRegistrationDataDTO> appList = new ArrayList<>();
        QueryWrapper<AppRegistrationPO> appQw = new QueryWrapper<>();
        appQw.eq("del_flag", 1);
        List<AppRegistrationPO> appPoList = registrationMapper.selectList(appQw);
        if (CollectionUtils.isEmpty(appPoList)) {
            return root;
        }
        appList = appRegistrationDataDTOList(appPoList);

        // 获取所有表配置数据,发布状态为3: 正在发布 -> 1:发布成功
        QueryWrapper<TableAccessPO> wrapper = new QueryWrapper<>();
        // 只需要发布状态为3: 正在发布 -> 1:发布成功
        wrapper.lambda().eq(TableAccessPO::getPublish, 3).or().eq(TableAccessPO::getPublish, 1);
        List<TableAccessPO> tableAccessList = accessMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(tableAccessList)) {
            // 存在多个应用，但每个应用下不存在表，则将应用赋值到每个root下
            for (AppAllRegistrationDataDTO parent : root) {
                parent.setAppList(appList.stream().filter(e -> e.getTargetDbId() == parent.getId()).collect(Collectors.toList()));
            }
            return root;
        }

        // 应用下表数据不为空时，获取表中字段配置
        List<TableFieldsPO> tableFieldsList = fieldsMapper.selectList(new QueryWrapper<>());
        // 遍历每个ods数据源
        for (AppRegistrationDataDTO item : appList) {
            item.tableDtoList = TableAccessMap.INSTANCES.poListToDtoList(tableAccessList.stream()
                    .filter(e -> e.appId == item.id).collect(Collectors.toList()));
            item.tableDtoList.stream().map(e -> e.tableName = TableNameGenerateUtils
                    .buildOdsTableName(e.tableName, item.appAbbreviation,
                            item.whetherSchema)).collect(Collectors.toList());
            if (item.tableDtoList.size() == 0 || tableFieldsList == null
                    || tableFieldsList.size() == 0) {
                continue;
            }
            item.tableDtoList.stream().map(e -> e.type = 1).collect(Collectors.toList());
            for (TableAccessDataDTO tableAccessDataDTO : item.tableDtoList) {
                tableAccessDataDTO.fieldDtoList = TableFieldsMap.INSTANCES
                        .poListToDtoList(tableFieldsList.stream()
                                .filter(e -> e.tableAccessId != null && e.tableAccessId == tableAccessDataDTO.id).collect(Collectors.toList()));
                if (CollectionUtils.isEmpty(tableAccessDataDTO.fieldDtoList)) {
                    continue;
                }
                tableAccessDataDTO.fieldDtoList.stream().map(e -> e.type = 2).collect(Collectors.toList());
            }
        }

        // 倒排
        Collections.reverse(appList);

        // 为当前ods划分应用
        for (AppAllRegistrationDataDTO parent : root) {
            parent.setAppList(appList.stream().filter(e -> e.getTargetDbId() == parent.getId()).collect(Collectors.toList()));
        }
        return root;
    }

    private List<AppRegistrationDataDTO> appRegistrationDataDTOList(List<AppRegistrationPO> poList) {
        if (CollectionUtils.isEmpty(poList)) {
            return null;
        }
        List<AppRegistrationDataDTO> parent = new ArrayList<>();
        for (AppRegistrationPO po : poList) {
            AppRegistrationDataDTO dto = new AppRegistrationDataDTO();
            dto.setId(po.getId());
            dto.setAppName(po.getAppName());
            dto.setAppAbbreviation(po.getAppAbbreviation());
            dto.setTargetDbId(po.getTargetDbId());
            if (po.getWhetherSchema() != null) {
                dto.setWhetherSchema(po.getWhetherSchema());
            }
            parent.add(dto);
        }
        return parent;
    }

    @Override
    public ResultEntity<Object> addTableAccessData(TbTableAccessDTO dto) {

        // dto -> po
        TableAccessPO model = TableAccessMap.INSTANCES.tbDtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }

        if (!CollectionUtils.isEmpty(appDataSourceImpl.getDataSourceMeta(dto.appId))) {
            //校验相同schema,不同应用是否存在表名重复问题
            verifySchemaTable(dto.appId, dto.tableName);
        }

        // 同一应用下表名不可重复
        boolean flag = this.checkTableName(dto);
        if (flag) {
            return ResultEntityBuild.build(ResultEnum.TABLE_NAME_EXISTS);
        }

        // 如果存在多个不使用schema的应用,则添加时不允许这些应用下出现重复表名的物理表
        Long appId = model.getAppId();
        AppRegistrationDTO data = appRegistration.getData(appId);
        Boolean whetherSchema = data.whetherSchema;
        if (!whetherSchema) {
            //获取此次新增的表名
            String tableName = model.getTableName();
            //获取所有不使用简称作为架构名的应用
            List<AppRegistrationPO> appsWithNoSchema = appRegistration.getAppListWithNoSchema();
            //新建集合预装载应用id
            ArrayList<Long> ids = new ArrayList<>();
            appsWithNoSchema.forEach(appRegistrationPO -> {
                ids.add(appRegistrationPO.id);
            });

            //通过 应用id集合 查询对应的物理表
            LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TableAccessPO::getAppId, ids);
            List<TableAccessPO> tableAccessPOS = list(wrapper);
            List<TableAccessPO> posAlreadyExists = tableAccessPOS.stream()
                    .filter(tableAccessPO -> tableName.equals(tableAccessPO.getTableName()))
                    .collect(Collectors.toCollection(ArrayList::new));

            // 如果不为空
            if (!CollectionUtils.isEmpty(posAlreadyExists)) {
                List<Long> uniqueAppIds = posAlreadyExists.stream()
                        .map(TableAccessPO::getAppId)
                        .distinct()
                        .collect(Collectors.toList());

                ArrayList<AppRegistrationPO> collect = appsWithNoSchema.stream()
                        .filter(appRegistrationPO -> uniqueAppIds.contains(appRegistrationPO.id))
                        .collect(Collectors.toCollection(ArrayList::new));

                List<String> appNames = collect.stream().map(AppRegistrationPO::getAppName).collect(Collectors.toList());
                return ResultEntityBuild.build(ResultEnum.TABLE_NAME_EXISTS, "表名存在于以下应用中：" + appNames);
            }
        }

        boolean save = this.save(model);
        if (!save) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        //2023-04-26李世纪修改，数据接入添加物理表时，设置默认的stg保存时间
        TableKeepNumberDTO tableKeepNumberDTO = new TableKeepNumberDTO();
        //默认五天
        tableKeepNumberDTO.setKeepNumber("5 day");
        //获取物理表id
        tableKeepNumberDTO.setId(model.getId());
        //调用设置stg保存时间的方法
        ResultEnum resultEnum = setKeepNumber(tableKeepNumberDTO);
        if (resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
            log.error("添加物理表时，设置stg默认保存时间失败...");
            throw new FkException(ResultEnum.SET_KEEP_NUMBER_ERROR, "修改物理表时，设置stg默认保存时间失败...");
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, model.id);
    }

    @Override
    public TbTableAccessDTO getTableAccessData(long id) {

        TableAccessPO model = this.query().eq("id", id).one();
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TbTableAccessDTO dto = TableAccessMap.INSTANCES.tbPoToDto(model);
        dto.appName = appRegistrationImpl.query().eq("id", model.appId).one().appName;
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateTableAccessData(TbTableAccessDTO dto) {

        TableAccessPO model = null;
        if (dto.getId() > 0) {
            model = this.getById(dto.id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
        }

        // sql保存时丢失
        if (dto.sqlFlag == 1 && "".equals(dto.sqlScript)) {
            return ResultEnum.SQL_EXCEPT_CLEAR;
        }

        if (model != null && !CollectionUtils.isEmpty(appDataSourceImpl.getDataSourceMeta(model.appId))) {
            //校验相同schema,不同应用是否存在表名重复问题
            verifySchemaTable(dto.appId, dto.tableName);
        }

        // 前端操作多了未命名,不传物理表id,提前保存物理表的sql脚本,导致更新失败
        if (dto.id > 0) {

            // 判断名称是否重复
            QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
            // 限制在同一应用下
            queryWrapper.lambda().eq(TableAccessPO::getTableName, dto.tableName).eq(TableAccessPO::getAppId, model.appId);
            TableAccessPO tableAccessPo = baseMapper.selectOne(queryWrapper);
            if (tableAccessPo != null && tableAccessPo.id != dto.id) {
                return ResultEnum.TABLE_IS_EXIST;
            }
        }

        // dto -> po
        TableAccessPO po = TableAccessMap.INSTANCES.tbDtoToPo(dto);

        if (po.appId != null && openMetadata) {
            synchronousMetadata(model.appDataSourceId, po);
        }
        /*if (po.getTableName() == null) {
            po.setTableName("");
        }*/
        //po.setPublish(0);

        //2023-04-26李世纪修改，数据接入编辑物理表时，设置默认的stg保存时间
        //如果前端传递的参数没有app id(应用id),则认为并不是要设置默认stg保存时间的操作，就不进行stg默认保存时间的操作
        Long appId = dto.appId;
        if (appId != null) {
            po.setKeepNumber("5 day");
            // 查询app应用信息
            AppRegistrationPO appRegistrationPO = appRegistrationMapper.selectById(po.appId);
            //获取不到应用信息，则抛出异常
            if (Objects.isNull(appRegistrationPO)) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS, "设置stg保存时间失败，应用不存在");
            }
            //获取应用下的数据源信息
            DataSourceDTO dataSourceDTO = tableFieldsImpl.getTargetDbInfo(appRegistrationPO.getTargetDbId());
            // 处理不同架构下的表名称
            String targetTableName = "";
            /*appRegistrationPO.whetherSchema
             * 是否将应用简称作为schema使用
             * 否：0  false
             * 是：1  true
             */
            if (appRegistrationPO.whetherSchema) {
                targetTableName = po.tableName;
            } else {
                targetTableName = "ods_" + appRegistrationPO.getAppAbbreviation() + "_" + po.getTableName();
            }
            List<String> stgAndTableName = tableFieldsImpl.getStgAndTableName(targetTableName, appRegistrationPO);

            TableKeepNumberDTO tableKeepNumberDTO = new TableKeepNumberDTO();
            //默认五天
            tableKeepNumberDTO.setKeepNumber(po.keepNumber);

            //根据数据库连接类型获取对应的stg保存时间的sql语句
            String delSql = BuildKeepNumberSqlHelper.getKeepNumberSqlHelperByConType(dataSourceDTO.conType).setKeepNumberSql(tableKeepNumberDTO, appRegistrationPO, stgAndTableName);

            //设置删除stg表时的默认保存时间 5day 的del_stg_sql
            po.setDeleteStgScript(String.valueOf(delSql));
        }

        return this.saveOrUpdate(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    /**
     * 同步元数据
     *
     * @param appDataSourceId
     */
    public void synchronousMetadata(long appDataSourceId, TableAccessPO po) {
        AppDataSourcePO dataSourcePO = appDataSourceMapper.selectById(appDataSourceId);
        if (dataSourcePO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        AppRegistrationPO app = appRegistrationMapper.selectById(dataSourcePO.appId);
        if (app == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(app.targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();

        // 实例
        String rdbmsType = dataSourceConfig.data.conType.getName();
        String platform = dataSourceConfig.data.platform;
        String hostname = dataSourceConfig.data.conIp;
        String port = dataSourceConfig.data.conPort.toString();
        String protocol = dataSourceConfig.data.protocol;
        String dbName = dataSourceConfig.data.conDbname;
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
        db.setTableList(new ArrayList<>());
        dbList.add(db);
        instance.setDbList(dbList);
        list.add(instance);

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
        table.setQualifiedName(list.get(0).dbList.get(0).qualifiedName + "_" + po.id);
        table.setName(TableNameGenerateUtils.buildOdsTableName(po.getTableName(),
                app.appAbbreviation,
                app.whetherSchema));
        table.setComment(String.valueOf(dataSourcePO.appId));
        table.setDisplayName(po.displayName);
        table.setOwner(app.appPrincipal);
        table.setDescription(po.tableDes);
        tableList.add(table);

        list.get(0).dbList.get(0).tableList = tableList;


        //修改元数据
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
        });

    }

    @Override
    public Page<TbTableAccessDTO> getTableAccessListData(TbTableAccessQueryDTO dto) {
        return baseMapper.getTableAccessListData(dto.page, dto);
    }

    /**
     * 判断同一应用下表名不可重复
     *
     * @param dto dto
     * @return true: 重复, false: 不重复
     */
    private boolean checkTableName(TbTableAccessDTO dto) {
        boolean flag = false;

        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
        tableNameVO.apiId = dto.apiId == null ? 0 : dto.apiId;
        tableNameVO.appId = dto.appId;
        tableNameVO.tableName = dto.tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            flag = true;
        }
        return flag;
    }

    public static OdsResultDTO resultSetToJsonArrayDataModel(ResultSet rs) throws SQLException, JSONException {
        OdsResultDTO data = new OdsResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldNameDTO> fieldNameDTOList = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        while (rs.next()) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equalsIgnoreCase(columnName)
                        || tableName.equalsIgnoreCase("ods_" + columnName.toLowerCase())
                        || "fi_createtime".equalsIgnoreCase(columnName)
                        || "fi_updatetime".equalsIgnoreCase(columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            array.add(jsonObj);
        }
        //获取列名
        for (int i = 1; i <= columnCount; i++) {
            FieldNameDTO dto = new FieldNameDTO();
            //源表
            dto.sourceTableName = metaData.getTableName(i);
            // 源字段
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName = metaData.getTableName(i) + "key";
            if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equalsIgnoreCase(dto.fieldName)
                    || tableName.equalsIgnoreCase("ods_" + dto.fieldName.toLowerCase())
                    || "fi_createtime".equalsIgnoreCase(dto.fieldName)
                    || "fi_updatetime".equalsIgnoreCase(dto.fieldName)) {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toLowerCase();
            dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i))) ? "255" : String.valueOf(metaData.getColumnDisplaySize(i));

            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    /**
     * 回显实时表
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public TableAccessNonDTO getData(long id) {

        // 查询tb_table_access数据
        TableAccessPO modelAccess = this.query().eq("id", id).eq("del_flag", 1).one();
        if (modelAccess == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableAccessNonDTO dto = TableAccessMap.INSTANCES.poToDtoNon(modelAccess);

        // 将应用名称封装进去
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", modelAccess.getAppId()).one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        dto.setAppName(modelReg.getAppName());

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();

        List<TableFieldsDTO> listField = new ArrayList<>();
        for (TableFieldsPO modelField : list) {
            TableFieldsDTO tableFieldsDTO = TableFieldsMap.INSTANCES.poToDto(modelField);

            listField.add(tableFieldsDTO);
        }

        dto.setList(listField);

        // 查询tb_table_syncmode
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        TableSyncmodeDTO sdto = new TableSyncmodeDTO(modelSync);
        dto.setTableSyncmodeDTO(sdto);

        // 只有存在业务时间覆盖时,才会给前端展示
        if (modelSync != null && modelSync.syncMode == 4) {
            // 查询tb_table_business
            QueryWrapper<TableBusinessPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TableBusinessPO::getAccessId, id);
            List<TableBusinessPO> modelBusiness = businessMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(modelBusiness)) {
                TableBusinessDTO businessDTO = TableBusinessMap.INSTANCES.poToDto(modelBusiness.get(0));
                dto.setBusinessDTO(businessDTO);
            }
        }

        dto.deltaTimes = systemVariables.getSystemVariable(id);

        return dto;
    }

    @Override
    public OdsResultDTO getTableFieldByQuery(OdsQueryDTO query) {
        OdsResultDTO array = new OdsResultDTO();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try {
            dataSourceConfig = userClient.getFiDataDataSourceById(query.dataSourceId);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            conn = getConnection(dataSourceConfig.data);
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setMaxRows(10);
            DataTranDTO dto = new DataTranDTO();
            dto.tableName = query.tableName;
            dto.querySql = query.querySql;
            Map<String, String> converSql = publishTaskClient.converSql(dto).data;
            query.querySql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            rs = st.executeQuery(query.querySql);
            // 获取数据集
            array = resultSetToJsonArrayDataAccess(rs);
            array.sql = query.querySql;
        } catch (SQLException e) {
            log.error("getTableFieldByQuery ex:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ":" + e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        ResultEntity<List<DataSourceDTO>> allSource = userClient.getAll();
        if (allSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }
        Optional<DataSourceDTO> first = allSource.data.stream().filter(e -> e.sourceBusinessType == SourceBusinessTypeEnum.DW).findFirst();
        if (!first.isPresent()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        //数据类型转换
        typeConversion(dataSourceConfig.data.conType, array.fieldNameDTOList, first.get().id);

        return array;

    }

    /**
     * 转换表字段类型和长度
     *
     * @param fieldType   fieldType
     * @param fieldLength fieldLength
     * @return target
     */
    private static List<String> transformField(String fieldType, String fieldLength) {

        Integer textLength = 5000;
        String timeType = "date";

        // 浮点型
        List<String> floatType = new ArrayList<>();
        floatType.add("double");

        // 文本类型
        List<String> textTpye = new ArrayList<>();
        textTpye.add("text");

        // 字符型
        List<String> charType = new ArrayList<>();
        charType.add("");

////        List<String> timeType = new ArrayList<>();
////        timeType.add("datetime");
////        timeType.add("");
////        timeType.add("");

        // Number型
        // 整型
        List<String> integerType = new ArrayList<>();
        integerType.add("tinyint");
        integerType.add("smallint");
        integerType.add("mediumint");
        integerType.add("int");
        integerType.add("integer");
        integerType.add("bigint");
        // 精确数值型
        List<String> accurateType = new ArrayList<>();
        accurateType.add("decimal");
        accurateType.add("numeric");
        // 货币、近似数值型
        List<String> otherType = new ArrayList<>();
        otherType.add("money");
        otherType.add("smallmoney");
        otherType.add("float");
        otherType.add("real");

        //
        List<String> fieldList = new LinkedList<>();

        // boolean类型长度放开
        if (integerType.contains(fieldType.toLowerCase())) {
            fieldList.add("INT");
            fieldList.add("0");
        } else if (textTpye.contains(fieldType.toLowerCase())) {
            fieldList.add("TEXT");
            fieldList.add("0");
        } else if (accurateType.contains(fieldType.toLowerCase()) || otherType.contains(fieldType.toLowerCase())) {
            fieldList.add("FLOAT");
            fieldList.add("0");
        } else if (Integer.parseInt(fieldLength) <= 1) {
            fieldList.add("NVARCHAR");
            fieldList.add("20");
        } else if (Integer.parseInt(fieldLength) >= textLength) {
            fieldList.add("NVARCHAR");
            fieldList.add("5000");
        } else if (fieldType.toLowerCase().contains(timeType)) {
            fieldList.add("TIMESTAMP");
            fieldList.add("6");
        } else {
            fieldList.add("NVARCHAR");
            fieldList.add(fieldLength);
        }
        return fieldList;
    }

    @Override
    public OdsResultDTO getDataAccessQueryList(OdsQueryDTO query) {
        AppDataSourcePO po = appDataSourceImpl.query().eq("id", query.appDataSourceId).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }
        AppRegistrationPO registration = appRegistrationImpl.query().eq("id", po.appId).one();
        if (registration == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        OdsResultDTO array = new OdsResultDTO();
        Instant inst1 = Instant.now();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum dataSourceTypeEnum = com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.getEnum(po.driveType.toUpperCase());
        try {
            // 如果是sapbw
            if (DataSourceTypeEnum.SAPBW.getName().equalsIgnoreCase(po.driveType)) {
                // 获取sapbw的destination和provider
                ProviderAndDestination providerAndDestination =
                        DbConnectionHelper.myDestination(po.host, po.sysNr, po.port, po.connectAccount, po.connectPwd, po.lang);
                JCoDestination destination = providerAndDestination.getDestination();
                MyDestinationDataProvider myProvider = providerAndDestination.getMyProvider();

                // 执行mdx语句，返回结果
                array = SapBwUtils.excuteMdx(destination, myProvider, query.mdxList);
                Instant inst2 = Instant.now();
                log.info("执行mdx时间 : " + Duration.between(inst1, inst2).toMillis());

            } else {
                AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
                conn = helper.connection(po.connectStr, po.connectAccount, po.connectPwd, dataSourceTypeEnum);
                st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                st.setMaxRows(10);

                //cdc模式
                if (po.driveType.equalsIgnoreCase(DataSourceTypeEnum.ORACLE_CDC.getName())) {
                    query.querySql = "SELECT * FROM " + query.querySql;
                }

                //系统变量替换
                if (!CollectionUtils.isEmpty(query.deltaTimes)) {
                    for (DeltaTimeDTO item : query.deltaTimes) {
                        boolean empty = StringUtils.isEmpty(item.variableValue);
                        if (item.deltaTimeParameterTypeEnum != DeltaTimeParameterTypeEnum.VARIABLE || empty) {
                            continue;
                        }
                        item.variableValue = AbstractCommonDbHelper.executeTotalSql(item.variableValue, conn, item.systemVariableTypeEnum.getName());
                    }
                }

                Instant inst2 = Instant.now();
                log.info("流式设置执行时间 : " + Duration.between(inst1, inst2).toMillis());
                Instant inst3 = Instant.now();
                String tableName = TableNameGenerateUtils.buildTableName(query.tableName, registration.appAbbreviation, registration.whetherSchema);
                log.info("时间增量值:{}", JSON.toJSONString(query.deltaTimes));
                // 传参改动
                DataTranDTO dto = new DataTranDTO();
                dto.tableName = tableName;
                dto.querySql = query.querySql;
                dto.driveType = po.driveType;
                dto.deltaTimes = JSON.toJSONString(query.deltaTimes);
                Map<String, String> converSql = publishTaskClient.converSql(dto).data;
                log.info("拼语句执行时间 : " + Duration.between(inst2, inst3).toMillis());

                String sql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
                rs = st.executeQuery(sql);
                Instant inst4 = Instant.now();
                log.info("执行sql时间 : " + Duration.between(inst3, inst4).toMillis());
                // 获取数据集
                array = resultSetToJsonArrayDataAccess(rs);

                Instant inst5 = Instant.now();
                log.info("封装数据执行时间 : " + Duration.between(inst4, inst5).toMillis());

                array.sql = sql;
            }
        } catch (Exception e) {
            log.error("数据接入执行自定义sql失败,ex:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        Instant inst5 = Instant.now();
        System.out.println("最终执行时间 : " + Duration.between(inst1, inst5).toMillis());

        //数据类型转换
        typeConversion(dataSourceTypeEnum, array.fieldNameDTOList, registration.targetDbId);

        return array;
    }

    /**
     * 不同数据库类型转换
     *
     * @param dataSourceTypeEnum
     * @param fieldList
     * @param targetDbId
     */
    public void typeConversion(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum dataSourceTypeEnum,
                               List<FieldNameDTO> fieldList,
                               Integer targetDbId) {

        //目标数据源
        ResultEntity<DataSourceDTO> targetDataSource = userClient.getFiDataDataSourceById(targetDbId);
        if (targetDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(dataSourceTypeEnum);
        DataTypeConversionDTO dto = new DataTypeConversionDTO();
        for (FieldNameDTO field : fieldList) {
            dto.dataLength = field.fieldLength;
            dto.dataType = field.fieldType;
            dto.precision = field.sourceFieldPrecision;
            String[] data = command.dataTypeConversion(dto, targetDataSource.data.conType);
            //设置目标字段类型
            field.fieldType = data[0].toUpperCase();
            field.fieldLength = data[1];
        }

    }

    @Override
    public ResultEntity<BuildPhysicalTableDTO> getBuildPhysicalTableDTO(long tableId, long appId) {

        //获取信息................
        //新建对象
        BuildPhysicalTableDTO dto = new BuildPhysicalTableDTO();
        //获取数据源
        AppDataSourcePO dataSourcePo = appDataSourceImpl.query().eq("id", appId).one();
        //获取应用信息
        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", dataSourcePo.appId).one();
        //获取物理表信息
        TableAccessPO tableAccessPo = this.query().eq("id", tableId).one();
        //获取物理表字段
        List<TableFieldsPO> listPo = tableFieldsImpl.query().eq("table_access_id", tableId).list();
        if (tableAccessPo == null || registrationPo == null || dataSourcePo == null || CollectionUtils.isEmpty(listPo)) {
            return ResultEntityBuild.build(ResultEnum.NIFI_NOT_FIND_DATA);
        }
        //获取物理表同步方式
        TableSyncmodePO tableSyncmodePo = tableSyncmodeImpl.query().eq("id", tableId).one();

        //装载参数................
        //装载同步方式
        dto.syncMode = tableSyncmodePo.syncMode;
        //装载数据源驱动
        dto.driveType = DbTypeEnum.getValue(dataSourcePo.driveType);
        //装载物理表字段集合
        dto.tableFieldsDTOS = TableFieldsMap.INSTANCES.listPoToDto(listPo);
        //装载应用简称
        dto.appAbbreviation = registrationPo.appAbbreviation;
        //装载物理表名
        dto.tableName = tableAccessPo.tableName;
        //装载查询sql
        dto.selectSql = tableAccessPo.sqlScript;
        //装载业务时间覆盖需要的条件sql  2023-04-28 李世纪：该属性目前不需要了，业务时间的覆盖语句已经包含条件sql
        dto.whereScript = "";
        // 非实时物理表才有sql
        if (!dto.driveType.getName().equals(DbTypeEnum.RestfulAPI.getName())
                && !dto.driveType.getName().equals(DbTypeEnum.api.getName())
                && !dto.driveType.getName().equals(DbTypeEnum.oracle_cdc.getName())
                && !dto.driveType.getName().equals(DbTypeEnum.ftp.getName())
                && !dto.driveType.getName().equals(DbTypeEnum.sftp.getName())) {
            String tableName = TableNameGenerateUtils.buildTableName(tableAccessPo.tableName, registrationPo.appAbbreviation, registrationPo.whetherSchema);
            DataTranDTO dtDto = new DataTranDTO();
            dtDto.tableName = tableName;
            dtDto.querySql = tableAccessPo.sqlScript;
            dtDto.driveType = dataSourcePo.driveType;
            Map<String, String> converSql = publishTaskClient.converSql(dtDto).data;
            //String sql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            dto.selectSql = tableAccessPo.sqlScript;
            dto.queryStartTime = converSql.get(SystemVariableTypeEnum.START_TIME.getValue());
            dto.queryEndTime = converSql.get(SystemVariableTypeEnum.END_TIME.getValue());
        }
        //        dto.selectSql = tableAccessPo.sqlScript;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    @Override
    public void updateTablePublishStatus(ModelPublishStatusDTO dto) {
        TableAccessPO model = baseMapper.selectById(dto.tableId);
        if (model != null) {
            baseMapper.updatePublishStatus(dto.tableId, dto.publish, StringUtils.isNotBlank(dto.publishErrorMsg) ? dto.publishErrorMsg : "");
            if (StringUtils.isNotEmpty(dto.subRunId)) {
                tableHistoryMapper.updateSubRunId(dto.tableHistoryId, dto.subRunId);
            }

        }
    }

    @Override
    public ResultEntity<ComponentIdDTO> getAppNameAndTableName(DataAccessIdsDTO dto) {

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", dto.appId).one();

        ComponentIdDTO componentIdDTO = new ComponentIdDTO();
        componentIdDTO.appName = registrationPo == null ? "" : registrationPo.appName;
        if (dto.tableId != null) {
            ChannelDataEnum type = ChannelDataEnum.getName(dto.flag);
            switch (type) {
                // 数据湖表任务
                case DATALAKE_TASK:
                    // 数据湖ftp任务
                case DATALAKE_FTP_TASK:
                    TableAccessPO accessPo = this.query().eq("id", dto.tableId).one();
                    componentIdDTO.tableName = accessPo == null ? "" : accessPo.tableName;
                    break;
                // 数据湖非实时api任务
                case DATALAKE_API_TASK:
                    ApiConfigPO apiConfigPo = this.apiConfigImpl.query().eq("id", dto.tableId).one();
                    componentIdDTO.tableName = apiConfigPo == null ? "" : apiConfigPo.apiName;
                    break;
                default:
                    break;
            }
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

    @Override
    public List<FieldNameDTO> getFieldList(TableAccessNonDTO dto) {

        // 查询tb_table_access数据
        TableAccessPO modelAccess = this.query().eq("id", dto.id).one();
        if (modelAccess == null) {
            throw new FkException(ResultEnum.TABLE_NOT_EXIST);
        }
        // 将应用名称封装进去
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", modelAccess.getAppId()).one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", dto.id).list();

        List<TableFieldsDTO> listField = list.stream().map(TableFieldsMap.INSTANCES::poToDto).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(listField)) {
            OdsQueryDTO queryDto = new OdsQueryDTO();
            // queryDto.appId = modelAccess.appId;
            queryDto.appDataSourceId = dto.appDataSourceId;
            queryDto.querySql = dto.sqlScript;
            queryDto.mdxList = dto.mdxList;
            queryDto.tableName = TableNameGenerateUtils.buildTableName(modelAccess.tableName, modelReg.appAbbreviation, modelReg.whetherSchema);
            return filterSqlFieldList(listField, queryDto);
        }
        return null;
    }

    @Override
    public ResultEnum cdcHeadConfig(CdcHeadConfigDTO dto) {
        TableAccessPO po = accessMapper.selectById(dto.dataAccessId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //判断job名称是否已存在
        QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(TableAccessPO::getPipelineName, dto.pipelineName);
        TableAccessPO selectOne = accessMapper.selectOne(queryWrapper);
        if (selectOne != null && selectOne.id != po.id) {
            throw new FkException(ResultEnum.PIPELINENAME_EXISTING);
        }

        po.checkPointInterval = dto.checkPointInterval;
        po.checkPointUnit = dto.checkPointUnit;
        po.pipelineName = dto.pipelineName;
        po.scanStartupMode = dto.scanStartupMode;
        return accessMapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    public OdsResultDTO cdcSetSourceInfo(OdsResultDTO data) {
        for (FieldNameDTO item : data.fieldNameDTOList) {
            oracleMappingFiDataFieldType(item);
        }
        return data;
    }

    /**
     * Oracle数据类型映射fiData类型
     *
     * @param dto
     * @return
     */
    public FieldNameDTO oracleMappingFiDataFieldType(FieldNameDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.sourceFieldType);
        switch (typeEnum) {
            case NUMBER:
                if (dto.sourceFieldPrecision > 0) {
                    dto.fieldType = FiDataDataTypeEnum.FLOAT.getName();
                    break;
                }
                dto.fieldType = FiDataDataTypeEnum.INT.getName();
                break;
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2:
            case NCHAR:
            case CHAR:
                dto.fieldType = FiDataDataTypeEnum.NVARCHAR.getName();
                break;
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
            case DATE:
                dto.fieldType = FiDataDataTypeEnum.TIMESTAMP.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                dto.fieldType = FiDataDataTypeEnum.FLOAT.getName();
                break;
            default:
                break;
        }
        return dto;
    }

    @Override
    public List<String> getUseExistTable() {
        //TODO cdc类型使用
       /* ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(odsSource);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        List<String> list = new ArrayList<>();

       IBuildAccessSqlCommand dbCommand = BuildFactoryAccessHelper.getDBCommand(dataSourceConfig.data.conType);
        String sql = dbCommand.buildUseExistTable();
        log.info("查询现有表sql:", sql);

        List<Map<String, Object>> resultMaps = AbstractDbHelper.execQueryResultMaps(sql, getConnection(dataSourceConfig.data));
        for (Map<String, Object> item : resultMaps) {
            list.add(item.get("name").toString());
        }*/

        return new ArrayList<>();
    }

    @Override
    public ResultEnum setKeepNumber(TableKeepNumberDTO dto) {
        // 查询物理表数据
        TableAccessPO model = baseMapper.selectById(dto.id);
        //获取不到，抛出异常
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "设置stg保存时间失败，物理表不存在");
        }
        // 查询app应用信息
        AppRegistrationPO appRegistrationPO = appRegistrationMapper.selectById(model.appId);
        //获取不到应用信息，则抛出异常
        if (Objects.isNull(appRegistrationPO)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "设置stg保存时间失败，应用不存在");
        }
        //获取应用下的数据源信息
        DataSourceDTO dataSourceDTO = tableFieldsImpl.getTargetDbInfo(appRegistrationPO.getTargetDbId());

        // 处理不同架构下的表名称
        String targetTableName = "";

        /*appRegistrationPO.whetherSchema
         * 是否将应用简称作为schema使用
         * 否：0  false
         * 是：1  true
         */
        if (appRegistrationPO.whetherSchema) {
            targetTableName = model.tableName;
        } else {
            targetTableName = "ods_" + appRegistrationPO.getAppAbbreviation() + "_" + model.getTableName();
        }

        List<String> stgAndTableName = tableFieldsImpl.getStgAndTableName(targetTableName, appRegistrationPO);

        //获取连接类型 根据连接类型的不同，生成不同的stg保存时间对应的sql
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum conType = dataSourceDTO.conType;
        //根据连接类型，获取对应连接类型的keepNumberSql实现类
        IBuildKeepNumber keepNumberSqlHelper = BuildKeepNumberSqlHelper.getKeepNumberSqlHelperByConType(conType);
        //获取keepNumberSql
        String keepNumberSql = keepNumberSqlHelper.setKeepNumberSql(dto, appRegistrationPO, stgAndTableName);
        return baseMapper.setKeepNumber(dto.id, dto.keepNumber, String.valueOf(keepNumberSql)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SET_KEEP_NUMBER_ERROR;
    }

    @Override
    public ApiColumnInfoDTO getTableColumnInfo(long tableAccessId) {
        TableAccessPO po = this.query().eq("id", tableAccessId).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ApiColumnInfoDTO dto = new ApiColumnInfoDTO();
        dto.tableName = po.tableName;
        dto.fieldNameDTOList = tableFieldsImpl.getTableFileInfo(tableAccessId);
        return dto;
    }

    /**
     * 通过表名（带架构）获取表信息
     *
     * @param tableName
     * @return
     */
    @Override
    public TableAccessDTO getAccessTableByTableName(String tableName) {
        //如果表名包含架构名，分别截取表名和架构名作为查询条件
        if (tableName.contains(".")) {
            String schemaName = tableName.split("\\.")[0];
            String tblName = tableName.split("\\.")[1];
            AppRegistrationPO app = appRegistration.getAppBySchemaName(schemaName);
            //获取表对应的应用id
            if (app == null) {
                return null;
            }
            long id = app.getId();
            LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TableAccessPO::getAppId, id)
                    .eq(TableAccessPO::getTableName, tblName);
            TableAccessPO one = getOne(wrapper);
            if (one == null) {
                return null;
            }
            return TableAccessMap.INSTANCES.poToDto(one);
        } else {
            //如果表名不包含架构名，则直接用表名作为条件查询物理表
            LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TableAccessPO::getTableName, tableName);
            TableAccessPO one = getOne(wrapper);
            if (one == null) {
                return null;
            }
            return TableAccessMap.INSTANCES.poToDto(one);
        }
    }

    /**
     * 通过应用id获取所选应用下的所有表--仅供智能发布调用
     *
     * @param appId
     * @return
     */
    @Override
    public List<TableAccessDTO> getTblByAppIdForSmart(Integer appId) {
        //使用jdbc的原因是绕开逻辑删除
        Connection connection = null;
        Statement statement = null;
        List<TableAccessDTO> tableAccessDTOS = new ArrayList<>();
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum dataSourceTypeEnum =
                com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.getEnum(dbType);
        try {
            connection = DbConnectionHelper.connection(accessConfigDbURL, username, pwd,
                    dataSourceTypeEnum);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from " + tbName + "where app_id = " + appId);

            while (resultSet.next()) {
                TableAccessDTO tableAccessDTO = new TableAccessDTO();
                tableAccessDTO.setId(Long.parseLong(resultSet.getString("id")));
                tableAccessDTO.setAppId(Long.parseLong(resultSet.getString("app_id")));
                tableAccessDTO.setApiId(Long.valueOf(resultSet.getString("api_id")));
                tableAccessDTO.setTableName(resultSet.getString("table_name"));
                tableAccessDTO.setTableDes(resultSet.getString("table_des"));
                tableAccessDTO.setIsRealtime(Integer.parseInt(resultSet.getString("is_realtime")));
                tableAccessDTO.setPublish(Integer.valueOf(resultSet.getString("publish")));
                tableAccessDTO.setSqlScript(resultSet.getString("sql_script"));
                tableAccessDTO.setSheet(resultSet.getString("sheet"));
                tableAccessDTO.setPublishErrorMsg(resultSet.getString("publish_error_msg"));
                tableAccessDTO.setCreateUser(resultSet.getString("create_user"));
                tableAccessDTO.setCreateTime(LocalDateTime.parse(resultSet.getString("create_time")));
                tableAccessDTO.setDelFlag(Integer.parseInt(resultSet.getString("del_flag")));
                tableAccessDTO.setUpdateUser(resultSet.getString("update_user"));
                tableAccessDTO.setUpdateTime(LocalDateTime.parse(resultSet.getString("update_time")));
                tableAccessDTO.setDisplayName(resultSet.getString("display_name"));
                tableAccessDTO.setKeepNumber(resultSet.getString("keep_number"));
                tableAccessDTO.setAppDataSourceId(Integer.valueOf(resultSet.getString("app_data_source_id")));
                tableAccessDTO.setStartLine(Integer.valueOf(resultSet.getString("start_line")));
                tableAccessDTO.setWhereScript(resultSet.getString("where_script"));
                tableAccessDTO.setCoverScript(resultSet.getString("cover_script"));
                tableAccessDTO.setDeleteStgScript(resultSet.getString("delete_stg_script"));
                tableAccessDTOS.add(tableAccessDTO);
            }
            return tableAccessDTOS;
        } catch (Exception e) {
            log.error("数据接入-智能发布-根据应用id获取物理表失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GET_TABLE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(connection);
        }
    }

    /**
     * 通过应用id获取所选应用下的所有表
     *
     * @param appId
     * @return
     */
    @Override
    public List<TableAccessDTO> getTblByAppId(Integer appId) {
        LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableAccessPO::getAppId, appId);
        List<TableAccessPO> list = list(wrapper);
        return TableAccessMap.INSTANCES.listPoToDto(list);
    }

    /**
     * 数接--回显统计当前数据接入总共有多少非实时表和实时api
     *
     * @return
     */
    @Override
    public PhyTblAndApiTblVO countTbl() {
        LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(TableAccessPO::getApiId);
        Integer phyCount = accessMapper.selectCount(wrapper);

        LambdaQueryWrapper<TableAccessPO> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.isNotNull(TableAccessPO::getApiId);
        Integer apiCount = accessMapper.selectCount(wrapper1);

        PhyTblAndApiTblVO vo = new PhyTblAndApiTblVO();
        vo.setPhyCount(phyCount);
        vo.setApiCount(apiCount);

        return vo;
    }

    /**
     * 根据应用id获取当前应用下表总数
     *
     * @return
     */
    @Override
    public Integer countTblByApp(Integer appId) {
        LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableAccessPO::getAppId, appId);
        return accessMapper.selectCount(wrapper);
    }

    /**
     * 首页--回显统计当前数据接入总共有多少表,多少重点接口，当日数据量等信息
     *
     * @return
     */
    @Override
    public AccessMainPageVO countTotal() {
        try {
            AccessMainPageVO vo = new AccessMainPageVO();
            LambdaQueryWrapper<TableAccessPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNull(TableAccessPO::getApiId);
            //非实时表个数
            Integer phyCount = accessMapper.selectCount(wrapper);

            LambdaQueryWrapper<TableAccessPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.isNotNull(TableAccessPO::getApiId);
            //实时表 restfulapi个数
            Integer apiCount = accessMapper.selectCount(wrapper1);

            //重点接口个数 非实时物理表
            LambdaQueryWrapper<TableAccessPO> wrapper2 = new LambdaQueryWrapper<>();
            //1代表是重点接口
            wrapper2.eq(TableAccessPO::getIsImportantInterface, 1);
            Integer count = accessMapper.selectCount(wrapper2);
            //重点接口个数 实时restfulapi
            LambdaQueryWrapper<ApiConfigPO> wrapper3 = new LambdaQueryWrapper<>();
            //1代表是重点接口
            wrapper3.eq(ApiConfigPO::getIsImportantInterface, 1);
            int restfulApiCount = apiConfigImpl.count(wrapper3);

            //当日接入数据总量
            ResultEntity<Long> result1 = publishTaskClient.accessDataTotalCount();
            Long dataTotal = result1.getData();
            if (result1.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }

            //成功次数和失败次数
            ResultEntity<AccessDataSuccessAndFailCountDTO> result2 = publishTaskClient.accessDataSuccessAndFailCount();
            AccessDataSuccessAndFailCountDTO dto = result2.getData();
            if (result2.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }

            //查询数据接入-ods库的存储数据总量（包括stg）
            String dataSize = countDbDataSize();

            vo.setInterfaceCount(phyCount + apiCount);
            vo.setDataCount(dataTotal);
            vo.setSuccessCount(dto.getSuccessCount());
            vo.setFailCount(dto.getFailCount());
            vo.setImportantInterfaceCount(count + restfulApiCount);
            vo.setDatastoreSize(dataSize);
            return vo;
        } catch (Exception e) {
            log.error("countTotal(),数据接入-首页展示查询失败!");
            throw new FkException(ResultEnum.ACCESS_MAINPAGE_SELECT_FAILURE, e);
        }
    }

    /**
     * 查询数据接入-ods库的存储数据总量（包括stg）
     *
     * @return
     */
    public String countDbDataSize() {
        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(2);
        if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        DataSourceDTO data = result.getData();
        com.fisk.common.core.enums.dataservice.DataSourceTypeEnum conType = data.getConType();
        IBuildFactoryDbDataSizeCount helper = DbDataSizeCountHelper.getDbDataSizeCountHelperByConType(conType);

        //去掉GB 交给前端显示
        return helper.DbDataStoredSize(data).replace("GB", "");
    }

}
