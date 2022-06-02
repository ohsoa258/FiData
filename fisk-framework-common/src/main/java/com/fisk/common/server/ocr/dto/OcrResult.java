package com.fisk.common.server.ocr.dto;

import lombok.Data;

/**
 * @author gy
 * @version 1.0
 * @description OCR服务处理结果对象
 * @date 2022/5/31 15:33
 */
@Data
public class OcrResult<T> {
    public boolean success;
    public String msg;
    public T data;
}
