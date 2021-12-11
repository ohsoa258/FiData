package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.dto.datamodel.AppRegistrationDataDTO;
import com.fisk.dataaccess.dto.datamodel.TableAccessDataDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.map.TableAccessMap;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.utils.MysqlConUtils;
import com.fisk.dataaccess.utils.SqlServerConUtils;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.TableNameVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.*;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OdsDataSyncTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;
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
    private AppRegistrationImpl appRegistrationImpl;
    @Resource
    private AppRegistrationMapper registrationMapper;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private TableSyncmodeImpl syncmodeImpl;
    @Resource
    private TableSyncmodeMapper syncmodeMapper;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private TableBusinessImpl businessImpl;
    @Resource
    private TableBusinessMapper businessMapper;
    @Resource
    private NifiConfigMapper nifiConfigMapper;
    @Resource
    private NifiConfigImpl nifiConfigImpl;
    @Value("${spring.datasource.url}")
    private String jdbcStr;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;
    @Resource
    private TableSyncmodeImpl tableSyncmodeImpl;
    @Resource
    private EtlIncrementalMapper etlIncrementalMapper;
    @Value("${pgsql-datamodel.url}")
    public String pgsqlDatamodelUrl;
    @Value("${pgsql-datamodel.username}")
    public String pgsqlDatamodelUsername;
    @Value("${pgsql-datamodel.password}")
    public String pgsqlDatamodelPassword;

    @Value("${pgsql-ods.url}")
    public String pgsqlOdsUrl;
    @Value("${pgsql-ods.username}")
    public String pgsqlOdsUsername;
    @Value("${pgsql-ods.password}")
    public String pgsqlOdsPassword;

    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO 请求参数
     * @return 返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addRealTimeData(TableAccessDTO tableAccessDTO) {

        // TODO: 原始SQL表创建(暂时不用集成)
        // 根据应用名称,查询出具体的数据源驱动(现阶段是MySqL和SQL Server)
////        AppRegistrationPO one = appRegistrationImpl.query()
////                .eq("app_name", tableAccessDTO.getAppName())
////                .eq("del_flag", 1)
////                .one();
////        // 0-1.获取appid
////        long appid = one.getId();
////
////        // 0-2.根据id查询数据源驱动类型(appid就是tb_app_drivetype表的id)
////        AppDriveTypePO driveTypePO = appDriveTypeImpl.query().eq("id", appid).one();
////        String driveName = driveTypePO.getName(); // 数据源驱动名称
////        if (driveName.equalsIgnoreCase("MySqL")) {
////            // 先创建表
////            MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
////
////            int i = mysqlTableUtils.createmysqltb(tableAccessDTO);
////            if (i != 0) {
////                throw new FkException(500, "创建" + tableAccessDTO.getTableName() + "表失败");
////            }
////        } else {
////            SqlServerTableUtils sqlServerTableUtils = new SqlServerTableUtils();
////            int i = sqlServerTableUtils.createSqlServerTB(tableAccessDTO);
////            if (i != 0) {
////                throw new FkException(500, "创建" + tableAccessDTO.getTableName() + "表失败");
////            }
////        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        // 数据保存: 添加应用的时候,相同的表名不可以再次添加
        /*List<String> tableNameList = baseMapper.getTableName();
        String tableName = modelAccess.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            return ResultEnum.Table_NAME_EXISTS;
        }*/
//        AppRegistrationPO modelReg = appRegistrationImpl.query()
//                .eq("app_name", tableAccessDTO.getAppName())
//                .eq("del_flag", 1)
//                .one();

        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        String tableName = modelAccess.getTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
//        tableNameVO.appId = modelReg.id;
        tableNameVO.appId = tableAccessDTO.appId;
        tableNameVO.tableName = tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            return ResultEnum.Table_NAME_EXISTS;
        }

        // 应用注册id
