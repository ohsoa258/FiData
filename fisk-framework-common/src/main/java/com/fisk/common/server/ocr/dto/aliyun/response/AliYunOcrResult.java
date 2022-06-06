package com.fisk.common.server.ocr.dto.aliyun.response;

import com.fisk.common.server.ocr.dto.BaseResponseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @description 阿里云OCR图文识别接口返回对象
 * @date 2022/5/31 15:41
 */
@Data
public class AliYunOcrResult extends BaseResponseDTO {

    /**
     * 唯一id，用于问题定位
     */
    public String sid;

    /**
     * 算法版本
     */
    public String prism_version;

    /**
     * 识别的文字块的数量，prism_wordsInfo数组大小
     */
    public Integer prism_wnum;

    /**
     * 识别的文字的具体内容
     */
    public List<WorksInfo> prism_wordsInfo;

    public Integer height;

    public Integer width;

    public Integer orgHeight;

    public Integer orgWidth;

    /**
     * 完整内容
     */
    public String content;
}
