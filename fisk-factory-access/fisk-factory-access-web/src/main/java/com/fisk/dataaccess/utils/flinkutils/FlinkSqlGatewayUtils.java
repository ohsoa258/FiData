package com.fisk.dataaccess.utils.flinkutils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class FlinkSqlGatewayUtils {

    @Value("${flink-gateway-host}")
    private String GATEWAY_HOST;
    @Value("${flink-restapi-host}")
    private String RESTAPI_HOST;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * 创建flink任务
     *
     * @param tableInfo
     */
    public String buildFlinkJob(TableAccessPO tableInfo) {
        String jobId;
        try {
            // 检查 SQL Gateway REST Endpoint 是否存在
            checkGatewayEndpoint();

            // 打开或获取一个会话
            String sessionHandle = openSession();
            log.info("Session Handle信息: " + sessionHandle);

            // 设置管道名称
            setPipelineName(sessionHandle, tableInfo);

            // 设置检查点间隔
            setCheckpointInterval(sessionHandle);

            // 定义 source 表
            createSourceTable(sessionHandle, tableInfo.getSourceSql());

            // 定义 target 表
            createTargetTable(sessionHandle, tableInfo.getSinkSql());

            // 创建 Insert SQL Job
            jobId = createSqlJob(sessionHandle, tableInfo.getInsertSql());

            // 获取所有 Job 信息
            getAllJobsInfo();
        } catch (Exception e) {
            log.error("【buildFlinkJob error】建立FlinkJob任务失败，原因: " + e);
            throw new FkException(ResultEnum.FLINK_BUILD_JOB_ERROR, e);
        }
        return jobId;
    }

    private void checkGatewayEndpoint() throws Exception {
        Response response = sendGetRequest(GATEWAY_HOST + "/v1/info");
        log.info("Gateway Info: " + Objects.requireNonNull(response.body()).string());
    }

    private String openSession() throws IOException {
        RequestBody body = RequestBody.create(JSON, "{}");
        Response response = sendPostRequest(GATEWAY_HOST + "/v1/sessions", body);
        JsonObject jsonResponse = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);
        return jsonResponse.get("sessionHandle").getAsString();

    }

    /**
     * 设置管道名称 (flink job name)
     *
     * @param sessionHandle
     * @param tableInfo
     * @throws IOException
     */
    private void setPipelineName(String sessionHandle, TableAccessPO tableInfo) throws Exception {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "SET 'pipeline.name' = '" + "CDC_TASK_" + tableInfo.getTableName() + "_" + tableInfo.getId() + "'");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Response response = sendPostRequest(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements", body);
        JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
        String operationHandleId = jsonResponse.get("operationHandle").getAsString();
        log.info("FIDATA Flink setPipelineName Handle ID: " + operationHandleId);
        getOperationResult(sessionHandle, operationHandleId);

    }

    /**
     * 设置检查点间隔
     *
     * @param sessionHandle
     * @throws IOException
     */
    private void setCheckpointInterval(String sessionHandle) throws Exception {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", "SET execution.checkpointing.interval = 300s;");
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Response response = sendPostRequest(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements", body);
        JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
        String operationHandleId = jsonResponse.get("operationHandle").getAsString();
        log.info("FIDATA Flink setCheckpointInterval Handle ID: " + operationHandleId);
        getOperationResult(sessionHandle, operationHandleId);

    }

    /**
     * 创建source表
     *
     * @param sessionHandle
     * @param sourceSql
     * @throws IOException
     */
    private void createSourceTable(String sessionHandle, String sourceSql) throws Exception {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", sourceSql);
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Response response = sendPostRequest(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements", body);
        JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
        String operationHandleId = jsonResponse.get("operationHandle").getAsString();
        log.info("FIDATA Flink createSourceTable Handle ID: " + operationHandleId);
        getOperationResult(sessionHandle, operationHandleId);

    }

    /**
     * 创建target表
     *
     * @param sessionHandle
     * @throws IOException
     */
    private void createTargetTable(String sessionHandle, String sinkSql) throws Exception {
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", sinkSql);
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Response response = sendPostRequest(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements", body);
        JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
        String operationHandleId = jsonResponse.get("operationHandle").getAsString();
        log.info("FIDATA Flink createTargetTable Handle ID: " + operationHandleId);
        getOperationResult(sessionHandle, operationHandleId);

    }

    /**
     * 创建 Insert SQL Job
     *
     * @param sessionHandle
     * @throws IOException
     */
    private String createSqlJob(String sessionHandle, String insertSql) throws Exception {
        String jobId = null;
        JsonObject statementData = new JsonObject();
        statementData.addProperty("statement", insertSql);
        RequestBody body = RequestBody.create(JSON, statementData.toString());
        Response response = sendPostRequest(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/statements", body);
        JsonObject jsonResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
        String operationHandleId = jsonResponse.get("operationHandle").getAsString();
        log.info("FIDATA Flink createSqlJob Handle ID: " + operationHandleId);
        jobId = getOperationResultwithJobId(sessionHandle, operationHandleId);
        return jobId;
    }

    /**
     * 获取所执行操作的结果
     *
     * @param sessionHandle
     * @param operationHandleId
     * @throws IOException
     */
    private void getOperationResult(String sessionHandle, String operationHandleId) throws Exception {
        Response response = sendGetRequest(GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/operations/" + operationHandleId + "/result/0");
        log.info("Operation Result操作结果: " + response.body().string());

    }

    private String getOperationResultwithJobId(String sessionHandle, String operationHandleId) throws Exception {
        String resultUrl = GATEWAY_HOST + "/v1/sessions/" + sessionHandle + "/operations/" + operationHandleId + "/result/0";
        int maxRetries = 10; // 最大重试次数
        int retryIntervalMillis = 1000; // 重试间隔时间（毫秒）

        for (int i = 0; i < maxRetries; i++) {
            Response response = sendGetRequest(resultUrl);
            String result = response.body().string();
            log.info("getOperationResultwithJobId Result操作结果: " + result);

            // 解析结果以获取 jobId
            JsonObject resultJson = new Gson().fromJson(result, JsonObject.class);

            if (resultJson.has("jobID")) {
                return resultJson.get("jobID").getAsString();
            } else if (resultJson.has("nextResultUri")) {
                resultUrl = GATEWAY_HOST + resultJson.get("nextResultUri").getAsString();
                log.info("Result not ready, retrying in " + retryIntervalMillis + " ms...");
                Thread.sleep(retryIntervalMillis);
            } else {
                log.warn("getOperationResultwithJobId Result does not contain jobId or nextResultUri: " + result);
                return null;
            }
        }

        log.warn("Failed to get jobId after " + maxRetries + " retries.");
        return null;
    }


    /**
     * 获取所有 Job 信息
     *
     * @throws IOException
     */
    private void getAllJobsInfo() throws Exception {
        Response response = sendGetRequest(RESTAPI_HOST + "/v1/jobs/overview");
        log.info(GATEWAY_HOST + "：当前Flink上的所有任务信息: " + response.body().string());
    }

    /**
     * 终止指定job  source资源正常关闭才会终止
     *
     * @throws IOException
     */
    public String stopJob(String jobId) throws Exception {
        String url = RESTAPI_HOST + "/v1/jobs/" + jobId + "/stop";
        RequestBody body = RequestBody.create(JSON, "{}");
        Response response = sendPostRequest(url, body);
        return response.body().string();
    }


    /**
     * 发送HTTP请求并返回响应
     *
     * @param url 完整的请求URL
     * @return 响应对象
     * @throws IOException 如果请求失败
     */
    public static Response sendGetRequest(String url) throws Exception {
        log.info("FIDATA Flink sendGetRequest 发送请求url：" + url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request).execute();
    }

    /**
     * 发送HTTP POST请求并返回响应
     *
     * @param url  完整的请求URL
     * @param body JSON格式的请求体
     * @return 响应对象
     * @throws IOException 如果请求失败
     */
    public static Response sendPostRequest(String url, RequestBody body) throws IOException {
        log.info("FIDATA Flink sendPostRequest 发送请求url：" + url);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return client.newCall(request).execute();
    }

}
