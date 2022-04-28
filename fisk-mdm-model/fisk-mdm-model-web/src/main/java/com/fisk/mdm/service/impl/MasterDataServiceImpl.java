package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.mapper.MasterDataMapper;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;

/**
 * 主数据服务impl
 *
 * @author ChenYa
 * @date 2022/04/25
 */
@Slf4j
@Service
public class MasterDataServiceImpl implements IMasterDataService {
    @Resource
    MasterDataMapper mapper;

    @Resource
    EntityService entityService;

    String connectionStr = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm?stringtype=unspecified";
    String acc = "postgres";
    String pwd = "Password01!";

    //系统字段
    String systemColumnName = ",fidata_id," +
            "fidata_version_id," +
            "fidata_create_time," +
            "fidata_create_user," +
            "fidata_update_time," +
            "fidata_update_user";

    static {
        //加载pg数据库驱动
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public Map<String, Object> getByCode(Integer entityId, Integer code) {
//        //获得主数据表名
//        String tableName = "viw_"+entityService.getDataById(entityId).getTableName();
//
//        //获得字段名
//        List<AttributeInfoDTO> attributeList = entityService.getAttributeById(entityId).getAttributeList();
//        List<String> list = new ArrayList<>();
//        for (AttributeInfoDTO attributeInfoDTO:attributeList){
//            list.add(attributeInfoDTO.getName());
//        }
//        String columnName = StringUtils.join(list, ",");
//
//
//        return mapper.getByCode(tableName,columnName,code);
//    }
    /**
     * 根据实体id查询主数据
     *
     * @param entityId 实体id
     */
    @Override
    public ResultObjectVO getAll(Integer entityId){

        ResultObjectVO resultObjectVO = new ResultObjectVO();
        //获得主数据表名
        String tableName = "viw_"+entityService.getDataById(entityId).getModelId()+"_"+entityId;

        //获取属性集合
        List<AttributeInfoDTO> attributeList = entityService.getAttributeById(entityId).getAttributeList();
        resultObjectVO.setAttributeInfoDTOList(attributeList);

        //获得业务字段名
        List<String> list = new ArrayList<>();
        for (AttributeInfoDTO attributeInfoDTO:attributeList){
            list.add(attributeInfoDTO.getName());
        }
        String businessColumnName = StringUtils.join(list, ",");

        //拼接sql语句
        String sql = "select "+ businessColumnName + systemColumnName  + " from "+tableName +" view";

        //准备返回的主数据
        List<Map<String,Object>> data = new ArrayList<>();

        try {
            Connection connection = getConnection();
            ResultSet resultSet = executeSelectSql(sql, connection);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()){
                Map<String,Object> map = new HashMap<>();
                for (int i = 1 ; i <=metaData.getColumnCount() ; i++){
                    map.put(metaData.getColumnName(i),resultSet.getString(metaData.getColumnName(i)));
                }
                data.add(map);
            }
            resultObjectVO.setResultData(data);
            return resultObjectVO;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObjectVO;
    }

    /**
     * 获得连接
     *
     * @return {@link Connection}
     */
    public Connection getConnection(){
        try {
            Connection connection = DriverManager.getConnection(connectionStr, acc, pwd);
            log.info("【connection】数据库连接成功, 连接信息【" + connectionStr + "】");
            return connection;
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, e.getLocalizedMessage());
        }
    }


    /**
     * 释放资源
     *
     * @param rs   ResultSet
     * @param stmt Statement
     * @param conn Connection
     */
    public static void release(ResultSet rs , Statement stmt , Connection conn){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            rs=null;
        }
        if(stmt!=null){
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stmt=null;
        }
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn=null;
        }
    }

    /**
     * 执行查询sql
     *
     * @param sql        sql
     * @param connection 连接
     * @throws SQLException sqlexception异常
     */
    public ResultSet executeSelectSql(String sql,Connection connection) throws SQLException {
        Statement statement =connection.createStatement();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("执行sql: 【" + sql + "】");
        return statement.executeQuery(sql);
    }
}
