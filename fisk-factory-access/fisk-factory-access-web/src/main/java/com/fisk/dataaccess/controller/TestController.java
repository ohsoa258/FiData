package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.olap4j.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
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
    public ResultEntity<Object> testGetFromSSAS() {
//        String password = "Password";
//        String user = "admin";
//        String xmlaDataBase = "Router";

        String address = "jdbc:xmla:Server=http://10.10.33.221/olap/msmdpump.dll;Catalog=test_wwj123";
        OlapStatement stmt = null;
        OlapConnection olapconn = null;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            //org.olap4j.driver.xmla.XmlaOlap4jDriver
            Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
            //获取连接
            OlapConnection conn = (OlapConnection) DriverManager.getConnection(address);
            olapconn = conn.unwrap(OlapConnection.class);
            log.info("连接成功...");
            //自定义MDX查询语句
            String mdx = "EVALUATE\n" +
                    "SUMMARIZECOLUMNS(\n" +
                    "    'FBM_DIM_BW_Customer'[Customer_ID],\n" +
                    "    SUM('FBM_Fact_BW_Sellin'[PracticalShipQTY],\n" +
                    "    SUM('FBM_Fact_BW_Sellin'[PracticalShipAmt]\n" +
                    ")";
            stmt = olapconn.createStatement();
            CellSet cellset = stmt.executeOlapQuery(mdx);

            //todo:处理返回值
            for (Position rowPos : cellset.getAxes().get(1)) {
                Map<String, Object> rowResult = new HashMap<>();
                for (Position colPos : cellset.getAxes().get(0)) {
                    Cell cell = cellset.getCell(colPos, rowPos);
                    String columnName = colPos.getMembers().get(0).getCaption();
                    Object cellValue = cell.getValue();
                    rowResult.put(columnName, cellValue);
                }
                result.add(rowResult);
            }

            stmt.close();
            olapconn.close();
            log.info("测试完毕");
        } catch (Exception e) {
            log.error("测试异常.."+e);
            throw new FkException(ResultEnum.ERROR,e.getMessage());
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

}
