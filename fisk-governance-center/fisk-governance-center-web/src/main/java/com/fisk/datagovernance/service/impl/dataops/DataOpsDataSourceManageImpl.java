package com.fisk.datagovernance.service.impl.dataops;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.PostgreDTO;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import com.fisk.datagovernance.service.dataops.IDataOpsDataSourceManageService;
import com.fisk.datagovernance.vo.dataops.DataOpsDataBaseVO;
import com.fisk.datagovernance.vo.dataops.DataOpsSourceVO;
import com.fisk.datagovernance.vo.dataops.ExecuteResultVO;
import com.fisk.datagovernance.vo.dataops.DataOpsTableFieldVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataBaseSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
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

    @Resource
    private DataOpsLogManageImpl dataOpsLogManageImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private UserClient userClient;

    @Override
    public ResultEntity<List<DataOpsSourceVO>> getDataOpsTableAll() {
        List<DataOpsSourceVO> dataOpsSourceVOList = new ArrayList<>();
        // 第一步：读取配置的数据源信息
        List<PostgreDTO> postgreDTOList = getPostgreDTOList();
        if (CollectionUtils.isEmpty(postgreDTOList)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, dataOpsSourceVOList);
        }
        // 第二步：读取数据源下的库、表信息
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        // 实例信息
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
                        List<String> tableList = postgresConUtils.getTableList(postgreDTO.getPgsqlUrl(), postgreDTO.getPgsqlUsername(),
                                postgreDTO.getPgsqlPassword(), postgreDTO.getDataSourceTypeEnum().getDriverName());
                        DataOpsDataBaseVO dataOpsDataBaseVO = new DataOpsDataBaseVO();
                        dataOpsDataBaseVO.setId(postgreDTO.getId());
                        dataOpsDataBaseVO.setConDbname(postgreDTO.getDbName());
                        dataOpsDataBaseVO.setChildren(tableList);
                        dataOpsDataBaseVOS.add(dataOpsDataBaseVO);
                    }
                }
                dataOpsSourceVO.setChildren(dataOpsDataBaseVOS);
                dataOpsSourceVOList.add(dataOpsSourceVO);
            }
        } catch (Exception ex) {
            log.error("getDataOpsTableAll执行异常：", ex);
            throw new FkException(ResultEnum.PG_READ_TABLE_ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataOpsSourceVOList);
    }

    @Override
    public ResultEntity<List<DataOpsTableFieldVO>> getDataOpsTableFieldAll(int datasourceId, String tableName) {
        List<DataOpsTableFieldVO> dataOpsTableFieldVOList = new ArrayList<>();
        if (datasourceId==0 || tableName==null || tableName.isEmpty()){
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, dataOpsTableFieldVOList);
        }
        // 第一步：读取配置的数据源信息
        List<PostgreDTO> postgreDTOList = getPostgreDTOList();
        if (CollectionUtils.isEmpty(postgreDTOList)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, dataOpsTableFieldVOList);
        }
        PostgreDTO postgreDTO = postgreDTOList.stream().filter(t -> t.getId() == datasourceId).findFirst().orElse(null);
        if (postgreDTO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, dataOpsTableFieldVOList);
        }
        // 第二步：读取表的所有字段信息
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        // 实例信息
        try {
            List<TableStructureDTO> tableColumnList = postgresConUtils.getTableColumnList(postgreDTO.getPgsqlUrl(), postgreDTO.getPgsqlUsername(),
                    postgreDTO.getPgsqlPassword(), postgreDTO.getDataSourceTypeEnum().getDriverName(), tableName);
            if (CollectionUtils.isNotEmpty(tableColumnList)) {
                for (TableStructureDTO tableStructureDTO : tableColumnList) {
                    DataOpsTableFieldVO tableFieldVO = new DataOpsTableFieldVO();
                    tableFieldVO.setFieldName(tableStructureDTO.getFieldName());
                    tableFieldVO.setFieldType(tableStructureDTO.getFieldType());
                    tableFieldVO.setFieldLength(tableStructureDTO.getFieldLength());
                    tableFieldVO.setFieldDes(tableStructureDTO.getFieldDes());
                    dataOpsTableFieldVOList.add(tableFieldVO);
                }
            }
        } catch (Exception ex) {
            log.error("getDataOpsTableFieldAll执行异常：", ex);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataOpsTableFieldVOList);
    }

    @Override
    public ResultEntity<ExecuteResultVO> executeDataOpsSql(ExecuteDataOpsSqlDTO dto) {
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
            boolean execute = st.execute(dto.executeSql);
            if (execute) {
                executeResultVO.query = true;
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
                    executeResultVO.setDataArray(array);
                }

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
                executeResultVO.query = false;
                affectedCount = st.getUpdateCount();
                executeResultVO.setAffectedCount(affectedCount);
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
                dataOpsLogPO.setExecuteSql(dto.executeSql);
                dataOpsLogPO.setExecuteResult(executeResult.getCode());
                dataOpsLogPO.setExecuteMsg(executeMsg);
                List<Long> userIds = new ArrayList<>();
                userIds.add(userHelper.getLoginUserInfo().getId());
                ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
                if (userListByIds != null && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                    dataOpsLogPO.setExecuteUser(userListByIds.getData().get(0).getUserAccount());
                }
                dataOpsLogManageImpl.saveLog(dataOpsLogPO);
            } catch (Exception ex) {
                log.error("executeDataOpsSql日志保存失败：", ex);
                throw new FkException(ResultEnum.DATA_OPS_CREATELOG_ERROR, ex.getMessage());
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, executeResultVO);
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
//        PostgreDTO postgreDTO_dw = new PostgreDTO();
//        postgreDTO_dw.setId(pgsqlDwId);
//        postgreDTO_dw.setPort(pgsqlDwPort);
//        postgreDTO_dw.setIp(pgsqlDwIp);
//        postgreDTO_dw.setDbName(pgsqlDwDbName);
//        postgreDTO_dw.setDataSourceTypeEnum(DataSourceTypeEnum.getEnumByDriverName(pgsqlDwDriverClassName));
//        postgreDTO_dw.setPgsqlUrl(pgsqlDwUrl);
//        postgreDTO_dw.setPgsqlUsername(pgsqlDwUsername);
//        postgreDTO_dw.setPgsqlPassword(pgsqlDwPassword);
//        postgreDTOList.add(postgreDTO_dw);
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
