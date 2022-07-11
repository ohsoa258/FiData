package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
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
        if (dto.datasourceType == SourceTypeEnum.FiData) {
            queryWrapper.lambda()
                    .eq(DataSourceConPO::getDatasourceId, dto.datasourceId)
                    .eq(DataSourceConPO::getDelFlag, 1)
                    .ne(DataSourceConPO::getId, dto.id);
        } else {
            queryWrapper.lambda()
                    .eq(DataSourceConPO::getName, dto.name)
                    .eq(DataSourceConPO::getDelFlag, 1)
                    .ne(DataSourceConPO::getId, dto.id);
        }
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
                case POSTGRESQL:
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
    public FiDataMetaDataTreeDTO getFiDataConfigMetaData() {
        FiDataMetaDataTreeDTO fiDataMetaDataTreeBase = null;
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDatasourceType, SourceTypeEnum.FiData.getValue())
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            fiDataMetaDataTreeBase = new FiDataMetaDataTreeDTO();
            fiDataMetaDataTreeBase.setId("-10");
            fiDataMetaDataTreeBase.setParentId("-100");
            fiDataMetaDataTreeBase.setLabel("FiData");
            fiDataMetaDataTreeBase.setLabelAlias("FiData");
            fiDataMetaDataTreeBase.setLevelType(LevelTypeEnum.BASEFOLDER);
            fiDataMetaDataTreeBase.children=new ArrayList<>();
            for (DataSourceConPO dataSourceConPO : dataSourceConPOList) {
                List<FiDataMetaDataDTO> fiDataMetaData = redisUtil.getFiDataMetaData(String.valueOf(dataSourceConPO.datasourceId));
                if (CollectionUtils.isNotEmpty(fiDataMetaData)) {
                    fiDataMetaDataTreeBase.children.add(fiDataMetaData.get(0).children.get(0));
                }
            }
        }
        return fiDataMetaDataTreeBase;
    }

    @Override
    public FiDataMetaDataTreeDTO getCustomizeMetaData() {
        FiDataMetaDataTreeDTO fiDataMetaDataTreeBase = null;

        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDatasourceType, SourceTypeEnum.custom.getValue())
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = mapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            fiDataMetaDataTreeBase = new FiDataMetaDataTreeDTO();
            fiDataMetaDataTreeBase.setId("-20");
            fiDataMetaDataTreeBase.setParentId("-200");
            fiDataMetaDataTreeBase.setLabel("Customize");
            fiDataMetaDataTreeBase.setLabelAlias("Customize");
            fiDataMetaDataTreeBase.setLevelType(LevelTypeEnum.BASEFOLDER);

            List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_Ips = new ArrayList<>();
            List<String> conIp = dataSourceConPOList.stream().map(t -> t.getConIp()).distinct().collect(Collectors.toList());
            for (String ip : conIp) {
                String uuid_Ip = UUID.randomUUID().toString().replace("-", "");
                FiDataMetaDataTreeDTO fiDataMetaDataTree_Ip = new FiDataMetaDataTreeDTO();
                fiDataMetaDataTree_Ip.setId(uuid_Ip);
                fiDataMetaDataTree_Ip.setParentId("-20");
                fiDataMetaDataTree_Ip.setLabel(ip);
                fiDataMetaDataTree_Ip.setLabelAlias(ip);
                fiDataMetaDataTree_Ip.setLevelType(LevelTypeEnum.FOLDER);
                List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_Ip_DataBases = new ArrayList<>();
                List<DataSourceConPO> dataSourcs = dataSourceConPOList.stream().filter(t -> t.getConIp().equals(ip)).collect(Collectors.toList());
                for (DataSourceConPO dataSource : dataSourcs) {
                    FiDataMetaDataTreeDTO fiDataMetaDataTree_DataBase = new FiDataMetaDataTreeDTO();
                    fiDataMetaDataTree_DataBase.setId(String.valueOf(dataSource.getId()));
                    fiDataMetaDataTree_DataBase.setParentId(uuid_Ip);
                    fiDataMetaDataTree_DataBase.setLabel(dataSource.name);
                    fiDataMetaDataTree_DataBase.setLabelAlias(dataSource.name);
                    fiDataMetaDataTree_DataBase.setLevelType(LevelTypeEnum.DATABASE);
                    fiDataMetaDataTree_DataBase.setChildren(getCustomizeMetaData_Table(dataSource));
                    fiDataMetaDataTree_Ip_DataBases.add(fiDataMetaDataTree_DataBase);
                }
                fiDataMetaDataTree_Ip.setChildren(fiDataMetaDataTree_Ip_DataBases);
                fiDataMetaDataTree_Ips.add(fiDataMetaDataTree_Ip);
            }
            fiDataMetaDataTreeBase.children=new ArrayList<>();
            fiDataMetaDataTreeBase.children.addAll(fiDataMetaDataTree_Ips);
        }
        return fiDataMetaDataTreeBase;
    }

    public List<FiDataMetaDataTreeDTO> getCustomizeMetaData_Table(DataSourceConPO conPo) {
        List<FiDataMetaDataTreeDTO> fiDataMetaDataTrees = new ArrayList<>();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        try {
            List<TablePyhNameDTO> tableNameAndColumns = null;
            switch (DataSourceTypeEnum.values()[conPo.conType]) {
                case MYSQL:
                    // 表结构
                    tableNameAndColumns = mysqlConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.MYSQL);
                    break;
                case SQLSERVER:
                    // 表结构
                    tableNameAndColumns = sqlServerPlusUtils.getTableNameAndColumnsPlus(conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                    break;
                case POSTGRE:
                    // 表结构
                    tableNameAndColumns = postgresConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.POSTGRESQL);
                    break;
            }
            if (CollectionUtils.isNotEmpty(tableNameAndColumns)) {
                for (TablePyhNameDTO table : tableNameAndColumns) {

                    String uuid_TableId = UUID.randomUUID().toString().replace("-", "");
                    FiDataMetaDataTreeDTO fiDataMetaDataTree_Table = new FiDataMetaDataTreeDTO();
                    fiDataMetaDataTree_Table.setId(uuid_TableId);
                    fiDataMetaDataTree_Table.setParentId(String.valueOf(conPo.id));
                    fiDataMetaDataTree_Table.setLabel(table.tableName);
                    fiDataMetaDataTree_Table.setLabelAlias(table.tableName);
                    fiDataMetaDataTree_Table.setSourceId(Math.toIntExact(conPo.id));
                    fiDataMetaDataTree_Table.setSourceType(SourceTypeEnum.custom.getValue());
                    fiDataMetaDataTree_Table.setLevelType(LevelTypeEnum.TABLE);
                    List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_Table_Children = new ArrayList<>();

                    if (CollectionUtils.isNotEmpty(table.fields)) {
                        for (TableStructureDTO field : table.fields) {
                            String uuid_FieldId = UUID.randomUUID().toString().replace("-", "");
                            FiDataMetaDataTreeDTO fiDataMetaDataTree_Field = new FiDataMetaDataTreeDTO();
                            fiDataMetaDataTree_Field.setId(uuid_FieldId);
                            fiDataMetaDataTree_Field.setParentId(uuid_TableId);
                            fiDataMetaDataTree_Field.setLabel(field.fieldName);
                            fiDataMetaDataTree_Field.setLabelAlias(field.fieldName);
                            fiDataMetaDataTree_Field.setSourceId(Math.toIntExact(conPo.id));
                            fiDataMetaDataTree_Field.setSourceType(SourceTypeEnum.custom.getValue());
                            fiDataMetaDataTree_Field.setLevelType(LevelTypeEnum.FIELD);
                            fiDataMetaDataTree_Field.setLabelType(field.fieldType);
                            fiDataMetaDataTree_Field.setLabelLength(String.valueOf(field.fieldLength));
                            fiDataMetaDataTree_Field.setLabelDesc(field.fieldDes);
                            fiDataMetaDataTree_Table_Children.add(fiDataMetaDataTree_Field);
                        }
                    }
                    fiDataMetaDataTree_Table.setChildren(fiDataMetaDataTree_Table_Children);
                    fiDataMetaDataTrees.add(fiDataMetaDataTree_Table);
                }
            }
        } catch (Exception ex) {
            return fiDataMetaDataTrees;
        }
        return fiDataMetaDataTrees;
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
        DataSourceConPO dataSourceConPO = new DataSourceConPO();
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getConIp().equals(conIp) && t.getConDbname().equals(conDbname)).findFirst().orElse(null);
            if (dataSourceConVO != null) {
                dataSourceConPO.setId(dataSourceConVO.getId());
                dataSourceConPO.setName(dataSourceConVO.getName());
                dataSourceConPO.setConIp(dataSourceConVO.getConIp());
                dataSourceConPO.setConPort(dataSourceConVO.getConPort());
                dataSourceConPO.setDatasourceId(dataSourceConVO.getDatasourceId());
                dataSourceConPO.setDatasourceType(dataSourceConVO.getDatasourceType().getValue());
                dataSourceConPO.setConDbname(dataSourceConVO.getConDbname());
                dataSourceConPO.setConType(dataSourceConVO.getConType().getValue());
                dataSourceConPO.setConAccount(dataSourceConVO.getConAccount());
                dataSourceConPO.setConPassword(dataSourceConVO.getConPassword());
            }
        }
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
