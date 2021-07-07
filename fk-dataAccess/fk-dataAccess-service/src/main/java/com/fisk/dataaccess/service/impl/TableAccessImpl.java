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
 * @author Lock
 */
@Service
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

        // 1.dto->po
        TableAccessPO tpo = tableAccessDTO.toEntity(TableAccessPO.class);

        // 数据保存: 添加应用的时候,相同的表名不可以再次添加
        List<String> tableNameList = baseMapper.getTableName();
        String tableName = tpo.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            throw new FkException(ResultEnum.Table_NAME_EXISTS, "当前" + tableName + "已存在,请重新输入");
        }

        AppRegistrationPO arpo = appRegistrationImpl.query()
                .eq("app_name", tableAccessDTO.getAppName())
                .eq("del_flag", 1)
                .one();

        long id = arpo.getId();
        if (id < 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "保存失败");
        }
        tpo.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        tpo.setIsRealtime(0);
        tpo.setSyncSrc(tableAccessDTO.getSyncSrc());
        tpo.setDelFlag(1);
        // 实时
        tpo.setIsRealtime(0);

        // 时间字段有问题,待定
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());

        tpo.setCreateTime(date1);
        tpo.setUpdateTime(date1);

        // 2.保存tb_table_access数据
        boolean save1 = this.save(tpo);

        if (!save1) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean save2 = true;
        List<TableFieldsDTO> list = tableAccessDTO.getList();

        //TODO: 这一块判断先不加
        // 表字段不为空判断
//        if (tableFieldsDTOS == null||tableFieldsDTOS.isEmpty()) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }

        for (TableFieldsDTO tableFieldsDTO : list) {
            TableFieldsPO tfpo = tableFieldsDTO.toEntity(TableFieldsPO.class);
            tfpo.setTableAccessId(tpo.getId());

            // 1是实时物理表的字段，0是非实时物理表的字段
            tfpo.setIsRealtime(1);
            tfpo.setDelFlag(1);

            // 时间
            Date date2 = new Date(System.currentTimeMillis());
            tfpo.setCreateTime(date2);
            tfpo.setUpdateTime(date2);

            save2 = tableFieldsImpl.save(tfpo);
        }

//        if (!save2) {
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
//        }

/*        CreateMysqlTableUtils createMysqlTableUtils = new CreateMysqlTableUtils();

        int i = createMysqlTableUtils.createmysqltb(tableAccessDTO);*/
//        System.out.println(i);

//        return i == 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

        return save2 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     *
     * @param tableAccessNonDTO dto
     * @return 执行结果
     */
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

        // 1.dto->po
        TableAccessPO tapo = tableAccessNonDTO.toEntity(TableAccessPO.class);

        List<String> tableNameList = baseMapper.getTableName();
        String tableName = tapo.getTableName();
        boolean contains = tableNameList.contains(tableName);
        if (contains) {
            throw new FkException(ResultEnum.Table_NAME_EXISTS, "当前" + tableName + "已存在,请重新输入");
        }

        AppRegistrationPO arpo = appRegistrationImpl.query()
                .eq("app_name", tableAccessNonDTO.getAppName())
                .eq("del_flag", 1)
                .one();

        long id = arpo.getId();
        if (id < 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "保存失败");
        }
        tapo.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        tapo.setIsRealtime(0);
        tapo.setSyncSrc(tableAccessNonDTO.getSyncSrc());
        tapo.setDelFlag(1);
        // 非实时
        tapo.setIsRealtime(1);

        // 时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());
        tapo.setCreateTime(date1);
        tapo.setUpdateTime(date1);

        // 2.保存tb_table_access数据
        boolean save1 = this.save(tapo);

        if (!save1) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean save2 = true;
        List<TableFieldsDTO> tfdto = tableAccessNonDTO.getList();

        // TODO: 这一块判断先不加
        // 表字段不为空判断
//        if (tableFieldsDTOS == null||tableFieldsDTOS.isEmpty()) {
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
//        }

        for (TableFieldsDTO tableFieldsDTO : tfdto) {
            TableFieldsPO tfpo = tableFieldsDTO.toEntity(TableFieldsPO.class);
            tfpo.setTableAccessId(tapo.getId());

            // 1是实时物理表的字段，0是非实时物理表的字段
            tfpo.setIsRealtime(1);
            tfpo.setDelFlag(1);

            // 时间
            Date date2 = new Date(System.currentTimeMillis());
            tfpo.setCreateTime(date2);
            tfpo.setUpdateTime(date2);

            save2 = tableFieldsImpl.save(tfpo);
        }

        if (!save2) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_syncmode数据
        TableSyncmodeDTO syncmodeDTO = tableAccessNonDTO.getTableSyncmodeDTO();
        TableSyncmodePO spo = syncmodeDTO.toEntity(TableSyncmodePO.class);
        long aid = tapo.getId();
        spo.setId(aid);

        boolean save3 = syncmodeImpl.save(spo);
