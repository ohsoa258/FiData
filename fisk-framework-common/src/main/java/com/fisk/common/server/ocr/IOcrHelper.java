package com.fisk.common.server.ocr;

import com.fisk.common.server.ocr.dto.BaseRequestDTO;
import com.fisk.common.server.ocr.dto.BaseResponseDTO;
import com.fisk.common.server.ocr.dto.OcrResult;

/**
 * @author gy
 * @version 1.0
 * @description OCR功能抽象
 * @date 2022/5/31 15:31
 */
public interface IOcrHelper {

    /**
     * 图文识别
     *
     * @param request 请求参数
     * @return ocr识别结果
     */
    OcrResult<? extends BaseResponseDTO> invoke(BaseRequestDTO request);

}
