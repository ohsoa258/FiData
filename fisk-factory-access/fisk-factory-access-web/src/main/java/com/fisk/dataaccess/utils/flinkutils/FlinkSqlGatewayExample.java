package com.fisk.dataaccess.utils.flinkutils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class FlinkSqlGatewayExample {

    private static final String GATEWAY_HOST = "http://192.168.1.92:8083";
    private static final String RESTAPI_HOST = "http://192.168.1.92:8081";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        // 检查 SQL Gateway REST Endpoint 是否存在
        checkGatewayEndpoint();

        // 打开或获取一个会话
        String sessionHandle = openSession();
        System.out.println("Session Handle: " + sessionHandle);

        // 设置管道名称
        setPipelineName(sessionHandle);

        // 设置检查点间隔
        setCheckpointInterval(sessionHandle);

        // 定义 source 表
        createSourceTable(sessionHandle);

        // 定义 target 表
        createTargetTable(sessionHandle);

        // 创建 SQL Job
        createSqlJob(sessionHandle);

        // 获取所有 Job 信息
        getAllJobsInfo();
    }

    private static void checkGatewayEndpoint() throws IOException {
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/info")
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Gateway Info: " + Objects.requireNonNull(response.body()).string());
        }
    }

    private static String openSession() throws IOException {
        RequestBody body = RequestBody.create(JSON, "{}");
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);
            return jsonResponse.get("sessionHandle").getAsString();
        }
    }

    private static void setPipelineName(String sessionHandle) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "SET 'pipeline.name' = 'SQLSERVER_CDC'");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            System.out.println("Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    private static void setCheckpointInterval(String sessionHandle) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "SET execution.checkpointing.interval = 10s;");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            System.out.println("Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    private static void createSourceTable(String sessionHandle) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "CREATE TABLE source_userinfo (" +
                "id INT," +
                "username STRING," +
                "sex STRING," +
                "age INT," +
                "address STRING," +
                "PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH (" +
                "'connector' = 'sqlserver-cdc'," +
                "'hostname' = '192.168.11.133'," +
                "'port' = '1433'," +
                "'username' = 'sa'," +
                "'password' = 'Password01!'," +
                "'database-name' = 'flink'," +
                "'table-name' = 'dbo.userinfo'" +
                ");");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            System.out.println("Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    private static void createTargetTable(String sessionHandle) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "CREATE TABLE target_userinfo (" +
                "    id INT," +
                "    username VARCHAR(255)," +
                "    sex VARCHAR(1)," +
                "    age INT," +
                "    address VARCHAR(255)," +
                "    PRIMARY KEY(id) NOT ENFORCED" +
                ") WITH (" +
                "    'connector' = 'jdbc'," +
                "    'url' = 'jdbc:sqlserver://192.168.1.35:1433;DatabaseName=dmp_ods;trustServerCertificate=true'," +
                "    'driver' = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'," +
                "    'username' = 'sa'," +
                "    'password' = 'password01!'," +
                "    'table-name' = 'dbo.userinfo'" +
                ");\n");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            System.out.println("Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    private static void createSqlJob(String sessionHandle) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "INSERT INTO target_userinfo SELECT id, username, sex, age, address FROM source_userinfo;");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            System.out.println("Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    private static void getOperationResult(String sessionHandle, String operationHandleId) throws IOException {
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/operations/" + operationHandleId + "/result/0")
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Operation Result: " + response.body().string());
        }
    }

    private static void getAllJobsInfo() throws IOException {
        Request request = new Request.Builder()
                .url(RESTAPI_HOST + "/v1/jobs/overview")
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Jobs Info: " + response.body().string());
        }
    }
}
