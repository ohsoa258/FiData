package com.fisk.dataservice.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class WebServiceTest {

    /**
     * 测试websService接口
     */
    @Test
    public void testWebService() {
        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        //todo：webService的这个动态客户端的地址需要从数据库中查出来
        Client client = dcf.createClient("http://localhost:8089/webservice/fidata-api?wsdl");

        //设置超时时间
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setAllowChunking(false);
        // 连接服务器超时时间 30秒
        policy.setConnectionTimeout(30000);
        // 等待服务器响应超时时间 30秒
        policy.setReceiveTimeout(30000);
        conduit.setClient(policy);
        JSONArray jsonArray = null;
        try {
            String json = "{\"result\":{\"data\":{\"name\":1},\"data2\":{\"name\":2}},\"authtokens\":\"token 1231241525\"}";
            JSONObject jsonObject1 = JSONObject.parseObject(json);
            Map<String,Object> map = new HashMap<>();
            map.put("authtokens","token 1231241525");
            map.put("result",jsonObject1);
            // invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke("webServiceGetToken", JSONObject.toJSONString(map));
            String s = JSON.toJSONString(objects);
            jsonArray = JSON.parseArray(s);
                //todo:webService拿到这个jsonObject对象后，后续流程就和api推数据差不多了
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objects[0]));
        } catch (Exception e) {
            System.out.println(e.toString());
        }finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
