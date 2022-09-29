package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.flink.FlinkApiConfigDTO;
import com.fisk.dataaccess.dto.flink.FlinkConfigDTO;
import com.fisk.dataaccess.service.IFlinkApi;
import com.fisk.dataaccess.utils.httprequest.HttpRequestHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class FlinkApiImpl implements IFlinkApi {

    @Resource
    FlinkConfigDTO flinkConfig;
    @Resource
    FlinkApiConfigDTO flinkApiConfig;

    private static String savePointStatus = "COMPLETED";

    @Override
    public String savePoints(String jobId, String folder) {
        String url = flinkApiConfig.host + ":" + flinkApiConfig.port + flinkApiConfig.savepoints;
        //参数占位符替换
        url = url.replace(":jobid", jobId);
        //请求参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cancel-job", true);
        jsonObject.put("target-directory", flinkConfig.savePointsPath + folder);

        String result = HttpRequestHelper.post(url, jsonObject.toJSONString());
        JSONObject parseObject = JSONObject.parseObject(result);
        return parseObject.getString("request-id");
    }

    @Override
    public String savePointsStatus(String jobId, String triggerId) {
        String url = flinkApiConfig.host + ":" + flinkApiConfig.port + flinkApiConfig.savepointStatus;
        //参数占位符替换
        url = url.replace(":jobid", jobId).replaceAll(":triggerid", triggerId);
        String result = HttpRequestHelper.get(url);
        if (result.indexOf(savePointStatus) > -1) {
            JSONObject resultJson = JSONObject.parseObject(result);
            String operation = resultJson.getString("operation");
            JSONObject operationJson = JSONObject.parseObject(operation);
            String location = operationJson.getString("location");
            return location.replace("file:", "");
        }
        return null;
    }

}
