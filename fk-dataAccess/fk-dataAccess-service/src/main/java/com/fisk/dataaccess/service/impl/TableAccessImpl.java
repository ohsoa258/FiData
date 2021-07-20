package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableSyncmodeMapper;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.utils.MysqlConUtils;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements ITableAccess {

    @Resource
    private TableFieldsImpl tableFieldsImpl;

    @Resource
    private AppRegistrationImpl appRegistrationImpl;

    @Resource
    private AppDataSourceImpl appDataSourceImpl;

    @Resource
    private TableSyncmodeImpl syncmodeImpl;

    @Resource
    private TableSyncmodeMapper syncmodeMapper;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private AppNifiFlowImpl nifiFlowImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private NifiSettingImpl nifiSettingImpl;

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
//        AppRegistrationPO one = appRegistrationImpl.query()
//                .eq("app_name", tableAccessDTO.getAppName())
//                .eq("del_flag", 1)
//                .one();
//        // 0-1.获取appid
//        long appid = one.getId();
//
//        // 0-2.根据id查询数据源驱动类型(appid就是tb_app_drivetype表的id)
//        AppDriveTypePO driveTypePO = appDriveTypeImpl.query().eq("id", appid).one();
//        String driveName = driveTypePO.getName(); // 数据源驱动名称
//        if (driveName.equalsIgnoreCase("MySqL")) {
//            // 先创建表
//            MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
//
//            int i = mysqlTableUtils.createmysqltb(tableAccessDTO);
//            if (i != 0) {
//                throw new FkException(500, "创建" + tableAccessDTO.getTableName() + "表失败");
//            }
//        } else {
//            SqlServerTableUtils sqlServerTableUtils = new SqlServerTableUtils();
//            int i = sqlServerTableUtils.createSqlServerTB(tableAccessDTO);
//            if (i != 0) {
//                throw new FkException(500, "创建" + tableAccessDTO.getTableName() + "表失败");
//            }
//        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        // 数据保存: 添加应用的时候,相同的表名不可以再次添加
        List<String> tableNameList = baseMapper.getTableName();
        String tableName = modelAccess.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            return ResultEnum.Table_NAME_EXISTS;
//            throw new FkException(ResultEnum.Table_NAME_EXISTS, "当前" + tableName + "已存在,请重新输入");
        }

        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("app_name", tableAccessDTO.getAppName())
                .eq("del_flag", 1)
                .one();

        // 应用注册id
        long id = modelReg.getId();
        if (id < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "保存失败");
        }
        modelAccess.setCreateUser("" + userId + "");
        modelAccess.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        modelAccess.setIsRealtime(0);
        modelAccess.setSyncSrc(tableAccessDTO.getSyncSrc());
        modelAccess.setDelFlag(1);
        // 实时
        modelAccess.setIsRealtime(0);

        // 时间字段有问题,待定
        Date dateAccess = new Date(System.currentTimeMillis());

        modelAccess.setCreateTime(dateAccess);
        modelAccess.setUpdateTime(dateAccess);

        // 2.保存tb_table_access数据
        boolean saveAccess = this.save(modelAccess);

        if (!saveAccess) {
            return ResultEnum.SAVE_DATA_ERROR;
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean save2 = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessDTO.getList();

        //TODO: 这一块判断先不加
        // 表字段不为空判断
//        if (tableFieldsDTOS == null||tableFieldsDTOS.isEmpty()) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setTableAccessId(modelAccess.getId());

            modelField.setCreateUser("" + userId + "");

            // 1是实时物理表的字段，0是非实时物理表的字段
            modelField.setIsRealtime(1);
            modelField.setDelFlag(1);

            // 时间
            Date dateField = new Date(System.currentTimeMillis());
            modelField.setCreateTime(dateField);
            modelField.setUpdateTime(dateField);

            save2 = tableFieldsImpl.save(modelField);
        }

        if (!save2) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存同步频率
        TableSyncmodeDTO dto = tableAccessDTO.getTableSyncmodeDTO();
        TableSyncmodePO po = dto.toEntity(TableSyncmodePO.class);
        po.setId(modelAccess.getId());
        boolean saveSync = syncmodeImpl.save(po);

        // TODO: atlas调用
