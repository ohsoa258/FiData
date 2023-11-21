package com.fisk.dataaccess.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.webservice.service.KSF_Inventory_Status;
import com.fisk.dataaccess.webservice.service.KSF_NoticeResult;
import com.fisk.dataaccess.webservice.service.KsfInventoryDTO;
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

import javax.annotation.Resource;
import java.sql.*;
import java.util.Arrays;

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
        }
    }

    @Test
    public void testDynamicVar() {
        int[] ints = new int[3];
        ints[0] = 1;
        ints[1] = 2;
        ints[2] = 3;
        System.out.println(sum(ints));
    }

    private int sum(int... ints) {
        return Arrays.stream(ints).sum();
    }

    @Resource
    private ApiConfigImpl apiConfig;

//    @Test
//    public void ksf_inventory_data() {
//        KSF_Inventory_Status inventory = JSONObject.parseObject("{\"aPI_Message\":{\"appKey\":\"?\",\"guid\":\"?\",\"isManualSend\":\"?\",\"isTest\":\"?\",\"singleTargetSys\":\"?\",\"sourceSys\":\"?\",\"targetSys\":\"?\",\"updateTime\":\"?\"},\"kSF_Inventory\":[{\"ANLN1\":\"?\",\"AUFNR\":\"?\",\"BELNR\":\"?\",\"BKTXT\":\"?\",\"BLART\":\"?\",\"BLDAT\":\"?\",\"BUDAT\":\"?\",\"BUKRS\":\"?\",\"BUTXT\":\"?\",\"BWART\":\"?\",\"CHARG\":\"?\",\"CPUDT\":\"?\",\"CPUTM\":\"?\",\"DMBTR\":\"?\",\"EBELN\":\"?\",\"EBELP\":\"?\",\"ERFME\":\"?\",\"ERFMG\":\"?\",\"GRUND\":\"?\",\"HSDAT\":\"?\",\"INSMK\":\"?\",\"KDAUF\":\"?\",\"KDPOS\":\"?\",\"KNAME1\":\"?\",\"KOSTL\":\"?\",\"KUNNR\":\"?\",\"KZBEW\":\"?\",\"KZZUG\":\"?\",\"LGOBE\":\"?\",\"LGORT\":\"?\",\"LGPLA\":\"?\",\"LIFNR\":\"?\",\"LNAME1\":\"?\",\"MAKTX\":\"?\",\"MATKL\":\"?\",\"MATNR\":\"?\",\"MBLNR\":\"?\",\"MEINS\":\"?\",\"MENGE\":\"?\",\"MJAHR\":\"?\",\"MTART\":\"?\",\"SAKTO\":\"?\",\"SGTXT\":\"?\",\"SOBKZ\":\"?\",\"UMCHA\":\"?\",\"UMLGO\":\"?\",\"UMMAT\":\"?\",\"UMWRK\":\"?\",\"USNAM\":\"?\",\"VFDAT\":\"?\",\"VTXTK\":\"?\",\"WAERS\":\"?\",\"WEMPF\":\"?\",\"WERKS\":\"?\",\"WNAME1\":\"?\",\"XAUTO\":\"?\",\"XBLNR\":\"?\",\"ZEILE\":\"?\"}]}", KSF_Inventory_Status.class);
//        //将webservice接收到的xml格式的数据转换为json格式的数据
//        KsfInventoryDTO data = new KsfInventoryDTO();
//        data.setSourceSys(inventory.getAPI_Message().getSourceSys());
//        data.setTargetSys(inventory.getAPI_Message().getTargetSys());
//        data.setUpdateTime(inventory.getAPI_Message().getUpdateTime());
//        data.setGuid(inventory.getAPI_Message().getGuid());
//        data.setSingleTargetSys(inventory.getAPI_Message().getSingleTargetSys());
//        data.setAppKey(inventory.getAPI_Message().getAppKey());
//        data.setIsTest(inventory.getAPI_Message().getIsTest());
//        data.setIsManualSend(inventory.getAPI_Message().getIsManualSend());
//        data.setKSF_Inventory(inventory.getKSF_Inventory());
//        String pushData = JSON.toJSONString(data);
//        //json解析的根节点 data
//        String rebuild = "{\"data\": [" + pushData + "]}";
//
//        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
//        receiveDataDTO.setApiCode(4L);
//        receiveDataDTO.setPushData(rebuild);
//        receiveDataDTO.setIfWebService(true);
//        String result = apiConfig.KsfWebServicePushData(receiveDataDTO);
//
//        KSF_NoticeResult ksf_noticeResult = new KSF_NoticeResult();
//        //统一报文返回类型
//        if (result.contains("失败") || !result.contains("成功")) {
//            ksf_noticeResult.setSTATUS("0");
//        } else {
//            ksf_noticeResult.setSTATUS("1");
//        }
//        ksf_noticeResult.setINFOTEXT(result);
//    }

}
