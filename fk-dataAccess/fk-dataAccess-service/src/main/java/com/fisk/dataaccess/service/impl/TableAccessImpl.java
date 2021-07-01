package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableSyncmodeMapper;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.utils.MysqlConUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
@Service
public class TableAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements ITableAccess {

    @Autowired
    private TableFieldsImpl tableFieldsImpl;

    @Autowired
    private AppRegistrationImpl appRegistrationImpl;

    @Autowired
    private AppDataSourceImpl appDataSourceImpl;

    @Autowired
    private TableSyncmodeImpl syncmodeImpl;

    @Resource
    private TableSyncmodeMapper syncmodeMapper;

    @Autowired
    private AppDriveTypeImpl appDriveTypeImpl;


    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO
     * @return
     */
    @Override
    @Transactional
    public ResultEnum addRTData(TableAccessDTO tableAccessDTO) {

        // TODO: 原始SQL表创建(暂时不用集成)
        // 根据应用名称,查询出具体的数据源驱动(现阶段是MySqL和SQL Server)
/*        AppRegistrationPO one = appRegistrationImpl.query()
                .eq("app_name", tableAccessDTO.getAppName())
                .eq("del_flag", 1)
                .one();
        // 0-1.获取appid
        long appid = one.getId();

        // 0-2.根据id查询数据源驱动类型(appid就是tb_app_drivetype表的id)
        AppDriveTypePO driveTypePO = appDriveTypeImpl.query().eq("id", appid).one();
        String driveName = driveTypePO.getName(); // 数据源驱动名称
        if (driveName.equalsIgnoreCase("MySqL")) {
            // 先创建表
            MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();

            int i = mysqlTableUtils.createMysqlTB(tableAccessDTO);
            if (i != 0) {
                throw new FkException(500, "创建" + tableAccessDTO.getTableName() + "表失败");
            }
        } else {
            SqlServerTableUtils sqlServerTableUtils = new SqlServerTableUtils();
            int i = sqlServerTableUtils.createSqlServerTB(tableAccessDTO);
            if (i != 0) {
                throw new FkException(500, "创建" + tableAccessDTO.getTableName() + "表失败");
            }
        }*/

        // 1.dto->po
        TableAccessPO tableAccessPO = tableAccessDTO.toEntity(TableAccessPO.class);


        /**
         * 数据保存: 添加应用的时候,相同的表名不可以再次添加
         */
        List<String> tableNameList = baseMapper.getTableName();
        String tableName = tableAccessPO.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            throw new FkException(ResultEnum.Table_NAME_EXISTS, "当前" + tableName + "已存在,请重新输入");
        }

        AppRegistrationPO registrationPO = appRegistrationImpl.query()
                .eq("app_name", tableAccessDTO.getAppName())
                .eq("del_flag", 1)
                .one();

        long id = registrationPO.getId();
        if (id < 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "保存失败");
        }
        tableAccessPO.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        tableAccessPO.setIsRealtime(0);
        // 实时物理表，需要提供数据同步地址(先来个硬编码)
//        tableAccessPO.setSyncSrc("jdbc:mysql://192.168.11.130:3306/dmp_datainput_db");
//        List<String> conn = tableAccessDTO.getConn();
        tableAccessPO.setSyncSrc(tableAccessDTO.getSyncSrc());
        tableAccessPO.setDelFlag(1);
        tableAccessPO.setIsRealtime(0); // 实时

        // 时间字段有问题,待定
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());

        tableAccessPO.setCreateTime(date1);
        tableAccessPO.setUpdateTime(date1);

        // 2.保存tb_table_access数据
        boolean save1 = this.save(tableAccessPO);

        if (!save1) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean save2 = true;
        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getTableFieldsDTOS();

        //TODO: 这一块判断先不加
        // 表字段不为空判断
//        if (tableFieldsDTOS == null||tableFieldsDTOS.isEmpty()) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }

        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
            tableFieldsPO.setTableAccessId(tableAccessPO.getId());
            /*// 1是业务时间，0非业务时间
            tableFieldsPO.setIsBusinesstime(0);
            // 1是时间戳，0非时间戳
            tableFieldsPO.setIsTimestamp(0);*/
            // 1是实时物理表的字段，0是非实时物理表的字段
            tableFieldsPO.setIsRealtime(1);
            tableFieldsPO.setDelFlag(1);

            // 时间
            Date date2 = new Date(System.currentTimeMillis());
            tableFieldsPO.setCreateTime(date2);
            tableFieldsPO.setUpdateTime(date2);

            save2 = tableFieldsImpl.save(tableFieldsPO);
        }

