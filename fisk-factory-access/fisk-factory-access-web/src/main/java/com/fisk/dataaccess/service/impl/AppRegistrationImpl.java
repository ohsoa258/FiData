package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datafactory.AccessRedirectDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class AppRegistrationImpl extends ServiceImpl<AppRegistrationMapper, AppRegistrationPO> implements IAppRegistration {

    @Resource
    private AppDataSourceMapper appDataSourceMapper;
    @Resource
    private AppRegistrationMapper mapper;
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
    RedisUtil redisUtil;

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
        List<String> appAbbreviationList = baseMapper.getAppAbbreviation();
        if (appAbbreviationList.contains(po.appAbbreviation)) {
            return ResultEntityBuild.build(ResultEnum.DATAACCESS_APPABBREVIATION_SUCCESS);
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

        AtlasEntityQueryVO vo = new AtlasEntityQueryVO();
        vo.userId = userId;
        vo.appId = String.valueOf(po.getId());

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
        // long totalPage = (long) (records1.size() + rows - 1) / rows;
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

        return appDataSourceMapper.updateById(modelDataSource) > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
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

        if (!CollectionUtils.isEmpty(accessList)) {
            // 删表之前,要将所有的数据提前查出来,不然会导致空指针异常
            tableIdList = accessList.stream().map(TableAccessPO::getId).collect(Collectors.toList());

            for (Long tableId : tableIdList) {
                TableListVO tableVO = new TableListVO();
                TableAccessPO po = tableAccessImpl.query().eq("id", tableId).eq("del_flag", 1).one();
                tableVO.tableName = model.appAbbreviation + "_" + po.tableName;
                tableList.add(tableVO);
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

        log.info("删除的应用信息,{}", vo);

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
//        modelReg.atlasInstanceId = atlasInstanceId;
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
        return getMetadata.getMetadataList(
                "dmp_datainput_db",
                "tb_app_registration",
                "",
                FilterSqlConstants.APP_REGISTRATION_SQL);
    }

    @Override
    public List<AppNameDTO> getDataList() {

        return baseMapper.getDataList();
    }

    @SneakyThrows
    @Override
    public ResultEntity<Object> connectDb(DbConnectionDTO dto) {
        Connection conn = null;
        try {
            switch (dto.driveType) {
                case "mysql":
                    Class.forName(DriverTypeEnum.MYSQL.getName());
                    conn = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    return ResultEntityBuild.build(ResultEnum.SUCCESS);
                case "sqlserver":
                    //1.加载驱动程序
                    Class.forName(DriverTypeEnum.SQLSERVER.getName());
                    //2.获得数据库的连接
                    conn = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    return ResultEntityBuild.build(ResultEnum.SUCCESS);
                case "oracle":
                    Class.forName(DriverTypeEnum.ORACLE.getName());
                    conn = DriverManager.getConnection(dto.connectStr, dto.connectAccount, dto.connectPwd);
                    return ResultEntityBuild.build(ResultEnum.SUCCESS);
                default:
                    return ResultEntityBuild.build(ResultEnum.DATAACCESS_CONNECTDB_WARN);
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
            return ResultEntityBuild.build(ResultEnum.DATAACCESS_CONNECTDB_ERROR);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new FkException(ResultEnum.DATAACCESS_CONNECTDB_ERROR);
            }
        }

    }

    @Override
    public ResultEntity<Object> getRepeatAppName(String appName) {

        List<String> appNameList = baseMapper.getAppName();

        return appNameList.contains(appName) ? ResultEntityBuild.build(ResultEnum.DATAACCESS_APPNAME_ERROR) : ResultEntityBuild.build(ResultEnum.DATAACCESS_APPNAME_SUCCESS);
    }

    @Override
    public ResultEntity<Object> getRepeatAppAbbreviation(String appAbbreviation) {
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
    public List<FiDataMetaDataDTO> setDataAccessStructure(FiDataMetaDataReqDTO reqDto) {

        List<FiDataMetaDataDTO> list = new ArrayList<>();
        FiDataMetaDataDTO dto = new FiDataMetaDataDTO();
        // FiData数据源id: 数据资产自定义
        dto.setDataSourceId(Integer.parseInt(StringUtils.isBlank(reqDto.dataSourceId) ? String.valueOf(0) : reqDto.dataSourceId));

        // 第一层id
        String uuid = UUID.randomUUID().toString();
        List<FiDataMetaDataTreeDTO> dataTreeList = new ArrayList<>();
        FiDataMetaDataTreeDTO dataTree = new FiDataMetaDataTreeDTO();
        dataTree.setId(uuid);
        dataTree.setParentId("-1");
        dataTree.setLabel("dmp_ods");
        dataTree.setLabelAlias("dmp_ods");
        dataTree.setLevelType(LevelTypeEnum.FOLDER);

        // 封装data-access所有结构数据
        dataTree.setChildren(buildChildren(uuid));
        dataTreeList.add(dataTree);

        dto.setChildren(dataTreeList);
        list.add(dto);

        if (!CollectionUtils.isEmpty(list)) {
            redisUtil.set(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId), JSON.toJSONString(list));
        }

        return list;
    }

    /**
     * 构建data-access子集树
     *
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/15 17:46
     * @param id guid
     */
    private List<FiDataMetaDataTreeDTO> buildChildren(String id) {

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

        appTreeByRealTime.setChildren(getFiDataMetaDataTreeByRealTime(id, appPoList));
        appTreeByNonRealTime.setChildren(getFiDataMetaDataTreeByNonRealTime(id, appPoList));

        appTypeTreeList.add(appTreeByRealTime);
        appTypeTreeList.add(appTreeByNonRealTime);

        return appTypeTreeList;
    }

    /**
     * 获取实时应用结构
     *
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/16 15:21
     * @params id guid
     * @param appPoList 所有的应用实体对象
     */
    private List<FiDataMetaDataTreeDTO> getFiDataMetaDataTreeByRealTime(String id, List<AppRegistrationPO> appPoList) {
        return appPoList.stream()
                .filter(Objects::nonNull)
                // 实时应用
                .filter(e -> e.appType == 0)
                .map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    String appGuid = UUID.randomUUID().toString();
                    // 当前层默认生成的uuid
                    appDtoTree.setId(appGuid);
                    // 上一级的id
                    appDtoTree.setParentId(id);
                    appDtoTree.setLabel(app.appName);
                    appDtoTree.setLabelAlias(app.appAbbreviation);
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
                                        String apiGuid = UUID.randomUUID().toString();
                                        apiDtoTree.setId(apiGuid);
                                        apiDtoTree.setParentId(appGuid);
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
                                                    String tableGuid = UUID.randomUUID().toString();
                                                    tableDtoTree.setId(tableGuid);
                                                    tableDtoTree.setParentId(apiGuid);
                                                    tableDtoTree.setLabel(table.tableName);
                                                    tableDtoTree.setLabelAlias(table.tableName);
                                                    tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                                    if (table.publish == null) {
                                                        tableDtoTree.setPublishState("0");
                                                        table.publish = 0;
                                                    }
                                                    tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                                    tableDtoTree.setLabelDesc(table.tableDes);

                                                    // 第四层: field层
                                                    List<FiDataMetaDataTreeDTO> fieldTreeList = this.tableFieldsImpl.query()
                                                            .eq("table_access_id", table.id)
                                                            .list()
                                                            .stream()
                                                            .filter(Objects::nonNull)
                                                            .map(field -> {

                                                                FiDataMetaDataTreeDTO fieldDtoTree = new FiDataMetaDataTreeDTO();
                                                                String fieldGuid = UUID.randomUUID().toString();
                                                                fieldDtoTree.setId(fieldGuid);
                                                                fieldDtoTree.setParentId(tableGuid);
                                                                fieldDtoTree.setLabel(field.fieldName);
                                                                fieldDtoTree.setLabelAlias(field.fieldName);
                                                                fieldDtoTree.setLevelType(LevelTypeEnum.FIELD);
                                                                fieldDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                                                fieldDtoTree.setLabelLength(String.valueOf(field.fieldLength));
                                                                fieldDtoTree.setLabelType(field.fieldType);
                                                                fieldDtoTree.setLabelDesc(field.fieldDes);

                                                                return fieldDtoTree;
                                                            }).collect(Collectors.toList());

                                                    // table的子级
                                                    tableDtoTree.setChildren(fieldTreeList);
                                                    return tableDtoTree;
                                                }).collect(Collectors.toList());

                                        // api的子级
                                        apiDtoTree.setChildren(tableTreeList);
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
    }

    /**
     * 获取非实时应用结构
     *
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/16 15:21
     * @param id guid
     * @param appPoList 所有的应用实体对象
     */
    private List<FiDataMetaDataTreeDTO> getFiDataMetaDataTreeByNonRealTime(String id, List<AppRegistrationPO> appPoList) {
        return appPoList.stream()
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
                                                    tableDtoTree.setLabel(table.tableName);
                                                    tableDtoTree.setLabelAlias(table.tableName);
                                                    tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                                    if (table.publish == null) {
                                                        tableDtoTree.setPublishState("0");
                                                        table.publish = 0;
                                                    } else {
                                                        tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                                    }
                                                    tableDtoTree.setLabelDesc(table.tableDes);

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

                                                                return fieldDtoTree;
                                                            }).collect(Collectors.toList());

                                                    // table的子级
                                                    tableDtoTree.setChildren(fieldTreeList);
                                                    return tableDtoTree;
                                                }).collect(Collectors.toList());

                                        // api的子级
                                        apiDtoTree.setChildren(tableTreeList);
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
                                        tableDtoTree.setLabel(table.tableName);
                                        tableDtoTree.setLabelAlias(table.tableName);
                                        tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                                        if (table.publish == null) {
                                            tableDtoTree.setPublishState("0");
                                            table.publish = 0;
                                        } else {
                                            tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                        }
                                        tableDtoTree.setLabelDesc(table.tableDes);

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

                                                    return fieldDtoTree;
                                                }).collect(Collectors.toList());

                                        // table的子级
                                        tableDtoTree.setChildren(fieldTreeList);
                                        return tableDtoTree;
                                    }).collect(Collectors.toList());

                            appDtoTree.setChildren(tableTreeList);
                            break;
                        case RestfulAPI:
                        default:
                            break;
                    }
                    return appDtoTree;
                }).collect(Collectors.toList());
    }
}
