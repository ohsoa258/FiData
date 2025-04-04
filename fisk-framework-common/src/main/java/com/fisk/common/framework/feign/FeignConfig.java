package com.fisk.common.framework.feign;

import com.fisk.common.core.constants.SystemConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gy
 */
@Component
public class FeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //添加token
            requestTemplate.header(SystemConstants.HTTP_HEADER_AUTH, request.getHeader(SystemConstants.HTTP_HEADER_AUTH));
        }
    }
}
