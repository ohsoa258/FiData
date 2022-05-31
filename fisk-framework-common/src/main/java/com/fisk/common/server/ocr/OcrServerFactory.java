package com.fisk.common.server.ocr;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.ocr.service.aliyun.AliyunOcrHelper;

/**
 * @author GY
 * @date 2022/5/31 16:25
 * OCR工厂
 */
public class OcrServerFactory {

    public static IOcrHelper getOCRServer(OcrServerEnum type) {
        if (type == OcrServerEnum.ALIYUN) {
            return new AliyunOcrHelper();
        }
        throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
    }
}
