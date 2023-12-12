package com.fisk.dataservice.handler.ksf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.stereotype.Component;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
@Slf4j
@Component
public abstract class KsfWebServiceHandler {


    public abstract ApiResultDTO sendApi(TableAppPO tableAppPO,long apiId,String fidata_batch_code,String sourcesys);

    public ApiResultDTO sendHttpPost(TableApiServicePO tableApiServicePO, String body){
        log.info("开始调用同步webservice");
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        Client client = null;
        try {
            //创建动态客户端
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            //webService的这个动态客户端的地址需要从数据库中查出来
            log.info("开始创建client");
            client = dcf.createClient(tableApiServicePO.getApiAddress());
            //设置超时时间
            log.info("设置超时时间");
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setAllowChunking(false);
            // 连接服务器超时时间 30秒
            policy.setConnectionTimeout(30000);
            // 等待服务器响应超时时间 30秒
            policy.setReceiveTimeout(30000);
            conduit.setClient(policy);
            JSONObject result = null;
            // invoke("方法名",参数1,参数2,参数3....);
            log.info("client.invoke");
            Object[] objects = client.invoke(tableApiServicePO.getMethodName(), body);
            log.info("发送webservice接口");
            JSONObject jsonObject = JSON.parseObject((String)objects[0]);
            if ((int)jsonObject.get("code") == 1){
                apiResultDTO.setFlag(true);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            }else if ((int)jsonObject.get("code") == -1){
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            }else {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("远程调用异常");
            }
        } catch (Exception e) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg(e.toString());
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    apiResultDTO.setFlag(false);
                    apiResultDTO.setMsg(e.toString());
                    e.printStackTrace();
                }
            }
        }
        return apiResultDTO;
    }
}