//        if (!save2) {
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
//        }

/*        CreateMysqlTableUtils createMysqlTableUtils = new CreateMysqlTableUtils();

        int i = createMysqlTableUtils.createMysqlTB(tableAccessDTO);*/
//        System.out.println(i);

//        return i == 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        return save2 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     *
     * @param tableAccessNDTO
     * @return
     */
    @Override
    public ResultEnum addNRTData(TableAccessNDTO tableAccessNDTO) {

        // 先创建表
/*        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();

        int i = mysqlTableUtils.createMysqlTB(tableAccessNDTO);
        if (i != 0) {
            throw new FkException(500, "创建" + tableAccessNDTO.getTableName() + "表失败");
        }*/

        // 根据应用名称,查询出具体的数据源驱动(现阶段是MySqL和SQL Server)
        // 0-1.获取appid
        // TODO: 原始SQL表创建(暂时不用集成)
/*        AppRegistrationPO one = appRegistrationImpl.query()
                .eq("app_name", tableAccessNDTO.getAppName())
                .eq("del_flag", 1)
                .one();
        // 0-1.获取appid
        long appid = one.getId();

        // 0-2.根据id查询数据源驱动类型(appid就是tb_app_drivetype表的id)
        AppDriveTypePO driveTypePO = appDriveTypeImpl.query().eq("id", appid).one();
        String driveName = driveTypePO.getName(); // 数据源驱动名称
        if (driveName.equalsIgnoreCase("MySqL")) {
            // 先创建表
            MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();

            int i = mysqlTableUtils.createMysqlTB(tableAccessNDTO);
            if (i != 0) {
                throw new FkException(500, "创建" + tableAccessNDTO.getTableName() + "表失败");
            }
        } else {
            SqlServerTableUtils sqlServerTableUtils = new SqlServerTableUtils();
            int i = sqlServerTableUtils.createSqlServerTB(tableAccessNDTO);
            if (i != 0) {
                throw new FkException(500, "创建" + tableAccessNDTO.getTableName() + "表失败");
            }
        }*/

        // 1.dto->po
        TableAccessPO tableAccessPO = tableAccessNDTO.toEntity(TableAccessPO.class);

        /**
         * 数据保存: 添加应用的时候,相同的表名不可以再次添加
         */
        List<String> tableNameList = baseMapper.getTableName();
        String tableName = tableAccessPO.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            throw new FkException(ResultEnum.Table_NAME_EXISTS, "当前" + tableName + "已存在,请重新输入");
        }

        AppRegistrationPO registrationPO = appRegistrationImpl.query()
                .eq("app_name", tableAccessNDTO.getAppName())
                .eq("del_flag", 1)
                .one();

        long id = registrationPO.getId();
        if (id < 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "保存失败");
        }
        tableAccessPO.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        tableAccessPO.setIsRealtime(0);
        // 实时物理表，需要提供数据同步地址(先来个硬编码)
//        tableAccessPO.setSyncSrc("jdbc:mysql://192.168.11.130:3306/dmp_datainput_db");
//        List<String> conn = tableAccessDTO.getConn();
        tableAccessPO.setSyncSrc(tableAccessNDTO.getSyncSrc());
        tableAccessPO.setDelFlag(1);
        tableAccessPO.setIsRealtime(1); // 非实时

        // 时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());
        tableAccessPO.setCreateTime(date1);
        tableAccessPO.setUpdateTime(date1);

        // 2.保存tb_table_access数据
        boolean save1 = this.save(tableAccessPO);

        if (!save1) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean save2 = true;
        List<TableFieldsDTO> tableFieldsDTOS = tableAccessNDTO.getTableFieldsDTOS();

        // TODO: 这一块判断先不加
        // 表字段不为空判断
//        if (tableFieldsDTOS == null||tableFieldsDTOS.isEmpty()) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }

        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
            tableFieldsPO.setTableAccessId(tableAccessPO.getId());
