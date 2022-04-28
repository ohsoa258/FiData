package com.fisk.datagovernance.service.impl.dataops;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.PostgreDTO;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.service.dataops.IDataOpsDataSourceManageService;
import com.fisk.datagovernance.vo.dataops.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维数据源实现类
 * @date 2022/4/22 13:38
 */
@Service
@Slf4j
@Component
public class DataOpsDataSourceManageImpl implements IDataOpsDataSourceManageService {

    @Value("${pgsql-dw.id}")
    private int pgsqlDwId;
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

    @Value("${pgsql-ods.id}")
    private int pgsqlOdsId;
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
    @Value("${dataops.pg_metadataentity_key}")
    private String pgMetaDataEntityKey;

    @Resource
    private DataOpsLogManageImpl dataOpsLogManageImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public ResultEntity<List<DataOpsSourceVO>> getDataOpsDataSource() {
        List<DataOpsSourceVO> list = new ArrayList<>();
        try {
            Boolean exist = redisTemplate.hasKey(pgMetaDataEntityKey);
            if (!exist) {
                setDataOpsDataSourceStart();
            }
            String json = redisTemplate.opsForValue().get(pgMetaDataEntityKey).toString();
            if (StringUtils.isNotEmpty(json)) {
                list = JSONArray.parseArray(json, DataOpsSourceVO.class);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, list);
            }
        } catch (Exception ex) {
            log.error("getDataOpsDataSource执行异常：", ex);
            throw new FkException(ResultEnum.PG_METADATA_GETREDIS_ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.PG_METADATA_READREDIS_EXISTS, list);
    }

