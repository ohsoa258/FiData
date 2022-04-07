package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    /**
     * 添加应用
     *
     * @param appRegistrationDTO 请求参数
     * @return 返回值
     */
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

        // 保存tb_app_registration数据
        boolean save = this.save(po);
        if (!save) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        //
        List<String> realtimeAccountList = appDataSourceMapper.getRealtimeAccountList();
        AppDataSourceDTO datasourceDTO = appRegistrationDTO.getAppDatasourceDTO();
        // 当前为实时应用
        if (po.appType == 0 && realtimeAccountList.contains(datasourceDTO.realtimeAccount)) {
            return ResultEntityBuild.build(ResultEnum.REALTIME_ACCOUNT_ISEXIST);
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

    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 返回值
     */
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

    /**
     * 应用注册-修改
     *
     * @param dto 请求参数
     * @return 返回值
     */
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

    /**
     * 删除应用注册
     * TODO: 删除应用时,同时删除下属所有物理表,nifi流程,元数据
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public ResultEntity<NifiVO> deleteAppRegistration(long id) {
        UserInfo userInfo = userHelper.getLoginUserInfo();

        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // atlas实例id
//        String atlasInstanceId = model.atlasInstanceId;

        // 1.删除tb_app_registration表数据
        int deleteReg = mapper.deleteByIdWithFill(model);
        if (deleteReg < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 2.删除tb_app_datasource表数据
        AppDataSourcePO modelDataSource = appDataSourceImpl.query()
                .eq("app_id", id)
                .one();

        int delDataSource = appDataSourceMapper.deleteByIdWithFill(modelDataSource);
        if (delDataSource < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 删除应用下的api(api是新增的功能,在改动最少代码的情况下,只删除api)
        QueryWrapper<ApiConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigPO::getAppId, model.id);
        List<ApiConfigPO> apiConfigPoList = apiConfigMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(apiConfigPoList)) {
            apiConfigPoList.forEach(e -> apiConfigMapper.deleteByIdWithFill(e));
        }

        // 删除应用下的物理表
        List<TableAccessPO> accessList = tableAccessImpl.query()
                .eq("app_id", model.id)
                .eq("del_flag", 1)
                .list();
        List<Long> tableIdList = new ArrayList<>();
        NifiVO vo = new NifiVO();
        List<TableListVO> tableList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(accessList)) {
            // 删表之前,要将所有的数据提前查出来,不然会导致空指针异常
            tableIdList = accessList.stream().map(TableAccessPO::getId).collect(Collectors.toList());
////            List<String> atlasTableIdList = accessList.stream().map(TableAccessPO::getAtlasTableId).collect(Collectors.toList());

            for (Long tableId : tableIdList) {
                TableListVO tableVO = new TableListVO();
                TableAccessPO po = tableAccessImpl.query().eq("id", tableId).eq("del_flag", 1).one();
////                tableVO.tableAtlasId = po.atlasTableId;
                tableVO.tableName = po.tableName;
                tableList.add(tableVO);
            }


            // 删除应用下面的所有表及表结构
            accessList.forEach(po -> tableAccessMapper.deleteByIdWithFill(po));
            // 先遍历accessList,取出每个对象中的id,再去tb_table_fields表中查询相应数据,将查询到的对象删除
            accessList.stream().map(
                            po -> tableFieldsImpl.query()
                                    .eq("table_access_id", po.id)
                                    .eq("del_flag", 1).list())
                    .flatMap(Collection::stream)
                    .forEachOrdered(po -> tableFieldsMapper.deleteByIdWithFill(po));
        }


        /*
          将方法的返回值封装
         */
        vo.userId = userInfo.id;
        vo.appId = String.valueOf(model.id);
        vo.tableIdList = tableIdList;
        // atlas应用id
//        vo.appAtlasId = atlasInstanceId;
        // atlas物理表信息
        vo.tableList = tableList;

        log.info("删除的应用信息,{}", vo);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);
    }

    /**
     * 查询所有应用名称(实时  非实时)
     *
     * @return 返回值
     */
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


    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
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

    /**
     * 查询所有非实时应用名称(弃用)
     *
     * @return 返回值
     */
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
//        modelData.atlasDbId = atlasDbId;
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
//        String key = "app_name";
//        StringBuilder querySql = getQuerySql(key, query.value);

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
}
