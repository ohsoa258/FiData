package com.fisk.dataaccess.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisk.dataaccess.webservice.entity.UserDTO;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author: lsj
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class WebServiceTest {

    /**
     * 测试websService接口
     */
    @Test
    public void testWebService() {
        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        //todo：webService的这个动态客户端的地址需要从数据库中查出来
        Client client = dcf.createClient("http://localhost:8089/webservice/api?wsdl");

        //设置超时时间
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setAllowChunking(false);
        // 连接服务器超时时间 10秒
        policy.setConnectionTimeout(10000);
        // 等待服务器响应超时时间 20秒
        policy.setReceiveTimeout(20000);
        conduit.setClient(policy);

        ObjectMapper mapper = new ObjectMapper();
        try {
            // invoke("方法名",参数1,参数2,参数3....);
            //todo：webService的这个方法名和参数同样需要从数据库查询得出
            Object[] objects = client.invoke("getUser", 99L);
            String s = JSON.toJSONString(objects);
            System.out.println(s);
            JSONArray jsonArray = JSON.parseArray(s);
            for (Object o : jsonArray) {
                //todo:webService拿到这个jsonObject对象后，后续流程就和api推数据差不多了
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
                System.out.println(jsonObject);
            }
            List<UserDTO> userDTOS = jsonArray.toJavaList(UserDTO.class);
            System.out.println(userDTOS);
            System.out.println(mapper.writeValueAsString(objects[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
