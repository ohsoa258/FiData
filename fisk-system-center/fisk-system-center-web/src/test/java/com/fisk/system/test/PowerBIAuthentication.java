package com.fisk.system.test;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

public class PowerBIAuthentication {
    private static final String AUTHORITY_URL = "https://login.microsoftonline.com/";
    private static final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static final String SCOPE = "https://analysis.windows.net/powerbi/api/.default";

    private final String tenantId;
    private final String clientId;
    private final String clientSecret;

    public PowerBIAuthentication(String tenantId, String clientId, String clientSecret) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() throws IOException {
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
                throw new IOException("Unexpected code " + response + "\n" + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
            return jsonObject.get("access_token").getAsString();
        }
    }

    public static void main(String[] args) {
        String tenantId = "cd6ec314-7506-4ed6-b46e-681443ca18e3";
        String clientId = "7177dd3d-ccae-491a-b37a-918837aaa236";
        String clientSecret = "7KH8Q~QlcU_pmviFq4NCYMXtIl2DlWo6VXVQQaI2";

        PowerBIAuthentication auth = new PowerBIAuthentication(tenantId, clientId, clientSecret);
        try {
            String accessToken = auth.getAccessToken();
            System.out.println("Access Token: " + accessToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
