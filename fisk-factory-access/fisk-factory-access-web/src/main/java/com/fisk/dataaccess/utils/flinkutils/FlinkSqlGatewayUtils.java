package com.fisk.dataaccess.utils.flinkutils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class FlinkSqlGatewayUtils {

    private static final String GATEWAY_HOST = "http://192.168.1.92:8083";
    private static final String RESTAPI_HOST = "http://192.168.1.92:8081";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * 创建flink任务
     *
     * @param tableInfo
     */
    public static void buildFlinkJob(TableAccessPO tableInfo) {
        try {
            // 检查 SQL Gateway REST Endpoint 是否存在
            checkGatewayEndpoint();

            // 打开或获取一个会话
            String sessionHandle = openSession();
            log.info("Session Handle信息: " + sessionHandle);

            // 设置管道名称
            setPipelineName(sessionHandle, tableInfo.getTableName());

            // 设置检查点间隔
            setCheckpointInterval(sessionHandle);

            // 定义 source 表
            createSourceTable(sessionHandle, tableInfo.getSourceSql());

            // 定义 target 表
            createTargetTable(sessionHandle, tableInfo.getSinkSql());

            // 创建 Insert SQL Job
            createSqlJob(sessionHandle, tableInfo.getInsertSql());

            // 获取所有 Job 信息
            getAllJobsInfo();
        } catch (Exception e) {
            log.error("【buildFlinkJob error】建立FlinkJob任务失败，原因: " + e);
            throw new FkException(ResultEnum.FLINK_BUILD_JOB_ERROR);
        }
    }

    private static void checkGatewayEndpoint() throws IOException {
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/info")
                .build();
        try (Response response = client.newCall(request).execute()) {
            log.info("Gateway Info: " + Objects.requireNonNull(response.body()).string());
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

    /**
     * 设置管道名称 (flink job name)
     *
     * @param sessionHandle
     * @param tableName
     * @throws IOException
     */
    private static void setPipelineName(String sessionHandle, String tableName) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "SET 'pipeline.name' = '" + "SQLSERVER_CDC_" + tableName + "'");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            log.info("FIDATA Flink Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    /**
     * 设置检查点间隔
     *
     * @param sessionHandle
     * @throws IOException
     */
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
            log.info("FIDATA Flink Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    /**
     * 创建source表
     *
     * @param sessionHandle
     * @param sourceSql
     * @throws IOException
     */
    private static void createSourceTable(String sessionHandle, String sourceSql) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", sourceSql);
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            log.info("FIDATA Flink FIDATA Flink Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    /**
     * 创建target表
     *
     * @param sessionHandle
     * @throws IOException
     */
    private static void createTargetTable(String sessionHandle, String sinkSql) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", sinkSql);
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            log.info("FIDATA Flink Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    /**
     * 创建 Insert SQL Job
     *
     * @param sessionHandle
     * @throws IOException
     */
    private static void createSqlJob(String sessionHandle, String insertSql) throws IOException {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", insertSql);
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
            String operationHandleId = jsonResponse.get("operationHandle").getAsString();
            log.info("FIDATA Flink Operation Handle ID: " + operationHandleId);
            getOperationResult(sessionHandle, operationHandleId);
        }
    }

    /**
     * 获取所执行操作的结果
     *
     * @param sessionHandle
     * @param operationHandleId
     * @throws IOException
     */
    private static void getOperationResult(String sessionHandle, String operationHandleId) throws IOException {
        Request request = new Request.Builder()
                .url(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/operations/" + operationHandleId + "/result/0")
                .build();
        try (Response response = client.newCall(request).execute()) {
            log.info("Operation Result操作结果: " + response.body().string());
        }
    }

    /**
     * 获取所有 Job 信息
     *
     * @throws IOException
     */
    private static void getAllJobsInfo() throws IOException {
        Request request = new Request.Builder()
                .url(RESTAPI_HOST + "/v1/jobs/overview")
                .build();
        try (Response response = client.newCall(request).execute()) {
            log.info(GATEWAY_HOST + "：当前Flink上的所有任务信息: " + response.body().string());
        }
    }
}