//        if (!save3) {
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
//        }
//
//        CreateMysqlTableUtils createMysqlTableUtils = new CreateMysqlTableUtils();
//
//        int i = createMysqlTableUtils.createmysqltb(tableAccessNonDTO);
//        System.out.println(i);

        return save3 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

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

        // 1.dto->po
        TableAccessPO tapo = tableAccessDTO.toEntity(TableAccessPO.class);

        // 时间字段
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());

        tapo.setUpdateTime(date1);
        tapo.setDelFlag(1);

        // 2.保存tb_table_access数据
        boolean update1 = this.updateById(tapo);

        if (!update1) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 保存tb_table_fields数据: 分为更新和添加数据
        boolean update2 = true;
        boolean saveField = true;

        List<TableFieldsDTO> list = tableAccessDTO.getList();


        for (TableFieldsDTO tableFieldsDTO : list) {

            // 0: 旧数据不操作  2: 新增  1: 修改
            int funcType = tableFieldsDTO.getFuncType();
            // 修改
            if (funcType == 1) {
                TableFieldsPO tfpo = tableFieldsDTO.toEntity(TableFieldsPO.class);
                Date date2 = new Date(System.currentTimeMillis());
                tfpo.setUpdateTime(date2);
                tfpo.setDelFlag(1);

                update2 = tableFieldsImpl.updateById(tfpo);
                // 新增
            } else if (funcType == 2) {
                TableFieldsPO tfpo = tableFieldsDTO.toEntity(TableFieldsPO.class);

                // 还要绑定tb_table_access id
                tfpo.setTableAccessId(tapo.getId());

                Date date3 = new Date(System.currentTimeMillis());
                tfpo.setCreateTime(date3);
                tfpo.setUpdateTime(date3);
                tfpo.setDelFlag(1);
                saveField = tableFieldsImpl.save(tfpo);

            }
        }

        if (!update2) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据保存失败");
        }

//        CreateTableUtils createTableUtils = new CreateTableUtils();
//
//        int i = createTableUtils.updatemysqltb(tableAccessDTO);
//        System.out.println(i);

        return saveField ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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
/*        MysqlTableUtils mysqlTableUtils = new MysqlTableUtils();

        int i = mysqlTableUtils.updatemysqltb(dto);
        if (i != 0) {
            throw new FkException(500, "操作数据库失败");
        }*/

        // dto->po
        TableAccessPO tapo = dto.toEntity(TableAccessPO.class);

        // 时间字段
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date1 = new Date(System.currentTimeMillis());

        tapo.setUpdateTime(date1);
        tapo.setDelFlag(1);

        // 2.保存tb_table_access数据
        boolean update1 = this.updateById(tapo);

        if (!update1) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 保存tb_table_fields数据: 分为更新和添加数据
        boolean update2 = true;
//        boolean saveField = true;

        List<TableFieldsDTO> list = dto.getList();


        for (TableFieldsDTO tableFieldsDTO : list) {

            TableFieldsPO tfpo = tableFieldsDTO.toEntity(TableFieldsPO.class);
            Date date2 = new Date(System.currentTimeMillis());
            tfpo.setUpdateTime(date2);
            tfpo.setDelFlag(1);

            update2 = tableFieldsImpl.updateById(tfpo);
        }

        if (!update2) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }
//        if (!saveField) {
//            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
//        }


        // 4.保存tb_table_syncmode数据
        boolean update3 = true;
        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        TableSyncmodePO tspo = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        update3 = syncmodeImpl.updateById(tspo);


        return update3 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * TODO: 暂时不需要此方法
     * 根据非实时应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appName appName
     * @return 返回值
     */
    @Override
    public Map<String, List<String>> queryDataBase(String appName) throws SQLException, ClassNotFoundException {

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
     * @param key key
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
        AppRegistrationPO apo = appRegistrationImpl.query().eq("id", tapo.getAppid()).one();
        dto.setAppName(apo.getAppName());

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .eq("del_flag", 1)
                .list();

        List<TableFieldsDTO> list1 = new ArrayList<>();
        for (TableFieldsPO fpo : list) {
            TableFieldsDTO tableFieldsDTO = new TableFieldsDTO(fpo);
            list1.add(tableFieldsDTO);
        }

        dto.setList(list1);

        int isRealtime = tapo.getIsRealtime();

        // 非实时数据比实时数据多了个tb_table_syncmode表数据
        // 当要回显的数据是非实时的时候,要将tb_table_syncmode表数据封装进去
        // 非实时数据
        if (isRealtime == 1) {
            TableSyncmodePO spo = this.syncmodeMapper.getData(id);
            TableSyncmodeDTO sdto = new TableSyncmodeDTO(spo);

            dto.setTableSyncmodeDTO(sdto);
        }

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
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {

            list = mysqlConUtils.getnrttable(url, user, pwd);
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

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
}
