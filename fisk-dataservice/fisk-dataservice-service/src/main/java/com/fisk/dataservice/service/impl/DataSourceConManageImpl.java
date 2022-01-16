package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.datadriven.sqlUtils.MysqlConUtils;
import com.fisk.common.datadriven.sqlUtils.SqlServerPlusUtils;
import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.dataservice.dto.datasource.*;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.mapper.AppRegisterMapper;
import com.fisk.dataservice.mapper.DataSourceConMapper;
import com.fisk.dataservice.service.IDataSourceConManageService;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.dataservice.vo.datasource.DataSourceVO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据源接口实现类
 *
 * @author dick
 */
@Service
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManageService {

    @Resource
    DataSourceConMapper mapper;

    @Resource
    UserHelper userHelper;

    @Resource
    AppRegisterMapper appRegisterMapper;

    @Override
    public Page<DataSourceConVO> listDataSourceCons(DataSourceConQuery query) {
        //UserInfo userInfo = userHelper.getLoginUserInfo();
        //query.userId = userInfo.id;
        if (query != null && query.keyword != null && query.keyword != "")
            query.keyword = query.keyword.toLowerCase();
        return mapper.listDataSourceConByUserId(query.page, query);
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
        //UserInfo userInfo = userHelper.getLoginUserInfo();
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourceConPO::getName, dto.name);
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
        //UserInfo userInfo = userHelper.getLoginUserInfo();
        DataSourceConPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getName, dto.name)
                //.eq(DataSourceConPO::getCreateUser, userInfo.id)
                .ne(DataSourceConPO::getId, dto.id);
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }

        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @SneakyThrows
    @Override
    public ResultEnum testConnection(TestConnectionDTO dto) {
        Connection conn = null;
        try {
            switch (dto.conType) {
                case MYSQL:
                    Class.forName(DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case SQLSERVER:
                    //1.加载驱动程序
                    Class.forName(DataSourceTypeEnum.SQLSERVER.getDriverName());
                    //2.获得数据库的连接
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                default:
                    return ResultEnum.DS_DATASOURCE_CON_WARN;
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
            return ResultEnum.DS_DATASOURCE_CON_ERROR;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new FkException(ResultEnum.DS_DATASOURCE_CON_ERROR);
            }
        }
    }

    @Override
    public List<DataSourceConVO> getAll() {
        return mapper.getAll();
    }

    @Override
    public DataSourceVO getMeta(int datasourceId) {
        DataSourceVO dataSource = new DataSourceVO();
        DataSourceConPO conPo = mapper.selectById(datasourceId);
        if (conPo == null)
            return dataSource;
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();

        switch (DataSourceTypeEnum.values()[conPo.conType])
        {
            case MYSQL:
                // 表结构
                dataSource.tableDtoList = mysqlConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.MYSQL);
                //视图结构
                //dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DriverTypeEnum.MYSQL, conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                break;
            case SQLSERVER:
                // 表结构
                dataSource.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                // 视图结构
                //dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DriverTypeEnum.SQLSERVER, conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                break;
        }
        dataSource.id = (int) conPo.id;
        dataSource.conType = DataSourceTypeEnum.values()[conPo.conType];
        dataSource.name = conPo.name;
        dataSource.conDbname = conPo.conDbname;

        return dataSource;
    }

}
