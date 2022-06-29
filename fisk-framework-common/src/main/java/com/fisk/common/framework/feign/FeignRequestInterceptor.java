package com.fisk.common.framework.feign;

import com.fisk.common.core.constants.TraceConstant;
import com.fisk.common.framework.mdc.MDCHelper;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author gy
 * @version 1.0
 * @description Feign请求拦截，设置traceId
 * @date 2022/6/28 17:01
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String traceId = MDCHelper.getTraceId();
        if (StringUtils.isNotEmpty(traceId)) {
            requestTemplate.header(TraceConstant.HTTP_HEADER_TRACE, traceId);
        }
    }
}