package com.fisk.dataservice.handler.webservice.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.entity.TableApiResultPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.AuthTypeEnum;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.handler.webservice.WebServiceHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import com.fisk.dataservice.service.ITableApiResultService;
import com.fisk.dataservice.util.HttpGetWithEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Slf4j
@Component
public class WebServiceBasicValidation extends WebServiceHandler {

    private static ITableApiAuthRequestService tableApiAuthRequestService;

    private static ITableApiResultService tableApiResultService;
    @Autowired
    public void setTableApiAuthRequestService(ITableApiAuthRequestService tableApiAuthRequestService) {
        WebServiceBasicValidation.tableApiAuthRequestService = tableApiAuthRequestService;
    }

    @Autowired
    public void setTableApiResultService(ITableApiResultService tableApiResultService) {
        WebServiceBasicValidation.tableApiResultService = tableApiResultService;
    }
    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        LambdaQueryWrapper<TableApiAuthRequestPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiAuthRequestPO::getAppId, tableAppPO.getId());
        List<TableApiAuthRequestPO> list = tableApiAuthRequestService.list(queryWrapper);
        ApiResultDTO authToken = getAuthToken(tableAppPO, list);
        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        //todo：webService的这个动态客户端的地址需要从数据库中查出来
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
            map.put("authtokens",authToken.getMsg());
            map.put("result",JSONObject.parseObject(body));
            // invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke(tableApiServicePO.getMethodName(), JSONObject.toJSONString(map));
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objects[0]));
            if ((int)jsonObject.get("code") == 0){
                apiResultDTO.setFlag(true);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            }else if ((int)jsonObject.get("code") == 2){
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

    private ApiResultDTO getAuthToken(TableAppPO tableAppPO,List<TableApiAuthRequestPO> list){
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        LambdaQueryWrapper<TableApiResultPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiResultPO::getAppId,tableAppPO.getId());
        List<TableApiResultPO> apiResultPOS = tableApiResultService.list(queryWrapper);
        List<String> key = new ArrayList<>();
        List<TableApiResultPO> collect = apiResultPOS.stream().filter(i -> i.getSelected() == 1).collect(Collectors.toList());
        key.add(collect.get(0).getName());
        if (collect.get(0).getPid() !=0){
            key = getKeys(key,apiResultPOS,collect.get(0).getPid());
        }
        Collections.reverse(key);
        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        //todo：webService的这个动态客户端的地址需要从数据库中查出来
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
            Map<String,String> map = new HashMap<>();
            for (TableApiAuthRequestPO tableApiAuthRequestPO : list) {
                map.put(tableApiAuthRequestPO.getParameterKey(), tableApiAuthRequestPO.getParameterValue());
            }
            // invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke(tableAppPO.getMethodName(), JSONObject.toJSONString(map));
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objects[0]));
            if ((int)jsonObject.get("code") == 0){
                apiResultDTO.setFlag(true);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            }else if ((int)jsonObject.get("code") == 2){
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            }else {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("远程调用异常");
            }
            for (String s : key) {
                result = (JSONObject) jsonObject.get(s);
            }
            apiResultDTO.setFlag(true);
            apiResultDTO.setMsg(result.toString());
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

    private List<String> getKeys(List<String> key,List<TableApiResultPO> apiResultPOS,Integer pid){
        List<TableApiResultPO> collect = apiResultPOS.stream().filter(i -> i.getId() == pid).collect(Collectors.toList());
        if (collect.get(0).getPid() !=0){
            key.add(collect.get(0).getName());
            getKeys(key,apiResultPOS,collect.get(0).getPid());
        }else {
            key.add(collect.get(0).getName());
        }
        return key;
    }
}
