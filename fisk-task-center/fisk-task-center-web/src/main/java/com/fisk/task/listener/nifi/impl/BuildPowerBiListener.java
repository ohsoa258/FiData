package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.factory.TaskSettingEnum;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.PowerBiDataSetRefreshDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.nifi.IpowerBiListener;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.apache.http.NameValuePair;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpHeaders;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildPowerBiListener implements IpowerBiListener {
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private IPipelJobLog iPipelJobLog;
    @Resource
    private KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;

    @Override
    public ResultEnum powerBiTask(String data, Acknowledgment acke) {
        PowerBiDataSetRefreshDTO powerBiDataSetRefresh = new PowerBiDataSetRefreshDTO();
        log.info("执行POWERBI数据集刷新任务参数:{}", data);
        String zgetTokenUrl = "https://login.partner.microsoftonline.cn/common/oauth2/token";
        String zgetResourceUri = "https://api.powerbi.cn/v1.0/myorg/datasets/{0}/refreshes";
        String wgetTokenUrl = "https://login.partner.microsoftonline.com/common/oauth2/token";
        String wgetResourceUri = "https://api.powerbi.com/v1.0/myorg/datasets/{0}/refreshes";
        String pwd = "";
        String name = "";
        String clientId = "";
        String token = "";
        String dataSetId = "";
        //默认1中国,其次2外国
        String type = "";
        try {
            data = "[" + data + "]";
            List<PowerBiDataSetRefreshDTO> powerBiDataSetRefreshs = JSON.parseArray(data, PowerBiDataSetRefreshDTO.class);
            if (CollectionUtils.isNotEmpty(powerBiDataSetRefreshs)) {
                for (PowerBiDataSetRefreshDTO dto : powerBiDataSetRefreshs) {
                    powerBiDataSetRefresh = dto;
                    TaskHierarchyDTO taskHierarchy = iPipelineTaskPublishCenter.getTaskHierarchy(powerBiDataSetRefresh.pipelTraceId, powerBiDataSetRefresh.taskId);
                    NifiCustomWorkflowDetailDTO itselfPort = taskHierarchy.itselfPort;
                    // 查具体的配置
                    List<TaskSettingDTO> list = dataFactoryClient.getTaskSettingsByTaskId(itselfPort.pid);
                    Map<String, String> map = list.stream()
                            .collect(Collectors.toMap(TaskSettingDTO::getSettingKey, TaskSettingDTO::getValue));
                    // 执行POWERBI数据集刷新任务
                    log.info("POWERBI数据集刷新任务,配置:{}", JSON.toJSONString(map));
                    pwd = map.get(TaskSettingEnum.powerbi_data_set_refresh_pwd.getAttributeName());
                    name = map.get(TaskSettingEnum.powerbi_data_set_refresh_name.getAttributeName());
                    clientId = map.get(TaskSettingEnum.powerbi_data_set_refresh_client_id.getAttributeName());
                    type = map.get(TaskSettingEnum.powerbi_data_set_refresh_type.getAttributeName());
                    dataSetId = map.get(TaskSettingEnum.powerbi_data_set_refresh_data_set_id.getAttributeName());
                    ApiHttpRequestDTO apiHttpRequestDto = new ApiHttpRequestDTO();
                    if (StringUtils.equals("1", type)) {
                        token = getToken(zgetTokenUrl, name, pwd, clientId);
                        apiHttpRequestDto.uri = zgetResourceUri.replace("{0}", dataSetId);
                        apiHttpRequestDto.requestHeader = token;
                    } else if (StringUtils.equals("2", type)) {
                        token = getToken(wgetTokenUrl, name, pwd, clientId);
                        apiHttpRequestDto.uri = wgetResourceUri.replace("{0}", dataSetId);
                        apiHttpRequestDto.requestHeader = token;
                    }
                    // 调用第三方api返回的数据
                    refreshPowerbi(apiHttpRequestDto.uri, apiHttpRequestDto.requestHeader);
                    refreshPowerbiToDispatch(powerBiDataSetRefresh);
                }
            }
        } catch (Exception e) {
            log.error("执行POWERBI数据集刷新任务报错" + StackTraceHelper.getStackTraceInfo(e));
            DispatchExceptionHandlingDTO dispatchExceptionHandling = new DispatchExceptionHandlingDTO();
            dispatchExceptionHandling.pipelTraceId = powerBiDataSetRefresh.pipelTraceId;
            dispatchExceptionHandling.pipelTaskTraceId = powerBiDataSetRefresh.pipelTaskTraceId;
            dispatchExceptionHandling.pipelJobTraceId = powerBiDataSetRefresh.pipelJobTraceId;
            dispatchExceptionHandling.pipelStageTraceId = powerBiDataSetRefresh.pipelStageTraceId;
            dispatchExceptionHandling.comment = "执行POWERBI数据集刷新任务报错";
            iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandling);
            refreshPowerbiToDispatch(powerBiDataSetRefresh);
            throw new FkException(ResultEnum.ERROR);
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 调用管道下一级task
     *
     * @param dto dto
     * @author cfk
     * @date 2023/4/7 11:31
     */
    public void refreshPowerbiToDispatch(PowerBiDataSetRefreshDTO dto) {
        log.info("开始组装结束信息:{}", JSON.toJSONString(dto));
        KafkaReceiveDTO kafkaReceiveDTO = KafkaReceiveDTO.builder().build();
        kafkaReceiveDTO.pipelTraceId = dto.pipelTraceId;
        kafkaReceiveDTO.pipelTaskTraceId = dto.pipelTaskTraceId;
        kafkaReceiveDTO.pipelStageTraceId = dto.pipelStageTraceId;
        kafkaReceiveDTO.pipelJobTraceId = dto.pipelJobTraceId;
        kafkaReceiveDTO.tableType = OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue();
        NifiGetPortHierarchyDTO nifiGetPortHierarchy = new NifiGetPortHierarchyDTO();
        nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(dto.taskId);
        ResultEntity<TaskHierarchyDTO> nifiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
        if (nifiPortHierarchy.code == ResultEnum.SUCCESS.getCode()) {
            TaskHierarchyDTO data = nifiPortHierarchy.data;
            kafkaReceiveDTO.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + data.pipelineId + "." + OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue() + ".0." + dto.taskId;
        } else {
            log.error("POWERBI数据集刷新任务失败2" + nifiPortHierarchy.msg);
        }
        kafkaReceiveDTO.nifiCustomWorkflowDetailId = Long.valueOf(dto.taskId);
        kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
        log.info("执行脚本任务完成,现在去往任务发布中心" + JSON.toJSONString(kafkaReceiveDTO));
        kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, JSON.toJSONString(kafkaReceiveDTO));
    }

    public static void main(String[] args) {
        // 设置Authorization
        String authHeader = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Il9DSkFPdHlzWVZtNXhjMVlvSzBvUTdxeUJDUSIsImtpZCI6Il9DSkFPdHlzWVZtNXhjMVlvSzBvUTdxeUJDUSJ9.eyJhdWQiOiJodHRwczovL2FuYWx5c2lzLmNoaW5hY2xvdWRhcGkuY24vcG93ZXJiaS9hcGkiLCJpc3MiOiJodHRwczovL3N0cy5jaGluYWNsb3VkYXBpLmNuL2EwN2E1ZjVkLTIyOTEtNDU1MS05YzBlLTViOThhOGE1ZGZmZC8iLCJpYXQiOjE2ODA4NTAyNjIsIm5iZiI6MTY4MDg1MDI2MiwiZXhwIjoxNjgwODU0MTYyLCJhY2N0IjowLCJhY3IiOiIxIiwiYWlvIjoiQVRRQXkvOEtBQUFBdHdWMWxvYWZBZmU2eTVrN1dmWVhDcldqdmMvY0NYWHNhYVc4d1VPM2h1MXRRbmZyWnkvZEIwWmF4dnF6Z0dtaiIsImFtciI6WyJwd2QiXSwiYXBwaWQiOiI5YTllNmRjZC00ZmQyLTQzMjUtYTQxNS03Yjc5MDU3MGEzMzkiLCJhcHBpZGFjciI6IjAiLCJpcGFkZHIiOiIxOTguOC44NS4yNDgiLCJuYW1lIjoiQkkgQWRtaW4iLCJvaWQiOiJhMTJlYmY5Yi1iNDJhLTQzNjAtYTg0ZS0yNzVjYjhkZDllNjIiLCJwdWlkIjoiMTAwMzMyMzBDNUU5Njc3MyIsInJoIjoiMC5BUU1BWFY5Nm9KRWlVVVdjRGx1WXFLWGZfUWtBQUFBQUFBQUF3QUFBQUFBQUFBQUJBQ1UuIiwic2NwIjoiQXBwLlJlYWQuQWxsIENhcGFjaXR5LlJlYWQuQWxsIENhcGFjaXR5LlJlYWRXcml0ZS5BbGwgQ29udGVudC5DcmVhdGUgRGFzaGJvYXJkLlJlYWQuQWxsIERhc2hib2FyZC5SZWFkV3JpdGUuQWxsIERhdGFzZXQuUmVhZC5BbGwgRGF0YXNldC5SZWFkV3JpdGUuQWxsIEdhdGV3YXkuUmVhZC5BbGwgR2F0ZXdheS5SZWFkV3JpdGUuQWxsIFJlcG9ydC5SZWFkLkFsbCBSZXBvcnQuUmVhZFdyaXRlLkFsbCBXb3Jrc3BhY2UuUmVhZC5BbGwgV29ya3NwYWNlLlJlYWRXcml0ZS5BbGwiLCJzdWIiOiJ4LUJUWFJwM2tCUWhYdTFSZlZkcmw0amk4Y2s0c0RZeGo2TGstVFlRb1FZIiwidGlkIjoiYTA3YTVmNWQtMjI5MS00NTUxLTljMGUtNWI5OGE4YTVkZmZkIiwidW5pcXVlX25hbWUiOiJiaS5hZG1pbkBzZW5zZXRpbWUuY29tIiwidXBuIjoiYmkuYWRtaW5Ac2Vuc2V0aW1lLmNvbSIsInV0aSI6Im56SzlNMDV1N0VDUFB2WE51SGhfQVEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbImE5ZWE4OTk2LTEyMmYtNGM3NC05NTIwLThlZGNkMTkyODI2YyIsImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdfQ.rVDP9oNWR-rhhEmSsyLRUvcGG11OJeVe_KiJBjRnW1oJwdF7piopRC3-fCNDjZW852AEx7unbLOPNyg3CY7vIg5H_EkcfcNaJoipxFtXdgPXvUjnnJYh3kW0odvse3ZdKva_e_k1j9JXYryjeJserRHDxHW9XdzqyCr11Nbm_U7Qb8Z4MfdkIIo2FH97TT392ol6NzLM5ehqBZOxSPJnWALRzwjTYy03j6ez2ZwIsu3AhtOkdxLnsJZCRLEEb9toGurYlFFdPbo2Nu9-7K-OPzvS4VWcUePoJGmLAKrBbvw3wiPkf47OFR5Ovb-NfbW4nIGCcIiKT8s9cBT1WzFnRw";
        String zgetResourceUri = "https://api.powerbi.cn/v1.0/myorg/datasets/{0}/refreshes";
        zgetResourceUri = zgetResourceUri.replace("{0}", "d79985de-2ad0-4b90-bfc9-bf2340c266bf");
        // 创建HttpClient对象
        HttpClient httpClient = HttpClientBuilder.create().build();

        // 创建HttpGet请求对象，设置请求的URL
        HttpGet httpGet = new HttpGet(zgetResourceUri);

        // 设置Authorization头部
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        try {
            // 执行请求
            HttpResponse response = httpClient.execute(httpGet);

            // 获取响应实体
            HttpEntity entity = response.getEntity();

            // 打印响应内容
            if (entity != null) {
                System.out.println(EntityUtils.toString(entity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshPowerbi(String url, String authHeader) {
        // 设置Authorization
        //String authHeader = "Bearer " + "your_access_token_here";

        // 创建HttpClient对象
        HttpClient httpClient = HttpClientBuilder.create().build();

        // 创建HttpGet请求对象，设置请求的URL
        HttpGet httpGet = new HttpGet(url);

        // 设置Authorization头部
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        try {
            // 执行请求
            httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    /*public static void main(String[] args) throws IOException {
        // 设置请求参数
        String url = "https://login.partner.microsoftonline.cn/common/oauth2/token";
        String username = "bi.admin@sensetime.com";
        String password = "Berata~!09Suona@#12";
        String clientId = "9a9e6dcd-4fd2-4325-a415-7b790570a339";
        String grantType = "password";

        // 创建HTTP客户端
        CloseableHttpClient client = HttpClients.createDefault();

        // 创建POST请求
        HttpPost post = new HttpPost(url);

        // 创建参数列表
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("grant_type", grantType));
        params.add(new BasicNameValuePair("resource", "https://analysis.chinacloudapi.cn/powerbi/api"));

        // 创建UrlEncodedFormEntity
        HttpEntity entity = new UrlEncodedFormEntity(params);

        // 将请求体设置到POST请求中
        post.setEntity(entity);

        // 发送请求并获取响应
        HttpResponse response = client.execute(post);

        // 读取响应体中的内容
        String responseBody = EntityUtils.toString(response.getEntity());

        // 输出响应内容
        System.out.println("-------------------------" + responseBody);

        // 关闭HTTP客户端
        client.close();
    }*/

    public String getToken(String url, String username, String password, String clientId) {
        // 设置请求参数
        //String url = "https://login.partner.microsoftonline.cn/common/oauth2/token";
        //String username = "bi.admin@sensetime.com";
        //String password = "Berata~!09Suona@#12";
        //String clientId = "9a9e6dcd-4fd2-4325-a415-7b790570a339";
        String grantType = "password";

        // 创建HTTP客户端
        CloseableHttpClient client = HttpClients.createDefault();

        // 创建POST请求
        HttpPost post = new HttpPost(url);

        // 创建参数列表
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("client_id", clientId));
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
            log.error("------------------------获取token失败:{}", StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.ERROR, StackTraceHelper.getStackTraceInfo(e));
        }
    }
}
