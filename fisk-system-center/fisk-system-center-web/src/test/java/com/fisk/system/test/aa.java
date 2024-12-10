package com.fisk.system.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class aa {

    public static void main(String[] args) {
        String passwordToken = getPasswordToken();
        log.info("------------------------powerbi刷新数据集token:{}", passwordToken);
    }

    public static String getPasswordToken() {
        String grantType = "password";
        String tenantId = "cd6ec314-7506-4ed6-b46e-681443ca18e3"; // 替换为你的租户 ID

        // 创建HTTP客户端
        CloseableHttpClient client = HttpClients.createDefault();

        // 创建POST请求
        HttpPost post = new HttpPost("https://login.partner.microsoftonline.cn/" + tenantId + "/oauth2/token");

        // 创建参数列表
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", "yhxu@fisksoft.com"));
        params.add(new BasicNameValuePair("password", "xuyanghui0821"));
        params.add(new BasicNameValuePair("client_id", "7177dd3d-ccae-491a-b37a-918837aaa236"));
        params.add(new BasicNameValuePair("grant_type", grantType));
        params.add(new BasicNameValuePair("resource", "https://analysis.chinacloudapi.cn/powerbi/api"));

        try {
            // 创建UrlEncodedFormEntity
            HttpEntity entity = new UrlEncodedFormEntity(params);

            // 将请求体设置到POST请求中
            post.setEntity(entity);

            // 发送请求并获取响应
            HttpResponse response = client.execute(post);

            // 读取响应体中的内容
            String responseBody = EntityUtils.toString(response.getEntity());

            // 输出响应内容
            log.info("-------------------------" + responseBody);

            // 关闭HTTP客户端
            client.close();
            JSONObject jsonObject = JSON.parseObject(responseBody);
            String token = jsonObject.getString("token_type") + " " + jsonObject.getString("access_token");
            log.info("------------------------powerbi刷新数据集token:{}", token);
            return token;
        } catch (Exception e) {
            log.error("------------------------获取token失败:{}", e);
        }
        return null;
    }
}
