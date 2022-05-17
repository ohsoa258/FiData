package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import com.fisk.datagovernance.map.dataquality.DataSourceConMap;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.service.dataquality.IDataSourceConManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataBaseSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceVO;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据源接口实现类
 *
 * @author dick
 */
@Service
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManageService {

    @Resource
    DataSourceConMapper mapper;

    @Value("${pgsql-dw.ip}")
    private String pgsqlDwIp;
    @Value("${pgsql-dw.port}")
    private int pgsqlDwPort;
    @Value("${pgsql-dw.dbName}")
    private String pgsqlDwDbName;
    @Value("${pgsql-dw.driverClassName}")
    private String pgsqlDwDriverClassName;
    @Value("${pgsql-dw.url}")
    private String pgsqlDwUrl;
    @Value("${pgsql-dw.username}")
    private String pgsqlDwUsername;
    @Value("${pgsql-dw.password}")
    private String pgsqlDwPassword;

    @Value("${pgsql-ods.ip}")
    private String pgsqlOdsIp;
    @Value("${pgsql-ods.port}")
    private int pgsqlOdsPort;
    @Value("${pgsql-ods.dbName}")
    private String pgsqlOdsDbName;
    @Value("${pgsql-ods.driverClassName}")
    private String pgsqlOdsDriverClassName;
    @Value("${pgsql-ods.url}")
    private String pgsqlOdsUrl;
    @Value("${pgsql-ods.username}")
    private String pgsqlOdsUsername;
    @Value("${pgsql-ods.password}")
    private String pgsqlOdsPassword;

    @Value("${pgsql-mdm.ip}")
    private String pgsqlMdmIp;
    @Value("${pgsql-mdm.port}")
    private int pgsqlMdmPort;
    @Value("${pgsql-mdm.dbName}")
    private String pgsqlMdmDbName;
    @Value("${pgsql-mdm.driverClassName}")
    private String pgsqlMdmDriverClassName;
    @Value("${pgsql-mdm.url}")
    private String pgsqlMdmUrl;
    @Value("${pgsql-mdm.username}")
    private String pgsqlMdmUsername;
    @Value("${pgsql-mdm.password}")
    private String pgsqlMdmPassword;

    @Override
    public Page<DataSourceConVO> listDataSourceCons(DataSourceConQuery query) {
        if (query != null && query.keyword != null && query.keyword != "")
            query.keyword = query.keyword.toLowerCase();
        return mapper.listDataSourceCon(query.page, query);
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
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
        DataSourceConPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getName, dto.name)
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
                case POSTGRE:
                    //1.加载驱动程序
                    Class.forName(DataSourceTypeEnum.POSTGRE.getDriverName());
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
    public List<DataSourceConVO> getSystemAll() {
        List<DataSourceConVO> dataSourceConVOS = new ArrayList<>();

        DataSourceConVO dw = new DataSourceConVO();
        dw.setConIp(pgsqlDwIp);
        dw.setName(pgsqlDwDbName);
        dw.setConDbname(pgsqlDwDbName);
        dw.setConPort(pgsqlDwPort);
        dw.setConType(DataSourceTypeEnum.getEnumByDriverName(pgsqlDwDriverClassName));
        dw.setConAccount(pgsqlDwUsername);
        dw.setConPassword(pgsqlDwPassword);
        dw.setConStr(pgsqlDwUrl);
        dw.setDatasourceType(SourceTypeEnum.FiData);
        dataSourceConVOS.add(dw);

        DataSourceConVO ods = new DataSourceConVO();
        ods.setConIp(pgsqlOdsIp);
        ods.setName(pgsqlOdsDbName);
        ods.setConDbname(pgsqlOdsDbName);
        ods.setConPort(pgsqlOdsPort);
        ods.setConType(DataSourceTypeEnum.getEnumByDriverName(pgsqlOdsDriverClassName));
        ods.setConAccount(pgsqlOdsUsername);
        ods.setConPassword(pgsqlOdsPassword);
        ods.setConStr(pgsqlOdsUrl);
        ods.setDatasourceType(SourceTypeEnum.FiData);
        dataSourceConVOS.add(ods);

        DataSourceConVO mdm = new DataSourceConVO();
        mdm.setConIp(pgsqlMdmIp);
        mdm.setName(pgsqlMdmDbName);
        mdm.setConDbname(pgsqlMdmDbName);
        mdm.setConPort(pgsqlMdmPort);
        mdm.setConType(DataSourceTypeEnum.getEnumByDriverName(pgsqlMdmDriverClassName));
        mdm.setConAccount(pgsqlMdmUsername);
        mdm.setConPassword(pgsqlMdmPassword);
        mdm.setConStr(pgsqlMdmUrl);
        mdm.setDatasourceType(SourceTypeEnum.FiData);
        dataSourceConVOS.add(mdm);

        return dataSourceConVOS;
    }

    @Override
    public List<DataExampleSourceVO> getTableAll() {
        List<DataExampleSourceVO> dataSourceList = new ArrayList<>();
        List<DataSourceVO> dataSources = new ArrayList<>();
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOS = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dataSourceConPOS)) {
            return dataSourceList;
        }
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        Connection connection = null;
        for (DataSourceConPO conPo : dataSourceConPOS) {
            try {
                DataSourceVO dataSource = new DataSourceVO();
                List<TablePyhNameDTO> tablePyhNameDTOS = new ArrayList<>();
                Map<String, String> tableNames = new IdentityHashMap<>();
                List<String> tables = null;
                switch (DataSourceTypeEnum.values()[conPo.conType]) {
                    case MYSQL:
                        // 表结构
                        connection = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                        tables = mysqlConUtils.getTables(connection);
                        if (CollectionUtils.isNotEmpty(tables)) {
                            for (String e : tables) {
                                tableNames.put(e, "");
                            }
                        }
                        break;
                    case SQLSERVER:
                        // 表结构
                        connection = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                        tableNames = sqlServerPlusUtils.getTablesPlus(connection, conPo.getConDbname());
                        break;
                    case POSTGRE:
                        // 表结构
                        tables = postgresConUtils.getTableList(conPo.conStr, conPo.conAccount, conPo.conPassword, DataSourceTypeEnum.POSTGRE.getDriverName());
                        if (CollectionUtils.isNotEmpty(tables)) {
                            for (String e : tables) {
                                tableNames.put(e, "");
                            }
                        }
                        break;
                }
                if (CollectionUtils.isNotEmpty(tableNames)) {
                    for (Map.Entry t : tableNames.entrySet()) {
                        TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                        tablePyhNameDTO.setTableName(t.getKey().toString());
                        tablePyhNameDTO.setTableFramework(t.getValue().toString());
                        tablePyhNameDTOS.add(tablePyhNameDTO);
                    }
                    if (CollectionUtils.isNotEmpty(tablePyhNameDTOS)) {
                        dataSource.tableDtoList = tablePyhNameDTOS;
                        dataSource.id = (int) conPo.id;
                        dataSource.conType = DataSourceTypeEnum.values()[conPo.conType];
                        dataSource.name = conPo.name;
                        dataSource.conDbname = conPo.conDbname;
                        dataSource.conIp = conPo.conIp;
                        dataSource.conPort = conPo.conPort;
                        dataSource.datasourceType = SourceTypeEnum.getEnum(conPo.datasourceType);
                        dataSources.add(dataSource);
                    }
                }
            } catch (Exception ex) {
                continue;
            }
        }
        dataSourceList = getDataSourceList(dataSources);
        return dataSourceList;
    }

    @Override
    public DataSourceVO getTableFieldAll(TableFieldQueryDTO dto) {
        DataSourceVO dataSourceVO = new DataSourceVO();
        if (dto == null) {
            return dataSourceVO;
        }
        DataSourceConPO conPo = mapper.selectById(dto.datasourceId);
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
                colNames = mysqlConUtils.getColNames(st, dto.tableName);
                break;
            case SQLSERVER:
                // 表结构
                SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                connection = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                colNames = sqlServerPlusUtils.getColumnsName(connection, dto.tableName, dto.tableFramework);
                break;
            case POSTGRE:
                // 表结构
                List<String> tables = new ArrayList<>();
                tables.add(dto.tableName);
                PostgresConUtils postgresConUtils = new PostgresConUtils();
                Map<String, List<TableStructureDTO>> tableColumnList = postgresConUtils.getTableColumnList(conPo.conStr, conPo.conAccount,
                        conPo.conPassword, DataSourceTypeEnum.POSTGRE.getDriverName(), tables);
                if (CollectionUtils.isNotEmpty(tableColumnList)) {
                    colNames = tableColumnList.get(dto.tableName);
                }
                break;
        }
        List<TablePyhNameDTO> tablePyhNameDTOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(colNames)) {
            int rowsCount = 0;
            // 表行数波动阈值模板才计算行数
            if (dto.templateTypeEnum == TemplateTypeEnum.TABLECOUNT_TEMPLATE) {
                try {
                    String tName = dto.tableFramework != null && dto.tableFramework != ""
                            ? dto.tableFramework + "." + dto.tableName : dto.tableName;
                    String sqlCountStr = String.format("select count(*) from %s", tName);
                    PreparedStatement preparedStatement = null;
                    preparedStatement = connection.prepareStatement(sqlCountStr);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    rowsCount = resultSet.getInt(1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
            tablePyhNameDTO.setTableName(dto.tableName);
            tablePyhNameDTO.setRowsCount(rowsCount);
            tablePyhNameDTO.setTableFramework(dto.tableFramework);
            tablePyhNameDTO.setFields(colNames);
            tablePyhNameDTOS.add(tablePyhNameDTO);
            dataSourceVO.tableDtoList = tablePyhNameDTOS;
            dataSourceVO.id = (int) conPo.id;
            dataSourceVO.conType = DataSourceTypeEnum.values()[conPo.conType];
            dataSourceVO.name = conPo.name;
            dataSourceVO.conDbname = conPo.conDbname;
            dataSourceVO.conIp = conPo.conIp;
            dataSourceVO.conPort = conPo.conPort;
            dataSourceVO.datasourceType=SourceTypeEnum.getEnum(conPo.datasourceType);
        }
        return dataSourceVO;
    }

    public List<DataExampleSourceVO> getDataSourceList(List<DataSourceVO> dataSources) {
        List<DataExampleSourceVO> dataExampleSourceVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dataSources)) {
            // 实例信息
            List<String> conIps = dataSources.stream().map(DataSourceVO::getConIp).distinct().collect(Collectors.toList());
            for (String conIp : conIps) {
                DataExampleSourceVO dataExampleSourceVO = null;
                List<DataBaseSourceVO> dataBaseSourceVOS = new ArrayList<>();
                for (DataSourceVO dataSourceVO : dataSources) {
                    if (dataSourceVO.getConIp().equals(conIp)) {
                        if (dataExampleSourceVO == null) {
                            dataExampleSourceVO = new DataExampleSourceVO();
                            dataExampleSourceVO.setConIp(dataSourceVO.getConIp());
                            dataExampleSourceVO.setConPort(dataSourceVO.getConPort());
                            dataExampleSourceVO.setConType(dataSourceVO.getConType());
                            dataExampleSourceVO.setName(dataSourceVO.getName());
                        }
                        DataBaseSourceVO dataBaseSourceVO = new DataBaseSourceVO();
                        dataBaseSourceVO.setId(dataSourceVO.getId());
                        dataBaseSourceVO.setConDbname(dataSourceVO.getConDbname());
                        dataBaseSourceVO.setChildren(dataSourceVO.getTableDtoList());
                        dataBaseSourceVOS.add(dataBaseSourceVO);
                    }
                }
                dataExampleSourceVO.setChildren(dataBaseSourceVOS);
                dataExampleSourceVOS.add(dataExampleSourceVO);
            }
        }
        return dataExampleSourceVOS;
    }

    /**
     * @return com.fisk.datagovernance.entity.dataquality.DataSourceConPO
     * @description 根据数据源配置信息查询数据源
     * @author dick
     * @date 2022/4/15 11:59
     * @version v1.0
     * @params conIp
     * @params conPort
     * @params conDbname
     */
    public DataSourceConPO getDataSourceInfo(String conIp, String conDbname) {
        DataSourceConPO dataSourceConPO = null;
        QueryWrapper<DataSourceConPO> dataSourceConPOQueryWrapper = new QueryWrapper<>();
        dataSourceConPOQueryWrapper.lambda().eq(DataSourceConPO::getConIp, conIp)
                .eq(DataSourceConPO::getConIp, conIp)
                .eq(DataSourceConPO::getConDbname, conDbname)
                .eq(DataSourceConPO::getDelFlag, 1);
        dataSourceConPO = baseMapper.selectOne(dataSourceConPOQueryWrapper);
        return dataSourceConPO;
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
    public static Connection getStatement(String driver, String url, String username, String password) {
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR);
        }
        return conn;
    }
}
