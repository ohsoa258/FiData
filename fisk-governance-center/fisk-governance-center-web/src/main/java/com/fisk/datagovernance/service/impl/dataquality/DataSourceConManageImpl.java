package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
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
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    private UserClient userClient;

    @Resource
    RedisUtil redisUtil;

    @Override
    public PageDTO<DataSourceConVO> page(DataSourceConQuery query) {
        PageDTO<DataSourceConVO> pageDTO = new PageDTO<>();
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (CollectionUtils.isNotEmpty(allDataSource) &&
                query != null && StringUtils.isNotEmpty(query.keyword)) {
            allDataSource = allDataSource.stream().filter(
                    t -> (t.getConDbname().contains(query.keyword)) ||
                            t.getConType().getName().contains(query.keyword)).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            pageDTO.setTotal(Long.valueOf(allDataSource.size()));
            query.current = query.current - 1;
            allDataSource = allDataSource.stream().sorted(Comparator.comparing(DataSourceConVO::getCreateTime).reversed()).skip((query.current - 1 + 1) * query.size).limit(query.size).collect(Collectors.toList());
        }
        pageDTO.setItems(allDataSource);
        return pageDTO;
    }

    @Override
    public ResultEnum add(DataSourceConDTO dto) {
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        if (dto.datasourceType == SourceTypeEnum.FiData) {
            queryWrapper.lambda().eq(DataSourceConPO::getDatasourceId, dto.datasourceId)
                    .eq(DataSourceConPO::getDelFlag, 1);
        } else {
            queryWrapper.lambda().eq(DataSourceConPO::getName, dto.name)
                    .eq(DataSourceConPO::getDelFlag, 1);
        }
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum edit(DataSourceConEditDTO dto) {
        DataSourceConPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getName, dto.name)
                .eq(DataSourceConPO::getDelFlag, 1)
                .ne(DataSourceConPO::getId, dto.id);
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delete(int id) {
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
                return ResultEnum.DS_DATASOURCE_CON_ERROR;
            }
        }
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
                List<String> tables = null;
                switch (DataSourceTypeEnum.values()[conPo.conType]) {
                    case MYSQL:
                        // 表结构
                        connection = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                        tables = mysqlConUtils.getTables(connection);
                        if (CollectionUtils.isNotEmpty(tables)) {
                            for (String e : tables) {
                                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                                tablePyhNameDTO.tableName = e;
                                tablePyhNameDTOS.add(tablePyhNameDTO);
                            }
                        }
                        break;
                    case SQLSERVER:
                        // 表结构
                        connection = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
                        Map<String, String> tablesPlus = sqlServerPlusUtils.getTablesPlus(connection, conPo.getConDbname());
                        for (Map.Entry<String, String> entry : tablesPlus.entrySet()) {
                            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                            tablePyhNameDTO.tableName = entry.getKey();
                            tablePyhNameDTO.tableFramework = entry.getValue();
                            tablePyhNameDTOS.add(tablePyhNameDTO);
                        }
                        break;
                    case POSTGRE:
                        // 表结构
                        tables = postgresConUtils.getTableList(conPo.conStr, conPo.conAccount, conPo.conPassword, DataSourceTypeEnum.POSTGRE.getDriverName());
                        if (CollectionUtils.isNotEmpty(tables)) {
                            for (String e : tables) {
                                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                                tablePyhNameDTO.tableName = e;
                                tablePyhNameDTOS.add(tablePyhNameDTO);
                            }
                        }
                        break;
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
            dataSourceVO.datasourceType = SourceTypeEnum.getEnum(conPo.datasourceType);
        }
        return dataSourceVO;
    }

    @Override
    public List<FiDataMetaDataDTO> getFiDataConfigMetaData() {
        List<FiDataMetaDataDTO> dataMetaDataDTOS = new ArrayList<>();
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDatasourceType, SourceTypeEnum.FiData.getValue())
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = mapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            for (DataSourceConPO dataSourceConPO : dataSourceConPOList) {
                List<FiDataMetaDataDTO> fiDataMetaData = redisUtil.getFiDataMetaData(String.valueOf(dataSourceConPO.datasourceId));
                if (CollectionUtils.isNotEmpty(fiDataMetaData)) {
                    dataMetaDataDTOS.addAll(fiDataMetaData);
                }
            }
        }
        return dataMetaDataDTOS;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO>
     * @description 自定义数据源信息组装
     * @author dick
     * @date 2022/6/27 17:32
     * @version v1.0
     * @params dataSources
     */
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
     * 根据数据源配置信息查询数据源
     *
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
     * 查询数据质量所有数据源信息，含FiData系统数据源
     *
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO>
     * @author dick
     * @date 2022/6/16 23:17
     * @version v1.0
     * @params
     */
    public List<DataSourceConVO> getAllDataSource() {
        List<DataSourceConVO> dataSourceList = new ArrayList<>();
        // FiData数据源信息
        ResultEntity<List<DataSourceDTO>> fiDataDataSourceResult = userClient.getAllFiDataDataSource();
        final List<DataSourceDTO> fiDataDataSources = fiDataDataSourceResult != null && fiDataDataSourceResult.getCode() == 0
                ? userClient.getAllFiDataDataSource().getData() : null;
        // 数据质量数据源信息
        QueryWrapper<DataSourceConPO> dataSourceConPOQueryWrapper = new QueryWrapper<>();
        dataSourceConPOQueryWrapper.lambda()
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = baseMapper.selectList(dataSourceConPOQueryWrapper);
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            dataSourceConPOList.forEach(t -> {
                DataSourceConVO dataSourceConVO = new DataSourceConVO();
                dataSourceConVO.setId(Math.toIntExact(t.getId()));
                dataSourceConVO.setDatasourceType(SourceTypeEnum.getEnum(t.getDatasourceType()));
                dataSourceConVO.setCreateTime(t.getCreateTime().format(pattern));
                if (t.getDatasourceType() == 2) {
                    dataSourceConVO.setName(t.getName());
                    dataSourceConVO.setConStr(t.getConStr());
                    dataSourceConVO.setConIp(t.getConIp());
                    dataSourceConVO.setConPort(t.getConPort());
                    dataSourceConVO.setConDbname(t.getConDbname());
                    dataSourceConVO.setConType(DataSourceTypeEnum.getEnum(t.getConType()));
                    dataSourceConVO.setConAccount(t.getConAccount());
                    dataSourceConVO.setConPassword(t.getConPassword());
                    dataSourceList.add(dataSourceConVO);
                } else if (t.getDatasourceType() == 1 && CollectionUtils.isNotEmpty(fiDataDataSources)) {
                    Optional<DataSourceDTO> first = fiDataDataSources.stream().filter(item -> item.getId() == t.getDatasourceId()).findFirst();
                    if (first.isPresent()) {
                        DataSourceDTO dataSourceDTO = first.get();
                        dataSourceConVO.setName(dataSourceDTO.getName());
                        dataSourceConVO.setConStr(dataSourceDTO.getConStr());
                        dataSourceConVO.setConIp(dataSourceDTO.getConIp());
                        dataSourceConVO.setConPort(dataSourceDTO.getConPort());
                        dataSourceConVO.setConDbname(dataSourceDTO.getConDbname());
                        dataSourceConVO.setConType(DataSourceTypeEnum.getEnum(dataSourceDTO.getConType().getValue()));
                        dataSourceConVO.setConAccount(dataSourceDTO.getConAccount());
                        dataSourceConVO.setConPassword(dataSourceDTO.getConPassword());
                        dataSourceList.add(dataSourceConVO);
                    }
                }
            });
        }
        return dataSourceList;
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
