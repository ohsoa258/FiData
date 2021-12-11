package com.fisk.task.utils;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class TaskPgTableStructureHelper
        extends ServiceImpl<TaskPgTableStructureMapper, TaskPgTableStructurePO> {


    public static String taskdbUrl;

    public static String taskdbUsername;

    public static String taskdbPassword;

    public static String driverClassName;

    public static String datainputDriverClassName;

    public static String  pgsqlDatainputUrl;

    public static String pgsqlDatainputUsername;

    public static String pgsqlDatainputPassword;

    public static String pgsqlDatamodelUrl;

    public static String pgsqlDatamodelUsername;

    public static String pgsqlDatamodelPassword;


    @Value("${spring.datasource.dynamic.datasource.taskdb.url}")
    public  void setTaskdbUrl(String taskdbUrl) {
        TaskPgTableStructureHelper.taskdbUrl = taskdbUrl;
    }
    @Value("${spring.datasource.dynamic.datasource.taskdb.username}")
    public  void setTaskdbUsername(String taskdbUsername) {
        TaskPgTableStructureHelper.taskdbUsername = taskdbUsername;
    }
    @Value("${spring.datasource.dynamic.datasource.taskdb.password}")
    public  void setTaskdbPassword(String taskdbPassword) {
        TaskPgTableStructureHelper.taskdbPassword = taskdbPassword;
    }
    @Value("${spring.datasource.dynamic.datasource.taskdb.driver-class-name}")
    public  void setDriverClassName(String driverClassName) {
        TaskPgTableStructureHelper.driverClassName = driverClassName;
    }
    @Value("${pgsql-datainput.driverClassName}")
    public void setDatainputDriverClassName(String datainputDriverClassName) {
        TaskPgTableStructureHelper.datainputDriverClassName = datainputDriverClassName;
    }
    @Value("${pgsql-datainput.url}")
    public void setPgsqlDatainputUrl(String pgsqlDatainputUrl) {
        TaskPgTableStructureHelper.pgsqlDatainputUrl = pgsqlDatainputUrl;
    }
    @Value("${pgsql-datainput.username}")
    public void setPgsqlDatainputUsername(String pgsqlDatainputUsername) {
        TaskPgTableStructureHelper.pgsqlDatainputUsername = pgsqlDatainputUsername;
    }
    @Value("${pgsql-datainput.password}")
    public void setPgsqlDatainputPassword(String pgsqlDatainputPassword) {
        TaskPgTableStructureHelper.pgsqlDatainputPassword = pgsqlDatainputPassword;
    }
    @Value("${pgsql-datamodel.url}")
    public void setPgsqlDatamodelUrl(String pgsqlDatamodelUrl) {
        TaskPgTableStructureHelper.pgsqlDatamodelUrl = pgsqlDatamodelUrl;
    }
    @Value("${pgsql-datamodel.username}")
    public void setPgsqlDatamodelUsername(String pgsqlDatamodelUsername) {
        TaskPgTableStructureHelper.pgsqlDatamodelUsername = pgsqlDatamodelUsername;
    }
    @Value("${pgsql-datamodel.password}")
    public void setPgsqlDatamodelPassword(String pgsqlDatamodelPassword) {
        TaskPgTableStructureHelper.pgsqlDatamodelPassword = pgsqlDatamodelPassword;
    }

    /**
     * 保存建模相关表结构数据(保存版本号)
     * @param
     */
    public ResultEnum saveTableStructure(ModelPublishTableDTO dto)
    {
        try {
            List<TaskPgTableStructurePO> poList=new ArrayList<>();
            Thread.sleep(200);
            //获取时间戳版本号
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Calendar calendar = Calendar.getInstance();
            String version = df.format(calendar.getTime());
            int type=dto.createType==0?2:dto.createType;
            for (ModelPublishFieldDTO item: dto.fieldList) {
                TaskPgTableStructurePO po = new TaskPgTableStructurePO();
                po.version = version;
                //判断是否为维度
                po.tableType=type;
                po.tableId = String.valueOf(dto.tableId);
                po.tableName = dto.tableName;
                po.fieldId = String.valueOf(item.fieldId);
                po.fieldName = item.fieldEnName;
                po.fieldType = item.fieldType;
                if ("VARCHAR".equals(item.fieldType)) {
                    po.fieldType = item.fieldType + "(" + item.fieldLength + ")";
                }
                poList.add(po);
                if (item.associateDimensionId !=0 && item.associateDimensionFieldId !=0)
                {
                    TaskPgTableStructurePO po2 = new TaskPgTableStructurePO();
                    po2.version = version;
                    po2.tableType=type;
                    po2.tableId = String.valueOf(dto.tableId);
                    po2.tableName = dto.tableName;
                    po2.fieldId=String.valueOf(item.associateDimensionId);
                    po2.fieldName=item.associateDimensionName.substring(4)+"key";
                    po2.fieldType="VARCHAR(255)";
                    poList.add(po2);
                }
            }
            //保存成功,调用存储过程,获取修改表结构SQL语句
            if (!this.saveBatch(poList.stream().distinct().collect(Collectors.toList())))
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //执行存储过程
            String sql = execProcedure(version,type);
            //判断是否有修改语句
            return updatePgTableStructure(sql,dto.tableName,dto.createType);
        }
        catch (Exception ex)
        {
            log.error("saveTableStructure:"+ex);
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
     * @param version
     * @return
     */
    public String execProcedure(String version,int type) throws Exception
    {
        //配置数据库参数
        Class.forName(driverClassName);
        String url=taskdbUrl;
        String user=taskdbUsername;
        String pass=taskdbPassword;
        //获取数据库连接
        Connection conn=DriverManager.getConnection(url,user,pass);
        try {
            //拼接SQL
            StringBuilder str=new StringBuilder();
            List<String> sqlList=new ArrayList<>();
            //调用过程stu_pro
            CallableStatement cs=(CallableStatement)conn.prepareCall("call pg_check_table_structure(?,?)");
            cs.setString(1,version);
            cs.setInt(2,type);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs==null)
            {
                return "";
            }
            while (rs.next()) {
                String value=rs.getString(1);
                if (value !=null && value.length()>0) {
                    sqlList.add(value.toLowerCase());
                }
            }
            //这个判断会自动指向下一个游标
            while(cs.getMoreResults()) {
                //得到第二个结果集
                ResultSet rs1 = cs.getResultSet();
                //处理第二个结果集
                while (rs1.next()) {
                    String value=rs1.getString(1);
                    if (value !=null && value.length()>0) {
                        sqlList.add(value.toLowerCase());
                    }
                    try{//关闭rs1
                        if(rs1 == null){
                            rs1.close();
                        }
                        if(rs==null){
                            rs.close();
                        }
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            }
            sqlList=sqlList.stream().distinct().collect(Collectors.toList());
            String sql=String.join(" ", sqlList.stream().distinct().collect(Collectors.toList()));
            str.append(sql);
            return str.toString();
        }catch (SQLException e) {
            log.error("execProcedure:"+e);
            conn.close();
            return "";
        }
    }

    /**
     * 根据语句修改PgTable表结构
     * @param sql
     * @param tableName
     * @return
     */
    public ResultEnum updatePgTableStructure(String sql,String tableName,int createType) throws Exception
    {
        Class.forName(datainputDriverClassName);
        String pgsqlDwUrl=pgsqlDatamodelUrl;
        String pgsqlOdsUrl=pgsqlDatainputUrl;
        String pgsqlOdsUsername=pgsqlDatainputUsername;
        String pgsqlDwUsername=pgsqlDatamodelUsername;
        String pgsqlDwPassword=pgsqlDatamodelPassword;
        String pgsqlOdsPassword=pgsqlDatainputPassword;
        Connection conn;
        if (createType == 3 || createType == 4) {
            // 数据接入
            conn = DriverManager.getConnection(pgsqlOdsUrl, pgsqlOdsUsername, pgsqlOdsPassword);
        } else {
            // 数据建模
            conn = DriverManager.getConnection(pgsqlDwUrl, pgsqlDwUsername, pgsqlDwPassword);
        }
        try {
            log.info("执行存储过程返回修改语句:"+sql);
            //修改表结构
            if (sql!=null && sql.length()>0)
            {
                Statement st = conn.createStatement();
                return st.execute(sql)==true?ResultEnum.SUCCESS:ResultEnum.SQL_ERROR;
            }
            //判断表是否存在
            DatabaseMetaData metaData=conn.getMetaData();
            ResultSet set=metaData.getTables(null,null,tableName,null);
            if (!set.next())
            {
                return ResultEnum.TASK_TABLE_NOT_EXIST;
            }
            return ResultEnum.SUCCESS;
        }catch (SQLException e) {
            log.error("updatePgTableStructure:"+e);
            conn.close();
            return ResultEnum.UPDATE_DATA_ERROR;
        }
    }
}
