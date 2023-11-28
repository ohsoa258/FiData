package com.fisk.dataservice.handler.ksf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.entity.TableApiResultPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import com.fisk.dataservice.service.ITableApiResultService;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Slf4j
@Component
public class KsfNotice extends KsfWebServiceHandler {

    private static ITableApiAuthRequestService tableApiAuthRequestService;

    private static ITableApiResultService tableApiResultService;
    @Autowired
    public void setTableApiAuthRequestService(ITableApiAuthRequestService tableApiAuthRequestService) {
        KsfNotice.tableApiAuthRequestService = tableApiAuthRequestService;
    }

    @Autowired
    public void setTableApiResultService(ITableApiResultService tableApiResultService) {
        KsfNotice.tableApiResultService = tableApiResultService;
    }

    @Override
    public ApiResultDTO sendApi(TableAppPO tableAppPO, long apiId) {
        return null;
    }

    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        //webService的这个动态客户端的地址需要从数据库中查出来
        Client client = dcf.createClient(tableAppPO.getAuthenticationUrl());
        //设置超时时间
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setAllowChunking(false);
        // 连接服务器超时时间 30秒
        policy.setConnectionTimeout(30000);
        // 等待服务器响应超时时间 30秒
        policy.setReceiveTimeout(30000);
        conduit.setClient(policy);
        JSONObject result = null;
        try {
            if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()){
                body = "["+body+"]";
            }
            Map<String,Object> map = new HashMap<>();
            map.put("result",JSONObject.parseObject(body));
            // invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke(tableApiServicePO.getMethodName(), JSONObject.toJSONString(map));
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objects[0]));
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
