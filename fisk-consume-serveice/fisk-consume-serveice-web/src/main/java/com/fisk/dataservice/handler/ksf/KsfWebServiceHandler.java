package com.fisk.dataservice.handler.ksf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.service.ITableApiParameterService;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.dataservice.service.ITableAppManageService;
import com.fisk.dataservice.util.TreeBuilder;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
@Slf4j
@Component
public abstract class KsfWebServiceHandler {

    private static ITableAppManageService tableAppService;
    private static ITableApiService tableApiService;
    private static ITableApiParameterService tableApiParameterService;
    private static UserClient userClient;

    @Autowired
    public void setTableAppService(ITableAppManageService tableAppService) {
        KsfWebServiceHandler.tableAppService = tableAppService;
    }
    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfWebServiceHandler.tableApiService = tableApiService;
    }
    @Autowired
    public void setTableApiParameterService(ITableApiParameterService tableApiParameterService) {
        KsfWebServiceHandler.tableApiParameterService = tableApiParameterService;
    }
    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfWebServiceHandler.userClient = userClient;
    }

    public abstract ApiResultDTO sendApi(TableAppPO tableAppPO,long apiId);

    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        //创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        //webService的这个动态客户端的地址需要从数据库中查出来
        Client client = dcf.createClient(tableApiServicePO.getApiAddress());
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
            // invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke(tableApiServicePO.getMethodName(), body);
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
