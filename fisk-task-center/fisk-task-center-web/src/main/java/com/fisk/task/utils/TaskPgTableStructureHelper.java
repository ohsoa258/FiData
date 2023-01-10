package com.fisk.task.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class TaskPgTableStructureHelper
        extends ServiceImpl<TaskPgTableStructureMapper, TaskPgTableStructurePO> {

    @Resource
    TaskPgTableStructureMapper taskPgTableStructureMapper;
    @Resource
    UserClient userClient;


    public static String taskdbUrl;

    public static String taskdbUsername;

    public static String taskdbPassword;

    public static String driverClassName;


    public static String dataSourceOdsId;

    public static String dataSourceDwId;


    @Value("${spring.datasource.dynamic.datasource.taskdb.url}")
    public void setTaskdbUrl(String taskdbUrl) {
        TaskPgTableStructureHelper.taskdbUrl = taskdbUrl;
    }

    @Value("${spring.datasource.dynamic.datasource.taskdb.username}")
    public void setTaskdbUsername(String taskdbUsername) {
        TaskPgTableStructureHelper.taskdbUsername = taskdbUsername;
    }

    @Value("${spring.datasource.dynamic.datasource.taskdb.password}")
    public void setTaskdbPassword(String taskdbPassword) {
        TaskPgTableStructureHelper.taskdbPassword = taskdbPassword;
    }

    @Value("${spring.datasource.dynamic.datasource.taskdb.driver-class-name}")
    public void setDriverClassName(String driverClassName) {
        TaskPgTableStructureHelper.driverClassName = driverClassName;
    }


    @Value("${fiData-data-ods-source}")
    public void setDataSourceOdsId(String dataSourceOdsId) {
        TaskPgTableStructureHelper.dataSourceOdsId = dataSourceOdsId;
    }

    @Value("${fiData-data-dw-source}")
    public void setDataSourceDwId(String dataSourceDwId) {
        TaskPgTableStructureHelper.dataSourceDwId = dataSourceDwId;
    }


    /**
     * 保存建模相关表结构数据(保存版本号)
     *
     * @param
     */
    public ResultEnum saveTableStructure(ModelPublishTableDTO dto, String version, DataSourceTypeEnum dataSourceType) {
        try {
            List<TaskPgTableStructurePO> poList = new ArrayList<>();
            Thread.sleep(200);
            int type = dto.createType == 0 ? 2 : dto.createType;
            for (ModelPublishFieldDTO item : dto.fieldList) {
                TaskPgTableStructurePO po = new TaskPgTableStructurePO();
                po.version = version;
                //判断是否为维度
                po.tableType = type;
                po.tableId = String.valueOf(dto.tableId);
                po.tableName = dto.tableName;
                po.fieldId = String.valueOf(item.fieldId);
                po.fieldName = item.fieldEnName;
                po.fieldType = item.fieldType;
                po.primaryKey = item.isPrimaryKey != 0;
                //默认为1
                po.validVersion = 1;
                if (item.fieldType.contains("VARCHAR")) {
                    po.fieldType = item.fieldType + "(" + item.fieldLength + ")";
                }
                if ("FLOAT".equals(item.fieldType)) {
                    po.fieldType = item.fieldType;
                }
                poList.add(po);
                if (item.associateDimensionId != 0 && item.associateDimensionFieldId != 0) {
                    TaskPgTableStructurePO po2 = new TaskPgTableStructurePO();
                    po2.version = version;
                    po2.tableType = type;
                    po2.tableId = String.valueOf(dto.tableId);
                    po2.tableName = dto.tableName;
                    po2.fieldId = String.valueOf(item.associateDimensionId);
                    po2.fieldName = (item.associateDimensionName.substring(4) + "key");
                    po2.fieldType = "VARCHAR(255)";
                    //默认为1
                    po2.validVersion = 1;
                    poList.add(po2);
                }
            }
            //保存成功,调用存储过程,获取修改表结构SQL语句
            if (!this.saveBatch(poList.stream().distinct().collect(Collectors.toList()))) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //执行存储过程
            String sql = execProcedure(version, type, dataSourceType);
            log.info("查看执行表结构方法,sql: {}, version: {},type: {}", sql, version, type);
            //判断是否有修改语句
            return updatePgTableStructure(sql, version, dto.createType);
        } catch (Exception ex) {
            log.error("saveTableStructure:" + ex);
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    /*@Test
    public void  tests()throws Exception
    {
        String aa=execProcedure("20211203140317267",1);
        String bb="";
    }*/

    /**
     * 执行存储过程,获取更改表结构SQL语句
     *
     * @param version
     * @return
     */
    public String execProcedure(String version, int type, DataSourceTypeEnum dataSourceType) throws Exception {
        //配置数据库参数
        Class.forName(driverClassName);
        String url = taskdbUrl;
        String user = taskdbUsername;
        String pass = taskdbPassword;
        //获取数据库连接
        Connection conn = DriverManager.getConnection(url, user, pass);
        try {
            //拼接SQL
            StringBuilder str = new StringBuilder();
            List<String> sqlList = new ArrayList<>();
            CallableStatement cs = null;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSourceType);
            cs = (CallableStatement) conn.prepareCall(dbCommand.prepareCallSql());
            cs.setString(1, version);
            cs.setInt(2, type);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs == null) {
                return "";
            }
            while (rs.next()) {
                String value = rs.getString(1);
                if (value != null && value.length() > 0) {
                    sqlList.add(value);
                }
            }
            //这个判断会自动指向下一个游标
            while (cs.getMoreResults()) {
                //得到第二个结果集
                ResultSet rs1 = cs.getResultSet();
                //处理第二个结果集
                while (rs1.next()) {
                    String value = rs1.getString(1);
                    if (value != null && value.length() > 0) {
                        sqlList.add(value);
                    }
                    try {//关闭rs1
                        if (rs1 == null) {
                            rs1.close();
                        }
                        if (rs == null) {
                            rs.close();
                        }
                    } catch (SQLException e) {
                        log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
                    }
                }
            }
            sqlList = sqlList.stream().distinct().collect(Collectors.toList());
            String sql = String.join(" ", sqlList.stream().distinct().collect(Collectors.toList()));
            str.append(sql);
            return str.toString();
        } catch (SQLException e) {
            log.error("execProcedure:" + e);
            conn.close();
            return "";
        }
    }

    /**
     * 根据语句修改PgTable表结构
     *
     * @param sql
     * @param version
     * @return
     */
    public ResultEnum updatePgTableStructure(String sql, String version, int createType) throws Exception {
        String pgsqlOdsUrl = "";
        String pgsqlOdsUsername = "";
        String pgsqlOdsPassword = "";
        String pgsqlOdsDriverClass = "";
        String pgsqlDwUrl = "";
        String pgsqlDwUsername = "";
        String pgsqlDwPassword = "";
        String pgsqlDwDriverClass = "";
        DataSourceTypeEnum type = null;
        DataSourceDTO odsData = new DataSourceDTO();
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            odsData = fiDataDataSource.data;
            pgsqlOdsUrl = odsData.conStr;
            pgsqlOdsUsername = odsData.conAccount;
            pgsqlOdsPassword = odsData.conPassword;
            pgsqlOdsDriverClass = odsData.conType.getDriverName();
        } else {
            log.error("userclient无法查询到ods库的连接信息");
            return ResultEnum.ERROR;
        }
        DataSourceDTO dwData = new DataSourceDTO();
        ResultEntity<DataSourceDTO> fiDataDataDwSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
        if (fiDataDataDwSource.code == ResultEnum.SUCCESS.getCode()) {
            dwData = fiDataDataDwSource.data;
            pgsqlDwUrl = dwData.conStr;
            pgsqlDwUsername = dwData.conAccount;
            pgsqlDwPassword = dwData.conPassword;
            pgsqlDwDriverClass = dwData.conType.getDriverName();
        } else {
            log.error("userclient无法查询到dw库的连接信息");
            return ResultEnum.ERROR;
        }

        Connection conn;
        Statement st = null;
        if (createType == 3) {
            Class.forName(pgsqlOdsDriverClass);
            // 数据接入
            conn = DriverManager.getConnection(pgsqlOdsUrl, pgsqlOdsUsername, pgsqlOdsPassword);
            type = odsData.conType;
        } else {
            Class.forName(pgsqlDwDriverClass);
            // 数据建模
            conn = DriverManager.getConnection(pgsqlDwUrl, pgsqlDwUsername, pgsqlDwPassword);
            type = dwData.conType;
        }
        try {
            //检查版本
            ResultEnum resultEnum = checkVersion(version, conn, type);
            if (resultEnum == ResultEnum.TASK_TABLE_NOT_EXIST) {
                return resultEnum;
            }
            log.info("执行存储过程返回修改语句:" + sql);
            //修改表结构
            if (sql != null && sql.length() > 0) {
                st = conn.createStatement();
                //无需判断ddl语句执行结果,因为如果执行失败会进catch
                st.execute(sql);
            }
            return ResultEnum.SUCCESS;
        } catch (SQLException e) {
            log.error("updatePgTableStructure:" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.SQL_ERROR;
        } finally {
            if (st != null) {
                st.close();
            }
            conn.close();
        }
    }

    /**
     * 根据版本号找出更新前表是否存在
     *
     * @param version
     * @param conn
     * @return
     * @throws Exception
     */
    public ResultEnum checkVersion(String version, Connection conn, DataSourceTypeEnum type) throws Exception {
        try {
            QueryWrapper<TaskPgTableStructurePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TaskPgTableStructurePO::getVersion, version);
            List<TaskPgTableStructurePO> taskPgTableStructurePOList = taskPgTableStructureMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(taskPgTableStructurePOList)) {
                QueryWrapper<TaskPgTableStructurePO> taskPgTableStructurePOQueryWrapper = new QueryWrapper<>();
                taskPgTableStructurePOQueryWrapper.orderByDesc("create_time").lambda()
                        .eq(TaskPgTableStructurePO::getTableType, taskPgTableStructurePOList.get(0).tableType)
                        .ne(TaskPgTableStructurePO::getVersion, version)
                        .eq(TaskPgTableStructurePO::getTableId, taskPgTableStructurePOList.get(0).tableId);
                List<TaskPgTableStructurePO> taskPgTableStructurePOList1 = taskPgTableStructureMapper
                        .selectList(taskPgTableStructurePOQueryWrapper);
                if (!CollectionUtils.isEmpty(taskPgTableStructurePOList1)) {
                    //判断表是否存在
                    DatabaseMetaData metaData = conn.getMetaData();
                    List<String> schemaAndTableName = TableNameGenerateUtils.getSchemaAndTableName(taskPgTableStructurePOList1.get(0).tableName, type);
                    ResultSet set = metaData.getTables(null, schemaAndTableName.get(0), schemaAndTableName.get(1), null);
                    if (set.next()) {
                        return ResultEnum.SUCCESS;
                    }
                }
                return ResultEnum.TASK_TABLE_NOT_EXIST;
            }
        } catch (Exception e) {
            log.error("checkVersion:" + e);
        }
        return ResultEnum.PARAMTER_ERROR;
    }


    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        Statement st = null;
        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // 数据接入
            conn = DriverManager.getConnection("jdbc:sqlserver://172.31.6.132:1433;DatabaseName=YF_ODS;encrypt=true;trustServerCertificate=true", "fisk_dev", "password01!");


            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet set = metaData.getTables(null, "YuG", "PCHANGEITEMREVISION2", null);
            if (set.next()) {
                System.out.println("此表存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            st.close();
            conn.close();
        }
    }


}
