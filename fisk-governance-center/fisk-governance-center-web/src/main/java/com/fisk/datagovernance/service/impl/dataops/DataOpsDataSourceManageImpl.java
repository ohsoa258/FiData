package com.fisk.datagovernance.service.impl.dataops;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.PostgreDTO;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.datagovernance.service.dataops.IDataOpsDataSourceManageService;
import com.fisk.datagovernance.vo.dataops.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维数据源实现类
 * @date 2022/4/22 13:38
 */
@Service
@Slf4j
public class DataOpsDataSourceManageImpl implements IDataOpsDataSourceManageService {

    @Value("${database.dw_id}")
    private int dwId;
    @Value("${database.ods_id}")
    private int odsId;
    @Value("${dataops.metadataentity_key}")
    private String metaDataEntityKey;

    @Resource
    private DataOpsLogManageImpl dataOpsLogManageImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserClient userClient;

    @Override
    public ResultEntity<List<DataOpsSourceVO>> getDataOpsDataSource() {
        List<DataOpsSourceVO> list = new ArrayList<>();
        try {
            Boolean exist = redisTemplate.hasKey(metaDataEntityKey);
            if (!exist) {
                reloadDataOpsDataSource();
            }
            String json = redisTemplate.opsForValue().get(metaDataEntityKey).toString();
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
    public ResultEntity<Object> reloadDataOpsDataSource() {
        setDataOpsDataSource();
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "已重新加载数据源");
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
                    postgreDTO.getSqlUrl(), postgreDTO.getSqlUsername(), postgreDTO.getSqlPassword());
            st = conn.createStatement();
            assert st != null;
           /*
           * QL语言包括四种主要程序设计语言类别的语句：
            数据定义语言(DDL)，数据操作语言(DML)，数据控制语言(DCL)和事务控制语言（TCL）。
            主要的DDL动词：CREATE（创建）、DROP（删除）、ALTER（修改）、TRUNCATE（截断）、RENAME（重命名）
            DML主要指数据的增删查改: SELECT、DELETE、UPDATE、INSERT、CALL(执行存储过程)
           * */
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
                affectedCount = st.getUpdateCount();
                executeResultVO.setExecuteType(2);
                executeResultVO.setExecuteState(true);
                executeResultVO.setAffectedCount(affectedCount);
                if (dto.executeSql.toUpperCase().contains("CREATE")
                        || dto.executeSql.toUpperCase().contains("DROP")
                        || dto.executeSql.toUpperCase().contains("ALTER")
                        || dto.executeSql.toUpperCase().contains("TRUNCATE")
                        || dto.executeSql.toUpperCase().contains("RENAME")
                ) {
                    executeResultVO.setExecuteType(3);
                    if (affectedCount == -1) {
                        // 为-1时表示失败
                        executeResultVO.setExecuteState(false);
                    }
                }
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
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
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
                        List<TablePyhNameDTO> tableNameAndColumns = null;
                        if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.POSTGRESQL) {
                            // 表/字段结构
                            tableNameAndColumns = postgresConUtils.getTableNameAndColumns(postgreDTO.sqlUrl, postgreDTO.sqlUsername, postgreDTO.sqlPassword, DriverTypeEnum.POSTGRESQL);
                        } else if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.SQLSERVER) {
                            // 表/字段结构
                            tableNameAndColumns = sqlServerPlusUtils.getTableNameAndColumnsPlus(postgreDTO.sqlUrl, postgreDTO.sqlUsername, postgreDTO.sqlPassword, postgreDTO.dbName);
                        }
                        if (CollectionUtils.isNotEmpty(tableNameAndColumns)) {
                            tableNameAndColumns.sort(Comparator.comparing(TablePyhNameDTO::getTableName));
                            for (TablePyhNameDTO tablePyhNameDTO : tableNameAndColumns) {
                                DataOpsDataTableVO dataOpsDataTableVO = new DataOpsDataTableVO();
                                if (CollectionUtils.isNotEmpty(tablePyhNameDTO.getFields())) {
                                    List<DataOpsTableFieldVO> fieldVOList = new ArrayList<>();
                                    for (TableStructureDTO tableStructureDTO : tablePyhNameDTO.getFields()) {
                                        DataOpsTableFieldVO dataOpsTableFieldVO = new DataOpsTableFieldVO();
                                        dataOpsTableFieldVO.setFieldName(tableStructureDTO.getFieldName());
                                        dataOpsTableFieldVO.setFieldType(tableStructureDTO.getFieldType());
                                        dataOpsTableFieldVO.setFieldLength(tableStructureDTO.getFieldLength());
                                        dataOpsTableFieldVO.setFieldDes(tableStructureDTO.getFieldDes());
                                        fieldVOList.add(dataOpsTableFieldVO);
                                    }
                                    dataOpsDataTableVO.setChildren(fieldVOList);
                                }
                                dataOpsDataTableVO.setTableName(tablePyhNameDTO.getTableName());
                                dataOpsDataTableVOList.add(dataOpsDataTableVO);
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
                redisTemplate.opsForValue().set(metaDataEntityKey, dataOpsSourceJson);
                log.info("setDataOpsDataSource 元数据信息已写入redis");
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
        List<PostgreDTO> dataBaseList = new ArrayList<>();
        // 数据源基础信息
        ResultEntity<List<DataSourceDTO>> fiDataDataSourceResult = userClient.getAllFiDataDataSource();
        final List<DataSourceDTO> fiDataDataSources = fiDataDataSourceResult != null && fiDataDataSourceResult.getCode() == 0
                ? userClient.getAllFiDataDataSource().getData() : null;
        if (CollectionUtils.isEmpty(fiDataDataSources)) {
            return dataBaseList;
        }
        // 配置的数据源ID
        List<Integer> datasourceId = new ArrayList<>();
        datasourceId.add(dwId);
        datasourceId.add(odsId);

        datasourceId.forEach(t -> {
            DataSourceDTO dataSourceDTO = fiDataDataSources.stream().filter(item -> item.getId() == t).findFirst().orElse(null);
            if (dataSourceDTO != null) {
                PostgreDTO dataBaseInfo = new PostgreDTO();
                dataBaseInfo.setId(dataSourceDTO.getId());
                dataBaseInfo.setPort(dataSourceDTO.getConPort());
                dataBaseInfo.setIp(dataSourceDTO.getConIp());
                dataBaseInfo.setDbName(dataSourceDTO.getConDbname());
                dataBaseInfo.setDataSourceTypeEnum(DataSourceTypeEnum.getEnum(dataSourceDTO.getConType().getValue()));
                dataBaseInfo.setSqlUrl(dataSourceDTO.getConStr());
                dataBaseInfo.setSqlUsername(dataSourceDTO.getConAccount());
                dataBaseInfo.setSqlPassword(dataSourceDTO.getConPassword());
                dataBaseList.add(dataBaseInfo);
            }
        });
        return dataBaseList;
    }

    public static Connection getConnection(String driver, String url, String username, String password) {
        Connection conn = null;
        try {
            // 加载驱动类
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("找不到数据库驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            System.out.println("数据库连接失败！");
            throw new FkException(ResultEnum.PG_CONNECT_ERROR);
        }
        return conn;
    }
}