//        long id = modelReg.getId();

        if (tableAccessDTO.appId < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        modelAccess.setCreateUser(String.valueOf(userId));
//        modelAccess.setAppId(id);
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

        // 保存同步频率
/*
        TableSyncmodeDTO dto = tableAccessDTO.getTableSyncmodeDTO();
        TableSyncmodePO po = dto.toEntity(TableSyncmodePO.class);
        po.setId(modelAccess.getId());
        boolean saveSync = syncmodeImpl.save(po);
*/

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

        // 先创建表
////        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
////
////        int i = mysqlTableUtils.createmysqltb(tableAccessNonDTO);
////        if (i != 0) {
////            throw new FkException(500, "创建" + tableAccessNonDTO.getTableName() + "表失败");
////        }

        // 根据应用名称,查询出具体的数据源驱动(现阶段是MySqL和SQL Server)
        // 0-1.获取appid
        // TODO: 原始SQL表创建(暂时不用集成)
////        AppRegistrationPO one = appRegistrationImpl.query()
////                .eq("app_name", tableAccessNonDTO.getAppName())
////                .eq("del_flag", 1)
////                .one();
////        // 0-1.获取appid
////        long appid = one.getId();
////
////        // 0-2.根据id查询数据源驱动类型(appid就是tb_app_drivetype表的id)
////        AppDriveTypePO driveTypePO = appDriveTypeImpl.query().eq("id", appid).one();
////        String driveName = driveTypePO.getName(); // 数据源驱动名称
////        if (driveName.equalsIgnoreCase("MySqL")) {
////            // 先创建表
////            MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
////
////            int i = mysqlTableUtils.createmysqltb(tableAccessNonDTO);
////            if (i != 0) {
////                throw new FkException(500, "创建" + tableAccessNonDTO.getTableName() + "表失败");
////            }
////        } else {
////            SqlServerTableUtils sqlServerTableUtils = new SqlServerTableUtils();
////            int i = sqlServerTableUtils.createSqlServerTB(tableAccessNonDTO);
////            if (i != 0) {
////                throw new FkException(500, "创建" + tableAccessNonDTO.getTableName() + "表失败");
////            }
////        }

        // 当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessNonDTO.toEntity(TableAccessPO.class);

//        List<String> tableNameList = baseMapper.getTableName();
//        String tableName = modelAccess.getTableName();
//        boolean contains = tableNameList.contains(tableName);
//        if (contains) {
////            return ResultEnum.Table_NAME_EXISTS;
//            return ResultEntityBuild.build(ResultEnum.Table_NAME_EXISTS);
//        }

/*
        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("app_name", tableAccessNonDTO.getAppName())
                .eq("del_flag", 1)
                .one();

*/
        // 判断table_name是否已存在(不同应用注册下,名称可以相同)
        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        // TODO: tableName 物理表名称
        String tableName = modelAccess.getTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
//        tableNameVO.appId = modelReg.id;
        tableNameVO.appId = tableAccessNonDTO.appId;
        tableNameVO.tableName = tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            return ResultEntityBuild.build(ResultEnum.Table_NAME_EXISTS);
        }

//        long id = modelReg.getId();
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
//            return ResultEnum.SAVE_DATA_ERROR;
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // TODO 新增tb_table_business业务时间表
        // 保存tb_table_business数据
        TableBusinessDTO businessDTO = tableAccessNonDTO.getBusinessDTO();
