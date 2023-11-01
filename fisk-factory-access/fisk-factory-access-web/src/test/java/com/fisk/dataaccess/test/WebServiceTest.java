package com.fisk.dataaccess.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.webservice.service.WebServiceUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.sql.*;
import java.util.List;

/**
 * @author: lsj
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class WebServiceTest {

    /**
     * 测试websService接口
     */
    @Test
    public void testWebServiceGetToken() {

        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient("http://localhost:8089/webservice/fidata-api?wsdl");

        //设置超时时间
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setAllowChunking(false);
        // 连接服务器超时时间 10秒
        policy.setConnectionTimeout(10000);
        // 等待服务器响应超时时间 20秒
        policy.setReceiveTimeout(20000);
        conduit.setClient(policy);
        JSONArray jsonArray = null;
        try {
            Object[] params = new Object[1];
            WebServiceUserDTO webServiceUserDTO = new WebServiceUserDTO();
            webServiceUserDTO.setPassword("1");
            webServiceUserDTO.setUseraccount("1");
            params[0] = webServiceUserDTO;
            // invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke("getToken", params);
            String s = JSON.toJSONString(objects);
            System.out.println(s);
            jsonArray = JSON.parseArray(s);
            for (Object o : jsonArray) {
                //todo:webService拿到这个jsonObject对象后，后续流程就和api推数据差不多了
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
                System.out.println(jsonObject);
            }
        } catch (Exception e) {
            log.error("webService报错--" + e);
        }
    }

    @Test
    public void getPrimaryKeys() throws Exception {
        DatabaseMetaData databaseMetaData = null;
        ResultSet rs = null;
        Connection connection = DriverManager.getConnection("jdbc:mysql://192.168.11.134:9030/dmp_ods",
                "root", "Password01!");
        try {
            databaseMetaData = connection.getMetaData();
            rs = databaseMetaData.getPrimaryKeys(null, "dmp_ods", "stg_doris_test_doris_test01");
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME"));
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    System.out.println("Label: " + resultSetMetaData.getColumnLabel(i) + "  Value:" + rs.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }}

    @Test
    public void testDynamicVar(){
        int[] ints = new int[3];
        ints[0] =1;
        ints[1] =2;
        ints[2] =3;
        System.out.println(sum(ints));
    }

    private int sum(int...ints){
        return Arrays.stream(ints).sum();
    }

}
