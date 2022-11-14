package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.CreateSchemaSqlUtils;
import com.fisk.common.core.utils.TableNameGenerateUtils;
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
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.GetConfigDTO;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datafactory.AccessRedirectDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobParameterDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.utils.httprequest.Impl.BuildHttpRequestImpl;
import com.fisk.dataaccess.utils.sql.*;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.*;
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
    GetConfigDTO getConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEntity<AtlasEntityQueryVO> addData(AppRegistrationDTO appRegistrationDTO) {

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

        //
        List<String> realtimeAccountList = appDataSourceMapper.getRealtimeAccountList();
        AppDataSourceDTO datasourceDTO = appRegistrationDTO.getAppDatasourceDTO();
        // 当前为实时应用
        if (po.appType == 0 && realtimeAccountList.contains(datasourceDTO.realtimeAccount)) {
            return ResultEntityBuild.build(ResultEnum.REALTIME_ACCOUNT_ISEXIST);
        }

        // 保存tb_app_registration数据
        boolean save = this.save(po);
        if (!save) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        AppDataSourcePO modelDataSource = AppDataSourceMap.INSTANCES.dtoToPo(datasourceDTO);
        // 保存tb_app_datasource数据
        modelDataSource.setAppId(po.getId());
        modelDataSource.setCreateUser(String.valueOf(userId));

        int insert = appDataSourceMapper.insert(modelDataSource);
        if (insert <= 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        //jtw类型配置返回结果json串
        if (appRegistrationDTO.appDatasourceDTO.authenticationMethod != null && appRegistrationDTO.appDatasourceDTO.authenticationMethod == 3) {
            AppDataSourceDTO dataSourceByAppId = appDataSourceImpl.getDataSourceByAppId(po.getId());
            apiResultConfig.apiResultConfig(dataSourceByAppId.id, appRegistrationDTO.appDatasourceDTO.apiResultConfigDtoList);
        }

        AtlasEntityQueryVO vo = new AtlasEntityQueryVO();
        vo.userId = userId;
        vo.appId = String.valueOf(po.getId());

        //是否添加schema
        if (appRegistrationDTO.whetherSchema) {
            VerifySchema(po.appAbbreviation, po.targetDbId);
        }

        // 添加元数据信息
        /*ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
        classificationInfoDto.setName(appRegistrationDTO.appName + "_" + appRegistrationDTO.appAbbreviation);
        classificationInfoDto.setDescription(appRegistrationDTO.appDes);
        classificationInfoDto.setSourceType(1);
        classificationInfoDto.setDelete(false);
        try {
            dataManageClient.appSynchronousClassification(classificationInfoDto);
        } catch (Exception e) {
            // 不同场景下，元数据可能不会部署，在这里只做日志记录，不影响正常流程
            log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
        }*/

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);
    }

    @Override
    public PageDTO<AppRegistrationDTO> listAppRegistration(String key, Integer page, Integer rows) {

        Page<AppRegistrationPO> pageReg = new Page<>(page, rows);

        boolean isKeyExists = StringUtils.isNoneBlank(key);
        query().like(isKeyExists, "app_name", key)
                // 未删除
                .eq("del_flag", 1)
                .page(pageReg);

        // 分页封装
        Page<AppRegistrationPO> poPage = new Page<>(page, rows);

        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();

        // 查询数据
        queryWrapper.like(isKeyExists, "app_name", key)
                .eq("del_flag", 1)
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
        AppDataSourceDTO appDatasourceDTO = dto.getAppDatasourceDTO();
        AppDataSourcePO modelDataSource = AppDataSourceMap.INSTANCES.dtoToPo(appDatasourceDTO);

        // 实时应用
        if (po.appType == 0) {
            QueryWrapper<AppDataSourcePO> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(AppDataSourcePO::getRealtimeAccount, appDatasourceDTO.realtimeAccount);
            AppDataSourcePO appDataSourcePo = appDataSourceMapper.selectOne(wrapper);
            if (appDataSourcePo != null && appDataSourcePo.id != appDatasourceDTO.id) {
                return ResultEnum.REALTIME_ACCOUNT_ISEXIST;
            }
        }

        // 2.2修改数据
        long appDataSid = appDataSourceImpl.query().eq("app_id", id).one().getId();
        modelDataSource.setId(appDataSid);
        modelDataSource.setAppId(id);

        //jtw类型配置返回结果json串
        if (dto.appDatasourceDTO.authenticationMethod != null && dto.appDatasourceDTO.authenticationMethod == 3) {
            AppDataSourceDTO dataSourceByAppId = appDataSourceImpl.getDataSourceByAppId(po.getId());
            apiResultConfig.apiResultConfig(dataSourceByAppId.id, dto.appDatasourceDTO.apiResultConfigDtoList);
        }

        return appDataSourceMapper.updateById(modelDataSource) > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
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

        // 1.删除tb_app_registration表数据
        int deleteReg = mapper.deleteByIdWithFill(model);
        if (deleteReg < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 2.删除tb_app_datasource表数据
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", id).one();

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
            accessList.stream().map(
                            po -> tableFieldsImpl.query()
                                    .eq("table_access_id", po.id)
                                    .eq("del_flag", 1).list())
                    .flatMap(Collection::stream)
                    .forEachOrdered(po -> tableFieldsMapper.deleteByIdWithFill(po));
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
        log.info("删除的应用信息,{}", vo);

        // 删除元数据信息
        ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
        classificationInfoDto.setName(model.appName + "_" + model.appAbbreviation);
        classificationInfoDto.setDescription(model.appDes);
        classificationInfoDto.setSourceType(1);
        classificationInfoDto.setDelete(true);
        try {
            dataManageClient.appSynchronousClassification(classificationInfoDto);
        } catch (Exception e) {
            // 不同场景下，元数据可能不会部署，在这里只做日志记录，不影响正常流程
            log.error("远程调用失败，方法名：【dataManageClient:appSynchronousClassification】");
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);
    }

    @Override
    public List<AppNameDTO> queryAppName() {

        // 查询所有应用名称
        List<AppRegistrationPO> list = this.query()
                .eq("del_flag", 1)
                .orderByDesc("create_time")
                .list();
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

        AppRegistrationPO modelReg = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();
        AppRegistrationDTO appRegistrationDTO = AppRegistrationMap.INSTANCES.poToDto(modelReg);

        AppDataSourcePO modelDataSource = appDataSourceImpl.query()
                .eq("app_id", id)
                .eq("del_flag", 1)
                .one();
        AppDataSourceDTO appDataSourceDTO = AppDataSourceMap.INSTANCES.poToDto(modelDataSource);
        // 数据库密码不展示
        appDataSourceDTO.connectPwd = "";

        //jwt类型展示返回结果json配置
        if (appDataSourceDTO.authenticationMethod != null && appDataSourceDTO.authenticationMethod == 3) {
            appDataSourceDTO.apiResultConfigDtoList = apiResultConfig.getApiResultConfig(modelDataSource.id);
        }

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

        List<AppRegistrationPO> list = this.query()
                .eq("del_flag", 1)
                .eq("app_type", 1)
                .list();
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

            AppRegistrationPO modelReg = this.query().eq("id", id)
                    .eq("del_flag", 1)
                    .one();

            AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", id)
                    .eq("del_flag", 1)
                    .one();

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

        AppRegistrationPO modelReg = this.query().eq("id", appid)
                .eq("del_flag", 1)
                .one();
        if (modelReg == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 保存tb_app_registration
        boolean update = this.updateById(modelReg);
        if (!update) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        AppDataSourcePO modelData = appDataSourceImpl.query()
                .eq("app_id", appid)
                .eq("del_flag", 1)
                .one();
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

        return baseMapper.filter(query.page, data);
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
        List<String> allDatabases = new ArrayList<>();

        DataSourceTypeEnum driveType = DataSourceTypeEnum.getValue(dto.driveType);
        try {
            Connection conn = null;
            OracleUtils oracleUtils = new OracleUtils();
            switch (Objects.requireNonNull(driveType)) {
                case MYSQL:
                    MysqlConUtils mysqlConUtils = new MysqlConUtils();
                    url = "jdbc:mysql://" + dto.host + ":" + dto.port;
                    conn = DbConnectionHelper.connection(url, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
                    allDatabases.addAll(mysqlConUtils.getAllDatabases(conn));
                    break;
                case POSTGRESQL:
                    url = "jdbc:postgresql://" + dto.host + ":" + dto.port + "/postgres";
                    PgsqlUtils pgsqlUtils = new PgsqlUtils();
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
                case ORACLE_CDC:
                    conn = DbConnectionHelper.connection(dto.connectStr, dto.connectAccount, dto.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE);
                    allDatabases.addAll(oracleUtils.getAllDatabases(conn));
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("测试连接失败:{}", e);
            throw new FkException(ResultEnum.DATAACCESS_CONNECTDB_ERROR);
        }

        final int[] count = {1};

        return allDatabases.stream().filter(Objects::nonNull)
                .map(e -> {
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
        if (DbTypeEnum.RestfulAPI.getName().equalsIgnoreCase(appDataSourcePo.driveType)) {
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
        if (dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.MYSQL.getName()) ||
                dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName()) ||
                dto.getDriveType().equalsIgnoreCase(DataSourceTypeEnum.ORACLE.getName())) {
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
        tableRuleInfoDto.name = TableNameGenerateUtils.buildOdsTableName(data.tableName,
                appRegistrationDto.appAbbreviation, appRegistrationDto.whetherSchema);
        // 类型 1: 表
        tableRuleInfoDto.type = 1;

        if (!CollectionUtils.isEmpty(data.list)) {
            StringBuilder transformationRules = new StringBuilder();
            data.list.stream()
                    .filter(Objects::nonNull)
                    .forEach(e -> {
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

        return dto.getTableUniques().keySet().stream()
                .map(e -> {
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

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(fiDataDataSourceById.data.conType);
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

        FiDataMetaDataTreeDTO appTreeByNonRealTime = new FiDataMetaDataTreeDTO();
        String appTreeByNonRealTimeGuid = UUID.randomUUID().toString();
        appTreeByNonRealTime.setId(appTreeByNonRealTimeGuid);
        appTreeByNonRealTime.setParentId(id);
        appTreeByNonRealTime.setLabel("非实时应用");
        appTreeByNonRealTime.setLabelAlias("非实时应用");
        appTreeByNonRealTime.setLevelType(LevelTypeEnum.FOLDER);

        // 所有应用
        List<AppRegistrationPO> appPoList = this.query().orderByDesc("create_time").list();
        // 所有应用下表字段信息
        List<FiDataMetaDataTreeDTO> tableFieldList = new ArrayList<>();

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTreeByRealTime = getFiDataMetaDataTreeByRealTime(id, appPoList);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTreeByRealTime = fiDataMetaDataTreeByRealTime.entrySet().iterator().next();
        appTreeByRealTime.setChildren(nextTreeByRealTime.getValue());
        tableFieldList.addAll(nextTreeByRealTime.getKey());

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTreeByNonRealTime = getFiDataMetaDataTreeByNonRealTime(id, appPoList);
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
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> getFiDataMetaDataTreeByRealTime(String id, List<AppRegistrationPO> appPoList) {
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = appPoList.stream()
                .filter(Objects::nonNull)
                // 实时应用
                .filter(e -> e.appType == 0)
                .map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    // 当前层默认生成的uuid
                    appDtoTree.setId(String.valueOf(app.id));
                    // 上一级的id
                    appDtoTree.setParentId(id);
                    appDtoTree.setLabel(app.appAbbreviation);
                    appDtoTree.setLabelAlias(app.appName);
                    appDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                    appDtoTree.setLabelDesc(app.appDes);

                    // 第二层: api层
                    // 查询驱动类型
                    AppDataSourcePO dataSourcePo = this.appDataSourceImpl.query().eq("app_id", app.id).one();
                    DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.getValue(dataSourcePo.driveType);
                    // 根据驱动类型封装不同的子级
                    switch (Objects.requireNonNull(dataSourceTypeEnum)) {
                        case RestfulAPI:
                            // 当前app下的所有api
                            List<FiDataMetaDataTreeDTO> apiTreeList = this.apiConfigImpl.query()
                                    .eq("app_id", app.id)
                                    .orderByDesc("create_time")
                                    .list()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(api -> {
                                        FiDataMetaDataTreeDTO apiDtoTree = new FiDataMetaDataTreeDTO();
                                        apiDtoTree.setId(String.valueOf(api.id));
                                        apiDtoTree.setParentId(String.valueOf(app.id));
                                        apiDtoTree.setLabel(api.apiName);
                                        apiDtoTree.setLabelAlias(api.apiName);
                                        apiDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                                        // 不是已发布的都当作未发布处理
                                        if (api.publish == null) {
                                            apiDtoTree.setPublishState("0");
                                        } else {
                                            apiDtoTree.setPublishState(String.valueOf(api.publish != 1 ? 0 : 1));
                                        }
                                        apiDtoTree.setLabelDesc(api.apiDes);

                                        // 第三层: table层
                                        List<FiDataMetaDataTreeDTO> tableTreeList = this.tableAccessImpl.query()
                                                .eq("api_id", api.id)
                                                .orderByDesc("create_time")
                                                .list()
                                                .stream()
                                                .filter(Objects::nonNull)
                                                .map(table -> {
                                                    FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                                                    tableDtoTree.setId(String.valueOf(table.id));
                                                    tableDtoTree.setParentId(String.valueOf(api.id));
                                                    tableDtoTree.setLabel(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                                    tableDtoTree.setLabelAlias(table.tableName);
                                                    tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                                    tableDtoTree.setSourceType(1);
                                                    tableDtoTree.setSourceId(Integer.parseInt(id));
                                                    if (table.publish == null) {
                                                        tableDtoTree.setPublishState("0");
                                                        table.publish = 0;
                                                    }
                                                    tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                                    tableDtoTree.setLabelDesc(table.tableDes);
                                                    tableDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());

                                                    // 第四层: field层
                                                    List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query()
                                                            .eq("table_access_id", table.id)
                                                            .list()
                                                            .stream()
                                                            .filter(Objects::nonNull)
                                                            .map(field -> {

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
                                                                fieldDtoTree.setParentName(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                                                fieldDtoTree.setParentNameAlias(table.tableName);
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
                        case API:
                        case MYSQL:
                        case SQLSERVER:
                        case ORACLE:
                        case POSTGRESQL:
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
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> getFiDataMetaDataTreeByNonRealTime(String id, List<AppRegistrationPO> appPoList) {
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = appPoList.stream()
                .filter(Objects::nonNull)
                // 非实时应用
                .filter(e -> e.appType == 1)
                .map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    appDtoTree.setId(String.valueOf(app.id));
                    // 上一级的id
                    appDtoTree.setParentId(id);
                    appDtoTree.setLabel(app.appName);
                    appDtoTree.setLabelAlias(app.appAbbreviation);
                    appDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                    appDtoTree.setLabelDesc(app.appDes);

                    // 查询驱动类型
                    AppDataSourcePO dataSourcePo = this.appDataSourceImpl.query().eq("app_id", app.id).one();
                    DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.getValue(dataSourcePo.driveType);
                    // 根据驱动类型封装不同的子级
                    switch (Objects.requireNonNull(dataSourceTypeEnum)) {
                        // 第二层: api层
                        case API:
                            // 当前app下的所有api
                            List<FiDataMetaDataTreeDTO> apiTreeList = this.apiConfigImpl.query()
                                    .eq("app_id", app.id)
                                    .orderByDesc("create_time")
                                    .list()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(api -> {
                                        FiDataMetaDataTreeDTO apiDtoTree = new FiDataMetaDataTreeDTO();
                                        apiDtoTree.setId(String.valueOf(api.id));
                                        apiDtoTree.setParentId(String.valueOf(app.id));
                                        apiDtoTree.setLabel(api.apiName);
                                        apiDtoTree.setLabelAlias(api.apiName);
                                        apiDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                                        // 不是已发布的都当作未发布处理
                                        if (api.publish == null) {
                                            apiDtoTree.setPublishState("0");
                                        } else {
                                            apiDtoTree.setPublishState(String.valueOf(api.publish != 1 ? 0 : 1));
                                        }
                                        apiDtoTree.setLabelDesc(api.apiDes);

                                        // 第三层: table层
                                        List<FiDataMetaDataTreeDTO> tableTreeList = this.tableAccessImpl.query()
                                                .eq("api_id", api.id)
                                                .orderByDesc("create_time")
                                                .list()
                                                .stream()
                                                .filter(Objects::nonNull)
                                                .map(table -> {
                                                    FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                                                    tableDtoTree.setId(String.valueOf(table.id));
                                                    tableDtoTree.setParentId(String.valueOf(api.id));
                                                    tableDtoTree.setLabel(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                                    tableDtoTree.setLabelAlias(table.tableName);
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
                                                    List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query()
                                                            .eq("table_access_id", table.id)
                                                            .list()
                                                            .stream()
                                                            .filter(Objects::nonNull)
                                                            .map(field -> {

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
                        case MYSQL:
                        case SQLSERVER:
                        case ORACLE:
                        case POSTGRESQL:

                            List<FiDataMetaDataTreeDTO> tableTreeList = this.tableAccessImpl.query()
                                    .eq("app_id", app.id)
                                    .orderByDesc("create_time")
                                    .list()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(table -> {
                                        FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                                        tableDtoTree.setId(String.valueOf(table.id));
                                        tableDtoTree.setParentId(String.valueOf(app.id));
                                        tableDtoTree.setLabel(TableNameGenerateUtils.buildOdsTableName(table.tableName, app.appAbbreviation, app.whetherSchema));
                                        tableDtoTree.setLabelAlias(table.tableName);
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
                                        List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query()
                                                .eq("table_access_id", table.id)
                                                .list()
                                                .stream()
                                                .filter(Objects::nonNull)
                                                .map(field -> {

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
                        case RestfulAPI:
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
        List<DataSourceDTO> collect = allExternalDataSource.data.stream()
                .filter(e -> SourceBusinessTypeEnum.ODS.getName().equals(e.sourceBusinessType.getName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return new ArrayList<>();
        }
        List<ExternalDataSourceDTO> list = new ArrayList<>();
        for (DataSourceDTO item : collect) {
            ExternalDataSourceDTO data = new ExternalDataSourceDTO();
            data.id = item.id;
            data.name = item.name;
            list.add(data);
        }
        return list;
    }

}
