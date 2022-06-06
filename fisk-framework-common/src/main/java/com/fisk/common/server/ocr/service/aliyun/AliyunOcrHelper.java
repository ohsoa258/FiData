package com.fisk.common.server.ocr.service.aliyun;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.ocr.IOcrHelper;
import com.fisk.common.server.ocr.dto.BaseRequestDTO;
import com.fisk.common.server.ocr.dto.OcrResult;
import com.fisk.common.server.ocr.dto.aliyun.request.AliyunOcrRequestParams;
import com.fisk.common.server.ocr.dto.aliyun.response.AliYunOcrResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * @author gy
 * @version 1.0
 * @description 阿里云OCR图文识别接口实现
 * @date 2022/5/31 15:58
 */
@Slf4j
public class AliyunOcrHelper implements IOcrHelper {

    @Override
    public OcrResult<AliYunOcrResult> invoke(BaseRequestDTO request) {
        if (Objects.isNull(request) || Objects.isNull(request.basisParams) || Objects.isNull(request.requestParams)) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }
        request.basisParams.checkParams();
        if (request.requestParams instanceof AliyunOcrRequestParams) {
            ((AliyunOcrRequestParams) request.requestParams).checkParams();
        } else {
            throw new FkException(ResultEnum.PARAMTER_ERROR, "错误的参数类型，参数类型应是: AliyunRequest");
        }

        log.debug("阿里云OCR图文识别接口准备调用，参数：【{}】", JSON.toJSONString(request));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "AppCode " + request.basisParams.appCode);
        HttpEntity<String> requestObj = new HttpEntity<>(JSON.toJSONString(request.requestParams), headers);
        try {
            ResponseEntity<AliYunOcrResult> responseObj = restTemplate.exchange(request.basisParams.url, HttpMethod.POST, requestObj, AliYunOcrResult.class);
            return buildResultObject(responseObj);
        } catch (Exception ex) {
            log.error("阿里云OCR图文识别接口调用失败，错误信息：", ex);
            OcrResult<AliYunOcrResult> res = new OcrResult<>();
            res.success = false;
            res.msg = "调用失败，" + ex.getMessage();
            return res;
        }

    }


    private OcrResult<AliYunOcrResult> buildResultObject(ResponseEntity<AliYunOcrResult> responseObj) {
        String errorMsg = null;
        switch (responseObj.getStatusCode()) {
            case OK:
                OcrResult<AliYunOcrResult> res = new OcrResult<>();
                res.success = true;
                res.msg = "调用成功";
                res.data = responseObj.getBody();
                return res;
            case BAD_REQUEST:
                errorMsg = "请求参数有误";
                break;
            case UNAUTHORIZED:
                errorMsg = "您无该功能的权限，请开通后使用";
                break;
            case FORBIDDEN:
                errorMsg = "购买的容量已用完或者签名错误";
                break;
            case INTERNAL_SERVER_ERROR:
                errorMsg = "服务器错误，请稍后重试";
                break;
            default:
                errorMsg = "未知错误信息";
                break;
        }
        log.error("请求失败，" + errorMsg + "。ex: " + JSON.toJSONString(responseObj));

        OcrResult<AliYunOcrResult> res = new OcrResult<>();
        res.success = false;
        res.msg = "调用失败，" + errorMsg;
        return res;
    }

}
