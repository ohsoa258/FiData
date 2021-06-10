package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.utils.CreateTableUtils;
import com.fisk.dataaccess.utils.MysqlConUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

//    @Resource
//    private TableFieldsMapper tableFieldsMapper;

    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO
     * @return
     */
    @Override
    @Transactional
    public ResultEnum addRTData(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {

        // 1.dto->po
        TableAccessPO tableAccessPO = tableAccessDTO.toEntity(TableAccessPO.class);

        AppRegistrationPO registrationPO = appRegistrationImpl.query().eq("app_name", tableAccessDTO.getAppName()).one();

        long id = registrationPO.getId();
        if (id < 0) {
            throw new FkException(500, "保存失败");
        }
        tableAccessPO.setAppid(id);

        // 0是实时物理表，1是非实时物理表
        tableAccessPO.setIsRealtime(0);
        // 实时物理表，需要提供数据同步地址(先来个硬编码)
//        tableAccessPO.setSyncSrc("jdbc:mysql://192.168.11.130:3306/dmp_datainput_db");
//        List<String> conn = tableAccessDTO.getConn();
        tableAccessPO.setSyncSrc(tableAccessDTO.getSyncSrc());
        tableAccessPO.setDelFlag(1);



        // 时间字段有问题,待定
        LocalDateTime dateTime = LocalDateTime.now();
        String  date = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00").format(dateTime);

        tableAccessPO.setCreateTime(dateTime);
        tableAccessPO.setUpdateTime(dateTime);

        // 2.保存tb_table_access数据
        boolean save1 = this.save(tableAccessPO);

        if (!save1) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        boolean save2 = true;
        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getTableFieldsDTOS();
        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
            tableFieldsPO.setTableAccessId(tableAccessPO.getId());
            // 1是业务时间，0非业务时间
            tableFieldsPO.setIsBusinesstime(0);
            // 1是时间戳，0非时间戳
            tableFieldsPO.setIsTimestamp(0);
            // 1是实时物理表的字段，0是非实时物理表的字段
            tableFieldsPO.setIsRealtime(1);
            tableFieldsPO.setDelFlag(1);

            // 时间
            tableFieldsPO.setCreateTime(dateTime);
            tableFieldsPO.setUpdateTime(dateTime);

            save2 = tableFieldsImpl.save(tableFieldsPO);
        }

        if (!save2) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        CreateTableUtils createTableUtils = new CreateTableUtils();

        int i = createTableUtils.createMysqlTB(tableAccessDTO);
        System.out.println(i);

        return i == 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 删除数据
     *
     * @param id
     * @return
     */
    @Override
    public ResultEnum deleteData(long id) {

        // 1.删除tb_table_access数据
        TableAccessPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 假删除
        model.setDelFlag(0);
        boolean update = this.updateById(model);
        if (!update) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.删除tb_table_fields数据
        TableFieldsPO po = tableFieldsImpl.query().eq("table_access_id", id).one();
        po.setDelFlag(0);
        boolean success = tableFieldsImpl.updateById(po);

        return success ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     *
     * @param tableAccessNDTO
     * @return
     */
    @Override
    public ResultEnum addNRTData(TableAccessNDTO tableAccessNDTO) {
        return null;
    }

    /**
     * 修改物理表(实时)
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateRTData(TableAccessDTO dto) {



        return null;
    }

    /**
     * 修改物理表(非实时)
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateNRTData(TableAccessNDTO dto) {
        return null;
    }

    /**
     * 根据应用名称,获取远程数据库的表及表对应的字段
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
            throw new FkException(500, "获取表字段失败");
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
    @Override
    public PageDTO<TablePhyHomeDTO> queryByPage(String key, Integer page, Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);  // 返回二者间较小的值,即当前页最大不超过100页,避免单词查询太多数据影响效率
        rows = Math.max(rows, 1);    // 每页至少1条

        Page<TableAccessPO> accessPOPage = new Page<>(page, rows);
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

        return pageDTO;
    }
}