//        AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
//        atlasEntityQueryDTO.userId = userId;
////        atlasEntityQueryDTO.appId = "6";
//        // 应用注册id
//        atlasEntityQueryDTO.appId = "" + id + "";
//
//        // 物理表id
//        atlasEntityQueryDTO.dbId = "" + modelAccess.getId() + "";
//
//        ResultEntity<Object> task = publishTaskClient.publishBuildAtlasTableTask(atlasEntityQueryDTO);
//        log.info("task:" + JSON.toJSONString(task));
//        System.out.println(task);

        return saveSync ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     *
     * @param tableAccessNonDTO dto
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addNonRealTimeData(TableAccessNonDTO tableAccessNonDTO) {

        // 先创建表
//        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
//
//        int i = mysqlTableUtils.createmysqltb(tableAccessNonDTO);
//        if (i != 0) {
//            throw new FkException(500, "创建" + tableAccessNonDTO.getTableName() + "表失败");
//        }

        // 根据应用名称,查询出具体的数据源驱动(现阶段是MySqL和SQL Server)
        // 0-1.获取appid
        // TODO: 原始SQL表创建(暂时不用集成)
//        AppRegistrationPO one = appRegistrationImpl.query()
//                .eq("app_name", tableAccessNonDTO.getAppName())
//                .eq("del_flag", 1)
//                .one();
//        // 0-1.获取appid
//        long appid = one.getId();
//
//        // 0-2.根据id查询数据源驱动类型(appid就是tb_app_drivetype表的id)
//        AppDriveTypePO driveTypePO = appDriveTypeImpl.query().eq("id", appid).one();
//        String driveName = driveTypePO.getName(); // 数据源驱动名称
//        if (driveName.equalsIgnoreCase("MySqL")) {
//            // 先创建表
//            MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
//
//            int i = mysqlTableUtils.createmysqltb(tableAccessNonDTO);
//            if (i != 0) {
//                throw new FkException(500, "创建" + tableAccessNonDTO.getTableName() + "表失败");
//            }
//        } else {
//            SqlServerTableUtils sqlServerTableUtils = new SqlServerTableUtils();
//            int i = sqlServerTableUtils.createSqlServerTB(tableAccessNonDTO);
//            if (i != 0) {
//                throw new FkException(500, "创建" + tableAccessNonDTO.getTableName() + "表失败");
//            }
//        }

        // 当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessNonDTO.toEntity(TableAccessPO.class);

        List<String> tableNameList = baseMapper.getTableName();
        String tableName = modelAccess.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            return ResultEnum.Table_NAME_EXISTS;
//            throw new FkException(ResultEnum.Table_NAME_EXISTS, "当前" + tableName + "已存在,请重新输入");
        }

        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("app_name", tableAccessNonDTO.getAppName())
                .eq("del_flag", 1)
                .one();

        long id = modelReg.getId();
        if (id < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "保存失败");
        }
        modelAccess.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        modelAccess.setIsRealtime(0);
        modelAccess.setSyncSrc(tableAccessNonDTO.getSyncSrc());
        modelAccess.setDelFlag(1);
        // 非实时
        modelAccess.setIsRealtime(1);
        // 当前登录人
        modelAccess.setCreateUser(String.valueOf(userId));

        // 时间
        Date date1 = new Date(System.currentTimeMillis());
        modelAccess.setCreateTime(date1);
        modelAccess.setUpdateTime(date1);

        // 2.保存tb_table_access数据
        boolean saveAccess = this.save(modelAccess);

        if (!saveAccess) {
            return ResultEnum.SAVE_DATA_ERROR;
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean saveField = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessNonDTO.getList();

        // TODO: 这一块判断先不加
        // 表字段不为空判断
//        if (tableFieldsDTOS == null||tableFieldsDTOS.isEmpty()) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setTableAccessId(modelAccess.getId());

            // 1是实时物理表的字段，0是非实时物理表的字段
            modelField.setIsRealtime(1);
            modelField.setDelFlag(1);
            modelField.setCreateUser(String.valueOf(userId));

            // 时间
            Date date2 = new Date(System.currentTimeMillis());
            modelField.setCreateTime(date2);
            modelField.setUpdateTime(date2);

            saveField = tableFieldsImpl.save(modelField);
        }

        if (!saveField) {
            return ResultEnum.SAVE_DATA_ERROR;
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_syncmode数据
        TableSyncmodeDTO syncmodeDTO = tableAccessNonDTO.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = syncmodeDTO.toEntity(TableSyncmodePO.class);
        long aid = modelAccess.getId();
        modelSync.setId(aid);

        boolean saveSync = syncmodeImpl.save(modelSync);

//        CreateMysqlTableUtils createMysqlTableUtils = new CreateMysqlTableUtils();
//
//        int i = createMysqlTableUtils.createmysqltb(tableAccessNonDTO);
//        System.out.println(i);

        // TODO: 调用atlas
        AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
        atlasEntityQueryDTO.userId = userId;
        // 应用注册id
        atlasEntityQueryDTO.appId = String.valueOf(id);
        atlasEntityQueryDTO.dbId = String.valueOf(modelAccess.getId());
        ResultEntity<Object> task = publishTaskClient.publishBuildAtlasTableTask(atlasEntityQueryDTO);
        log.info("task:" + JSON.toJSONString(task));
        System.out.println(task);

        return saveSync ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

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
//        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
//
//        int i = mysqlTableUtils.updatemysqltb(tableAccessDTO);
//        if (i != 0) {
//            throw new FkException(500, "操作数据库失败");
//        }

        // 当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        // 时间字段
        Date date1 = new Date(System.currentTimeMillis());

        modelAccess.setUpdateUser(String.valueOf(userId));
        modelAccess.setUpdateTime(date1);
        modelAccess.setDelFlag(1);

        // 2.保存tb_table_access数据
        boolean updateAccess = this.updateById(modelAccess);

        if (!updateAccess) {
            return ResultEnum.UPDATE_DATA_ERROR;
//            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 保存tb_table_fields数据: 分为更新和添加数据
        boolean update2 = true;
        boolean saveField = true;

        List<TableFieldsDTO> fieldsDTOList = tableAccessDTO.getList();


        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {

            // 0: 旧数据不操作  1: 修改  2: 新增
            int funcType = tableFieldsDTO.getFuncType();
            // 修改
            if (funcType == 1) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                Date date2 = new Date(System.currentTimeMillis());
                modelField.setUpdateUser(String.valueOf(userId));
                modelField.setUpdateTime(date2);
                modelField.setDelFlag(1);

                update2 = tableFieldsImpl.updateById(modelField);
                // 新增
            } else if (funcType == 2) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);

                // 还要绑定tb_table_access id
                modelField.setTableAccessId(modelAccess.getId());
                Date date3 = new Date(System.currentTimeMillis());
                modelField.setUpdateUser(String.valueOf(userId));
                modelField.setCreateTime(date3);
                modelField.setUpdateTime(date3);
                modelField.setDelFlag(1);
                saveField = tableFieldsImpl.save(modelField);
            }
        }

        if (!update2) {
            return ResultEnum.UPDATE_DATA_ERROR;
//            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据保存失败");
        }
        if (!saveField) {
            return ResultEnum.UPDATE_DATA_ERROR;
//            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据保存失败");
        }

