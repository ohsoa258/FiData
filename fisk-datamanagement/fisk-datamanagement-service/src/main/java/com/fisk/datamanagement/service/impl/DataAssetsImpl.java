package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;
import com.fisk.datamanagement.service.IDataAssets;
import com.google.inject.internal.cglib.core.$Signature;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataAssetsImpl implements IDataAssets {

    @Resource
    GenerateCondition generateCondition;
    @Resource
    EntityImpl entity;

    @Override
    public DataAssetsResultDTO getDataAssetsTableList(DataAssetsParameterDTO dto)
    {
        DataAssetsResultDTO data=new DataAssetsResultDTO();
        try {
            //获取实例配置信息
            JSONObject instanceEntity = this.entity.getEntity(dto.instanceGuid);
            JSONObject entity= JSON.parseObject(instanceEntity.getString("entity"));
            JSONObject attributes= JSON.parseObject(entity.getString("attributes"));
            //获取数据库类型
            String rdbmsType = attributes.getString("rdbms_type").toLowerCase();
            String driver="";
            String url="";
            //获取账号密码
            String[] comments = attributes.getString("comment").split("&");
            /*rdbmsType="doris";
            String userName="root";//comments[0];
            String password="Password01!";//comments[1];
            String hostname="192.168.11.134";//attributes.getString("hostname");
            String port="9030";//attributes.getString("port");*/
            String userName=comments[0];
            String password=comments[1];
            String hostname=attributes.getString("hostname");
            String port=attributes.getString("port");
            switch (rdbmsType)
            {
                case "mysql":
                    driver="com.mysql.jdbc.Driver";
                    url="jdbc:mysql://"+hostname+":"+port+"/"+dto.dbName;
                    break;
                case "sqlserver":
                    driver="com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    url="jdbc:sqlserver://"+hostname+":"+port+";DatabaseName="+dto.dbName;
                    break;
                case "oracle":
                    driver="oracle.jdbc.driver.OracleDriver";
                    url="jdbc:oracle:thin:@ " +hostname+":"+port+":"+dto.dbName;
                    break;
                case "postgresql":
                    driver="org.postgresql.Driver";
                    url="jdbc:postgresql://"+hostname+":"+port+"/"+dto.dbName;
                    break;
                case "doris":
                    driver="com.mysql.jdbc.Driver";
                    url="jdbc:mysql://"+hostname+":"+port+"/"+dto.dbName;
                    break;
                default:
                    throw new FkException(ResultEnum.NOT_SUPPORT);
            }
            //连接数据源
            Connection conn=getStatement(driver,url,userName,password);
            Statement st = conn.createStatement();
            //拼接筛选条件
            String condition=" where 1=1 ";
            if (CollectionUtils.isNotEmpty(dto.filterQueryDTOList))
            {
                condition += generateCondition.getCondition(dto.filterQueryDTOList);
            }
            String sql="";
            //是否导出
            if (dto.export)
            {
                sql="select * from "+dto.tableName+condition;
            }else {
                //获取总条数
                String getTotalSql = "select count(*) as total from " + dto.tableName + " as tab "+condition+" ";
                ResultSet rSet = st.executeQuery(getTotalSql);
                int rowCount = 0;
                if (rSet.next()) {
                    rowCount = rSet.getInt("total");
                }
                rSet.close();
                data.total=rowCount;
                //分页获取数据
                int offset = (dto.pageIndex - 1) * dto.pageSize;
                sql = "select * from "+ dto.tableName+condition+" order by "+dto.columnName+ " limit " + dto.pageSize + " offset " + offset;
                switch (rdbmsType)
                {
                    case "mysql":
                    case "sqlserver":
                        sql="select top "+dto.pageSize+" * from (select row_number() over(order by " +dto.columnName+"asc"
                                +") as rownumber,* from "+dto.tableName+") temp_row "+condition+" and rownumber>"+offset;
                        break;
                    case "oracle":
                        sql="select * from (select rownum as rowno, t.* from "+dto.tableName +condition
                                +" and rownum <= "+offset +")table_alias where table_alias.rowno >= "+dto.pageIndex;
                    case "postgresql":
                    case "doris":
                    default:
                }
            }
            ResultSet rs = st.executeQuery(sql);
            // json数组
            JSONArray array = new JSONArray();
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    String value = rs.getString(columnName);
                    jsonObj.put(columnName, value==null?"":value);
                }
                array.add(jsonObj);
            }
            data.dataArray=array;
            //获取列名
            List<String> columnList=new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnList.add(metaData.getColumnLabel(i));
            }
            data.columnList=columnList;
            data.pageIndex=dto.pageIndex;
            data.pageSize=dto.pageSize;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e.getMessage());
        }
        return data;
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
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return conn;
    }

}
