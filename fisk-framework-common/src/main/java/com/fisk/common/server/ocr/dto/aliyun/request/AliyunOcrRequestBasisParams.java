package com.fisk.common.server.ocr.dto.aliyun.request;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author gy
 * @version 1.0
 * @description 阿里云接口请求基础参数
 * @date 2022/5/31 16:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AliyunOcrRequestBasisParams {
    public String appCode;
    public String url;


    public void checkParams() {
        if (StringUtils.isEmpty(appCode)) {
            throw new FkException(ResultEnum.SYSTEM_PARAMS_ISEMPTY, "缺少AppCode");
        }
        if (StringUtils.isEmpty(url)) {
            throw new FkException(ResultEnum.SYSTEM_PARAMS_ISEMPTY, "缺少Url");
        }
    }
}
