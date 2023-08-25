package com.fisk.task.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
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
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
     * @param dto            版本号和修改表结构的参数
     * @param version        时间戳版本号
     * @param dataSourceType 数据源连接类型
     */
//    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = FkException.class)
    public ResultEnum saveTableStructure(ModelPublishTableDTO dto, String version, DataSourceTypeEnum dataSourceType) {
        try {
            List<TaskPgTableStructurePO> poList = new ArrayList<>();
            Thread.sleep(200);
            //创建表方式 2:维度 1:事实 3: 数据接入
            int type = dto.createType == 0 ? 2 : dto.createType;
            //遍历字段列表
            for (ModelPublishFieldDTO item : dto.fieldList) {
                //获取 表版本结构表的对象   dmp_task_db -> tb_task_pg_table_structure
                TaskPgTableStructurePO po = new TaskPgTableStructurePO();
                //装载时间戳版本号
                po.version = version;
                //判断是否为维度
                po.tableType = type;
                //字段对应的表id
                po.tableId = String.valueOf(dto.tableId);
                //字段所属表名
                po.tableName = dto.tableName;
                //字段id
                po.fieldId = String.valueOf(item.fieldId);
                //字段名称
                po.fieldName = item.fieldEnName;
                //字段类型
                po.fieldType = item.fieldType;
                //是否为主键
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
            //保存表结构到tb_task_pg_table_structure
            List<TaskPgTableStructurePO> pos = poList.stream().distinct().collect(Collectors.toList());
            boolean saveResult = this.saveBatch(pos);
            log.info("保存表结构到tb_task_pg_table_structure保存结果：{}", saveResult);
            if (!saveResult) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //保存成功,调用存储过程,获取修改表结构SQL语句
            String sql = execProcedure(version, type, dataSourceType);
            log.info("查看执行表结构方法,sql: {}, version: {},type: {}", sql, version, type);
            //判断是否有修改语句
            return updatePgTableStructure(sql, version, dto.createType);
        } catch (Exception ex) {
            log.error("saveTableStructure:" + ex);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, StackTraceHelper.getStackTraceInfo(ex));
        }
    }

    /**
     * 执行存储过程,获取更改表结构SQL语句
     *
     * @param version
     * @return
     */
//    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
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
            //dbCommand.prepareCallSql() = "call pg_check_table_structure_sqlserver(?,?)"
            cs = (CallableStatement) conn.prepareCall(dbCommand.prepareCallSql());
            //第一个参数为前面我们生成的版本号
            cs.setString(1, version);
            //第二个参数为数据库连接类型，type=2--ods,也是前面获取的
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
//            //如果出现异常，手动将当前事务标记为回滚，触发事务回滚并抛出异常,以便事务注解能够处理它，并且确保事务能够回滚。
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "";
        } finally {
            if (conn != null) {
                conn.close();
            }
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
            if (!StringUtils.isEmpty(sql) && sql.contains("DECLARE")) {
                sql = subSql(sql);
            }

            //修改表结构
            if (sql != null && sql.length() > 0) {
                st = conn.createStatement();
                //无需判断ddl语句执行结果,因为如果执行失败会进catch
                st.execute(sql);
            }
            return ResultEnum.SUCCESS;
        } catch (SQLException e) {
            log.error("updatePgTableStructure:" + StackTraceHelper.getStackTraceInfo(e));
            //如果执行修改表结构的语句报错，则将刚才插入到tb_task_pg_table_structure表里的数据设置为无效，避免脏数据
//            taskPgTableStructureMapper.updatevalidVersion(version);
            throw new FkException(ResultEnum.SQL_ERROR, StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (st != null) {
                st.close();
            }
            conn.close();
        }
    }

    /**
     * 包含DECLARE时，切分sql进行重组
     *
     * @param sql
     * @return
     */
    private String subSql(String sql) {
        String[] ds = sql.split("DECLARE");
        String[] d = ds[1].split("EXEC \\( @primary_key \\);");
        if (d.length > 1) {
            return " DECLARE " + d[0] + " EXEC ( @primary_key ); " + ds[0] + d[1];
        }
        return " DECLARE " + d[0] + " EXEC ( @primary_key ); " + ds[0];
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
                    log.info(String.valueOf(set.getRow()));
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