/*            // 1是业务时间，0非业务时间
            tableFieldsPO.setIsBusinesstime(0);
            // 1是时间戳，0非时间戳
            tableFieldsPO.setIsTimestamp(0);*/
            // 1是实时物理表的字段，0是非实时物理表的字段
            tableFieldsPO.setIsRealtime(1);
            tableFieldsPO.setDelFlag(1);

            // 时间
            Date date2 = new Date(System.currentTimeMillis());
            tableFieldsPO.setCreateTime(date2);
            tableFieldsPO.setUpdateTime(date2);

            save2 = tableFieldsImpl.save(tableFieldsPO);
        }

        if (!save2) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_syncmode数据
        TableSyncmodeDTO syncmodeDTO = tableAccessNDTO.getTableSyncmodeDTO();
        TableSyncmodePO syncmodePO = syncmodeDTO.toEntity(TableSyncmodePO.class);
        long accessPOId = tableAccessPO.getId();
        syncmodePO.setId(accessPOId);

        boolean save3 = syncmodeImpl.save(syncmodePO);
//        if (!save3) {
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
//        }
//
//        CreateMysqlTableUtils createMysqlTableUtils = new CreateMysqlTableUtils();
//
//        int i = createMysqlTableUtils.createMysqlTB(tableAccessNDTO);
//        System.out.println(i);

//        return i == 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        return save3 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

    }

    /**
     * 修改物理表(实时)
     *
     * @param tableAccessDTO
     * @return
     */
    @Override
    public ResultEnum updateRTData(TableAccessDTO tableAccessDTO) {

        // TODO: 原始SQL表修改(暂时不用集成)
/*        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();

        int i = mysqlTableUtils.updateMysqlTB(tableAccessDTO);
        if (i != 0) {
            throw new FkException(500, "操作数据库失败");
        }*/

        // 1.dto->po
        TableAccessPO tableAccessPO = tableAccessDTO.toEntity(TableAccessPO.class);

        // 时间字段
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());

        tableAccessPO.setUpdateTime(date1);
        tableAccessPO.setDelFlag(1);

        // 2.保存tb_table_access数据
        boolean update1 = this.updateById(tableAccessPO);

        if (!update1) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        /**
         * 保存tb_table_fields数据: 分为更新和添加数据
         */
        boolean update2 = true;
        boolean saveField = true;

//        Date date2 = new Date(System.currentTimeMillis());
        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getTableFieldsDTOS();
       /* for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);

            // 时间
            tableFieldsPO.setUpdateTime(date2);

            update2 = tableFieldsImpl.updateById(tableFieldsPO);
        }*/


        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {

            // 0: 旧数据不操作  2: 新增  1: 修改
            int funcType = tableFieldsDTO.getFuncType();
            if (funcType == 1) { // 修改
                TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
                Date date2 = new Date(System.currentTimeMillis());
                tableFieldsPO.setUpdateTime(date2);
                tableFieldsPO.setDelFlag(1);

                update2 = tableFieldsImpl.updateById(tableFieldsPO);
            } else if (funcType == 2) { // 新增
                TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);

                // 还要绑定tb_table_access id
                /*long fid = tableFieldsPO.getId();
                // 根据已传的field查询tb_table_access id
                TableFieldsPO one = tableFieldsImpl.query().eq("id", fid).eq("del_flag", 1).one();
                long accessId = one.getTableAccessId();*/
                tableFieldsPO.setTableAccessId(tableAccessPO.getId());

                Date date3 = new Date(System.currentTimeMillis());
                tableFieldsPO.setCreateTime(date3);
                tableFieldsPO.setUpdateTime(date3);
                tableFieldsPO.setDelFlag(1);
                saveField = tableFieldsImpl.save(tableFieldsPO);

            }
        }

        if (!update2) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据保存失败");
        }

        /*CreateTableUtils createTableUtils = new CreateTableUtils();

        int i = createTableUtils.updateMysqlTB(tableAccessDTO);*/
//        System.out.println(i);

