package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.dataservice.dto.datasource.DataSourceConDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConEditDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConQuery;
import com.fisk.dataservice.dto.datasource.TestConnectionDTO;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.enums.SourceTypeEnum;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.mapper.DataSourceConMapper;
import com.fisk.dataservice.service.IDataSourceConManageService;
import com.fisk.dataservice.vo.api.FieldInfoVO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.dataservice.vo.datasource.DataSourceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
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
    RedisUtil redisUtil;

    @Resource
    private UserClient userClient;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("${dataservice.datasource.metadataentity_key}")
    private String metaDataEntityKey;

    @Override
    public PageDTO<DataSourceConVO> listDataSourceCons(DataSourceConQuery query) {
        PageDTO<DataSourceConVO> pageDTO = new PageDTO<>();
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            if (query != null && StringUtils.isNotEmpty(query.keyword)) {
                allDataSource = allDataSource.stream().filter(
                        t -> (t.getConDbname().contains(query.keyword)) ||
                                t.getConType().getName().contains(query.keyword)).collect(Collectors.toList());
            }
            allDataSource.forEach(t -> {
                t.setConPassword("");
            });
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
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
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
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
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
                case POSTGRESQL:
                    //1.加载驱动程序
                    Class.forName(DataSourceTypeEnum.POSTGRESQL.getDriverName());
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
        return getAllDataSource();
    }

    @Override
    public ResultEntity<DataSourceVO> getTableAll(int datasourceId) {
        DataSourceVO dataSource = null;
        try {
            DataSourceConPO conPo = mapper.selectById(datasourceId);
            if (conPo == null) {
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_DATASOURCE_EXISTS, dataSource);
            }
            String redisKey = metaDataEntityKey + "_" + datasourceId;
            Boolean exist = redisTemplate.hasKey(redisKey);
            if (!exist) {
                setDataSourceToRedis(datasourceId, 1);
            }
            String json = redisTemplate.opsForValue().get(redisKey).toString();
            if (StringUtils.isNotEmpty(json)) {
                dataSource = JSONObject.parseObject(json, DataSourceVO.class);
            }
        } catch (Exception ex) {
            log.error("getTableAll执行异常：", ex);
            throw new FkException(ResultEnum.DS_DATASOURCE_READ_ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataSource);
    }

    @Override
    public ResultEntity<Object> reloadDataSource(int datasourceId) {
        setDataSourceToRedis(datasourceId, 2);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "已重新加载数据源");
    }

    public DataSourceVO getMeta(int datasourceId) {
        DataSourceVO dataSource = new DataSourceVO();
        DataSourceConPO conPo = mapper.selectById(datasourceId);
        if (conPo == null)
            return dataSource;
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        List<DataBaseViewDTO> dataBaseViewDTOS = null;
        switch (DataSourceTypeEnum.values()[conPo.conType]) {
            case MYSQL:
                // 表结构
                dataSource.tableDtoList = mysqlConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.MYSQL);
                //视图结构
                //dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DriverTypeEnum.MYSQL, conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                dataBaseViewDTOS = mysqlConUtils.loadViewDetails(DriverTypeEnum.MYSQL, conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                break;
            case SQLSERVER:
                // 表结构
                dataSource.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                // 视图结构
                //dataSource.viewDtoList = sqlServerPlusUtils.loadViewDetails(DriverTypeEnum.SQLSERVER, conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                dataBaseViewDTOS = sqlServerPlusUtils.loadViewDetails(DriverTypeEnum.SQLSERVER, conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                break;
            case POSTGRESQL:
                dataSource.tableDtoList = postgresConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.POSTGRESQL);
                break;
        }
        Connection conn = null;
        if (conPo.getConType() == com.fisk.dataservice.enums.DataSourceTypeEnum.MYSQL.getValue()) {
            conn = getStatement(com.fisk.dataservice.enums.DataSourceTypeEnum.MYSQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
        } else if (conPo.getConType() == com.fisk.dataservice.enums.DataSourceTypeEnum.SQLSERVER.getValue()) {
            conn = getStatement(com.fisk.dataservice.enums.DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
        } else if (conPo.getConType() == com.fisk.dataservice.enums.DataSourceTypeEnum.POSTGRESQL.getValue()) {
            conn = getStatement(com.fisk.dataservice.enums.DataSourceTypeEnum.POSTGRESQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
        }
        List<FieldInfoVO> tableFieldList = getTableFieldList(conn, conPo);
        if (CollectionUtils.isNotEmpty(tableFieldList)
                && CollectionUtils.isNotEmpty(dataSource.tableDtoList))
            dataSource.tableDtoList.forEach(e -> {
                List<FieldInfoVO> collect = tableFieldList.stream().filter(item -> item.originalTableName.equals(e.tableName)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    e.fields.forEach(fieldItem -> {
                        Optional<FieldInfoVO> first = collect.stream().filter(item -> item.originalFieldName.equals(fieldItem.fieldName)).findFirst();
                        if (first.isPresent()) {
                            FieldInfoVO fieldInfoVO = first.get();
                            if (fieldInfoVO != null)
                                fieldItem.fieldDes = fieldInfoVO.originalFieldDesc;
                        }
                    });
                }
            });
        // 演示需要暂时将试图对象也写入到表对象中
        if (CollectionUtils.isNotEmpty(dataBaseViewDTOS)) {
            List<TablePyhNameDTO> tableDtoList = new ArrayList<>();
            dataBaseViewDTOS.forEach(t -> {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(t.getViewName());
                if (CollectionUtils.isNotEmpty(t.getFields())) {
                    List<TableStructureDTO> fields = new ArrayList<>();
                    t.fields.forEach(s -> {
                        TableStructureDTO field = new TableStructureDTO();
                        field.setFieldName(s.getFieldName());
                        field.setFieldType(s.getFieldType());
                        field.setFieldLength(s.getFieldLength());
                        field.setFieldDes(s.getFieldDes());
                        fields.add(field);
                    });
                    tablePyhNameDTO.setFields(fields);
                }
                tableDtoList.add(tablePyhNameDTO);
            });
            if (CollectionUtils.isNotEmpty(dataSource.tableDtoList)) {
                dataSource.tableDtoList.addAll(0, tableDtoList);
            } else {
                dataSource.tableDtoList = tableDtoList;
            }
        }
        dataSource.id = (int) conPo.id;
        dataSource.conType = DataSourceTypeEnum.values()[conPo.conType];
        dataSource.name = conPo.name;
        dataSource.conDbname = conPo.conDbname;

        return dataSource;
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

    /**
     * 查询表字段信息
     *
     * @param conn       连接
     * @param dataSource 数据源信息
     * @return statement
     */
    private static List<FieldInfoVO> getTableFieldList(Connection conn, DataSourceConPO dataSource) {
        List<FieldInfoVO> fieldlist = new ArrayList<>();
        String sql = "";
        com.fisk.dataservice.enums.DataSourceTypeEnum value = com.fisk.dataservice.enums.DataSourceTypeEnum.values()[dataSource.getConType()];
        switch (value) {
            case MYSQL:
                sql = String.format("SELECT\n" +
                        "\tTABLE_NAME AS originalTableName,\n" +
                        "\tCOLUMN_NAME AS originalFieldName,\n" +
                        "\tCOLUMN_COMMENT AS originalFieldDesc,\n" +
                        "\t'' AS originalFramework \n" +
                        "FROM\n" +
                        "\tinformation_schema.`COLUMNS` \n" +
                        "WHERE\n" +
                        "\tTABLE_SCHEMA = '%s'", dataSource.conDbname);
                break;
            case SQLSERVER:
                sql = "SELECT\n" +
                        "\td.name AS originalTableName,\n" +
                        "\ta.name AS originalFieldName,\n" +
                        "\tisnull( g.[value], '' ) AS originalFieldDesc,\n" +
                        "\tschema_name(tb.schema_id) AS originalFramework\n" +
                        "FROM\n" +
                        "\tsyscolumns a\n" +
                        "\tLEFT JOIN systypes b ON a.xusertype= b.xusertype\n" +
                        "\tINNER JOIN sysobjects d ON a.id= d.id \n" +
                        "\tAND d.xtype= 'U' \n" +
                        "\tAND d.name<> 'dtproperties'\n" +
                        "\tLEFT JOIN  sys.tables tb ON tb.name=d.name\n" +
                        "\tLEFT JOIN syscomments e ON a.cdefault= e.id\n" +
                        "\tLEFT JOIN sys.extended_properties g ON a.id= g.major_id \n" +
                        "\tAND a.colid= g.minor_id\n" +
                        "\tLEFT JOIN sys.extended_properties f ON d.id= f.major_id \n" +
                        "\tAND f.minor_id= 0";
                break;
            case POSTGRESQL:
                sql = "SELECT c.relname as originalTableName,a.attname as originalFieldName,col_description(a.attrelid,a.attnum) as originalFieldDesc,'' AS originalFramework \n" +
                        "FROM pg_class as c,pg_attribute as a inner join pg_type on pg_type.oid = a.atttypid\n" +
                        "where c.relname in  (SELECT tablename FROM pg_tables ) and a.attrelid = c.oid and a.attnum>0";
                break;
        }
        if (sql == null || sql.isEmpty())
            return fieldlist;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                FieldInfoVO fieldInfoVO = new FieldInfoVO();
                fieldInfoVO.originalTableName = resultSet.getString("originalTableName");
                fieldInfoVO.originalFieldName = resultSet.getString("originalFieldName");
                fieldInfoVO.originalFieldDesc = resultSet.getString("originalFieldDesc");
                fieldInfoVO.originalFramework = resultSet.getString("originalFramework");
                if (StringUtils.isNotEmpty(fieldInfoVO.originalTableName) && StringUtils.isNotEmpty(fieldInfoVO.originalFieldName)
                        && StringUtils.isNotEmpty(fieldInfoVO.originalFieldDesc)) {
                    //if (fieldInfoVO.originalFramework != null && fieldInfoVO.originalFramework.length() > 0)
                    //fieldInfoVO.originalTableName = fieldInfoVO.originalFramework + "." + fieldInfoVO.originalTableName;
                    fieldlist.add(fieldInfoVO);
                }
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, ":" + ex.getMessage());
        }
        return fieldlist;
    }

    /**
     * @return void
     * @description 保存数据源到redis缓存
     * @author dick
     * @date 2022/5/11 10:33
     * @version v1.0
     * @params datasourceId 数据源id
     * @params operationType 操作类型 1、新增 2、修改 3、删除
     */
    public void setDataSourceToRedis(int datasourceId, int operationType) {
        if (datasourceId <= 0 && StringUtils.isNotEmpty(metaDataEntityKey)) {
            return;
        }
        String redisKey = metaDataEntityKey + "_" + datasourceId;
        if (operationType == 3) {
            Boolean exist = redisTemplate.hasKey(redisKey);
            if (exist) {
                redisTemplate.delete(redisKey);
            }
        } else if (operationType == 1 || operationType == 2) {
            DataSourceVO meta = getMeta(datasourceId);
            String json = JSONArray.toJSON(meta).toString();
            redisTemplate.opsForValue().set(redisKey, json);
        }
    }

    /**
     * @return void
     * @description 保存数据源到redis缓存
     * @author dick
     * @date 2022/5/11 10:33
     * @version v1.0
     */
    public void setDataSourceToRedis() {
        List<DataSourceConVO> all = mapper.getAll();
        if (CollectionUtils.isNotEmpty(all)) {
            for (DataSourceConVO dataSourceConVO : all) {
                setDataSourceToRedis(dataSourceConVO.getId(), 2);
            }
        }
    }

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
            fiDataMetaDataTreeBase.children = new ArrayList<>();
            for (DataSourceConPO dataSourceConPO : dataSourceConPOList) {
                List<FiDataMetaDataDTO> fiDataMetaData = redisUtil.getFiDataMetaData(String.valueOf(dataSourceConPO.datasourceId));
                if (CollectionUtils.isNotEmpty(fiDataMetaData)) {
                    fiDataMetaDataTreeBase.children.add(fiDataMetaData.get(0).children.get(0));
                }
            }
        }
        return fiDataMetaDataTreeBase;
    }

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
            fiDataMetaDataTreeBase.children = new ArrayList<>();
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
            Connection connection = null;
            List<TablePyhNameDTO> tableNameAndColumns = null;
            switch (DataSourceTypeEnum.values()[conPo.conType]) {
                case MYSQL:
                    // 表结构
                    connection = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), conPo.getConStr(), conPo.getConAccount(), conPo.getConPassword());
                    tableNameAndColumns = mysqlConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.MYSQL);
                    break;
                case SQLSERVER:
                    // 表结构
                    connection = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.getConStr(), conPo.getConAccount(), conPo.getConPassword());
                    tableNameAndColumns = sqlServerPlusUtils.getTableNameAndColumnsPlus(conPo.conStr, conPo.conAccount, conPo.conPassword, conPo.conDbname);
                    break;
                case POSTGRESQL:
                    // 表结构
                    connection = getStatement(DataSourceTypeEnum.POSTGRESQL.getDriverName(), conPo.getConStr(), conPo.getConAccount(), conPo.getConPassword());
                    tableNameAndColumns = postgresConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.POSTGRESQL);
                    break;
            }
            List<FieldInfoVO> tableFieldList = getTableFieldList(connection, conPo);
            connection.close();

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
                            if (CollectionUtils.isNotEmpty(tableFieldList)) {
                                FieldInfoVO fieldInfoVO = tableFieldList.stream()
                                        .filter(item -> item.originalTableName.equals(table.getTableName()) && item.originalFieldName.equals(field.getFieldName()))
                                        .findFirst().orElse(null);
                                if (fieldInfoVO != null) {
                                    fiDataMetaDataTree_Field.setLabelDesc(fieldInfoVO.originalFieldDesc);
                                }
                            }
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
}
