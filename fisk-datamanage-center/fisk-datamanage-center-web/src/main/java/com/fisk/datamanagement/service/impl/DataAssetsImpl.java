package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;
import com.fisk.datamanagement.service.IDataAssets;
import com.fisk.datamanagement.vo.ConnectionInformationDTO;
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
            //获取账号密码
            String[] comments = attributes.getString("comment").split("&");
            ConnectionInformationDTO connectionDTO = jointConnection(rdbmsType, attributes.getString("hostname"), attributes.getString("port"), dto.dbName);
            //连接数据源
            Connection conn=getStatement(connectionDTO.driver,connectionDTO.url,comments[0],comments[1]);
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
                String getTotalSql = "select count(*) as totalNum from " + dto.tableName+condition;
                ResultSet rSet = st.executeQuery(getTotalSql);
                int rowCount = 0;
                if (rSet.next()) {
                    rowCount = rSet.getInt("totalNum");
                }
                rSet.close();
                data.total=rowCount;
                //分页获取数据
                int offset = (dto.pageIndex - 1) * dto.pageSize;
                int skipCount=dto.pageIndex*dto.pageSize;
                sql = "select * from "+ dto.tableName+condition+" order by "+dto.columnName+ " limit " + dto.pageSize + " offset " + offset;
                switch (rdbmsType)
                {
                    case "mysql":
                    case "sqlserver":
                        sql="select top "+dto.pageSize+" * from (select row_number() over(order by "
                                +dto.columnName+" asc "
                                +") as rownumber,* from "
                                +dto.tableName+") temp_row "+condition
                                +" and rownumber>"+offset;
                        break;
                    case "oracle":
                        sql="select * from ( select rownum, t.* from "+dto.tableName
                                +" t "
                                +condition
                                +" and rownum <= "+skipCount
                                +" ) table_alias where table_alias.\"ROWNUM\" >= "+offset+"";
                    case "postgresql":
                    case "doris":
                    default:
                }
            }
            ResultSet rs = st.executeQuery(sql);
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            data.dataArray=columnDataList(rs,metaData,columnCount);
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
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
        return data;
    }

    /**
     * 返回数据库连接驱动以及拼接连接地址
     * @param rdbmsType
     * @param hostname
     * @param port
     * @param dbName
     * @return
     */
    public ConnectionInformationDTO jointConnection(String rdbmsType,String hostname,String port,String dbName)
    {
        String driver="";
        String url="";
        switch (rdbmsType)
        {
            case "mysql":
            case "doris":
                driver="com.mysql.jdbc.Driver";
                url="jdbc:mysql://"+hostname+":"+port+"/"+dbName;
                break;
            case "sqlserver":
                driver="com.microsoft.sqlserver.jdbc.SQLServerDriver";
                url="jdbc:sqlserver://"+hostname+":"+port+";DatabaseName="+dbName;
                break;
            case "oracle":
                driver="oracle.jdbc.OracleDriver";
                url="jdbc:oracle:thin:@" +hostname+":"+port+":"+"ORCLCDB";
                break;
            case "postgresql":
                driver="org.postgresql.Driver";
                url="jdbc:postgresql://"+hostname+":"+port+"/"+dbName;
                break;
            default:
                throw new FkException(ResultEnum.NOT_SUPPORT);
        }
        ConnectionInformationDTO data=new ConnectionInformationDTO();
        data.driver=driver;
        data.url=url;
        return data;
    }

    /**
     * 获取行数据
     * @param rs
     * @param metaData
     * @param columnCount
     * @return
     */
    public JSONArray columnDataList(ResultSet rs,ResultSetMetaData metaData,int columnCount){
        try {
            // json数组
            JSONArray array = new JSONArray();
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
            return array;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
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
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
        return conn;
    }

}
