package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConEditDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConQuery;
import com.fisk.datagovernance.dto.dataquality.datasource.TestConnectionDTO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.ModuleDataSourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.DataSourceConMap;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.service.dataquality.IDataSourceConManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceVO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Page<DataSourceConVO> listDataSourceCons(DataSourceConQuery query) {
        //UserInfo userInfo = userHelper.getLoginUserInfo();
        //query.userId = userInfo.id;
        if (query != null && query.keyword != null && query.keyword != "")
            query.keyword = query.keyword.toLowerCase();
        return mapper.listDataSourceCon(query.page, query);
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

    //    @Override
//    public List<DataSourceConVO> getAll() {
//        return mapper.getAll();
//    }
    @Override
    public List<DataSourceVO> getMeta(String tableName) {
        List<DataSourceVO> dataSources = new ArrayList<>();
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOS = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dataSourceConPOS)) {
            return dataSources;
        }
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        Connection connection = null;
        for (DataSourceConPO conPo : dataSourceConPOS) {
            try {
                DataSourceVO dataSource = new DataSourceVO();
                List<TablePyhNameDTO> tablePyhNameDTOS = new ArrayList<>();
                Map<String, String> tableNames = new IdentityHashMap<>();

                switch (DataSourceTypeEnum.values()[conPo.conType]) {
                    case MYSQL:
                        // 表结构
                        connection = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                        List<String> tables = mysqlConUtils.getTables(connection);
                        if (CollectionUtils.isNotEmpty(tables)) {
                            for (String e : tables) {
                                tableNames.put(e, "");
                            }
                        }
                        break;
                    case SQLSERVER:
                        // 表结构
                        connection = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                        tableNames = sqlServerPlusUtils.getTablesPlus(connection);
                        break;
                }
                if (CollectionUtils.isNotEmpty(tableNames)) {
                    for (Map.Entry t : tableNames.entrySet()) {
                        TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                        if (tableName != null && tableName != "") {
                            if (t.getKey().toString().contains(tableName)) {
                                tablePyhNameDTO.setTableName(t.getKey().toString());
                                tablePyhNameDTO.setTableFramework(t.getValue().toString());
                            }
                        } else {
                            tablePyhNameDTO.setTableName(t.getKey().toString());
                            tablePyhNameDTO.setTableFramework(t.getValue().toString());
                        }
                        if (tablePyhNameDTO.tableName != null && tablePyhNameDTO.tableName != "") {
                            tablePyhNameDTOS.add(tablePyhNameDTO);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(tablePyhNameDTOS)) {
                        dataSource.tableDtoList = tablePyhNameDTOS;
                        dataSource.id = (int) conPo.id;
                        dataSource.conType = DataSourceTypeEnum.values()[conPo.conType];
                        dataSource.name = conPo.name;
                        dataSource.conDbname = conPo.conDbname;
                        dataSource.conIp = conPo.conIp;
                        dataSource.conPort = conPo.conPort;
                        dataSources.add(dataSource);
                    }
                }
            } catch (Exception ex) {
                continue;
            }
        }
        return dataSources;
    }

    @Override
    public DataSourceVO getTableFieldAll(int datasourceId, ModuleDataSourceTypeEnum datasourceTyoe,
                                         String tableName, String tableFramework) {
        DataSourceVO dataSourceVO = new DataSourceVO();
        if (datasourceId == 0 || tableName == null || tableName == "") {
            return dataSourceVO;
        }
        DataSourceConPO conPo = null;
        switch (datasourceTyoe) {
            case METADATA:
                // 调用元数据接口，根据datasourceId查询数据源信息
                conPo = null;
                break;
            case DATAQUALITY:
                conPo = mapper.selectById(datasourceId);
                break;
        }
        List<TablePyhNameDTO> tablePyhNameDTOS = new ArrayList<>();
        Connection connection = null;
        List<TableStructureDTO> colNames = new ArrayList<>();
        switch (DataSourceTypeEnum.values()[conPo.conType]) {
            case MYSQL:
                // 表结构
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                connection = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                Statement st = null;
                try {
                    st = connection.createStatement();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                colNames = mysqlConUtils.getColNames(st, tableName);
                break;
            case SQLSERVER:
                // 表结构
                String f = tableFramework == null || tableFramework == "" ? "%" : tableFramework;
                SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                connection = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                colNames = sqlServerPlusUtils.getColumnsName(connection, tableName, f);
                break;
        }
        if (CollectionUtils.isNotEmpty(colNames)) {
            int rowsCount = 0;
            try {
                String tName = tableFramework != null && tableFramework != "" ? tableFramework + "." + tableName : tableName;
                String sqlCountStr = String.format("select count(*) from %s", tName);
                PreparedStatement preparedStatement = null;
                preparedStatement = connection.prepareStatement(sqlCountStr);
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                rowsCount = resultSet.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
            tablePyhNameDTO.setTableName(tableName);
            tablePyhNameDTO.setRowsCount(rowsCount);
            tablePyhNameDTO.setTableFramework(tableFramework);
            tablePyhNameDTO.setFields(colNames);
            tablePyhNameDTOS.add(tablePyhNameDTO);
            dataSourceVO.tableDtoList = tablePyhNameDTOS;
            dataSourceVO.id = (int) conPo.id;
            dataSourceVO.conType = DataSourceTypeEnum.values()[conPo.conType];
            dataSourceVO.name = conPo.name;
            dataSourceVO.conDbname = conPo.conDbname;
            dataSourceVO.conIp = conPo.conIp;
            dataSourceVO.conPort = conPo.conPort;
        }
        return dataSourceVO;
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
            throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR);
        }
        return conn;
    }
}