//        return i == 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        return saveField ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 修改物理表(非实时)
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateNRTData(TableAccessNDTO dto) {

        // TODO: 原始SQL表修改(暂时不用集成)
        // 1.先修改表
/*        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();

        int i = mysqlTableUtils.updateMysqlTB(dto);
        if (i != 0) {
            throw new FkException(500, "操作数据库失败");
        }*/

        // dto->po
        TableAccessPO tableAccessPO = dto.toEntity(TableAccessPO.class);

        // 时间字段
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());

        tableAccessPO.setUpdateTime(date1);
        tableAccessPO.setDelFlag(1);

        // 2.保存tb_table_access数据
        boolean update1 = this.updateById(tableAccessPO);

        if (!update1) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        /**
         * 保存tb_table_fields数据: 分为更新和添加数据
         */
        boolean update2 = true;
        boolean saveField = true;

        List<TableFieldsDTO> tableFieldsDTOS = dto.getTableFieldsDTOS();


        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {

            /*// 0:旧数据不操作  1:修改表字段  2:新增表字段
            int funcType = tableFieldsDTO.getFuncType();
            if (funcType == 1) { // 修改
                TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
                Date date2 = new Date(System.currentTimeMillis());
                tableFieldsPO.setUpdateTime(date2);
                tableFieldsPO.setDelFlag(1);

                update2 = tableFieldsImpl.updateById(tableFieldsPO);
            } else if (funcType == 2) { // 新增
                TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);

                // 还要绑定tb_table_access id
                tableFieldsPO.setTableAccessId(tableAccessPO.getId());

                Date date3 = new Date(System.currentTimeMillis());
                tableFieldsPO.setCreateTime(date3);
                tableFieldsPO.setUpdateTime(date3);
                tableFieldsPO.setDelFlag(1);
                saveField = tableFieldsImpl.save(tableFieldsPO);

            }*/
            TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
            Date date2 = new Date(System.currentTimeMillis());
            tableFieldsPO.setUpdateTime(date2);
            tableFieldsPO.setDelFlag(1);

            update2 = tableFieldsImpl.updateById(tableFieldsPO);
        }

        if (!update2) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }
        if (!saveField) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }


        // 4.保存tb_table_syncmode数据
        boolean update3 = true;
        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        TableSyncmodePO tableSyncmodePO = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        update3 = syncmodeImpl.updateById(tableSyncmodePO);


        return update3 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * TODO: 暂时不需要此方法
     * 根据非实时应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appName
     * @return
     */
    @Override
    public Map<String, List<String>> queryDataBase(String appName) throws SQLException, ClassNotFoundException {

        // 1.根据应用名称查询表id
        AppRegistrationPO registrationPO = appRegistrationImpl.query().eq("app_name", appName).one();

        // tb_app_registration表id
        long appid = registrationPO.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO dataSourcePO = appDataSourceImpl.query().eq("appid", appid).one();
        String url = dataSourcePO.getConnectStr();
        String user = dataSourcePO.getConnectAccount();
        String pwd = dataSourcePO.getConnectPwd();

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
     * @param key
     * @param page
     * @param rows
     * @return
     */
//    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);  // 返回二者间较小的值,即当前页最大不超过100页,避免单词查询太多数据影响效率
        rows = Math.max(rows, 1);    // 每页至少1条

/*        Page<TableAccessPO> accessPOPage = new Page<>(page, rows);
        PageDTO<TablePhyHomeDTO> pageDTO = new PageDTO<>();

        // 分页对象集合
        List<TablePhyHomeDTO> homeDTOList = new ArrayList<>();

        // 2.先查询tb_table_access数据 表名  表描述
        boolean isKeyExists = StringUtils.isNoneBlank(key);

        List<TableAccessPO> accessPOList = this.query()
                .like(isKeyExists, "table_name", key) // 根据表名称模糊查询
                .eq("del_flag", 1)
                .list();// 未删除

        // 3.查询tb_table_fields数据  更新时间 数据列数  数据行数  增量字段
        List<TableFieldsPO> fieldsPOList = new ArrayList<>();
        for (TableAccessPO accessPO : accessPOList) {

            long id = accessPO.getId();

            TableFieldsPO fieldsPO = tableFieldsImpl.query()
                    .eq("table_access_id", id)
                    .eq("del_flag", 1)
                    .one();

            fieldsPOList.add(fieldsPO);
        }

        // 4.查询tb_table_syncmode数据   同步方式  同步频率
        List<TableSyncmodePO> syncmodePOList = new ArrayList<>();
        for (TableAccessPO accessPO : accessPOList) {
            long id = accessPO.getId();
            TableSyncmodePO syncmodePO = syncmodeImpl.query().eq("id", id).one();
            syncmodePOList.add(syncmodePO);
        }

//        Page<TableAccessPO> selectPage = baseMapper.selectPage(accessPOPage, this.query().like(isKeyExists, "table_name", key)
//                .eq("del_flag", 1));


        // 封装分页查询出来的数据
        // 总条数
        pageDTO.setTotal((long) accessPOList.size());
        // 总页数
        long totalPage = (long) (accessPOList.size() + rows - 1) / rows;
        pageDTO.setTotalPage(accessPOPage.getPages());
//        pageDTO.setItems();

        return pageDTO;*/

//        List<TablePhyHomeDTO> homeDTOList = baseMapper.queryByPage(key);

        // 新建分页
        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    /**
     * 回显实时表
     *
     * @param id
     * @return
     */
    @Override
    public TableAccessNDTO getData(long id) {

        // 查询tb_table_access数据
        TableAccessPO accessPO = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();

        TableAccessNDTO accessNDTO = new TableAccessNDTO(accessPO);

        // 将应用名称封装进去
        AppRegistrationPO registrationPO = appRegistrationImpl.query().eq("id", accessPO.getAppid()).one();
        accessNDTO.setAppName(registrationPO.getAppName());

        // 查询tb_table_fields数据
        List<TableFieldsPO> fieldsPOS = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        List<TableFieldsDTO> tableFieldsDTOS = new ArrayList<>();
        for (TableFieldsPO fieldsPO : fieldsPOS) {
            TableFieldsDTO tableFieldsDTO = new TableFieldsDTO(fieldsPO);
            tableFieldsDTOS.add(tableFieldsDTO);
        }

        accessNDTO.setTableFieldsDTOS(tableFieldsDTOS);

        int isRealtime = accessPO.getIsRealtime();

        // 非实时数据比实时数据多了个tb_table_syncmode表数据
        // 当要回显的数据是非实时的时候,要将tb_table_syncmode表数据封装进去
        if (isRealtime == 1) {// 非实时数据
//            TableSyncmodePO syncmodePO = this.syncmodeImpl.query().eq("id", id).one();
            TableSyncmodePO syncmodePO = this.syncmodeMapper.getData(id);
            TableSyncmodeDTO syncmodeDTO = new TableSyncmodeDTO(syncmodePO);

            accessNDTO.setTableSyncmodeDTO(syncmodeDTO);
        }

        return accessNDTO;
    }


    /**
     * 根据应用名称,获取物理表名及表对应的字段(非实时)
     *
     * @param appName
     * @return
     */
    @Override
    public List<TablePyhNameDTO> getTableFields(String appName) {

        // 1.根据应用名称查询表id
        AppRegistrationPO registrationPO = appRegistrationImpl.query().eq("app_name", appName).one();

        // tb_app_registration表id
        long appid = registrationPO.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO dataSourcePO = appDataSourceImpl.query().eq("appid", appid).one();
        String url = dataSourcePO.getConnectStr();
        String user = dataSourcePO.getConnectAccount();
        String pwd = dataSourcePO.getConnectPwd();

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {

            list = mysqlConUtils.getNRTTable(url, user, pwd);
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

        return list;
    }

    /**
     * 删除数据
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(long id) {

        // 1.删除tb_table_access数据
        TableAccessPO accessPO = this.getById(id);
        if (accessPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 假删除
        accessPO.setDelFlag(0);
        boolean update = this.updateById(accessPO);
        if (!update) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.删除tb_table_fields数据
        List<TableFieldsPO> fieldsPOList = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        // 判断是否存在表字段
        if (fieldsPOList == null || fieldsPOList.isEmpty()) {
            return ResultEnum.SUCCESS;
        }

        for (TableFieldsPO tableFieldsPO : fieldsPOList) {
            tableFieldsPO.setDelFlag(0);
        }

        boolean success = tableFieldsImpl.updateBatchById(fieldsPOList);

        return success ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
