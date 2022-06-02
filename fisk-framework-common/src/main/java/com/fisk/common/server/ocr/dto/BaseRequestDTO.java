package com.fisk.common.server.ocr.dto;

import com.fisk.common.server.ocr.dto.aliyun.request.AliyunOcrRequestBasisParams;
import lombok.Data;

/**
 * @author gy
 * @version 1.0
 * @description OCR接口请求参数对象基类
 * @date 2022/5/31 15:54
 */
@Data
public class BaseRequestDTO {
    public AliyunOcrRequestBasisParams basisParams;
    public Object requestParams;
}
