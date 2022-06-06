package com.fisk.common.server.ocr.dto.aliyun.request;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author gy
 * @version 1.0
 * @description 阿里云OCR接口请求参数对象
 * @date 2022/5/31 15:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AliyunOcrRequestParams {

    /**
     * 图像数据：base64编码，要求base64编码后大小不超过25M，最短边至少15px，最长边最大8192px，支持jpg/png/bmp格式，和url参数只能同时存在一个
     */
    public String img;

    /**
     * 图像url地址：图片完整URL，URL长度不超过1024字节，URL对应的图片base64编码后大小不超过25M，最短边至少15px，最长边最大8192px，支持jpg/png/bmp格式，和img参数只能同时存在一个
     */
    public String url;

    /**
     * 是否需要识别结果中每一行的置信度，默认不需要。 true：需要 false：不需要
     */
    public Boolean prob;

    /**
     * 是否需要单字识别功能，默认不需要。 true：需要 false：不需要
     */
    public Boolean charInfo;

    /**
     * 是否需要自动旋转功能，默认不需要。 true：需要 false：不需要
     */
    public Boolean rotate;

    /**
     * 目前不支持表格功能
     * 是否需要表格识别功能，默认不需要。 true：需要 false：不需要
     */
    public Boolean table;

    /**
     * 字块返回顺序，false表示从左往右，从上到下的顺序，true表示从上到下，从左往右的顺序，默认false
     */
    public Boolean sortPage;

    /**
     * 是否需要去除印章功能，默认不需要。true：需要 false：不需要
     */
    public Boolean noStamp;

    /**
     * 是否需要图案检测功能，默认不需要。true：需要 false：不需要
     */
    public Boolean figure;

    /**
     * 是否需要成行返回功能，默认不需要。true：需要 false：不需要
     */
    public Boolean row;

    /**
     * 是否需要分段功能，默认不需要。true：需要 false：不需要
     */
    public Boolean paragraph;


    public void checkParams() throws FkException {
        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(img)) {
            throw new FkException(ResultEnum.PARAMTER_ERROR, "Url参数和Img参数不能同时传递");
        }
    }
}
