package com.fisk.task.utils;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class TaskPgTableStructureHelper
        extends ServiceImpl<TaskPgTableStructureMapper, TaskPgTableStructurePO> {
    /**
     * 保存建模相关表结构数据
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
            for (ModelPublishFieldDTO item: dto.fieldList) {
                TaskPgTableStructurePO po = new TaskPgTableStructurePO();
                po.version = version;
                po.tableId = String.valueOf(dto.tableId);
                po.tableName = dto.tableName;
                po.fieldId = String.valueOf(item.fieldId);
                po.fieldName = item.fieldEnName;
                po.fieldType = item.fieldType;
                if (item.fieldLength != 0) {
                    po.fieldType = item.fieldType + "(" + item.fieldLength + ")";
                }
                poList.add(po);
                if (item.associateDimensionId !=0 && item.associateDimensionFieldId !=0)
                {
                    po.fieldId=String.valueOf(item.associateDimensionId);
                    po.fieldName=item.associateDimensionName.substring(4)+"key";
                    po.fieldType="VARCHAR(255)";
                    poList.add(po);
                }
            }
            //保存成功,调用存储过程,获取修改表结构SQL语句
            if (!this.saveBatch(poList))
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //执行存储过程
            String sql = execProcedure(version);
            //判断是否有修改语句
            return updatePgTableStructure(sql,dto.tableName);
        }
        catch (Exception ex)
        {
            log.error("saveTableStructure:"+ex);
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    /**
     * 执行存储过程,获取更改表结构SQL语句
     * @param version
     * @return
     */
    public String execProcedure(String version) throws Exception
    {
        //拼接SQL
        StringBuilder str=new StringBuilder();
        //配置数据库参数
        Class.forName("com.mysql.jdbc.Driver");
        String url="jdbc:mysql://192.168.11.130:3306/dmp_task_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user="root";
        String pass="root123";
        //获取数据库连接
        Connection conn=DriverManager.getConnection(url,user,pass);
        //调用过程stu_pro
        CallableStatement cs=(CallableStatement)conn.prepareCall("call pg_check_table_structure(?)");
        cs.setString(1,version);
        cs.execute();
        ResultSet rs = cs.getResultSet();
        while (rs.next()) {
            String value=rs.getString(1);
            if (value !=null && value.length()>0) {
                str.append(value.toLowerCase());
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
                    str.append(value.toLowerCase());
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
        return str.toString();
    }

    /**
     * 根据语句修改PgTable表结构
     * @param sql
     * @param tableName
     * @return
     */
    public ResultEnum updatePgTableStructure(String sql,String tableName)
    {
        try {
            Class.forName("org.postgresql.Driver");
            String pgsqlOdsUrl="jdbc:postgresql://192.168.1.250:5432/dmp_dw?stringtype=unspecified";
            String pgsqlOdsUsername="postgres";
            String pgsqlOdsPassword="Password01!";
            Connection conn = DriverManager.getConnection(pgsqlOdsUrl, pgsqlOdsUsername, pgsqlOdsPassword);
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
        }catch (ClassNotFoundException | SQLException e) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
    }
}
