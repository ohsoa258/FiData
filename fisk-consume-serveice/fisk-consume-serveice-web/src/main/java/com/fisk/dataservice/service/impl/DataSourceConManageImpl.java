package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.service.dbMetaData.dto.DataBaseViewDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.datasource.*;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.mapper.DataSourceConMapper;
import com.fisk.dataservice.service.IDataSourceConManageService;
import com.fisk.dataservice.vo.api.FieldInfoVO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.vo.datasource.DataSourceVO;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    UserHelper userHelper;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("${dataservice.datasource.metadataentity_key}")
    private String metaDataEntityKey;

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
        model.setCreateTime(LocalDateTime.now());
        Long userId = userHelper.getLoginUserInfo().getId();
        model.setCreateUser(userId.toString());
        boolean isInsert = baseMapper.insertOne(model) > 0;
        if (!isInsert)
            return ResultEnum.SAVE_DATA_ERROR;
        int id = (int) model.getId();
        setDataSourceToRedis(id,1);
        return ResultEnum.SUCCESS;
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
        setDataSourceToRedis(dto.getId(), 2);
        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        setDataSourceToRedis(id, 3);
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
    public DataSourceVO getTableAll(int datasourceId) {
        DataSourceVO dataSource = null;
        try {
            String redisKey = metaDataEntityKey + "_" + datasourceId;
            Boolean exist = redisTemplate.hasKey(redisKey);
            if (!exist) {
                setDataSourceToRedis(datasourceId,1);
            }
            String json = redisTemplate.opsForValue().get(redisKey).toString();
            if (StringUtils.isNotEmpty(json)) {
                dataSource = JSONObject.parseObject(json,DataSourceVO.class);
            }
        } catch (Exception ex) {
            log.error("getTableAll执行异常：", ex);
            throw new FkException(ResultEnum.DS_DATASOURCE_READ_ERROR, ex.getMessage());
        }
        return dataSource;
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
            case POSTGRE:
                dataSource.tableDtoList = postgresConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DriverTypeEnum.POSTGRESQL);
                break;
        }
        Connection conn = null;
        if (conPo.getConType() == com.fisk.dataservice.enums.DataSourceTypeEnum.MYSQL.getValue()) {
            conn = getStatement(com.fisk.dataservice.enums.DataSourceTypeEnum.MYSQL.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
        } else if (conPo.getConType() == com.fisk.dataservice.enums.DataSourceTypeEnum.SQLSERVER.getValue()) {
            conn = getStatement(com.fisk.dataservice.enums.DataSourceTypeEnum.SQLSERVER.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
        } else if (conPo.getConType() == com.fisk.dataservice.enums.DataSourceTypeEnum.POSTGRE.getValue()) {
            conn = getStatement(com.fisk.dataservice.enums.DataSourceTypeEnum.POSTGRE.getDriverName(), conPo.conStr, conPo.conAccount, conPo.conPassword);
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
            case POSTGRE:
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
                if (fieldInfoVO.originalTableName != null
                        && fieldInfoVO.originalTableName.length() > 0
                        && fieldInfoVO.originalFieldName != null
                        && fieldInfoVO.originalFieldName.length() > 0
                        && fieldInfoVO.originalFieldDesc != null
                        && fieldInfoVO.originalFieldDesc.length() > 0)
                    if (fieldInfoVO.originalFramework != null && fieldInfoVO.originalFramework.length() > 0)
                        fieldInfoVO.originalTableName = fieldInfoVO.originalFramework + "." + fieldInfoVO.originalTableName;
                fieldlist.add(fieldInfoVO);
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
            redisTemplate.delete(redisKey);
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
}