    @Override
    public ResultEntity<ExecuteResultVO> executeDataOpsSql(ExecuteDataOpsSqlDTO dto) {
        /*
        * requset TDD:
            {
                "current":"",
                "datasourceId":1,
                "executeSql":[
                    "DROP TABLE IF EXISTS \"public\".\"test0424\";CREATE TABLE \"public\".\"test0424\" (\"pid\" varchar(50) COLLATE \"pg_catalog\".\"default\" NOT NULL,\"pname\" varchar(255) COLLATE \"pg_catalog\".\"default\");ALTER TABLE \"public\".\"test0424\" ADD CONSTRAINT \"test0424_pkey\" PRIMARY KEY (\"pid\");"
                ],
                "size":""
            }
        * */
        ExecuteResultVO executeResultVO = new ExecuteResultVO();
        PostgreDTO postgreDTO = null;
        Statement st = null;
        Connection conn = null;
        int affectedCount = 0;
        ResultEnum executeResult = ResultEnum.REQUEST_SUCCESS;
        String executeMsg = "执行成功";
        try {
            List<PostgreDTO> postgreDTOList = getPostgreDTOList();
            if (CollectionUtils.isEmpty(postgreDTOList)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, executeResultVO);
            }
            postgreDTO = postgreDTOList.stream().filter(t -> t.getId() == dto.datasourceId).findFirst().orElse(null);
            if (postgreDTO == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, executeResultVO);
            }
            conn = getConnection(postgreDTO.getDataSourceTypeEnum().getDriverName(),
                    postgreDTO.getPgsqlUrl(), postgreDTO.getPgsqlUsername(), postgreDTO.getPgsqlPassword());
            st = conn.createStatement();
            assert st != null;
            // 返回值为true时，表示执行的是查询语句，可以通过st.getResultSet方法获取结果；
            // 返回值为false时，执行的是更新语句或DDL语句，st.getUpdateCount方法获取更新的记录数量。
            // 查询时，不能有多个结果集，也就是sql中不能有多个查询结果，否则会直接抛出异常
            // 修改时，允许新增、修改一起出现，但只会返回第一条sql受影响的行数
            boolean execute = st.execute(dto.executeSql);
            if (execute) {
                executeResultVO.setExecuteType(1);
                ResultSet rs = st.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                // 获取查询数据
                JSONArray array = new JSONArray();
                while (rs.next()) {
                    JSONObject jsonObj = new JSONObject();
                    // 遍历每一列
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        //获取sql查询数据集合
                        String value = rs.getString(columnName);
                        jsonObj.put(columnName, value);
                    }
                    array.add(jsonObj);
                }
                rs.close();
                if (array != null && array.size() > 0) {
                    List<Object> collect = array;
                    if (dto.current != null && dto.size != null) {
                        int rowsCount = array.stream().toArray().length;
                        executeResultVO.setCurrent(dto.current);
                        executeResultVO.setSize(dto.size);
                        executeResultVO.setTotal(rowsCount);
                        executeResultVO.setPage((int) Math.ceil(1.0 * rowsCount / dto.size));
                        dto.current = dto.current - 1;
                        collect = array.stream().skip((dto.current - 1 + 1) * dto.size).limit(dto.size).collect(Collectors.toList());
                    }
                    executeResultVO.setDataArray(collect);
                }
                executeResultVO.setExecuteState(true);
                // 获取列名、类型
                List<DataOpsTableFieldVO> dataOpsTableFieldVOList = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    DataOpsTableFieldVO fieldConfigVO = new DataOpsTableFieldVO();
                    // 源字段
                    fieldConfigVO.fieldName = metaData.getColumnLabel(i);
                    fieldConfigVO.fieldType = metaData.getColumnTypeName(i).toUpperCase();
                    dataOpsTableFieldVOList.add(fieldConfigVO);
                }
                if (CollectionUtils.isNotEmpty(dataOpsTableFieldVOList)) {
                    executeResultVO.setDataOpsTableFieldVO(dataOpsTableFieldVOList);
                }
            } else {
                executeResultVO.setExecuteType(2);
                // 执行 INSERT、UPDATE、DELETE 将返回受影响行数
                affectedCount = st.getUpdateCount();
                log.debug("executeDataOpsSql affectedCount:" + affectedCount);
                if (affectedCount < 0) {
                    // 如果返回的结果是一个结果集对象或没有更多结果，则返回-1。小于0默认未返回结果
                    // TRUNCATE、DROP、CREATE、ALTER 返回为-1，也就说明未返回结果
                    executeResultVO.setExecuteType(3);
                }
                log.debug("executeDataOpsSql executeType:" + executeResultVO.getExecuteType());
                executeResultVO.setAffectedCount(affectedCount);
                executeResultVO.setExecuteState(true);
            }
        } catch (Exception ex) {
            executeResult = ResultEnum.ERROR;
            executeMsg = ex.getMessage();
            log.error("executeDataOpsSql执行异常：", ex);
            throw new FkException(ResultEnum.DATA_OPS_SQL_EXECUTE_ERROR, ex.getMessage());
        } finally {
            try {
                if (st != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // do nothing
                executeResult = ResultEnum.ERROR;
                executeMsg = ex.getMessage();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                executeResult = ResultEnum.ERROR;
                executeMsg = ex.getMessage();
                log.error("executeDataOpsSql数据库连接关闭异常：", ex);
                throw new FkException(ResultEnum.DATA_OPS_CLOSESTATEMENT_ERROR, ex.getMessage());
            }
            // 保存日志
            try {
                DataOpsLogPO dataOpsLogPO = new DataOpsLogPO();
                dataOpsLogPO.setConIp(postgreDTO.getIp());
                dataOpsLogPO.setConDbname(postgreDTO.getDbName());
                dataOpsLogPO.setConDbtype(postgreDTO.getDataSourceTypeEnum().getValue());
                dataOpsLogPO.setExecuteSql(dto.executeSql);
                dataOpsLogPO.setExecuteResult(executeResult.getCode());
                dataOpsLogPO.setExecuteMsg(executeMsg);
                dataOpsLogPO.setExecuteUser(userHelper.getLoginUserInfo().getUsername());
//                List<Long> userIds = new ArrayList<>();
//                userIds.add(userHelper.getLoginUserInfo().getId());
//                ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
//                if (userListByIds != null && CollectionUtils.isNotEmpty(userListByIds.getData())) {
//                    dataOpsLogPO.setExecuteUser(userListByIds.getData().get(0).getUserAccount());
//                }
                dataOpsLogManageImpl.saveLog(dataOpsLogPO);
            } catch (Exception ex) {
                log.error("executeDataOpsSql日志保存失败：", ex);
                throw new FkException(ResultEnum.DATA_OPS_CREATELOG_ERROR, ex.getMessage());
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, executeResultVO);
    }

    @Override
    public Object setDataOpsDataSourceStart() {
        setDataOpsDataSource();
        return "success";
    }

    public void setDataOpsDataSource() {
        log.info("setDataOpsDataSource 开始");
        List<DataOpsSourceVO> dataOpsSourceVOList = new ArrayList<>();
        // 第一步：读取配置的数据源信息
        List<PostgreDTO> postgreDTOList = getPostgreDTOList();
        if (CollectionUtils.isEmpty(postgreDTOList)) {
            log.error("setDataOpsDataSource 数据源配置不存在");
            return;
        }
        // 第二步：读取数据源下的库、表、字段信息
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        try {
            List<String> conIps = postgreDTOList.stream().map(PostgreDTO::getIp).distinct().collect(Collectors.toList());
            for (String conIp : conIps) {
                DataOpsSourceVO dataOpsSourceVO = null;
                List<DataOpsDataBaseVO> dataOpsDataBaseVOS = new ArrayList<>();
                for (PostgreDTO postgreDTO : postgreDTOList) {
                    if (postgreDTO.getIp().equals(conIp)) {
                        if (dataOpsSourceVO == null) {
                            dataOpsSourceVO = new DataOpsSourceVO();
                            dataOpsSourceVO.setConIp(postgreDTO.getIp());
                            dataOpsSourceVO.setConType(postgreDTO.getDataSourceTypeEnum());
                            dataOpsSourceVO.setConPort(postgreDTO.getPort());
                        }
                        List<DataOpsDataTableVO> dataOpsDataTableVOList = new ArrayList<>();
                        // pg数据库信息读取
                        if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.POSTGRE) {
                            // 表信息
                            List<String> tableList = postgresConUtils.getTableList(postgreDTO.getPgsqlUrl(), postgreDTO.getPgsqlUsername(),
                                    postgreDTO.getPgsqlPassword(), postgreDTO.getDataSourceTypeEnum().getDriverName());
                            if (CollectionUtils.isNotEmpty(tableList)) {
                                // 字段信息
                                Map<String, List<TableStructureDTO>> tableColumnList = postgresConUtils.getTableColumnList(postgreDTO.getPgsqlUrl(), postgreDTO.getPgsqlUsername(),
                                        postgreDTO.getPgsqlPassword(), postgreDTO.getDataSourceTypeEnum().getDriverName(), tableList);
                                for (String tableName : tableList) {
                                    DataOpsDataTableVO dataOpsDataTableVO = new DataOpsDataTableVO();
                                    if (tableColumnList != null && tableColumnList.size() > 0) {
                                        List<TableStructureDTO> tableStructureDTOS = tableColumnList.get(tableName);
                                        if (CollectionUtils.isNotEmpty(tableStructureDTOS)) {
                                            List<DataOpsTableFieldVO> fieldVOList = new ArrayList<>();
                                            for (TableStructureDTO tableStructureDTO : tableStructureDTOS) {
                                                DataOpsTableFieldVO dataOpsTableFieldVO = new DataOpsTableFieldVO();
                                                dataOpsTableFieldVO.setFieldName(tableStructureDTO.getFieldName());
                                                dataOpsTableFieldVO.setFieldType(tableStructureDTO.getFieldType());
                                                dataOpsTableFieldVO.setFieldLength(tableStructureDTO.getFieldLength());
                                                dataOpsTableFieldVO.setFieldDes(tableStructureDTO.getFieldDes());
                                                fieldVOList.add(dataOpsTableFieldVO);
                                            }
                                            dataOpsDataTableVO.setChildren(fieldVOList);
                                        }
                                    }
                                    dataOpsDataTableVO.setTableName(tableName);
                                    dataOpsDataTableVOList.add(dataOpsDataTableVO);
                                }
                            }
                        }
                        DataOpsDataBaseVO dataOpsDataBaseVO = new DataOpsDataBaseVO();
                        dataOpsDataBaseVO.setDatasourceId(postgreDTO.getId());
                        dataOpsDataBaseVO.setConDbname(postgreDTO.getDbName());
                        dataOpsDataBaseVO.setChildren(dataOpsDataTableVOList);
                        dataOpsDataBaseVOS.add(dataOpsDataBaseVO);
                    }
                }
                dataOpsSourceVO.setChildren(dataOpsDataBaseVOS);
                dataOpsSourceVOList.add(dataOpsSourceVO);
            }
            if (CollectionUtils.isNotEmpty(dataOpsSourceVOList)) {
                String dataOpsSourceJson = JSONArray.toJSON(dataOpsSourceVOList).toString();
                // 生成目录加 ：
                redisTemplate.opsForValue().set("DataOps_PGMetaDataEntityKey", dataOpsSourceJson);
                log.info("setDataOpsDataSource pg元数据信息已写入redis");
            }
        } catch (Exception ex) {
            log.error("setDataOpsDataSource执行异常：", ex);
        } finally {
            log.info("setDataOpsDataSource 结束");
        }
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.dto.dataops.PostgreDTO>
     * @description 读取pg配置文件转实体
     * @author dick
     * @date 2022/4/22 14:26
     * @version v1.0
     * @params
     */
    public List<PostgreDTO> getPostgreDTOList() {
        List<PostgreDTO> postgreDTOList = new ArrayList<>();
        PostgreDTO postgreDTO_dw = new PostgreDTO();
        postgreDTO_dw.setId(pgsqlDwId);
        postgreDTO_dw.setPort(pgsqlDwPort);
        postgreDTO_dw.setIp(pgsqlDwIp);
        postgreDTO_dw.setDbName(pgsqlDwDbName);
        postgreDTO_dw.setDataSourceTypeEnum(DataSourceTypeEnum.getEnumByDriverName(pgsqlDwDriverClassName));
        postgreDTO_dw.setPgsqlUrl(pgsqlDwUrl);
        postgreDTO_dw.setPgsqlUsername(pgsqlDwUsername);
        postgreDTO_dw.setPgsqlPassword(pgsqlDwPassword);
        postgreDTOList.add(postgreDTO_dw);
        PostgreDTO postgreDTO_ods = new PostgreDTO();
        postgreDTO_ods.setId(pgsqlOdsId);
        postgreDTO_ods.setPort(pgsqlOdsPort);
        postgreDTO_ods.setIp(pgsqlOdsIp);
        postgreDTO_ods.setDbName(pgsqlOdsDbName);
        postgreDTO_ods.setDataSourceTypeEnum(DataSourceTypeEnum.getEnumByDriverName(pgsqlOdsDriverClassName));
        postgreDTO_ods.setPgsqlUrl(pgsqlOdsUrl);
        postgreDTO_ods.setPgsqlUsername(pgsqlOdsUsername);
        postgreDTO_ods.setPgsqlPassword(pgsqlOdsPassword);
        postgreDTOList.add(postgreDTO_ods);
        return postgreDTOList;
    }

    public static Connection getConnection(String driver, String url, String username, String password) {
        Connection conn = null;
        try {
            // 加载驱动类
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("找不到pg驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            System.out.println("pg数据库连接失败！");
            throw new FkException(ResultEnum.PG_CONNECT_ERROR);
        }
        return conn;
    }
}
