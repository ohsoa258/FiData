package com.fisk.dataaccess.controller;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lsj
 */
@Api(tags = {SwaggerConfig.test})
@RestController
@RequestMapping("/accessTest")
@Slf4j
public class TestController {

    /**
     * 测试使用olap4j从SSAS中获取数据--并发测试
     *
     * @return
     */
    @ApiOperation("测试使用olap4j从SSAS中获取数据--并发测试")
    @GetMapping("/testGetFromSSAS")
    public ResultEntity<Object> testGetFromSSAS(@RequestParam("mdx") String mdx) {
//        String password = "Password";
//        String user = "admin";
//        String xmlaDataBase = "Router";

        String address = "jdbc:xmla:Server=http://10.10.33.221/olap/msmdpump.dll;Catalog=test_wwj123";
        // Connection connection = DriverManager.getConnection(
        //"jdbc:xmla: Server=http://192.168.0.151/OLAP/msmdpump.dll;" +
        //"Data Source=http://192.168.0.151/OLAP/msmdpump.dll;" +
        //"Initial Catalog=AdventureWorksDW2014; " +
        //"Integrated Security=Basic; " +
        //"User ID=XXX; " +
        //"Password=XXX;");
        OlapStatement stmt = null;
        OlapConnection olapconn = null;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            //org.olap4j.driver.xmla.XmlaOlap4jDriver
            Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
            //获取连接
            OlapConnection conn = (OlapConnection) DriverManager.getConnection(address);
            olapconn = conn.unwrap(OlapConnection.class);
            //获取信息
            String database = olapconn.getDatabase();
            String catalog = olapconn.getCatalog();
            String roleName = olapconn.getRoleName();
            log.info("database:[{}]", database);
            log.info("catalog:[{}]", catalog);
            log.info("roleName:[{}]", roleName);

            log.info("连接成功...");
            //自定义MDX查询语句
//            mdx = "EVALUATE SUMMARIZECOLUMNS('FBM_DIM_BW_Customer'[Customer_ID],[SUM_PracticalShipQTY])";
            log.info("自定义MDX查询语句：[{}]", mdx);
            stmt = olapconn.createStatement();
            log.info("开始执行查询...");
            CellSet cellset = stmt.executeOlapQuery(mdx);

//            //todo:处理返回值
//            for (Position rowPos : cellset.getAxes().get(1)) {
//                Map<String, Object> rowResult = new HashMap<>();
//                for (Position colPos : cellset.getAxes().get(0)) {
//                    Cell cell = cellset.getCell(colPos, rowPos);
//                    String columnName = colPos.getMembers().get(0).getCaption();
//                    Object cellValue = cell.getValue();
//                    rowResult.put(columnName, cellValue);
//                }
//                result.add(rowResult);
//            }

            stmt.close();
            olapconn.close();
            log.info("测试完毕..");
        } catch (Exception e) {
            log.error("测试异常.." + e);
            throw new FkException(ResultEnum.ERROR, e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (olapconn != null) olapconn.close();
            } catch (Exception e) {
                log.info("异常..." + e);
            }
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }


    @ApiOperation("批量初始化数据")
    @GetMapping("/initData")
    public void initData(
            @RequestParam("conStr") String conStr,
            @RequestParam("uname") String uname,
            @RequestParam("pwd") String pwd,
            @RequestParam("sql") String sql,
            @RequestParam("dataSize") Long dataSize

    ) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DbConnectionHelper.connection(conStr, uname, pwd, DataSourceTypeEnum.MYSQL);
            statement = connection.createStatement();
            long l = dataSize / 100000;
            for (long i = 0; i <= l; i++) {
                long count = i * 100000;
                String trueSql = sql + " limit " + count + "," + 100000;
                log.info("本次执行sql:" + trueSql + " 本次执行次数:" + (i + 1));
                statement.executeUpdate(trueSql);
            }

        } catch (Exception e) {
            log.error("报错：" + e);
        } finally {
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(connection);
        }
    }


}
