package com.fisk.datamanagement.utils.atlas;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class AtlasClient {

    @Value("${atlas.request.url}")
    private String requestIp;
    @Value("${atlas.account}")
    private String account;
    @Value("${atlas.password}")
    private String password;

    public ResultDataDTO<String> Get(String url)
    {
        ResultDataDTO<String> resultDataDTO=new ResultDataDTO<String>();
        resultDataDTO.code=ResultEnum.UNKNOWN;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            String newUrl=requestIp+url;
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(newUrl);
            // 设置请求头信息，鉴权
            httpGet.setHeader("Authorization", getAuthorization());
            RequestConfig requestConfig = getRequestConfig();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            httpResponse = httpClient.execute(httpGet);
            //获取返回状态码
            resultDataDTO.code = ResultEnum.getEnum(httpResponse.getStatusLine().getStatusCode());
            // 通过返回对象获取返回数据
            HttpEntity entity = httpResponse.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            resultDataDTO.data = EntityUtils.toString(entity);
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
            log.error("Atlas Get ClientProtocolException ex:",e);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Atlas Get IOException ex:",e);
        }
        finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultDataDTO;
    }

    public ResultDataDTO<String> Post(String url,String parameter)
    {
        ResultDataDTO<String> resultDataDTO=new ResultDataDTO<String>();
        resultDataDTO.code=ResultEnum.UNKNOWN;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        String newUrl=requestIp+url;
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(newUrl);
        httpPost.setHeader("Authorization", getAuthorization());
        // 配置请求参数实例
        RequestConfig requestConfig = getRequestConfig();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.addHeader("Content-Type", "application/json");
        //请求参数
        if (parameter != null && parameter.length()>0)
        {
            try {
                httpPost.setEntity(new StringEntity(parameter));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            resultDataDTO.code = ResultEnum.getEnum(httpResponse.getStatusLine().getStatusCode());
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            resultDataDTO.data = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultDataDTO;
    }

    public String Put(String url,String parameter)
    {
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String newUrl=requestIp+url;
        HttpPut httpPut = new HttpPut(newUrl);
        RequestConfig requestConfig = getRequestConfig();
        httpPut.setConfig(requestConfig);
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setHeader("DataEncoding", "UTF-8");
        httpPut.setHeader("Authorization", getAuthorization());
        try {
            httpPut.setEntity(new StringEntity(parameter));
            httpResponse = httpClient.execute(httpPut);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public ResultDataDTO<String> Delete(String url)
    {
        ResultDataDTO<String> resultDataDTO=new ResultDataDTO<String>();
        resultDataDTO.code=ResultEnum.UNKNOWN;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String newUrl=requestIp+url;
        HttpDelete httpDelete = new HttpDelete(newUrl);
        RequestConfig requestConfig = getRequestConfig();
        httpDelete.setConfig(requestConfig);
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setHeader("DataEncoding", "UTF-8");
        httpDelete.setHeader("Authorization", getAuthorization());
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpDelete);
            HttpEntity entity = httpResponse.getEntity();
            resultDataDTO.code = ResultEnum.getEnum(httpResponse.getStatusLine().getStatusCode());
            resultDataDTO.data = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultDataDTO;
    }

    /**
     * 请求超时配置
     * @return
     */
    private RequestConfig getRequestConfig()
    {
        // 设置配置请求参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 连接主机服务超时时间
                .setConnectTimeout(35000)
                // 请求超时时间
                .setConnectionRequestTimeout(35000)
                // 数据读取超时时间
                .setSocketTimeout(60000)
                .build();
        return requestConfig;
    }

    /**
     * 获取权限
     * @return
     */
    private String getAuthorization() {
        BASE64Encoder encoder = new BASE64Encoder();
        String auth = this.account + ":" + this.password;
        String base64 = encoder.encode(auth.getBytes());
        return "Basic " + base64;
    }

}