//        CreateTableUtils createTableUtils = new CreateTableUtils();
//        int i = createTableUtils.updatemysqltb(tableAccessDTO);
//        System.out.println(i);

        // 修改tb_table_syncmode
        TableSyncmodeDTO dto = tableAccessDTO.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = dto.toEntity(TableSyncmodePO.class);
        boolean updateSync = syncmodeImpl.updateById(modelSync);

        return updateSync ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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
//        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();
//
//        int i = mysqlTableUtils.updatemysqltb(dto);
//        if (i != 0) {
//            throw new FkException(500, "操作数据库失败");
//        }

        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // dto->po
        TableAccessPO modelAccess = dto.toEntity(TableAccessPO.class);

        // 时间字段
        Date dateAccess = new Date(System.currentTimeMillis());

        modelAccess.setUpdateUser(String.valueOf(userId));
        modelAccess.setUpdateTime(dateAccess);
        modelAccess.setDelFlag(1);

        // 2.保存tb_table_access数据
        boolean updateAccess = this.updateById(modelAccess);

        if (!updateAccess) {
            return ResultEnum.UPDATE_DATA_ERROR;
//            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 保存tb_table_fields数据: 分为更新和添加数据
        boolean updateField = true;

        List<TableFieldsDTO> list = dto.getList();


        for (TableFieldsDTO tableFieldsDTO : list) {

            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            Date dateField = new Date(System.currentTimeMillis());

            modelField.setUpdateUser(String.valueOf(userId));
            modelField.setUpdateTime(dateField);
            modelField.setDelFlag(1);

            updateField = tableFieldsImpl.updateById(modelField);
        }

        if (!updateField) {
            return ResultEnum.UPDATE_DATA_ERROR;
//            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }
//        if (!saveField) {
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
//        }


        // 4.保存tb_table_syncmode数据
        boolean updateSync = true;
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
        AppDataSourcePO dpo = appDataSourceImpl.query().eq("appid", appid).one();
        String url = dpo.getConnectStr();
        String user = dpo.getConnectAccount();
        String pwd = dpo.getConnectPwd();

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        Map<String, List<String>> table = mysqlConUtils.getTable(url, user, pwd);

        // 将实时表的数据同步地址封装进去
        ArrayList<String> conn = new ArrayList<>();
        conn.add(url);
//        conn.add(user);
//        conn.add(pwd);
        table.put("conn", conn);
        if (table.isEmpty()) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

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

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);
        rows = Math.max(rows, 1);

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
        TableAccessPO tapo = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();

        TableAccessNonDTO dto = new TableAccessNonDTO(tapo);

        // 将应用名称封装进去
        AppRegistrationPO modelAccess = appRegistrationImpl.query().eq("id", tapo.getAppid()).one();
        dto.setAppName(modelAccess.getAppName());

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        List<TableFieldsDTO> listField = new ArrayList<>();
        for (TableFieldsPO modelField : list) {
            TableFieldsDTO tableFieldsDTO = new TableFieldsDTO(modelField);
            listField.add(tableFieldsDTO);
        }

        dto.setList(listField);

//        int isRealtime = tapo.getIsRealtime();

        // 非实时数据比实时数据多了个tb_table_syncmode表数据
        // 当要回显的数据是非实时的时候,要将tb_table_syncmode表数据封装进去
        // 非实时数据
//        if (isRealtime == 1) {
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        TableSyncmodeDTO sdto = new TableSyncmodeDTO(modelSync);

        dto.setTableSyncmodeDTO(sdto);
//        }

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
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("app_name", appName).one();

        // tb_app_registration表id
        long appid = modelReg.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("appid", appid).one();
        String url = modelDataSource.getConnectStr();
        String user = modelDataSource.getConnectAccount();
        String pwd = modelDataSource.getConnectPwd();

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        List<TablePyhNameDTO> list = new ArrayList<>();

        list = mysqlConUtils.getnrttable(url, user, pwd);

        return list;
    }


    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(long id) {

        // 1.删除tb_table_access数据
        TableAccessPO tapo = this.getById(id);
        if (tapo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 假删除
        tapo.setDelFlag(0);
        boolean update = this.updateById(tapo);
        if (!update) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.删除tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        // 判断是否存在表字段
        if (list == null || list.isEmpty()) {
            return ResultEnum.SUCCESS;
        }

        for (TableFieldsPO tfpo : list) {
            tfpo.setDelFlag(0);
        }

        boolean success = tableFieldsImpl.updateBatchById(list);

        return success ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_BUILDTABLE_AND_COLUMN)
    @Override
    public AtlasEntityDbTableColumnDTO getAtlasBuildTableAndColumn(long id, long appid) {

        TableAccessPO modelAccess = this.query().eq("id", id)
                .eq("appid", appid)
                .eq("del_flag", 1)
                .one();
        if (modelAccess == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        AppDataSourcePO sourcepo = appDataSourceImpl.query().
                eq("appid", appid)
                .eq("del_flag", 1)
                .one();

        if (sourcepo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        AtlasEntityDbTableColumnDTO dto = new AtlasEntityDbTableColumnDTO();

        dto.dbId = sourcepo.getAtlasDbId();
        dto.tableName = modelAccess.getTableName();
        dto.createUser = modelAccess.getCreateUser();
        // TODO
//        dto.tableId = "" + modelAccess.getId() + "";
        dto.tableId = String.valueOf(modelAccess.getId());

        List<AtlasEntityColumnDTO> columns = new ArrayList<>();

        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        if (list.isEmpty()) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
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

        return dto;
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_WRITEBACKDATA)
    @Override
    public AtlasWriteBackDataDTO getAtlasWriteBackDataDTO(long appid, long id) {

        AtlasWriteBackDataDTO dto = new AtlasWriteBackDataDTO();

        // 查询tb_app_registration
        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("id", appid)
                .eq("del_flag", 1)
                .one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        dto.appId = modelReg.atlasInstanceId;

        // 查询tb_app_datasource
        AppDataSourcePO modelData = appDataSourceImpl.query()
                .eq("appid", appid)
                .eq("del_flag", 1)
                .one();
        if (modelData == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        dto.atlasTableId = modelData.atlasDbId;
//        // 查询tb_app_nifiFlow
//        AppNifiFlowPO modelNifiFlow = nifiFlowImpl.query()
//                .eq("id", appid)
//                .one();
//        if (modelNifiFlow == null) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }
//        dto.dorisSelectSqlStr = modelNifiFlow.dorisSelectSqlStr;


        // 查询tb_table_access
        TableAccessPO modelAccess = this.query()
                .eq("id", id)
                .eq("appid", appid)
                .eq("del_flag", 1)
                .one();

        dto.tableId = modelAccess.atlasTableId;
        dto.dorisSelectSqlStr = modelAccess.dorisSelectSqlStr;

        AtlasEntityDbTableColumnDTO atlasDTO = new AtlasEntityDbTableColumnDTO();
        atlasDTO.dbId = modelData.getAtlasDbId();
        atlasDTO.tableName = modelAccess.getTableName();
        atlasDTO.createUser = modelAccess.getCreateUser();

        List<AtlasEntityColumnDTO> columns = new ArrayList<>();

        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();
        if (list.isEmpty()) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
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

        return dto;
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
                .eq("appid", appid)
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
            boolean updateField = this.tableFieldsImpl.updateById(modelFields);
            if (!updateField) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        // nifi配置表: tb_nifi_setting
        NifiSettingPO po = new NifiSettingPO();
        po.tableId = tableId;
        po.appid = appid;
        po.tableName = dto.tableName;
        po.selectSql = dto.dorisSelectSqlStr;

        boolean save = nifiSettingImpl.save(po);

        return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_CONFIG)
    @Override
    public DataAccessConfigDTO dataAccessConfig(long id, long appid) {
        DataAccessConfigDTO dto = new DataAccessConfigDTO();

        // app组配置
        GroupConfig groupConfig = new GroupConfig();

        //任务组配置
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();

        // 数据源jdbc配置
        DataSourceConfig sourceDsConfig = new DataSourceConfig();

        // 目标源jdbc连接
        DataSourceConfig targetDsConfig = new DataSourceConfig();

        // 表及表sql
        ProcessorConfig processorConfig = new ProcessorConfig();

        // 1.app组配置
        // select * from tb_app_registration where id=id and del_flag=1;
        AppRegistrationPO modelReg = this.appRegistrationImpl.query()
                .eq("id", appid)
                .eq("del_flag", 1)
                .one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        groupConfig.setAppName(modelReg.getAppName());
        groupConfig.setAppDetails(modelReg.getAppDes());
        // 回写应用注册组件id
        groupConfig.setComponentId(modelReg.componentId);
        // TODO: 缺失字段(给个默认值)
//        groupConfig.setNewApp(false);


        // 2.任务组配置
        taskGroupConfig.setAppName(modelReg.getAppName());
        taskGroupConfig.setAppDetails(modelReg.getAppDes());


        //3.数据源jdbc配置
        AppDataSourcePO modelDataSource = appDataSourceImpl.query()
                .eq("appid", appid)
                .eq("del_flag", 1)
                .one();
        if (modelDataSource == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        sourceDsConfig.setJdbcStr(modelDataSource.getConnectStr());
        // 先硬编码
        sourceDsConfig.setType(DriverTypeEnum.MYSQL);
        sourceDsConfig.setUser(modelDataSource.getConnectAccount());
        sourceDsConfig.setPassword(modelDataSource.getConnectPwd());
        sourceDsConfig.componentId = modelReg.sourceDbPoolComponentId;

        // 4.目标源jdbc连接
        targetDsConfig.componentId = modelReg.targetDbPoolComponentId;

        // 5.表及表sql
        TableSyncmodePO modelSync = syncmodeMapper.getData(id);
        if (modelSync == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        TableAccessPO modelAccess = this.query()
                .eq("id", id)
                .eq("appid", appid)
                .eq("del_flag", 1)
                .one();
        if (modelAccess == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        if (modelReg.componentId == null && modelAccess.componentId == null) {
            groupConfig.setNewApp(true);
        } else {
            groupConfig.setNewApp(false);
        }

        // TODO 回写物理表组件id
        taskGroupConfig.setComponentId(modelAccess.componentId);

        NifiSettingPO modelNifi = nifiSettingImpl.query()
                .eq("appid", appid)
                .eq("table_id", id)
                .one();
        if (modelNifi == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

//        // TODO: 将app组配置中的setNewApp加上
//        if (modelNifi.appGroupId == null && modelNifi.tableGroupId == null) {
//            groupConfig.setNewApp(true);
//        } else {
//            groupConfig.setNewApp(false);
//        }


        // corn_expression
        processorConfig.scheduleExpression = modelSync.getCornExpression();

        String timerDriver = "timer driver";
        String corn = "CORN driven";

        if (timerDriver.equalsIgnoreCase(modelSync.timerDriver)) {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.TIMER;
        } else if (corn.equalsIgnoreCase(modelSync.timerDriver)) {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.CRON;
        } else {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.EVENT;
        }

        // ods sql
        processorConfig.sourceExecSqlQuery = modelNifi.selectSql;
        // atlas返回的tableName
        processorConfig.targetTableName = modelNifi.tableName;

        dto.groupConfig = groupConfig;
        dto.taskGroupConfig = taskGroupConfig;
        dto.sourceDsConfig = sourceDsConfig;
        dto.targetDsConfig = targetDsConfig;
        dto.processorConfig = processorConfig;

//        int a = 1 / 0;

        return dto;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addComponentId(NifiAccessDTO dto) {

        AppRegistrationPO modelReg = this.appRegistrationImpl.query()
                .eq("id", dto.appid)
                .eq("del_flag", 1)
                .one();

        if (modelReg==null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        modelReg.targetDbPoolComponentId = dto.targetDbPoolComponentId;
        modelReg.sourceDbPoolComponentId = dto.sourceDbPoolComponentId;

        boolean updateReg = true;
        if (modelReg.componentId == null) {
            modelReg.componentId = dto.appGroupId;
            // 更新tb_app_appRegistration表componentId
            updateReg = this.appRegistrationImpl.updateById(modelReg);
        }

        if (!updateReg) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        TableAccessPO modelAccess = this.query()
                .eq("id", dto.tableId)
                .eq("appid", dto.appid)
                .eq("del_flag", 1)
                .one();
        modelAccess.componentId = dto.tableGroupId;

        boolean updateAccess = this.updateById(modelAccess);

//        NifiSettingPO modelNifi = this.nifiSettingImpl.query()
//                .eq("table_id", dto.tableId)
//                .eq("appid", dto.appid)
//                .one();
//        if (modelNifi == null) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }
//
//        modelNifi.appGroupId = dto.appGroupId;
//        modelNifi.tableGroupId = dto.tableGroupId;
//        boolean updateNifi = this.nifiSettingImpl.updateById(modelNifi);

        return updateAccess ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
