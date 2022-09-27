package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.flink.FlinkApiConfigDTO;
import com.fisk.dataaccess.dto.flink.FlinkConfigDTO;
import com.fisk.dataaccess.service.IFlinkApi;
import com.fisk.dataaccess.utils.httprequest.HttpRequestHelper;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
public class FlinkApiImpl implements IFlinkApi {

    @Resource
    FlinkConfigDTO flinkConfig;
    @Resource
    FlinkApiConfigDTO flinkApiConfig;

    @Override
    public String savePoints(String jobId) {
        String url = flinkApiConfig.host + ":" + flinkApiConfig.port + flinkApiConfig.savepoints;
        //参数占位符替换
        url = url.replace(":jobid", jobId);
        //请求参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cancel-job", true);
        jsonObject.put("target-directory", flinkConfig.savePointsPath);

        String result = HttpRequestHelper.post(url, jsonObject.toJSONString());
        JSONObject parseObject = JSONObject.parseObject(result);
        return parseObject.getString("request-id");
    }

    @Override
    public ResultEnum savePointsStatus(String jobId, String triggerId) {

        return ResultEnum.ACCEPTED;
    }

}
