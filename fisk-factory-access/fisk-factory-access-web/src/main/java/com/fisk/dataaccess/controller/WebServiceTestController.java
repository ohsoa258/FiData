package com.fisk.dataaccess.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.webservice.service.WebServiceReceiveDataDTO;
import com.fisk.dataaccess.webservice.service.WebServiceUserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该控制器用于测试接收webService方式发送的数据
 */
@Api(tags = SwaggerConfig.WebService_Api)
@RestController
@RequestMapping("/webServiceConfig")
@Slf4j
public class WebServiceTestController {

    @Value("${webService-ip-address}")
    private String webIp;

    /**
     * webService推送数据测试 --cxf方式
     *
     * @param dto
     * @return
     */
    @PostMapping("/webServicePushData")
    @ApiModelProperty(value = "webService推送数据测试")
    public ResultEntity<Object> wsPushData(@RequestBody WebServiceReceiveDataDTO dto) {
        String result = null;
        try {
            // 创建动态客户端
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            // webService的动态客户端的地址 http://localhost:8089/webservice/fidata-api?wsdl
            Client client = dcf.createClient("http://" + webIp + "/webservice/fidata-api?wsdl");
            // 设置超时时间
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setAllowChunking(false);
            // 连接服务器超时时间 30秒
            policy.setConnectionTimeout(30000);
            // 等待服务器响应超时时间 30秒
            policy.setReceiveTimeout(30000);
            conduit.setClient(policy);

            // 组装方法的参数
            WebServiceReceiveDataDTO webServiceReceiveDataDTO = new WebServiceReceiveDataDTO();
            webServiceReceiveDataDTO.setWebServiceCode(dto.getWebServiceCode());
            webServiceReceiveDataDTO.setData(dto.getData());
            webServiceReceiveDataDTO.setToken(dto.getToken());
            // 开始调用方法--invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke("webServicePushData", webServiceReceiveDataDTO);
            // 将执行结果转换为json字符串
            result = JSON.toJSONString(objects);
        } catch (Exception e) {
            log.error("webService报错--" + e);
            return ResultEntityBuild.build(ResultEnum.WEBSERVICE_PUSH_DATA_ERROR, e.getMessage());
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    /**
     * webService获取token测试 --cxf方式
     *
     * @param dto
     * @return
     */
    @PostMapping("/webServiceGetToken")
    @ApiModelProperty(value = "webService获取token测试")
    public ResultEntity<Object> webServiceGetToken(@RequestBody WebServiceUserDTO dto) {
        String token = null;
        try {
            // 创建动态客户端
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            // webService的动态客户端的地址http://localhost:8089/webservice/fidata-api?wsdl
            Client client = dcf.createClient("http://" + webIp + "/webservice/fidata-api?wsdl");
            // 设置超时时间
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setAllowChunking(false);
            // 连接服务器超时时间 30秒
            policy.setConnectionTimeout(30000);
            // 等待服务器响应超时时间 30秒
            policy.setReceiveTimeout(30000);
            conduit.setClient(policy);

            // 组装方法的参数
            WebServiceUserDTO webServiceUserDTO = new WebServiceUserDTO();
            webServiceUserDTO.setPassword(dto.getPassword());
            webServiceUserDTO.setUseraccount(dto.getUseraccount());
            // 开始调用方法--invoke("方法名",参数1,参数2,参数3....);
            Object[] objects = client.invoke("webServiceGetToken", webServiceUserDTO);
            // 获取返回结果里的第一条数据(这里有且只有一条) -- 即token或报错msg
            token = objects[0].toString();
        } catch (Exception e) {
            log.error("webService报错--" + e);
            return ResultEntityBuild.build(ResultEnum.WEBSERVICE_GET_TOKEN_ERROR, e.getMessage());
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, token);
    }

    @Resource
    private ApiConfigImpl apiConfig;

    @PostMapping("/AESTest")
    @ApiModelProperty(value = "AES测试")
    public void AESTest() {
        // JSON 数据
        String jsonData = "{\"data\": [{\"b\": \"数据b\", \"a\": \"数据a\"}]}";
        String aseKey= "ScfDBzRCteDkQSt2tL6S2A==";
        SecretKeySpec secretKeySpec = decryptionKey(aseKey);

        //加密数据
        String s = encryptJsonData(jsonData, secretKeySpec);

        ReceiveDataDTO dto = new ReceiveDataDTO();
        dto.setApiCode(48L);
        dto.setPushData(s);
        //推送
        apiConfig.pushData(dto);
    }

    // AES加密 JSON 数据
    private static String encryptJsonData(String jsonData, SecretKey secretKey) {

        Cipher cipher = null;
        try {
            // 使用 ECB 模式和 PKCS5Padding 填充
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // 将 JSON 数据转换为字节数组
            byte[] dataBytes = jsonData.getBytes(StandardCharsets.UTF_8);
            // 加密数据
            byte[] encryptedBytes = cipher.doFinal(dataBytes);
            // 将加密后的数据转换为 Base64 编码
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 将base64编码的密钥转换为AES密钥对象
    private static SecretKeySpec decryptionKey(String base64EncodedKey) {
        byte[] keyBytes = Base64.getDecoder().decode(base64EncodedKey);
        // 将加密后的数据转换为 Base64 编码
        return new SecretKeySpec(keyBytes, "AES");
    }

}