//        TableBusinessPO modelBusiness = businessDTO.toEntity(TableBusinessPO.class);
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
////        CreateMysqlTableUtils createMysqlTableUtils = new CreateMysqlTableUtils();
////
////       int i = createMysqlTableUtils.createmysqltb(tableAccessNonDTO);
////        System.out.println(i);

        AtlasIdsVO atlasIdsVO = new AtlasIdsVO();
        atlasIdsVO.userId = userId;
        // 应用注册id
        atlasIdsVO.appId = String.valueOf(tableAccessNonDTO.appId);
        atlasIdsVO.dbId = String.valueOf(modelAccess.getId());
        atlasIdsVO.tableName = tableName;

        return ResultEntityBuild.build(ResultEnum.SUCCESS, atlasIdsVO);
    }

    /**
     * 修改物理表(实时)
     *
     * @param tableAccessDTO dto
     * @return 执行结果
     */
    @Override
    public ResultEnum updateRealTimeData(TableAccessDTO tableAccessDTO) {

        // TODO: 原始SQL表修改(暂时不用集成)
////        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
////
////        int i = mysqlTableUtils.updatemysqltb(tableAccessDTO);
////        if (i != 0) {
////            throw new FkException(500, "操作数据库失败");
////        }

        // 当前登录人信息
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
////        CreateTableUtils createTableUtils = new CreateTableUtils();
////        int i = createTableUtils.updatemysqltb(tableAccessDTO);
////        System.out.println(i);

        // 修改tb_table_syncmode
/*
        TableSyncmodeDTO dto = tableAccessDTO.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = dto.toEntity(TableSyncmodePO.class);
        boolean updateSync = syncmodeImpl.updateById(modelSync);
*/

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

        // TODO: 原始SQL表修改(暂时不用集成)
        // 1.先修改表
////        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
////
////        int i = mysqlTableUtils.updatemysqltb(dto);
////        if (i != 0) {
////            throw new FkException(500, "操作数据库失败");
////        }

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
/*
        for (TableFieldsDTO tableFieldsDTO : list) {

            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setUpdateUser(String.valueOf(userId));

            updateField = tableFieldsImpl.updateById(modelField);
        }
*/

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
        TableBusinessDTO businessDTO = dto.getBusinessDTO();
//        TableBusinessPO modelBusiness = businessDTO.toEntity(TableBusinessPO.class);
        TableBusinessPO modelBusiness = TableBusinessMap.INSTANCES.dtoToPo(businessDTO);
        boolean updateBusiness = this.businessImpl.updateById(modelBusiness);
        if (!updateBusiness) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 5.保存tb_table_syncmode数据
        boolean updateSync;
        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        updateSync = syncmodeImpl.updateById(modelSync);

        return updateSync ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * TODO: 暂时不需要此方法
     * 根据非实时应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appName appName
     * @return 返回值
     */
    @Override
    public Map<String, List<String>> queryDataBase(String appName) {

        // 1.根据应用名称查询表id
        AppRegistrationPO arpo = appRegistrationImpl.query().eq("app_name", appName).one();

        // tb_app_registration表id
        long appid = arpo.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO dpo = appDataSourceImpl.query().eq("app_id", appid).one();
        String url = dpo.getConnectStr();
        String user = dpo.getConnectAccount();
        String pwd = dpo.getConnectPwd();

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        Map<String, List<String>> table = mysqlConUtils.getTable(url, user, pwd);

        // 将实时表的数据同步地址封装进去
        ArrayList<String> conn = new ArrayList<>();
        conn.add(url);
        table.put("conn", conn);

        return table;
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

    /**
     * 回显实时表
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public TableAccessNonDTO getData(long id) {

        // 查询tb_table_access数据
        TableAccessPO modelAccess = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();
        TableAccessNonDTO dto = TableAccessMap.INSTANCES.poToDtoNon(modelAccess);

        // 将应用名称封装进去
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", modelAccess.getAppId()).one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        dto.setAppName(modelReg.getAppName());

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        List<TableFieldsDTO> listField = new ArrayList<>();
        for (TableFieldsPO modelField : list) {
            TableFieldsDTO tableFieldsDTO = TableFieldsMap.INSTANCES.poToDto(modelField);

            listField.add(tableFieldsDTO);
        }

        dto.setList(listField);

        // 查询tb_table_business
        TableBusinessPO modelBusiness = this.businessMapper.getData(id);
        TableBusinessDTO businessDTO = TableBusinessMap.INSTANCES.poToDto(modelBusiness);
        dto.setBusinessDTO(businessDTO);

        // 查询tb_table_syncmode
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        TableSyncmodeDTO sdto = new TableSyncmodeDTO(modelSync);

        dto.setTableSyncmodeDTO(sdto);

        return dto;
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
                // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                list = mysqlConUtils.getTableNameAndColumns(url, user, pwd);
                break;
            case "sqlserver":
                list = new SqlServerConUtils().getTableNameAndColumns(url, user, pwd, dbName);
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
        switch (modelDataSource.driveType) {
            case "mysql":
                // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                list = mysqlConUtils.getTableNameAndColumns(url, user, pwd);
                break;
            case "sqlserver":
                list = new SqlServerConUtils().getTableNameAndColumns(url, user, pwd, dbName);
                break;
            default:
                break;
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

        AppRegistrationPO registrationPO = appRegistrationImpl.query().eq("id", modelAccess.appId).eq("del_flag", 1).one();

        NifiVO vo = new NifiVO();
        List<TableListVO> voList = new ArrayList<>();
        TableListVO tableListVO = new TableListVO();

        tableListVO.userId = userInfo.id;
//        tableListVO.tableAtlasId = modelAccess.atlasTableId;
        tableListVO.tableName = registrationPO.appAbbreviation + "_" + modelAccess.tableName;
        voList.add(tableListVO);
        List<Long> tableIdList = new ArrayList<>();
        tableIdList.add(id);

        vo.appId = String.valueOf(modelAccess.appId);
        vo.userId = userInfo.id;
        vo.appAtlasId = registrationPO.atlasInstanceId;
        vo.tableList = voList;
        vo.tableIdList = tableIdList;
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

        dto.dbId = modelDataSource.getAtlasDbId();
        dto.tableName = modelAccess.getTableName();
        dto.createUser = modelAccess.getCreateUser();

        // TODO:驱动类型(改为枚举类型)
        if (StringUtils.isNotBlank(modelDataSource.driveType)) {
//            dto.dbType = modelDataSource.driveType;

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
            atlasEntityColumnDTO.setGuid(po.atlasFieldId);

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
        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("id", appid)
                .eq("del_flag", 1)
                .one();
        // 查询tb_app_datasource
        AppDataSourcePO modelDataSource = appDataSourceImpl.query()
                .eq("app_id", appid)
                .eq("del_flag", 1)
                .one();
        if (modelReg == null || modelDataSource == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        dto.appId = modelReg.atlasInstanceId;
        dto.atlasTableId = modelDataSource.atlasDbId;

        // 查询tb_table_access
        TableAccessPO modelAccess = this.query()
                .eq("id", id)
                .eq("app_id", appid)
                .eq("del_flag", 1)
                .one();

        dto.tableId = modelAccess.atlasTableId;
//        dto.dorisSelectSqlStr = modelAccess.dorisSelectSqlStr;

        AtlasEntityDbTableColumnDTO atlasDTO = new AtlasEntityDbTableColumnDTO();
        atlasDTO.dbId = modelDataSource.getAtlasDbId();
        atlasDTO.tableName = modelAccess.getTableName();
        atlasDTO.createUser = modelAccess.getCreateUser();

        // TODO:驱动类型
        if (StringUtils.isNotBlank(modelDataSource.driveType)) {
//            atlasDTO.dbType = modelDataSource.driveType;
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

        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();
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
        TableAccessPO modelAccess = this.query()
                .eq("id", tableId)
                .eq("app_id", appid)
                .eq("del_flag", 1)
                .one();
        if (modelAccess == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        modelAccess.atlasTableId = dto.atlasTableId;
//        modelAccess.updateUser = dto.userId;
        boolean update = this.updateById(modelAccess);
        if (!update) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        List<AtlasEntityColumnDTO> list = dto.columnsKeys;

        boolean updateField = true;
        for (AtlasEntityColumnDTO columnDTO : list) {

            // 根据本表id查询tb_table_fields
            TableFieldsPO modelFields = this.tableFieldsImpl.query()
                    .eq("id", columnDTO.columnId)
                    .one();
            if (modelFields == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }

            // 回写的字段GUID
            modelFields.atlasFieldId = columnDTO.getGuid();
            // 更新tb_table_fields表数据
            updateField = this.tableFieldsImpl.updateById(modelFields);
            if (!updateField) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
/*
        // nifi配置表: tb_nifi_setting
        NifiSettingPO po = new NifiSettingPO();
        po.tableId = tableId;
        po.appId = appid;
        po.tableName = dto.tableName;
        po.selectSql = dto.dorisSelectSqlStr;

        boolean save = nifiSettingImpl.save(po);

*/

        return updateField ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * nifi流程
     *
     * @param id    物理表id
     * @param appid 应用注册id
     * @return
     */
    @TraceType(type = TraceTypeEnum.DATAACCESS_CONFIG)
    @Override
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(long id, long appid) {
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
        // 1.app组配置
        // select * from tb_app_registration where id=id and del_flag=1;
        AppRegistrationPO modelReg = this.appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        if (modelReg == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        groupConfig.setAppName(modelReg.getAppName());
        groupConfig.setAppDetails(modelReg.getAppDes());
        // 回写应用注册组件id
//        groupConfig.setComponentId(modelReg.componentId);
        //3.数据源jdbc配置
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        if (modelDataSource == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        sourceDsConfig.setJdbcStr(modelDataSource.getConnectStr());
        // 选择驱动类型
        if (Objects.equals(modelDataSource.driveType, "sqlserver")) {
            sourceDsConfig.setType(DriverTypeEnum.SQLSERVER);
        } else if (Objects.equals(modelDataSource.driveType, "mysql")) {
            sourceDsConfig.setType(DriverTypeEnum.MYSQL);
        } else if (Objects.equals(modelDataSource.driveType, "postgresql")) {
            sourceDsConfig.setType(DriverTypeEnum.POSTGRESQL);
        }
        sourceDsConfig.setUser(modelDataSource.getConnectAccount());
        sourceDsConfig.setPassword(modelDataSource.getConnectPwd());
//        sourceDsConfig.componentId = modelReg.sourceDbPoolComponentId;
        // 4.目标源jdbc连接
//        targetDsConfig.componentId = modelReg.targetDbPoolComponentId;
        // 5.表及表sql
        TableSyncmodePO modelSync = syncmodeMapper.getData(id);
        if (modelSync == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // TODO: 新增同步方式
        targetDsConfig.syncMode = modelSync.syncMode;

        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
        // 2.任务组配置
        taskGroupConfig.setAppName(modelAccess.getTableName());
        taskGroupConfig.setAppDetails(modelAccess.getTableDes());
        if (modelAccess == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        // TODO: 将app组配置中的setNewApp加上
//        if (modelReg.componentId == null && modelAccess.componentId == null) {
//            groupConfig.setNewApp(true);
//        } else {
//            groupConfig.setNewApp(false);
//        }
        // TODO 回写物理表组件id
//        taskGroupConfig.setComponentId(modelAccess.componentId);

//        NifiSettingPO modelNifi = nifiSettingImpl.query().eq("app_id", appid).eq("table_id", id).one();
//        if (modelNifi == null) {
//            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
//        }
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
        // ods sql
//        processorConfig.sourceExecSqlQuery = modelNifi.selectSql;
        // atlas返回的tableName
//        processorConfig.targetTableName = modelNifi.tableName;
        // TODO  新增: 增量配置库源jdbc连接
        cfgDsConfig.setType(DriverTypeEnum.MYSQL);
        cfgDsConfig.setJdbcStr(jdbcStr);
        cfgDsConfig.setUser(user);
        cfgDsConfig.setPassword(password);
        /**
         * 查询tb_nifi_config表,查询value是否未空,
         */


        // TODO: 2021/9/4 nifi流程需要物理表字段
//        TableAccessPO one = this.query().eq("id", id).eq("del_flag", 1).one();
//        NifiSettingPO settingPO = nifiSettingImpl.query().eq("table_id", id).eq("app_id", appid).one();
        List<TableFieldsPO> list = this.tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();
        List<TableFieldsDTO> tableFieldsDTOS = TableFieldsMap.INSTANCES.listPoToDto(list);

        if (list != null && !list.isEmpty()) {
//            targetDsConfig.targetTableName = settingPO.tableName;
            targetDsConfig.tableFieldsList = tableFieldsDTOS;
        }

        dto.groupConfig = groupConfig;
        dto.taskGroupConfig = taskGroupConfig;
        dto.sourceDsConfig = sourceDsConfig;
        dto.targetDsConfig = targetDsConfig;
        dto.processorConfig = processorConfig;
        dto.cfgDsConfig = cfgDsConfig;

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    public TableAccessPO insertTableAccessPO(TableAccessPO tableAccessPO) {
        accessMapper.insertTableAccessPO(tableAccessPO);
        return tableAccessPO;
    }

    @Override
    public BuildNifiFlowDTO createPgToDorisConfig(String tableName, String selectSql) {
        //添五张表tb_app_registration  tb_app_datasource   tb_table_access  tb_nifi_setting; tb_etl_Incremental
        BuildNifiFlowDTO buildNifiFlowDTO = new BuildNifiFlowDTO();
        AppRegistrationPO appRegistrationPO = new AppRegistrationPO();
        AppDataSourcePO appDataSourcePO = new AppDataSourcePO();
        TableAccessPO tableAccessPO = new TableAccessPO();
        TableSyncmodePO tableSyncmodePO = new TableSyncmodePO();
//        NifiSettingPO nifiSettingPO = new NifiSettingPO();
        appRegistrationPO.appName = "postgerToDoris";
        appRegistrationPO.appDes = "postgerToDoris";
        appRegistrationPO.appType = 1;
        appRegistrationPO.delFlag = 1;
        //appRegistrationImpl.insertAppRegistrationPO(appRegistrationPO);//添加返回id 就是appid
        appDataSourcePO.appId = appRegistrationPO.id;//后面加上AppRegistrationPO.id
        //ConnectStr   driveType   ConnectAccount   ConnectPwd
        appDataSourcePO.driveType = "postgresql";
        appDataSourcePO.connectAccount = pgsqlDatamodelUsername;
        appDataSourcePO.connectPwd = pgsqlDatamodelPassword;
        appDataSourcePO.connectStr = pgsqlDatamodelUrl;
        //appDataSourceImpl.save(appDataSourcePO);
        tableAccessPO.appId = appRegistrationPO.id;//后续补上AppRegistrationPO.id
        tableAccessPO.isRealtime = 1;
        tableAccessPO.tableName = tableName;
        tableAccessPO.delFlag = 1;
        insertTableAccessPO(tableAccessPO);//可能都要返回id
        tableSyncmodePO.syncMode = 1;
        tableSyncmodePO.id = tableAccessPO.id;//tableAccess.id
        //tableSyncmodeImpl.save(tableSyncmodePO);
//        nifiSettingPO.tableId = tableAccessPO.id;//tableAccess.id
//        nifiSettingPO.appId = appRegistrationPO.id;
//        nifiSettingPO.selectSql = selectSql;
//        nifiSettingPO.tableName = tableName;
//        nifiSettingImpl.save(nifiSettingPO);
        EtlIncrementalPO etlIncrementalPO = new EtlIncrementalPO();
//        etlIncrementalPO.objectName = nifiSettingPO.tableName;
        etlIncrementalPO.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
        etlIncrementalMapper.insert(etlIncrementalPO);
        buildNifiFlowDTO.appId = appRegistrationPO.id;
        buildNifiFlowDTO.id = tableAccessPO.id;
        buildNifiFlowDTO.synchronousTypeEnum = SynchronousTypeEnum.PGTODORIS;
        return buildNifiFlowDTO;
    }

    @Override
    public ResultEntity<ComponentIdDTO> getComponentId(DataAccessIdsDTO dto) {

        ComponentIdDTO componentIdDTO = new ComponentIdDTO();

        AppRegistrationPO appRegistrationPO = appRegistrationImpl.query().eq("id", dto.appId).eq("del_flag", 1).one();
        TableAccessPO tableAccessPO = this.query().eq("id", dto.tableId).eq("del_flag", 1).one();
//        componentIdDTO.appComponentId = appRegistrationPO.componentId;
//        componentIdDTO.tableComponentId = tableAccessPO.componentId;
//        componentIdDTO.schedulerComponentId = tableAccessPO.schedulerComponentId;

        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addComponentId(NifiAccessDTO dto) {

        AppRegistrationPO modelReg = this.appRegistrationImpl.query()
                .eq("id", dto.appId)
                .eq("del_flag", 1)
                .one();
        if (modelReg == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

//        modelReg.targetDbPoolComponentId = dto.targetDbPoolComponentId;
//        modelReg.sourceDbPoolComponentId = dto.sourceDbPoolComponentId;
/*
        boolean updateReg = true;
        if (modelReg.componentId == null) {
            modelReg.componentId = dto.appGroupId;
            // 更新tb_app_appRegistration表componentId
            updateReg = this.appRegistrationImpl.updateById(modelReg);
        }
        if (!updateReg) {
            return ResultEnum.SAVE_DATA_ERROR;
        }*/

        TableAccessPO modelAccess = this.query()
                .eq("id", dto.tableId)
                .eq("app_id", dto.appId)
                .eq("del_flag", 1)
                .one();
//        modelAccess.componentId = dto.tableGroupId;
        // 调度组件id
//        modelAccess.schedulerComponentId = dto.schedulerComponentId;
        boolean updateAccess = this.updateById(modelAccess);
        if (!updateAccess) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        boolean saveNifiConfig = true;
        String nifiKey = nifiConfigMapper.getNifiKey();
        // 为空的话,要保存值
        if (StringUtils.isEmpty(nifiKey)) {
            NifiConfigPO modelNifi = new NifiConfigPO();
            modelNifi.componentKey = ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName();
            modelNifi.componentId = dto.cfgDbPoolComponentId;
            saveNifiConfig = nifiConfigImpl.save(modelNifi);
        }
        return saveNifiConfig ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<TableAccessVO> listData(TableAccessQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        if (query.key != null && query.key.length() > 0) {
            querySql.append(" and table_name like concat('%', " + "'" + query.key + "'" + ", '%') ");
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
        List<FilterFieldDTO> list = new ArrayList<>();
        list = getMetadata.getMetadataList(
                "dmp_datainput_db",
                "tb_table_access",
                "a",
                FilterSqlConstants.TABLE_ACCESS_SQL);
        List<FilterFieldDTO> fieldDTOList = getMetadata.getMetadataList(
                "dmp_datainput_db",
                "tb_table_syncmode",
                "b",
                FilterSqlConstants.TABLE_SYNCMODE_SQL);
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
    public Object getDimensionMeta() {
        return null;
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
        //List<Integer> list=(List)fieldsMapper.selectObjs(queryWrapper).stream().collect(Collectors.toList());
        return list;
    }

    @Override
    public List<ChannelDataDTO> getTableId() {

        // select id,app_name from tb_app_registration where del_flag=1 ORDER BY create_time DESC;
        List<AppRegistrationPO> list = appRegistrationImpl.list(Wrappers.<AppRegistrationPO>lambdaQuery()
                .select(AppRegistrationPO::getId,AppRegistrationPO::getAppName)
                .orderByDesc(AppRegistrationPO::getCreateTime));

        // list: po -> dto
        List<ChannelDataDTO> channelDataDTOList = AppRegistrationMap.INSTANCES.listPoToChannelDataDto(list);

        // 查询当前应用下面的所有表
        channelDataDTOList.forEach(dto -> {
            // select id,table_name from tb_table_access where app_id =#{dto.id} and del_flag = 1
            List<TableAccessPO> poList = this.list(Wrappers.<TableAccessPO>lambdaQuery()
                    .eq(TableAccessPO::getAppId, dto.id)
                    // publish=3: 正在发布
                    .eq(TableAccessPO::getPublish,3)
                    .select(TableAccessPO::getId, TableAccessPO::getTableName));
            // list: po->dto 并赋值给dto.list
            dto.list = TableAccessMap.INSTANCES.listPoToChannelDataDto(poList);
        });

        return channelDataDTOList;
    }

    @Override
    public List<AppRegistrationDataDTO> getDataAppRegistrationMeta() {

        List<AppRegistrationDataDTO> list = new ArrayList<>();

        //获取所有应用注册列表
        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();
        List<AppRegistrationPO> appRegistrationPOList = registrationMapper.selectList(queryWrapper);
        if (appRegistrationPOList == null || appRegistrationPOList.size() == 0) {
            return list;
        }
        list = AppRegistrationMap.INSTANCES.listPoToDtoList(appRegistrationPOList);
        //获取所有表配置数据
        QueryWrapper<TableAccessPO> tableAccessPOQueryWrapper = new QueryWrapper<>();
        // 只需要发布状态为3: 正在发布
        tableAccessPOQueryWrapper.lambda().eq(TableAccessPO::getPublish,3);
        List<TableAccessPO> tableAccessPOList = accessMapper.selectList(tableAccessPOQueryWrapper);
        if (tableAccessPOList == null || tableAccessPOList.size() == 0) {
            return list;
        }
        //获取表中所有字段配置数据
        QueryWrapper<TableFieldsPO> tableFieldsPOQueryWrapper = new QueryWrapper<>();
        List<TableFieldsPO> tableFieldsPOList = fieldsMapper.selectList(tableFieldsPOQueryWrapper
        );
        for (AppRegistrationDataDTO item : list) {
            item.tableDtoList = TableAccessMap.INSTANCES.poListToDtoList(tableAccessPOList.stream()
                    .filter(e -> e.appId == item.id).collect(Collectors.toList()));
            item.tableDtoList.stream().map(e -> e.tableName = "ods_" + item.appAbbreviation + "_"+ e.tableName).collect(Collectors.toList());
            if ((item.tableDtoList == null || item.tableDtoList.size() == 0) ||
                    (tableFieldsPOList == null || tableFieldsPOList.size() == 0)) {
                continue;
            }
            item.tableDtoList.stream().map(e -> e.type = 1).collect(Collectors.toList());
            for (TableAccessDataDTO tableAccessDataDTO : item.tableDtoList) {
                tableAccessDataDTO.fieldDtoList = TableFieldsMap.INSTANCES.poListToDtoList(tableFieldsPOList.stream()
                        .filter(e -> e.tableAccessId == tableAccessDataDTO.id).collect(Collectors.toList()));
                if (tableAccessDataDTO.fieldDtoList == null || tableAccessDataDTO.fieldDtoList.size() == 0) {
                    continue;
                }
                tableAccessDataDTO.fieldDtoList.stream().map(e -> e.type = 2).collect(Collectors.toList());
            }
        }
        // 反转: 倒序排序
        Collections.reverse(list);
        return list;
    }

/*
    @Override
    public List<FieldNameDTO> getTableFieldByQuery(String query) {
        List<FieldNameDTO> list = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcStr, user, password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                FieldNameDTO dto = new FieldNameDTO();
                dto.id = rs.getInt("id");
                dto.fieldName = rs.getString("field_name");
                dto.fieldType = rs.getString("field_type");
                dto.fieldLength = rs.getString("field_length");
                dto.fieldDes = rs.getString("field_des");
                dto.tableAccessId = rs.getInt("table_access_id");
                list.add(dto);
            }
            rs.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return list;
    }
*/

    @Override
    public ResultEnum addTableAccessData(TbTableAccessDTO dto) {

        // dto -> po
        TableAccessPO model = TableAccessMap.INSTANCES.tbDtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 同一应用下表名不可重复
        boolean flag = this.checkTableName(dto);
        if (flag) {
            return ResultEnum.Table_NAME_EXISTS;
        }

        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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

        TableAccessPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // sql保存时丢失
        if (dto.sqlFlag == 1 && "".equals(dto.sqlScript)) {
            return ResultEnum.SQL_EXCEPT_CLEAR;
        }

        // 判断名称是否重复
        QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
        // 限制在同一应用下
        queryWrapper.lambda().eq(TableAccessPO::getTableName, dto.tableName).eq(TableAccessPO::getAppId, model.appId);
        TableAccessPO tableAccessPo = baseMapper.selectOne(queryWrapper);
        if (tableAccessPo != null && tableAccessPo.id != dto.id) {
            return ResultEnum.TABLE_IS_EXIST;
        }

        // dto -> po
        TableAccessPO po = TableAccessMap.INSTANCES.tbDtoToPo(dto);

        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Transactional
    @Override
    public ResultEnum deleteTableAccessData(long id) {
        // 参数校验
        TableAccessPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        return accessMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TbTableAccessDTO> getTableAccessListData(long appId) {

        List<TableAccessPO> list = this.query().eq("app_id", appId).list();

        return TableAccessMap.INSTANCES.listTbPoToDto(list);
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
        tableNameVO.appId = dto.appId;
        tableNameVO.tableName = dto.tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            flag = true;
        }
        return flag;
    }

    @Override
    public OdsResultDTO getTableFieldByQuery(OdsQueryDTO query) {
        OdsResultDTO array = new OdsResultDTO();
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(pgsqlOdsUrl, pgsqlOdsUsername, pgsqlDatamodelPassword);
            Statement st = conn.createStatement();
            //获取总条数
            String getTotalSql = "select count(*) as total from(" + query.querySql + ") as tab";
            ResultSet rSet = st.executeQuery(getTotalSql);
            int rowCount = 0;
            if (rSet.next()) {
                rowCount = rSet.getInt("total");
            }
            rSet.close();
            //分页获取数据
            int offset=(query.pageIndex-1)*query.pageSize;
            query.querySql = query.querySql + " limit " + query.pageSize + " offset " + offset;
            ResultSet rs = st.executeQuery(query.querySql);
            //获取数据集
            array = resultSetToJsonArray(rs);
            array.pageIndex = query.pageIndex;
            array.pageSize = query.pageSize;
            array.total = rowCount;
            rs.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return array;
    }

    public static OdsResultDTO resultSetToJsonArray(ResultSet rs) throws SQLException, JSONException {
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
                String tableName=metaData.getTableName(i)+"key";
                if ("fi_batch_code".equals(columnName) || tableName.equals("ods_"+columnName))
                {
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
            String fieldType1 = "date";
            String fieldType2 = "time";
            //源表
            dto.sourceTableName=metaData.getTableName(i);
            // 源字段
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName=metaData.getTableName(i)+"key";
            if ("fi_batch_code".equals(dto.fieldName) || tableName.equals("ods_"+dto.fieldName))
            {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toUpperCase();
            if (dto.fieldType.contains("INT2") || dto.fieldType.contains("INT4") || dto.fieldType.contains("INT8"))
            {
                dto.fieldType="INT";
            }
            if (dto.fieldType.toLowerCase().contains(fieldType1) || dto.fieldType.toLowerCase().contains(fieldType2)) {
                dto.fieldLength = "50";
            }else {
                dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i)))?"255":String.valueOf(metaData.getColumnDisplaySize(i));
            }

            // 转换表字段类型和长度
            List<String> list = transformField(dto.fieldType, dto.fieldLength);
            dto.fieldType = list.get(0);
            dto.fieldLength = list.get(1);

            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    /**
     * 转换表字段类型和长度
     *
     * @param fieldType   fieldType
     * @param fieldLength fieldLength
     * @return target
     */
    private static List<String> transformField(String fieldType, String fieldLength) {

        // 浮点型
        List<String> floatType = new ArrayList<>();
        floatType.add("double");

        // 字符型
        List<String> charType = new ArrayList<>();
        charType.add("");

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
        } else if (accurateType.contains(fieldType.toLowerCase()) || otherType.contains(fieldType.toLowerCase())) {
            fieldList.add("FLOAT");
            fieldList.add("0");
        } else if (Integer.parseInt(fieldLength) <= 1) {
            fieldList.add("VARCHAR");
            fieldList.add("20");
        } else if (Integer.parseInt(fieldLength) >= 5000) {
            fieldList.add("TEXT");
            fieldList.add("0");
        } else {
            fieldList.add("VARCHAR");
            fieldList.add(fieldLength);
        }
        return fieldList;
    }

    @Override
    public OdsResultDTO getDataAccessQueryList(OdsQueryDTO query) {
        String mysqlDriver = "mysql";
        String sqlserverDriver = "sqlserver";
        AppDataSourcePO po = appDataSourceImpl.query().eq("app_id", query.appId).one();

        OdsResultDTO array = new OdsResultDTO();
        try {
            Statement st = null;
            if (po.driveType.equalsIgnoreCase(mysqlDriver)) {
                Connection conn = getStatement(DriverTypeEnum.MYSQL.getName(), po.connectStr, po.connectAccount, po.connectPwd);
                st = conn.createStatement();
                // 显示前十条
                query.querySql = query.querySql + " limit 10 offset 0";
            } else if (po.driveType.equalsIgnoreCase(sqlserverDriver)) {
                //1.加载驱动程序
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                //2.获得数据库的连接
                Connection conn = DriverManager.getConnection(po.connectStr, po.connectAccount, po.connectPwd);
                st = conn.createStatement();

                // 显示前十条
                query.querySql = "SELECT top 10 * FROM (" + query.querySql + ") as tba111";
            }
            //获取总条数
            String getTotalSql = "select count(*) as total from(" + query.querySql + ") as tab";
            assert st != null;

            ResultSet rs = st.executeQuery(query.querySql);
            //获取数据集
            array = resultSetToJsonArray(rs);
            rs.close();
        } catch (Exception e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return array;
    }

    @Override
    public ResultEntity<BuildPhysicalTableDTO> getBuildPhysicalTableDTO(long tableId, long appId) {

        BuildPhysicalTableDTO dto = new BuildPhysicalTableDTO();

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", appId).one();
        TableAccessPO tableAccessPo = this.query().eq("id", tableId).one();
        AppDataSourcePO dataSourcePo = appDataSourceImpl.query().eq("app_id", appId).one();
        List<TableFieldsPO> listPo = tableFieldsImpl.query().eq("table_access_id", tableId).list();
        if (tableAccessPo == null || registrationPo == null || dataSourcePo == null || CollectionUtils.isEmpty(listPo)) {
            return ResultEntityBuild.build(ResultEnum.NIFI_NOT_FIND_DATA);
        }

        DbTypeEnum dbTypeEnum = DbTypeEnum.getValue(dataSourcePo.driveType);
        switch (dbTypeEnum) {
            case sqlserver:
                dto.driveType = DbTypeEnum.sqlserver;
                break;
            case mysql:
                dto.driveType = DbTypeEnum.mysql;
                break;
            case oracle:
                dto.driveType = DbTypeEnum.oracle;
            default:
                break;
        }
        dto.tableFieldsDTOS = TableFieldsMap.INSTANCES.listPoToDto(listPo);
        dto.appAbbreviation = registrationPo.appAbbreviation;
        dto.tableName = tableAccessPo.tableName;
        dto.selectSql = tableAccessPo.sqlScript;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    @Override
    public void updateTablePublishStatus(ModelPublishStatusDTO dto) {
        TableAccessPO model = baseMapper.selectById(dto.tableId);
        if (model != null) {
            model.publish = dto.publish;
            baseMapper.updateById(model);
        }
    }

    @Override
    public ResultEntity<ComponentIdDTO> getAppNameAndTableName(DataAccessIdsDTO dto) {

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", dto.appId).one();
        TableAccessPO accessPo = this.query().eq("id", dto.tableId).one();
        if (registrationPo == null || accessPo == null) {
            ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        ComponentIdDTO componentIdDTO = new ComponentIdDTO();
        componentIdDTO.appName = registrationPo.appName;
        componentIdDTO.tableName = accessPo.tableName;
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

    /**
     * 连接数据库
     *
     * @param driver   driver
     * @param url      url
     * @param username username
     * @param password password
     * @return statement
     */
    private Connection getStatement(String driver, String url, String username, String password) {
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return conn;
    }
}
