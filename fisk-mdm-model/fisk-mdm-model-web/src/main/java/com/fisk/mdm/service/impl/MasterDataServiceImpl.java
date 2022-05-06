package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.vo.masterdata.ExportResultVO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
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
    EntityService entityService;

    @Resource
    AttributeMapper attributeMapper;
    @Resource
    EntityMapper entityMapper;

    String connectionStr = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm?stringtype=unspecified";
    String acc = "postgres";
    String pwd = "Password01!";

    /**
     * 系统字段
     */
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

    /**
     * 根据实体id查询主数据
     *
     * @param entityId 实体id
     */
    @Override
    public ResultEntity<ResultObjectVO> getAll(Integer entityId, Integer modelVersionId){

        if(entityId == null || modelVersionId == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();

        EntityVO entityVo = entityService.getDataById(entityId);
        if(entityVo == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
        }
        //获得主数据表名
        String tableName = "viw_"+entityService.getDataById(entityId).getModelId()+"_"+entityId;

        //查询该实体下发布的属性
        QueryWrapper<AttributePO> attributeColumnWrapper = new QueryWrapper<>();
        attributeColumnWrapper.lambda().eq(AttributePO::getStatus, AttributeStatusEnum.SUBMITTED)
                .eq(AttributePO::getEntityId,entityId);
        List<AttributePO> attributePoList = attributeMapper.selectList(attributeColumnWrapper);
        if(attributePoList.isEmpty()){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.poToColumnVoList(attributePoList);
        resultObjectVO.setAttributeColumnVoList(attributeColumnVoList);

        //获得业务字段名
        List<String> list = new ArrayList<>();
        for (AttributeColumnVO attributeColumnVo:attributeColumnVoList){
            list.add(attributeColumnVo.getName());
        }
        String businessColumnName = StringUtils.join(list, ",");

        //拼接sql语句
        String sql = "select "+ businessColumnName + systemColumnName  + " from "+tableName + " view " +
                "where fidata_del_flag = 1 and fidata_version_id = " + modelVersionId;

        //准备主数据集合
        List<Map<String,Object>> data = new ArrayList<>();

        try {
            //获得工厂
            Connection connection = getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //执行sql，获得结果集
            log.info("执行sql: 【" + sql + "】");
            ResultSet resultSet = statement.executeQuery(sql);
            //判断结果集是否为空
            if(!resultSet.next()){
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
            }
            //获取结果集的结构信息
            ResultSetMetaData metaData = resultSet.getMetaData();
            //重置结果集游标，遍历结果集，取出数据
            resultSet.beforeFirst();
            while (resultSet.next()){
                //用map接收对象
                Map<String,Object> map = new HashMap<>();
                //遍历每一行数据，取出每一个字段名与其对应值
                for (int i = 1 ; i <=metaData.getColumnCount() ; i++){
                    map.put(metaData.getColumnName(i),resultSet.getString(metaData.getColumnName(i)));
                }
                //将接收到的对象放入主数据集合中
                data.add(map);
            }
            //将主数据集合添加装入结果对象
            resultObjectVO.setResultData(data);
            //释放资源
            release(resultSet,statement,connection);
            return ResultEntityBuild.build(ResultEnum.SUCCESS,resultObjectVO);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS,resultObjectVO);
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

    /***
     * 下载模板
     * @param entityId
     * @param response
     * @return
     */
    @Override
    public ResultEnum downloadTemplate(int entityId, HttpServletResponse response)
    {
        EntityPO entityPO=entityMapper.selectById(entityId);
        if (entityPO==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ExportResultVO vo=new ExportResultVO();
        QueryWrapper<AttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("display_name").lambda().eq(AttributePO::getEntityId,entityId);
        vo.headerList=(List)attributeMapper.selectObjs(queryWrapper);
        vo.fileName=entityPO.getDisplayName();
        return exportExcel(vo,response);
    }

    /**
     * 导出Excel
     * @param vo
     * @param response
     * @return
     */
    public ResultEnum exportExcel(ExportResultVO vo,HttpServletResponse response)
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("sheet1");
        HSSFRow row1 = sheet.createRow(0);
        if (CollectionUtils.isEmpty(vo.headerList))
        {
            ResultEntityBuild.build(ResultEnum.VISUAL_QUERY_ERROR);
        }
        for (int i = 0; i < vo.headerList.size(); i++) {
            row1.createCell(i).setCellValue(vo.headerList.get(i));
        }
        if (!CollectionUtils.isEmpty(vo.dataArray))
        {
            for (int i=0;i<vo.dataArray.size();i++)
            {
                HSSFRow row = sheet.createRow(i+1);
                JSONObject jsonObject = JSONObject.parseObject(vo.dataArray.get(i).toString());
                for (int j = 0; j < vo.headerList.size(); j++)
                {
                    row.createCell(j).setCellValue(jsonObject.get(vo.headerList.get(j)).toString());
                }
            }
        }
        //将文件存到指定位置
        try {
            //输出Excel文件
            OutputStream output=response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment;filename="+vo.fileName+".xls");
            response.setContentType("application/x-xls");
            workbook.write(output);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FkException(ResultEnum.SQL_ANALYSIS,e);
        }
        return ResultEnum.SUCCESS;
    }


}
