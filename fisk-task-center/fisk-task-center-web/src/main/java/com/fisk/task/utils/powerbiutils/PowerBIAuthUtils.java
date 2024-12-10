package com.fisk.task.utils.powerbiutils;

import com.fisk.common.framework.redis.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class PowerBIAuthUtils {
    @Resource
    private RedisUtil redisUtil;

    private static String AUTHORITY_URL = "https://login.microsoftonline.com/";
    private static String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static String SCOPE = "https://analysis.windows.net/powerbi/api/.default";

//    public static void main(String[] args) {
//        String tenantId = "cd6ec314-7506-4ed6-b46e-681443ca18e3";
//        String clientId = "7177dd3d-ccae-491a-b37a-918837aaa236";
//        String clientSecret = "7KH8Q~QlcU_pmviFq4NCYMXtIl2DlWo6VXVQQaI2";
//        PowerBIAuthUtils auth = new PowerBIAuthUtils(tenantId, clientId, clientSecret);
//        try {
//            String accessToken = auth.getAccessToken(dto.powerbiTenantId, dto.powerbiClientId, dto.powerbiClientSecret);
//            System.out.println("Access Token: " + accessToken);
//        } catch (IOException e) {
//            log.error("获取powerbi accessToken失败", e);
//        }
//    }

    public String getAccessToken(String tenantId, String clientId, String clientSecret) throws IOException {

        if (redisUtil.hasKey(clientId)) {
            return (String) redisUtil.get(clientId);
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("scope", SCOPE)
                .add("client_secret", clientSecret)
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(AUTHORITY_URL + tenantId + TOKEN_ENDPOINT)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("获取powerbi accessToken失败" + response);
                throw new IOException("Unexpected code " + response + "\n" + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
            String access_token = jsonObject.get("access_token").getAsString();
            redisUtil.set(clientId, access_token, 3600);
            return access_token;
        }
    }

}
