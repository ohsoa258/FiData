package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.Dto.SqlParmDto;
import com.fisk.common.core.utils.Dto.SqlWhereDto;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.api.*;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiTypeEnum;
import com.fisk.dataservice.enums.DataSourceTypeEnum;
import com.fisk.dataservice.map.ApiFieldMap;
import com.fisk.dataservice.map.ApiFilterConditionMap;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.map.ApiRegisterMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IApiRegisterManageService;
import com.fisk.dataservice.vo.api.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * api接口实现类
 *
 * @author dick
 */
@Service
public class ApiRegisterManageImpl extends ServiceImpl<ApiRegisterMapper, ApiConfigPO> implements IApiRegisterManageService {

    @Resource
    private ApiFieldMapper apiFieldMapper;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private ApiFilterConditionMapper apiFilterConditionMapper;

    @Resource
    private ApiParmMapper apiParmMapper;

    @Resource
    private ApiFieldManageImpl apiFieldManageImpl;

    @Resource
    private ApiFilterConditionManageImpl apiFilterConditionManageImpl;

    @Resource
    private ApiParmManageImpl apiParmManageImpl;

    @Resource
    private AppApiMapper appApiMapper;

    @Resource
    UserHelper userHelper;

    @Resource
    private UserClient userClient;

    @Override
    public Page<ApiConfigVO> getAll(ApiRegisterQueryDTO query) {
        Page<ApiConfigVO> all = baseMapper.getAll(query.page, query);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Long> userIds = all.getRecords().stream()
                    .filter(x -> StringUtils.isNotEmpty(x.createUser))
                    .map(x -> Long.valueOf(x.createUser))
                    .distinct()
                    .collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                all.getRecords().forEach(e -> {
                    userListByIds.getData()
                            .stream()
                            .filter(user -> user.getId().toString().equals(e.createUser))
                            .findFirst()
                            .ifPresent(user -> e.createUser = user.userAccount);
                });
            }
        }
        return all;
    }

    @Override
    public PageDTO<ApiSubVO> getApiSubAll(ApiSubQueryDTO dto) {
        PageDTO<ApiSubVO> pageDTO = new PageDTO<>();
        List<ApiSubVO> apiSubVOS = new ArrayList<>();
        List<ApiConfigPO> apiConfigPOS = baseMapper.getList(dto.keyword);
        if (CollectionUtils.isNotEmpty(apiConfigPOS)) {
            apiSubVOS = ApiRegisterMap.INSTANCES.poToApiSubVO(apiConfigPOS);
            List<AppApiPO> subscribeListByAppId = appApiMapper.getSubscribeListByAppId(dto.appId);
            if (CollectionUtils.isNotEmpty(subscribeListByAppId)) {
                apiSubVOS.forEach(e -> {
                    subscribeListByAppId
                            .stream()
                            .filter(item -> item.getApiId() == e.id)
                            .findFirst()
                            .ifPresent(user -> e.apiSubState = 1);
                });
            }
            pageDTO.setTotal(Long.valueOf(apiSubVOS.size()));
            dto.current = dto.current - 1;
            apiSubVOS = apiSubVOS.stream().sorted(Comparator.comparing(ApiSubVO::getApiSubState).reversed()).skip((dto.current - 1 + 1) * dto.size).limit(dto.size).collect(Collectors.toList());
            List<Long> userIds = apiSubVOS.stream()
                    .filter(x -> StringUtils.isNotEmpty(x.createUser))
                    .map(x -> Long.valueOf(x.createUser))
                    .distinct()
                    .collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                apiSubVOS.forEach(e -> {
                    userListByIds.getData()
                            .stream()
                            .filter(user -> user.getId().toString().equals(e.createUser))
                            .findFirst()
                            .ifPresent(user -> e.createUser = user.userAccount);
                });
            }
        }
        pageDTO.setItems(apiSubVOS);
        return pageDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(ApiRegisterDTO dto) {
        boolean isInsert = false;
        int apiId;
        // 第一步：保存api信息
        ApiConfigPO apiConfigPO = ApiRegisterMap.INSTANCES.dtoToPo(dto.apiDTO);
        if (apiConfigPO == null)
            return ResultEnum.SAVE_DATA_ERROR;
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(apiConfigPO.getDatasourceId());
        if (dataSourceConPO == null)
            return ResultEnum.DS_DATASOURCE_EXISTS;
        String apiCode = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        apiConfigPO.setApiCode(apiCode);
        apiConfigPO.setCreateTime(LocalDateTime.now());
        Long userId = userHelper.getLoginUserInfo().getId();
        apiConfigPO.setCreateUser(userId.toString());
        isInsert = baseMapper.insertOne(apiConfigPO) > 0;
        if (!isInsert)
            return ResultEnum.SAVE_DATA_ERROR;
        apiId = (int) apiConfigPO.getId();

        // 第二步：保存字段信息
        List<FieldConfigPO> fieldConfigPOS = ApiFieldMap.INSTANCES.listDtoToPo_Add(dto.fieldDTO);
        if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
            fieldConfigPOS.forEach(e -> {
                e.apiId = apiId;
            });
            isInsert = apiFieldManageImpl.saveBatch(fieldConfigPOS);
            if (!isInsert)
                return ResultEnum.SAVE_DATA_ERROR;
        }

        // 第三步：保存过滤条件信息
        if (dto.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()) {
            List<FilterConditionConfigPO> filterConditionConfigPOS = ApiFilterConditionMap.INSTANCES.listDtoToPo(dto.whereDTO);
            if (CollectionUtils.isNotEmpty(filterConditionConfigPOS)) {
                filterConditionConfigPOS.forEach(e -> {
                    e.apiId = apiId;
                });
                isInsert = apiFilterConditionManageImpl.saveBatch(filterConditionConfigPOS);
                if (!isInsert)
                    return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        // 第四步：保存输入参数信息
        List<ParmConfigPO> parmConfigPOS = ApiParmMap.INSTANCES.listDtoToPo(dto.parmDTO);
        if (CollectionUtils.isNotEmpty(parmConfigPOS)) {
            parmConfigPOS.forEach(e -> {
                e.apiId = apiId;
            });
            isInsert = apiParmManageImpl.saveBatch(parmConfigPOS);
            if (!isInsert)
                return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(ApiRegisterEditDTO dto) {
        ApiConfigPO model = baseMapper.selectById(dto.apiDTO.getId());
        if (model == null) {
            return ResultEnum.DS_API_EXISTS;
        }
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(dto.apiDTO.getDatasourceId());
        if (dataSourceConPO == null)
            return ResultEnum.DS_DATASOURCE_EXISTS;
        int apiId;
        boolean isUpdate = false;
        // 第一步：编辑保存api信息
        ApiConfigPO apiConfigPO = ApiRegisterMap.INSTANCES.dtoToPo_Edit(dto.apiDTO);
        if (apiConfigPO == null)
            return ResultEnum.SAVE_DATA_ERROR;
        isUpdate = baseMapper.updateById(apiConfigPO) > 0;
        if (!isUpdate)
            return ResultEnum.SAVE_DATA_ERROR;
        apiId = (int) apiConfigPO.getId();

        // 第二步：编辑保存字段信息[数据库可能修改表字段描述，此处直接全量更新]
        apiFieldMapper.updateByApiId(apiId);
        List<FieldConfigPO> fieldConfigPOS = ApiFieldMap.INSTANCES.listDtoToPo_Add(dto.fieldDTO);
        if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
            isUpdate = apiFieldManageImpl.saveBatch(fieldConfigPOS);
            if (!isUpdate)
                return ResultEnum.SAVE_DATA_ERROR;
        }

        // 第三步：保存编辑过滤信息
        apiFilterConditionMapper.updateByApiId(apiId);
        if (dto.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()) {
            List<FilterConditionConfigPO> filterConditionConfigPOS = ApiFilterConditionMap.INSTANCES.listDtoToPo(dto.whereDTO);
            if (CollectionUtils.isNotEmpty(filterConditionConfigPOS)) {
                isUpdate = apiFilterConditionManageImpl.saveBatch(filterConditionConfigPOS);
                if (!isUpdate)
                    return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        // 第四步：保存编辑参数信息
        /* 因为参数信息又被用作于内置参数，因此在此处不能直接删除
         * 1、删除不存在的参数
         * 2、修改参数
         * 3、新增参数
         * */
        QueryWrapper<ParmConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ParmConfigPO::getApiId, apiId).eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> parmConfigPOS = apiParmMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(parmConfigPOS)) {
            parmConfigPOS.forEach(e -> {
                Optional<ParmConfigEditDTO> first = dto.parmDTO.stream().filter(item -> item.getId() == e.id).findFirst();
                if (!first.isPresent()) {
                    apiParmMapper.deleteByIdWithFill(e);
                }
            });
        }
        List<ParmConfigPO> parmConfigPOS1 = ApiParmMap.INSTANCES.listDtoToPo_Edit(dto.parmDTO);
        if (CollectionUtils.isNotEmpty(parmConfigPOS1)) {
            isUpdate = apiParmManageImpl.saveOrUpdateBatch(parmConfigPOS1);
            if (!isUpdate)
                return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int apiId) {
        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null) {
            return ResultEnum.DS_API_EXISTS;
        }
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<ApiRegisterDetailVO> detail(int apiId) {
        ApiRegisterDetailVO apiRegisterDetailVO = new ApiRegisterDetailVO();

        // 第一步：查询API信息,selectById仅查询有效的
        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_API_EXISTS, apiRegisterDetailVO);
        apiRegisterDetailVO.apiVO = ApiRegisterMap.INSTANCES.poToVo(model);

        // 第二步：查询字段信息
        QueryWrapper<FieldConfigPO> fieldQueryWrapper = new QueryWrapper<>();
        fieldQueryWrapper.lambda()
                .eq(FieldConfigPO::getApiId, apiId)
                .eq(FieldConfigPO::getDelFlag, 1);
        List<FieldConfigPO> fieldConfigPOS = apiFieldMapper.selectList(fieldQueryWrapper);
        if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
            apiRegisterDetailVO.fieldVO = ApiFieldMap.INSTANCES.listPoToVo(fieldConfigPOS);
        }

        // 第三步：查询过滤信息
        QueryWrapper<FilterConditionConfigPO> filterConditionQueryWrapper = new QueryWrapper<>();
        filterConditionQueryWrapper.lambda()
                .eq(FilterConditionConfigPO::getApiId, apiId)
                .eq(FilterConditionConfigPO::getDelFlag, 1);
        List<FilterConditionConfigPO> filterConditionConfigPOS = apiFilterConditionMapper.selectList(filterConditionQueryWrapper);
        if (CollectionUtils.isNotEmpty(filterConditionConfigPOS)) {
            apiRegisterDetailVO.whereVO = ApiFilterConditionMap.INSTANCES.listPoToVo(filterConditionConfigPOS);
        }

        // 第四步：查询参数信息
        QueryWrapper<ParmConfigPO> parmQueryWrapper = new QueryWrapper<>();
        parmQueryWrapper.lambda()
                .eq(ParmConfigPO::getApiId, apiId)
                .eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> parmConfigPOS = apiParmMapper.selectList(parmQueryWrapper);
        if (CollectionUtils.isNotEmpty(parmConfigPOS)) {
            apiRegisterDetailVO.parmVO = ApiParmMap.INSTANCES.listPoToVo(parmConfigPOS);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, apiRegisterDetailVO);
    }

    @Override
    public List<FieldConfigVO> getFieldAll(int apiId) {
        List<FieldConfigVO> fieldList = new ArrayList<>();
        QueryWrapper<FieldConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(FieldConfigPO::getApiId, apiId)
                .eq(FieldConfigPO::getDelFlag, 1);
        List<FieldConfigPO> selectList = apiFieldMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            fieldList = ApiFieldMap.INSTANCES.listPoToVo(selectList);
        }
        return fieldList;
    }

    @Override
    public ResultEnum setField(List<FieldConfigEditDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<FieldConfigPO> fieldList = ApiFieldMap.INSTANCES.listDtoToPo(dto);
        return apiFieldManageImpl.updateBatchById(fieldList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ApiPreviewVO preview(ApiPreviewDTO dto) {
        ApiPreviewVO apiPreviewVO = new ApiPreviewVO();
        String sql = dto.apiDTO.getCreateSql();

        // 第一步：拼接过滤条件
        if (dto.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()) {
            sql = String.format("SELECT %s FROM %s WHERE 1=1 ", sql, dto.apiDTO.getTableName());
            if (CollectionUtils.isNotEmpty(dto.whereDTO)) {
                List<SqlWhereDto> sqlWhereDtos = ApiFilterConditionMap.INSTANCES.listDtoToSqlWhereDto(dto.whereDTO);
                String s = SqlParmUtils.SqlWhere(sqlWhereDtos);
                if (s != null && s.length() > 0)
                    sql += s;
            }
        }

        // 第二步：拼接参数条件
        if (CollectionUtils.isNotEmpty(dto.parmDTO)) {
            List<SqlParmDto> sqlParmDtos = ApiParmMap.INSTANCES.listDtoToSqlParmDto(dto.parmDTO);
            String s = SqlParmUtils.SqlParm(sqlParmDtos, sql, "@");
            if (s != null && s.length() > 0)
                sql = s;
        }

        // 第三步：查询数据源信息
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(dto.apiDTO.getDatasourceId());
        if (dataSourceConPO == null)
            return apiPreviewVO;
        try {
            Statement st = null;
            Connection conn = null;
            if (dataSourceConPO.getConType() == DataSourceTypeEnum.MYSQL.getValue()) {
                conn = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), dataSourceConPO.conStr, dataSourceConPO.conAccount, dataSourceConPO.conPassword);
            } else if (dataSourceConPO.getConType() == DataSourceTypeEnum.SQLSERVER.getValue()) {
                conn = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), dataSourceConPO.conStr, dataSourceConPO.conAccount, dataSourceConPO.conPassword);
            }
            /*
                以流的形式 TYPE_FORWARD_ONLY: 只可向前滚动查询 CONCUR_READ_ONLY: 指定不可以更新 ResultSet
                如果PreparedStatement对象初始化时resultSetType参数设置为TYPE_FORWARD_ONLY，
                在从ResultSet（结果集）中读取记录的时，对于访问过的记录就自动释放了内存。
                而设置为TYPE_SCROLL_INSENSITIVE或TYPE_SCROLL_SENSITIVE时为了保证能游标能向上移动到任意位置，
                已经访问过的所有都保留在内存中不能释放。所以大量数据加载的时候，就OOM了
                 */
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // 查询10条
            st.setFetchSize(10);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            //获取数据集
            apiPreviewVO = resultSetToJsonArray(conn, dataSourceConPO, rs, dto);
            rs.close();
        } catch (Exception e) {
            throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR, e.getMessage());
        }
        return apiPreviewVO;
    }

    /**
     * 预览结果转Json数组
     *
     * @param conn       数据库连接
     * @param dataSource 数据库信息
     * @param rs         查询结果
     * @param pvDTO      预览请求参数
     * @return target
     */
    private static ApiPreviewVO resultSetToJsonArray(Connection conn, DataSourceConPO dataSource, ResultSet rs, ApiPreviewDTO pvDTO)
            throws SQLException, JSONException {
        ApiPreviewVO data = new ApiPreviewVO();

        JSONArray array = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldConfigVO> fieldConfigVOS = new ArrayList<>();
        int count = 1;
        while (rs.next() && count <= 10) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }

        // 获取描述信息
        List<FieldInfoVO> tableFieldList = null;
        if (pvDTO.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()
                && pvDTO.apiDTO.getTableName() != null
                && pvDTO.apiDTO.getTableName().length() > 0) {
            List<String> tableNames = new ArrayList<>();
            tableNames.add(pvDTO.apiDTO.getTableName());
            tableFieldList = getTableFieldList(conn, dataSource, tableNames);
        }

        //获取列名、描述
        for (int i = 1; i <= columnCount; i++) {
            FieldConfigVO fieldConfigVO = new FieldConfigVO();
            // 源字段
            fieldConfigVO.fieldName = metaData.getColumnLabel(i);
            fieldConfigVO.fieldType = metaData.getColumnTypeName(i).toUpperCase();
            fieldConfigVO.fieldSort = i;
            if (pvDTO.apiId != 0)
                fieldConfigVO.apiId = pvDTO.apiId;

            if (fieldConfigVO.fieldType.contains("INT2")
                    || fieldConfigVO.fieldType.contains("INT4")
                    || fieldConfigVO.fieldType.contains("INT8")) {
                fieldConfigVO.fieldType = "INT".toLowerCase();
            }

            // 转换表字段类型
//            List<String> list = transformField(fieldConfigVO.fieldType);
//            fieldConfigVO.fieldType = list.get(0);
            fieldConfigVO.fieldType = fieldConfigVO.fieldType.toLowerCase();
            // 读取不到类型，默认字符串类型
            if (fieldConfigVO.fieldType == null ||
                    fieldConfigVO.fieldType == "")
                fieldConfigVO.fieldType = "varchar";

            // 获取表字段描述
            if (CollectionUtils.isNotEmpty(tableFieldList)) {
                Optional<FieldInfoVO> first = tableFieldList.stream().filter(item -> item.originalFieldName.equals(fieldConfigVO.fieldName)).findFirst();
                if (first.isPresent()) {
                    FieldInfoVO fieldInfoVO = first.get();
                    if (fieldInfoVO != null)
                        fieldConfigVO.fieldDesc = fieldInfoVO.originalFieldDesc;
                }
            }

            fieldConfigVOS.add(fieldConfigVO);
        }
        data.fieldVO = fieldConfigVOS.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    /**
     * 转换表字段类型
     *
     * @param fieldType fieldType
     * @return target
     */
    private static List<String> transformField(String fieldType) {
        // 日期型
        String timeType = "date";

        // 浮点型
        List<String> floatType = new ArrayList<>();
        floatType.add("double");

        // 文本类型
        List<String> textTpye = new ArrayList<>();
        textTpye.add("text");

        // 字符型
        List<String> charType = new ArrayList<>();
        charType.add("");

        // 双字符型
        List<String> ncharType = new ArrayList<>();
        charType.add("nvarchar");

        // 整型
        List<String> integerType = new ArrayList<>();
        integerType.add("tinyint");
        integerType.add("smallint");
        integerType.add("mediumint");
        integerType.add("int");
        integerType.add("integer");
        integerType.add("bigint");

        // 精确数值型
        List<String> accurateType = new ArrayList<>();
        accurateType.add("decimal");
        accurateType.add("numeric");

        // 货币、近似数值型
        List<String> otherType = new ArrayList<>();
        otherType.add("money");
        otherType.add("smallmoney");
        otherType.add("float");
        otherType.add("real");

        List<String> fieldList = new LinkedList<>();

        if (integerType.contains(fieldType.toLowerCase())) {
            fieldList.add("INT".toLowerCase());
        } else if (textTpye.contains(fieldType.toLowerCase())) {
            fieldList.add("TEXT".toLowerCase());
        } else if (accurateType.contains(fieldType.toLowerCase())
                || otherType.contains(fieldType.toLowerCase())) {
            fieldList.add("FLOAT".toLowerCase());
        } else if (fieldType.toLowerCase().contains(timeType)) {
            fieldList.add("TIMESTAMP".toLowerCase());
        } else if (ncharType.contains(fieldType.toLowerCase())) {
            fieldList.add("NVARCHAR".toLowerCase());
        } else {
            fieldList.add("VARCHAR".toLowerCase());
        }
        return fieldList;
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
    private static Connection getStatement(String driver, String url, String username, String password) {
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
     * @param tableNames 查询的表
     * @return statement
     */
    private static List<FieldInfoVO> getTableFieldList(Connection conn, DataSourceConPO dataSource, List<String> tableNames) throws SQLException {
        List<FieldInfoVO> fieldlist = new ArrayList<>();
        if (CollectionUtils.isEmpty(tableNames))
            return fieldlist;
        String sql = "";
        DataSourceTypeEnum value = DataSourceTypeEnum.values()[dataSource.getConType()];
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
                        "\tTABLE_SCHEMA = '%s' \n" +
                        "\tAND TABLE_NAME = '%s'", dataSource.conDbname, tableNames.get(0));
                break;
            case SQLSERVER:
                sql = String.format("SELECT\n" +
                        "\td.name AS originalTableName,\n" +
                        "\ta.name AS originalFieldName,\n" +
                        "\tisnull( g.[value], '' ) AS originalFieldDesc,\n" +
                        "\tschema_name( tb.schema_id ) AS originalFramework \n" +
                        "FROM\n" +
                        "\tsyscolumns a\n" +
                        "\tLEFT JOIN systypes b ON a.xusertype= b.xusertype\n" +
                        "\tINNER JOIN sysobjects d ON a.id= d.id \n" +
                        "\tAND d.xtype= 'U' \n" +
                        "\tAND d.name<> 'dtproperties'\n" +
                        "\tLEFT JOIN sys.tables tb ON tb.name= d.name\n" +
                        "\tLEFT JOIN syscomments e ON a.cdefault= e.id\n" +
                        "\tLEFT JOIN sys.extended_properties g ON a.id= g.major_id \n" +
                        "\tAND a.colid= g.minor_id\n" +
                        "\tLEFT JOIN sys.extended_properties f ON d.id= f.major_id \n" +
                        "\tAND f.minor_id= 0\n" +
                        "\tWHERE d.name = '%s'", tableNames.get(0));
                break;
            case POSTGRE:
                sql=String.format("SELECT c.relname as originalTableName,a.attname as originalFieldName,col_description(a.attrelid,a.attnum) as originalFieldDesc,'' AS originalFramework \n" +
                        "FROM pg_class as c,pg_attribute as a inner join pg_type on pg_type.oid = a.atttypid\n" +
                        "where c.relname in  (SELECT tablename FROM pg_tables ) and a.attrelid = c.oid and a.attnum>0\n" +
                        "and c.relname ='%s'",tableNames.get(0));
        }
        if (sql == null || sql.isEmpty())
            return fieldlist;
        try {
            //String instr = "(\'" + String.join("\',\'", tableNames) + "\')";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            //preparedStatement.setString(1, instr);
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
                        && fieldInfoVO.originalFieldDesc.length() > 0) {
                    if (fieldInfoVO.originalFramework != null && fieldInfoVO.originalFramework.length() > 0)
                        fieldInfoVO.originalTableName = fieldInfoVO.originalFramework + "." + fieldInfoVO.originalTableName;
                    fieldlist.add(fieldInfoVO);
                }
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, ":" + ex.getMessage());
        }
        return fieldlist;
    }
}
