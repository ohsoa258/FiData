package com.fisk.datagovernance.service.impl.dataops;

import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.Excel.ExcelDto;
import com.fisk.common.core.utils.Dto.Excel.RowDto;
import com.fisk.common.core.utils.Dto.Excel.SheetDto;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelTableDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.governance.BuildGovernanceHelper;
import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.DorisConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.dataops.TableInfoDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.GetDataOpsFieldSourceDTO;
import com.fisk.datagovernance.dto.dataops.PostgreDTO;
import com.fisk.datagovernance.dto.dataops.TableDataSyncDTO;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;
import com.fisk.datagovernance.enums.dataquality.AttachmentCateGoryEnum;
import com.fisk.datagovernance.mapper.dataquality.AttachmentInfoMapper;
import com.fisk.datagovernance.service.dataops.IDataOpsDataSourceManageService;
import com.fisk.datagovernance.service.impl.dataquality.DataSourceConManageImpl;
import com.fisk.datagovernance.vo.dataops.*;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;
import com.fisk.mdm.client.MdmClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasecurity.DataSecurityColumnsDTO;
import com.fisk.system.dto.datasecurity.DataSecurityRowsDTO;
import com.fisk.system.dto.datasecurity.DataSecurityTablesDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildTableNifiSettingDTO;
import com.fisk.task.dto.task.TableNifiSettingDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
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

    @Value("${database.mdm_id}")
    private int mdmId;
    @Value("${dataops.metadataentity_key}")
    private String metaDataEntityKey;
    //@Value("${file.excelFilePath}")
    private String excelFilePath;

    @Resource
    private DataOpsLogManageImpl dataOpsLogManageImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserClient userClient;

    @Resource
    private DataModelClient dataModelClient;

    @Resource
    private DataAccessClient dataAccessClient;
    @Resource
    private MdmClient mdmClient;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Value("${dataSecurity}")
    private Boolean dataSecurity;

    @Override
    public ResultEntity<List<DataOpsSourceVO>> getDataOpsTableSource() {
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
    public ResultEntity<List<DataOpsTableFieldVO>> getDataOpsFieldSource(GetDataOpsFieldSourceDTO dto) {
        try {
            List<DataOpsTableFieldVO> dataOpsTableFieldVOS = new ArrayList<>();
            if (dto == null || dto.getDatasourceId() == 0 || StringUtils.isEmpty(dto.getTableName())) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, dataOpsTableFieldVOS);
            }
            List<PostgreDTO> postgreDTOList = getPostgreDTOList();
            if (CollectionUtils.isEmpty(postgreDTOList)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, dataOpsTableFieldVOS);
            }
            PostgreDTO postgreDTO = postgreDTOList.stream().filter(t -> t.getId() == dto.getDatasourceId()).findFirst().orElse(null);
            if (postgreDTO == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, dataOpsTableFieldVOS);
            }
            PostgresConUtils postgresConUtils = new PostgresConUtils();
            SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
            DorisConUtils dorisConUtils = new DorisConUtils();
            Connection connection = DataSourceConManageImpl.getStatement(postgreDTO.getDataSourceTypeEnum(), postgreDTO.getSqlUrl(), postgreDTO.getSqlUsername(), postgreDTO.getSqlPassword());

            List<TableStructureDTO> columns = null;
            if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.POSTGRESQL) {
                String tableFullName = StringUtils.isEmpty(dto.getTableFramework()) ? dto.getTableName() : dto.getTableFramework() + "." + dto.getTableName();
                columns = postgresConUtils.getColumns(connection, tableFullName);
            } else if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.SQLSERVER) {
                columns = sqlServerPlusUtils.getColumns(connection, dto.getTableName(), dto.getTableFramework());
            } else if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.DORIS) {
                //doris就去查询所选目录下的数据库
                Statement statement = connection.createStatement();
                columns = dorisConUtils.getCatalogNameAndTblName(statement, dto.getTableName());
            }
            if (CollectionUtils.isEmpty(columns)) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataOpsTableFieldVOS);
            }

            //数据安全  根据角色的数据安全配置 筛选数据库运维可回显的表
            if (dataSecurity) {
                List<TableStructureDTO> columns1 = new ArrayList<>();

                //筛选数据安全-列级别安全配置了该表的哪些可见字段
                List<String> columnNames = filterColumnsBiDataSecurity(postgreDTO.getDataSourceTypeEnum(), dto.getTableFramework() + "." + dto.getTableName(), dto.datasourceId);
                if (CollectionUtils.isEmpty(columnNames)) {
                    columns1 = columns;
                } else {
                    for (TableStructureDTO column : columns) {
                        if (columnNames.contains(column.getFieldName())) {
                            columns1.add(column);
                        }
                    }
                }

                columns1.forEach(tableStructureDTO -> {
                    DataOpsTableFieldVO dataOpsTableFieldVO = new DataOpsTableFieldVO();
                    dataOpsTableFieldVO.setFieldName(tableStructureDTO.getFieldName());
                    dataOpsTableFieldVO.setFieldType(tableStructureDTO.getFieldType());
                    dataOpsTableFieldVO.setFieldLength(tableStructureDTO.getFieldLength());
                    dataOpsTableFieldVO.setFieldDes(tableStructureDTO.getFieldDes());
                    dataOpsTableFieldVO.setDorisTblNames(tableStructureDTO.getDorisTblNames());
                    dataOpsTableFieldVOS.add(dataOpsTableFieldVO);
                });
            } else {
                columns.forEach(tableStructureDTO -> {
                    DataOpsTableFieldVO dataOpsTableFieldVO = new DataOpsTableFieldVO();
                    dataOpsTableFieldVO.setFieldName(tableStructureDTO.getFieldName());
                    dataOpsTableFieldVO.setFieldType(tableStructureDTO.getFieldType());
                    dataOpsTableFieldVO.setFieldLength(tableStructureDTO.getFieldLength());
                    dataOpsTableFieldVO.setFieldDes(tableStructureDTO.getFieldDes());
                    dataOpsTableFieldVO.setDorisTblNames(tableStructureDTO.getDorisTblNames());
                    dataOpsTableFieldVOS.add(dataOpsTableFieldVO);
                });
            }

            return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataOpsTableFieldVOS);
        } catch (Exception e) {
            log.error("数据库运维获取数据库表列信息失败：" + e);
            return ResultEntityBuild.build(ResultEnum.DATA_OPS_GET_TABLE_SCHEMA_ERROR);
        }

    }

    /**
     * 根据数据安全 筛选表名
     *
     * @return
     */
    private List<String> filterColumnsBiDataSecurity(DataSourceTypeEnum conType, String tableName, Integer datasourceId) {

        Map<String, Integer> nameAndId = new HashMap<>();
        List<String> colNames = new ArrayList<>();

        List<AccessAndModelAppDTO> data1 = new ArrayList<>();
        //获取数据接入 应用和应用下的表
        ResultEntity<List<AccessAndModelAppDTO>> result1 = dataAccessClient.getAllAppAndTables();
        if (result1.getCode() == ResultEnum.SUCCESS.getCode()) {
            data1 = result1.getData();

        }

        //拿到用户角色
        UserInfo user = userHelper.getLoginUserInfo();
        int id = user.getId().intValue();

        RoleInfoDTO roleInfoDTO = null;

        ResultEntity<List<RoleInfoDTO>> resultEntity = userClient.getRolebyUserId(id);
        if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
            List<RoleInfoDTO> roleInfos = resultEntity.getData();
            if (CollectionUtils.isNotEmpty(roleInfos)) {
                roleInfoDTO = roleInfos.get(0);
            }

        }

        if (roleInfoDTO != null) {
            //获取当前登录用户的角色id
            int roleId = (int) roleInfoDTO.getId();
            // 获取当前角色的列级安全权限
            ResultEntity<List<DataSecurityColumnsDTO>> resultEntity1 = userClient.getColumnsByRoleId(roleId);

            if (resultEntity1.getCode() == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(resultEntity1.getData())) {
                List<DataSecurityColumnsDTO> dataSecurityColumnsDTOS = resultEntity1.getData();

                List<Integer> tblIds = dataSecurityColumnsDTOS.stream().map(DataSecurityColumnsDTO::getTblId).collect(Collectors.toList());
                for (AccessAndModelAppDTO accessAndModelAppDTO : data1) {
                    for (AccessAndModelTableDTO table : accessAndModelAppDTO.getTables()) {
                        if (tblIds.contains(table.getTblId())) {
                            String tblName;
                            if (accessAndModelAppDTO.whetherSchema) {
                                tblName = accessAndModelAppDTO.getAppAbbreviation() + "." + table.getTableName();
                            } else {
                                String preNmae = "";
                                if (conType == DataSourceTypeEnum.SQLSERVER) {
                                    preNmae = "dbo.";
                                } else if (conType == DataSourceTypeEnum.POSTGRESQL) {
                                    preNmae = "public.";
                                }
                                tblName = preNmae + "ods_" + accessAndModelAppDTO.getAppAbbreviation() + "_" + table.getTableName();
                            }
                            nameAndId.put(tblName, table.getTblId());
                        }
                    }
                }

                //获取
                if (CollectionUtils.isNotEmpty(nameAndId)) {
                    //获取表id
                    int tblId = nameAndId.get(tableName);

                    //通过这个表id 获取对应列级安全权限hang行  从而获取hang行内配置的可查询字段id
                    List<Integer> readableFieldIds = dataSecurityColumnsDTOS.stream()
                            .filter(dto -> dto.getTblId() == tblId)
                            .findFirst()
                            .map(DataSecurityColumnsDTO::getReadableFieldIds)
                            .orElse(Collections.emptyList());

                    //将获取到的这些字段id转为字段名称 最终返回
                    if (CollectionUtils.isNotEmpty(readableFieldIds)) {
                        //2 意味着是dmp_ods
                        if (datasourceId == 2) {
                            ResultEntity<List<TableFieldsDTO>> resultEntity2 = dataAccessClient.getFieldInfosByIds(readableFieldIds);
                            if (resultEntity2.getCode() == ResultEnum.SUCCESS.getCode()) {
                                List<TableFieldsDTO> data = resultEntity2.getData();
                                data.stream().map(TableFieldsDTO::getFieldName).forEach(colNames::add);
                            }
                        }
                    }
                }
            }
        }

        return colNames;
    }


    @Override
    public ResultEntity<Object> reloadDataOpsDataSource() {
        setMetaDataToRedis();
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
            if (StringUtils.isEmpty(dto.executeSql)) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, executeResultVO);
            }
            conn = DataSourceConManageImpl.getStatement(postgreDTO.getDataSourceTypeEnum(),
                    postgreDTO.getSqlUrl(), postgreDTO.getSqlUsername(), postgreDTO.getSqlPassword());
            st = conn.createStatement();
            assert st != null;
           /*
           * QL语言包括四种主要程序设计语言类别的语句：
            数据定义语言(DDL)，数据操作语言(DML)，数据控制语言(DCL)和事务控制语言（TCL）。
            主要的DDL动词：CREATE（创建）、DROP（删除）、ALTER（修改）、TRUNCATE（截断）、RENAME（重命名）
            DML主要指数据的增删查改: SELECT、DELETE、UPDATE、INSERT、CALL(执行存储过程)
           * */
            String sql = dto.executeSql;
            DataSourceTypeEnum dataSourceTypeEnum = postgreDTO.getDataSourceTypeEnum();

            //如果开启数据安全 则查询该表是否有配置行级安全 如果有 则需要添加行级安全的where条件到sql里面
            if (dataSecurity) {
                String tblFullName = dto.getTblFullName();

                //获取该表在数据安全配置的where条件
                String whereSql = filterRowsBiDataSecurity(dataSourceTypeEnum, tblFullName).toLowerCase();
                if (StringUtils.isNotEmpty(whereSql)) {
                    //如果原生sql包含where条件 则去掉行级安全权限的过滤条件中的where并替换为and
                    if (sql.toLowerCase().contains("where")) {
                        whereSql = whereSql.replace("where", " and ");
                        sql = sql + whereSql;
                    } else {
                        sql = sql + " " + whereSql;
                    }
                }
                log.info("数据安全拼接的sql" + sql);
            }


            //doris走mysql协议
            if (DataSourceTypeEnum.DORIS.getName().equals(dataSourceTypeEnum.getName())) {
                postgreDTO.dataSourceTypeEnum = DataSourceTypeEnum.MYSQL;
            }

            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(
                    sql, postgreDTO.dataSourceTypeEnum.getName().toLowerCase());
            if (Token.SELECT.equals(parser.getExprParser().getLexer().token())) {
                IBuildGovernanceSqlCommand dbCommand = BuildGovernanceHelper.getDBCommand(postgreDTO.getDataSourceTypeEnum());

                //前端需要返回一个数据总数 count sql
                String countSqlName = String.format("(%s) AS tb_count", sql);
                String countSql = dbCommand.buildCountSql(countSqlName);

                //先执行count sql 查询此次查询语句的数据总数
                try {
                    log.info("数据库运维查询原sql语句数据总数执行的sql:" + countSql);
                    boolean execute = st.execute(countSql);
                    if (execute) {
                        ResultSet rs = st.getResultSet();
                        while (rs.next()) {
                            int dataCount = rs.getInt("dataCount");
                            log.info("原sql语句数据总数：" + dataCount);
                            executeResultVO.setTotal(dataCount);
                        }
                    }
                } catch (Exception e) {
                    log.error("数据库运维查询原sql语句数据总数报错，原因：" + e);
                }

                //分页sql
                String orderByClause = "";
                if (sql.contains("order by")) {
                    int orderByIndex = sql.indexOf("order by");
                    if (orderByIndex != -1) {
                        orderByClause = sql.substring(orderByIndex + 9).trim();
                    }
                }
                // SQL 语句是查询操作
                String tableName = String.format("(%s) AS tb_page", sql);
                dto.current = dto.current - 1;
                String buildQuerySchemaSql = dbCommand.buildPagingSql(tableName, "*", orderByClause, dto.current, dto.size);
                dto.executeSql = buildQuerySchemaSql;
            }
            log.info("数据库运维执行的sql语句：" + dto.executeSql);
            boolean execute = st.execute(dto.executeSql);
            log.info("数据库运维执行的结果：" + execute);
            if (execute) {
                executeResultVO.setExecuteType(1);
                ResultSet rs = st.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                log.info("数据库运维查询到的数据行数：" + columnCount);
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
//                    if (dto.current != null && dto.size != null) {
//                        int rowsCount = array.stream().toArray().length;
//                        executeResultVO.setCurrent(dto.current);
//                        executeResultVO.setSize(dto.size);
//                        executeResultVO.setTotal(rowsCount);
//                        executeResultVO.setPage((int) Math.ceil(1.0 * rowsCount / dto.size));
//                        dto.current = dto.current - 1;
//                        collect = array.stream().skip((dto.current - 1 + 1) * dto.size).limit(dto.size).collect(Collectors.toList());
//                    }
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
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
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
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, executeResultVO);
    }

    /**
     * 根据数据安全 筛选表名
     *
     * @return
     */
    private String filterRowsBiDataSecurity(DataSourceTypeEnum conType, String tblFullName) {

        Map<String, Integer> nameAndId = new HashMap<>();
        String whereCondition = "";

        List<AccessAndModelAppDTO> data1 = new ArrayList<>();
        //获取数据接入 应用和应用下的表
        ResultEntity<List<AccessAndModelAppDTO>> result1 = dataAccessClient.getAllAppAndTables();
        if (result1.getCode() == ResultEnum.SUCCESS.getCode()) {
            data1 = result1.getData();

        }

        //拿到用户角色
        UserInfo user = userHelper.getLoginUserInfo();
        int id = user.getId().intValue();

        RoleInfoDTO roleInfoDTO = null;

        ResultEntity<List<RoleInfoDTO>> resultEntity = userClient.getRolebyUserId(id);
        if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
            List<RoleInfoDTO> roleInfos = resultEntity.getData();
            if (CollectionUtils.isNotEmpty(roleInfos)) {
                roleInfoDTO = roleInfos.get(0);
            }

        }

        if (roleInfoDTO != null) {
            //获取当前登录用户的角色id
            int roleId = (int) roleInfoDTO.getId();
            // 获取当前角色的表级安全权限
            ResultEntity<List<DataSecurityRowsDTO>> resultEntity1 = userClient.getRowsByRoleId(roleId);
            if (resultEntity1.getCode() == ResultEnum.SUCCESS.getCode()) {
                List<DataSecurityRowsDTO> dataSecurityRowsDTOS = resultEntity1.getData();

                List<Integer> tblIds = dataSecurityRowsDTOS.stream().map(DataSecurityRowsDTO::getTblId).collect(Collectors.toList());
                for (AccessAndModelAppDTO accessAndModelAppDTO : data1) {
                    for (AccessAndModelTableDTO table : accessAndModelAppDTO.getTables()) {
                        if (tblIds.contains(table.getTblId())) {
                            String tblName;
                            if (accessAndModelAppDTO.whetherSchema) {
                                tblName = accessAndModelAppDTO.getAppAbbreviation() + "." + table.getTableName();
                            } else {
                                String preNmae = "";
                                if (conType == DataSourceTypeEnum.SQLSERVER) {
                                    preNmae = "dbo.";
                                } else if (conType == DataSourceTypeEnum.POSTGRESQL) {
                                    preNmae = "public.";
                                }
                                tblName = preNmae + "ods_" + accessAndModelAppDTO.getAppAbbreviation() + "_" + table.getTableName();
                            }
                            nameAndId.put(tblName, table.getTblId());
                        }
                    }
                }

                //获取where条件
                if (CollectionUtils.isNotEmpty(nameAndId) && tblFullName != null) {
                    //获取表id
                    int tblId = nameAndId.get(tblFullName);
                    whereCondition = dataSecurityRowsDTOS.stream()
                            .filter(dto -> dto.getTblId() == tblId)
                            .findFirst()
                            .map(DataSecurityRowsDTO::getWhereCondition)
                            .orElse(""); // 或者其它默认值
                }
            }
        }
        return whereCondition;
    }

    @Override
    public ResultEnum tableDataSync(TableDataSyncDTO dto) {
        if (dto == null || dto.getDatasourceId() == 0 || StringUtils.isEmpty(dto.getTableFullName())) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {
            log.info("【tableDataSync】请求参数：" + JSON.toJSONString(dto));
            TableInfoDTO tableInfoDTO = new TableInfoDTO();
            if (dto.getDatasourceId() == 1) {
                // 调用数据建模接口获取表信息
                ResultEntity<DataModelTableInfoDTO> tableInfo = dataModelClient.getTableInfo(dto.getTableFullName());
                if (tableInfo != null
                        && tableInfo.getCode() == ResultEnum.SUCCESS.getCode()
                        && tableInfo.getData() != null) {
                    tableInfoDTO.setTableAccessId(tableInfo.getData().getTableId());
                    tableInfoDTO.setAppId(tableInfo.getData().getBusinessAreaId());
                    tableInfoDTO.setTableName(tableInfo.getData().getTableName());
                    tableInfoDTO.setOlapTable(tableInfo.getData().getOlapTable());
                }
            } else if (dto.getDatasourceId() == 2) {
                // 调用数据接入接口获取表信息
                ResultEntity<TableInfoDTO> tableInfo = dataAccessClient.getTableInfo(dto.getTableFullName());
                if (tableInfo != null
                        && tableInfo.getCode() == ResultEnum.SUCCESS.getCode()
                        && tableInfo.getData() != null) {
                    tableInfoDTO = tableInfo.getData();
                }
            } else if (dto.getDatasourceId() == 3) {
                // 调用数据接入接口获取表信息
                ResultEntity<com.fisk.mdm.dto.dataops.TableInfoDTO> tableInfo = mdmClient.getTableInfo(dto.getTableFullName());
                if (tableInfo != null
                        && tableInfo.getCode() == ResultEnum.SUCCESS.getCode()
                        && tableInfo.getData() != null) {
                    com.fisk.mdm.dto.dataops.TableInfoDTO data = tableInfo.getData();
                    tableInfoDTO.tableAccessId = data.tableAccessId;
                    tableInfoDTO.appId = data.appId;
                    tableInfoDTO.tableName = data.tableName;
                    tableInfoDTO.olapTable = data.olapTable;
                }
            }
            if (tableInfoDTO == null || StringUtils.isEmpty(tableInfoDTO.getTableName())) {
                return ResultEnum.DATAACCESS_GETTABLE_ERROR;
            }
            log.info("【tableDataSync】查询表信息返回数据：" + JSON.toJSONString(tableInfoDTO));
            BuildTableNifiSettingDTO buildTableNifiSetting = new BuildTableNifiSettingDTO();
            buildTableNifiSetting.setUserId(userHelper.getLoginUserInfo().getId());
            List<TableNifiSettingDTO> tableNifiSettings = new ArrayList<>();
            TableNifiSettingDTO tableNifiSetting = new TableNifiSettingDTO();
            tableNifiSetting.setTableName(dto.getTableFullName());
            tableNifiSetting.setTableAccessId(tableInfoDTO.getTableAccessId());
            tableNifiSetting.setAppId(tableInfoDTO.getAppId());
            tableNifiSetting.setType(tableInfoDTO.getOlapTable());
            tableNifiSettings.add(tableNifiSetting);
            buildTableNifiSetting.setTableNifiSettings(tableNifiSettings);
            log.info("【tableDataSync】调用nifi同步表数据请求参数：" + JSON.toJSONString(buildTableNifiSetting));
            ResultEntity<Object> result = publishTaskClient.immediatelyStart(buildTableNifiSetting);
            if (result != null && result.getCode() == ResultEnum.SUCCESS.getCode()) {
                return ResultEnum.SUCCESS;
            }
        } catch (Exception ex) {
            log.error("【tableDataSync】执行异常：" + ex);
            return ResultEnum.ERROR;
        }
        return ResultEnum.TABLE_DATA_SYNC_FAIL;
    }

    /**
     * 数仓建模-表数据同步
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum tableDataSyncForModel(TableDataSyncDTO dto) {
        if (dto == null || dto.getDatasourceId() == 0 || StringUtils.isEmpty(dto.getTableFullName())) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {
            log.info("【tableDataSync】请求参数：" + JSON.toJSONString(dto));
            TableInfoDTO tableInfoDTO = new TableInfoDTO();
            if (dto.getDatasourceId() == 1) {
                // 调用数据建模接口获取表信息
                ResultEntity<DataModelTableInfoDTO> tableInfo = dataModelClient.getTableInfo(dto.getTableFullName());
                if (tableInfo != null
                        && tableInfo.getCode() == ResultEnum.SUCCESS.getCode()
                        && tableInfo.getData() != null) {
                    tableInfoDTO.setTableAccessId(tableInfo.getData().getTableId());
                    tableInfoDTO.setAppId(tableInfo.getData().getBusinessAreaId());
                    tableInfoDTO.setTableName(tableInfo.getData().getTableName());
                    tableInfoDTO.setOlapTable(tableInfo.getData().getOlapTable());
                }
            } else if (dto.getDatasourceId() == 2) {
                // 调用数据接入接口获取表信息
                ResultEntity<TableInfoDTO> tableInfo = dataAccessClient.getTableInfo(dto.getTableFullName());
                if (tableInfo != null
                        && tableInfo.getCode() == ResultEnum.SUCCESS.getCode()
                        && tableInfo.getData() != null) {
                    tableInfoDTO = tableInfo.getData();
                }
            } else if (dto.getDatasourceId() == 3) {
                // 调用数据接入接口获取表信息
                ResultEntity<com.fisk.mdm.dto.dataops.TableInfoDTO> tableInfo = mdmClient.getTableInfo(dto.getTableFullName());
                if (tableInfo != null
                        && tableInfo.getCode() == ResultEnum.SUCCESS.getCode()
                        && tableInfo.getData() != null) {
                    com.fisk.mdm.dto.dataops.TableInfoDTO data = tableInfo.getData();
                    tableInfoDTO.tableAccessId = data.tableAccessId;
                    tableInfoDTO.appId = data.appId;
                    tableInfoDTO.tableName = data.tableName;
                    tableInfoDTO.olapTable = data.olapTable;
                }
            }
            if (tableInfoDTO == null || StringUtils.isEmpty(tableInfoDTO.getTableName())) {
                return ResultEnum.DATAACCESS_GETTABLE_ERROR;
            }
            log.info("【tableDataSync】查询表信息返回数据：" + JSON.toJSONString(tableInfoDTO));
            BuildTableNifiSettingDTO buildTableNifiSetting = new BuildTableNifiSettingDTO();
//            buildTableNifiSetting.setUserId(userHelper.getLoginUserInfo().getId());
            List<TableNifiSettingDTO> tableNifiSettings = new ArrayList<>();
            TableNifiSettingDTO tableNifiSetting = new TableNifiSettingDTO();
            tableNifiSetting.setTableName(dto.getTableFullName());
            tableNifiSetting.setTableAccessId(tableInfoDTO.getTableAccessId());
            tableNifiSetting.setAppId(tableInfoDTO.getAppId());
            tableNifiSetting.setType(tableInfoDTO.getOlapTable());
            tableNifiSettings.add(tableNifiSetting);
            buildTableNifiSetting.setTableNifiSettings(tableNifiSettings);
            log.info("【tableDataSync】调用nifi同步表数据请求参数：" + JSON.toJSONString(buildTableNifiSetting));
            ResultEntity<Object> result = publishTaskClient.immediatelyStart(buildTableNifiSetting);
            if (result != null && result.getCode() == ResultEnum.SUCCESS.getCode()) {
                return ResultEnum.SUCCESS;
            }
        } catch (Exception ex) {
            log.error("【tableDataSync】执行异常：" + ex);
            return ResultEnum.ERROR;
        }
        return ResultEnum.TABLE_DATA_SYNC_FAIL;
    }


    public String createTableStructureTemplate(GetDataOpsFieldSourceDTO dto) {
        // 第一步：获取表字段
        ResultEntity<List<DataOpsTableFieldVO>> dataOpsFieldSource = getDataOpsFieldSource(dto);
        List<String> fieldNameList = new ArrayList<>();
        if (dataOpsFieldSource != null && CollectionUtils.isNotEmpty(dataOpsFieldSource.getData())) {
            dataOpsFieldSource.getData().forEach(t -> {
                fieldNameList.add(t.getFieldName());
            });
        }
        if (CollectionUtils.isEmpty(fieldNameList)) {
            return "";
        }

        // 第二步：生成Excel
        List<SheetDto> sheetList = new ArrayList<>();
        SheetDto sheet = new SheetDto();
        String sheetName = "";
        if (StringUtils.isNotEmpty(dto.getTableFramework())) {
            sheetName = dto.getTableFramework() + ".";
        }
        if (StringUtils.isNotEmpty(dto.getTableName())) {
            sheetName += dto.getTableName();
        }
        sheetName += "_" + dto.getDatasourceId();
        sheet.setSheetName(sheetName);
        List<RowDto> singRows = createTableStructureTemplate_GetSingRows(fieldNameList);
        sheet.setSingRows(singRows);
        sheetList.add(sheet);
        String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        String uploadUrl = excelFilePath + "dataOps_excelFile/";
        ExcelDto excelDto = new ExcelDto();
        excelDto.setExcelName(currentFileName);
        excelDto.setSheets(sheetList);
        ExcelReportUtil.createExcel(excelDto, uploadUrl, currentFileName, true);

        // 第三步：数据库记录Excel附件信息并返回附件Id用于下载附件
        AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
        attachmentInfoPO.setCurrentFileName(currentFileName);
        attachmentInfoPO.setExtensionName(".xlsx");
        attachmentInfoPO.setAbsolutePath(uploadUrl);
        attachmentInfoPO.setOriginalName(String.format("%s上传模板%s.xlsx", dto.getTableName(), DateTimeUtils.getNowToShortDate().replace("-", "")));
        attachmentInfoPO.setCategory(AttachmentCateGoryEnum.DATA_OPERATION_AND_MAINTENANCE_GENERATE_IMPORT_TEMPLATES.getValue());
        attachmentInfoMapper.insertOne(attachmentInfoPO);

        return attachmentInfoPO.getOriginalName() + "," + attachmentInfoPO.getId();
    }

    public void setMetaDataToRedis() {
        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(dwId);
        if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        DataSourceDTO data = result.getData();
        //如果是dw是Doris类型,则刷新所拥有的外部目录
        if (data.conType.getName().equals(DataSourceTypeEnum.DORIS.getName())) {
            Connection conn = null;
            Statement statement = null;
            ResultSet resultSet = null;
            ArrayList<String> cataLogNames = new ArrayList<>();
            try {
                conn = DataSourceConManageImpl.getStatement(data.conType, data.conStr, data.conAccount, data.conPassword);
                statement = conn.createStatement();
                resultSet = statement.executeQuery("SHOW CATALOGS");
                while (resultSet.next()) {
                    if (!"internal".equals(resultSet.getString("CatalogName"))) {
                        cataLogNames.add(resultSet.getString("CatalogName"));
                    }
                }

                log.info("开始刷新doris外部目录------------------------------");
                for (String cataLogName : cataLogNames) {
                    log.info("REFRESH CATALOG " + cataLogName);
                    statement.executeQuery("REFRESH CATALOG " + cataLogName);
                    Thread.sleep(1500);
                }
                log.info("doris外部目录刷新完毕------------------------------");
            } catch (Exception e) {
                log.error("刷新doris外部目录失败！");
            } finally {
                AbstractCommonDbHelper.closeResultSet(resultSet);
                AbstractCommonDbHelper.closeStatement(statement);
                AbstractCommonDbHelper.closeConnection(conn);
            }
        }


        log.info("setMetaDataToRedis-ops 开始");
        List<DataOpsSourceVO> dataOpsSourceVOList = new ArrayList<>();
        // 第一步：读取配置的数据源信息
        List<PostgreDTO> postgreDTOList = getPostgreDTOList();
        if (CollectionUtils.isEmpty(postgreDTOList)) {
            log.error("setMetaDataToRedis-ops 数据源配置不存在");
            return;
        }
        // 第二步：读取数据源下的库、表
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        DorisConUtils dorisConUtils = new DorisConUtils();
        try {
            Map<String, List<DataOpsDataBaseVO>> conIpToDataBaseMap = new HashMap<>();
            //根据ip和端口去重
            postgreDTOList = removeDuplicates(postgreDTOList);

            for (PostgreDTO postgreDTO : postgreDTOList) {
                int ifDoris = 0;
                List<DataOpsDataTableVO> tableVOList = new ArrayList<>();
                Connection connection = DataSourceConManageImpl.getStatement(postgreDTO.getDataSourceTypeEnum(), postgreDTO.getSqlUrl(), postgreDTO.getSqlUsername(), postgreDTO.getSqlPassword());
                List<TablePyhNameDTO> tablesPlus = null;
                if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.POSTGRESQL) {
                    tablesPlus = postgresConUtils.getTablesPlus(connection);
                } else if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.SQLSERVER) {
                    tablesPlus = sqlServerPlusUtils.getTablesPlus(connection);
                } else if (postgreDTO.getDataSourceTypeEnum() == DataSourceTypeEnum.DORIS) {
                    ifDoris += 1;
                    if (postgreDTO.getDbName().equals("dmp_ods")) {
                        continue;
                    }
                    tablesPlus = dorisConUtils.getTablesPlusForOps(connection);
                }


                //数据安全  根据角色的数据安全配置 筛选数据库运维可回显的表
                if (dataSecurity) {
                    List<TablePyhNameDTO> tablesPlus1 = new ArrayList<>();
                    //筛选数据安全配置了哪些可见表
                    List<String> tblNames = filterTablesBiDataSecurity(data.conType);

                    //数据安全 表级安全是反着来的  如果配的有 则只能看那些配的表 否则能看所有表
                    if (CollectionUtils.isNotEmpty(tablesPlus)) {
                        //如果没配置数据安全  则可以看所有表
                        if (CollectionUtils.isEmpty(tblNames)) {
                            tablesPlus1 = tablesPlus;
                        } else {
                            //如果配置的有 则只能看到那些配置的表
                            for (TablePyhNameDTO plus : tablesPlus) {
                                if (tblNames.contains(plus.getTableFullName())) {
                                    tablesPlus1.add(plus);
                                }
                            }
                        }
                    }

                    if (CollectionUtils.isNotEmpty(tablesPlus1)) {
                        tablesPlus1.forEach(t -> {
                            DataOpsDataTableVO dataOpsDataTableVO = new DataOpsDataTableVO();
                            dataOpsDataTableVO.setTableFramework(t.getTableFramework());
                            dataOpsDataTableVO.setTableName(t.getTableName());
                            dataOpsDataTableVO.setTableFullName(t.getTableFullName());
                            tableVOList.add(dataOpsDataTableVO);
                        });
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(tablesPlus)) {
                        tablesPlus.forEach(t -> {
                            DataOpsDataTableVO dataOpsDataTableVO = new DataOpsDataTableVO();
                            dataOpsDataTableVO.setTableFramework(t.getTableFramework());
                            dataOpsDataTableVO.setTableName(t.getTableName());
                            dataOpsDataTableVO.setTableFullName(t.getTableFullName());
                            tableVOList.add(dataOpsDataTableVO);
                        });
                    }
                }

//                        if (CollectionUtils.isNotEmpty(tableVOList)) {
//                            // 增加排序
//                            tableVOList.sort(Comparator.comparing(DataOpsDataTableVO::getTableName));
//                        }

                List<DataOpsDataBaseVO> dataOpsDataBaseVOS = conIpToDataBaseMap.get(postgreDTO.ip);
                if (CollectionUtils.isEmpty(dataOpsDataBaseVOS)) {
                    dataOpsDataBaseVOS = new ArrayList<>();
                }
                DataOpsDataBaseVO dataOpsDataBaseVO = new DataOpsDataBaseVO();
                dataOpsDataBaseVO.setDatasourceId(postgreDTO.getId());
                if (ifDoris > 0) {
                    dataOpsDataBaseVO.setConDbname("doris_catalogs");
                } else {
                    dataOpsDataBaseVO.setConDbname(postgreDTO.getDbName());
                }
                dataOpsDataBaseVO.setConType(postgreDTO.dataSourceTypeEnum);
                dataOpsDataBaseVO.setConPort(postgreDTO.port);
                dataOpsDataBaseVO.setChildren(tableVOList);
                dataOpsDataBaseVOS.add(dataOpsDataBaseVO);
                conIpToDataBaseMap.put(postgreDTO.ip, dataOpsDataBaseVOS);
                if (connection != null) {
                    connection.close();
                }

            }
            for (String conIp : conIpToDataBaseMap.keySet()) {
                DataOpsSourceVO dataOpsSourceVO = new DataOpsSourceVO();
                dataOpsSourceVO.setConIp(conIp);
                dataOpsSourceVO.setChildren(conIpToDataBaseMap.get(conIp));
                dataOpsSourceVOList.add(dataOpsSourceVO);
            }
            if (CollectionUtils.isNotEmpty(dataOpsSourceVOList)) {
                String dataOpsSourceJson = JSONArray.toJSON(dataOpsSourceVOList).toString();
                // 生成目录加 ：
                redisTemplate.opsForValue().set(metaDataEntityKey, dataOpsSourceJson);
                log.info("setMetaDataToRedis-ops 元数据信息已写入redis");
            }
        } catch (Exception ex) {
            log.error("setMetaDataToRedis-ops 执行异常：", ex);
        } finally {
            log.info("setMetaDataToRedis-ops 结束");
        }
    }

    /**
     * 根据数据安全 筛选表名
     *
     * @return
     */
    private List<String> filterTablesBiDataSecurity(DataSourceTypeEnum conType) {

        List<String> tblNames = new ArrayList<>();

        List<AccessAndModelAppDTO> data1 = new ArrayList<>();
        //获取数据接入 应用和应用下的表
        ResultEntity<List<AccessAndModelAppDTO>> result1 = dataAccessClient.getAllAppAndTables();
        if (result1.getCode() == ResultEnum.SUCCESS.getCode()) {
            data1 = result1.getData();

        }

        //拿到用户角色
        UserInfo user = userHelper.getLoginUserInfo();
        int id = user.getId().intValue();

        RoleInfoDTO roleInfoDTO = null;

        ResultEntity<List<RoleInfoDTO>> resultEntity = userClient.getRolebyUserId(id);
        if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
            List<RoleInfoDTO> roleInfos = resultEntity.getData();
            if (CollectionUtils.isNotEmpty(roleInfos)) {
                roleInfoDTO = roleInfos.get(0);
            }

        }

        if (roleInfoDTO != null) {
            //获取当前登录用户的角色id
            int roleId = (int) roleInfoDTO.getId();
            // 获取当前角色的表级安全权限
            ResultEntity<List<DataSecurityTablesDTO>> resultEntity1 = userClient.getTablesByRoleId(roleId);
            if (resultEntity1.getCode() == ResultEnum.SUCCESS.getCode()) {
                List<DataSecurityTablesDTO> dataSecurityTablesDTOS = resultEntity1.getData();

                List<Integer> tblIds = dataSecurityTablesDTOS.stream().map(DataSecurityTablesDTO::getTblId).collect(Collectors.toList());
                for (AccessAndModelAppDTO accessAndModelAppDTO : data1) {
                    for (AccessAndModelTableDTO table : accessAndModelAppDTO.getTables()) {
                        if (tblIds.contains(table.getTblId())) {
                            String tblName;
                            if (accessAndModelAppDTO.whetherSchema) {
                                tblName = accessAndModelAppDTO.getAppAbbreviation() + "." + table.getTableName();
                            } else {
                                String preNmae = "";
                                if (conType == DataSourceTypeEnum.SQLSERVER) {
                                    preNmae = "dbo.";
                                } else if (conType == DataSourceTypeEnum.POSTGRESQL) {
                                    preNmae = "public.";
                                }
                                tblName = preNmae + "ods_" + accessAndModelAppDTO.getAppAbbreviation() + "_" + table.getTableName();
                            }
                            tblNames.add(tblName);
                        }
                    }
                }
            }
        }
        return tblNames;
    }

    public List<PostgreDTO> removeDuplicates(List<PostgreDTO> list) {
        List<PostgreDTO> uniqueList = new ArrayList<>();

        for (PostgreDTO postgreDTO : list) {
            boolean exists = false;
            for (PostgreDTO uniquePostgreDTO : uniqueList) {
                if (postgreDTO.getIp().equals(uniquePostgreDTO.getIp()) && postgreDTO.getPort() == uniquePostgreDTO.getPort() && Objects.equals(postgreDTO.getDbName(), uniquePostgreDTO.getDbName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueList.add(postgreDTO);
            }
        }

        return uniqueList;
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
        datasourceId.add(mdmId);

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

    public List<RowDto> createTableStructureTemplate_GetSingRows(List<String> fieldNameList) {
        List<RowDto> singRows = new ArrayList<>();
        RowDto rowDto = new RowDto();
        rowDto.setRowIndex(0);
        List<String> Columns = fieldNameList;
        rowDto.setColumns(Columns);
        singRows.add(rowDto);
        return singRows;
    }
}
