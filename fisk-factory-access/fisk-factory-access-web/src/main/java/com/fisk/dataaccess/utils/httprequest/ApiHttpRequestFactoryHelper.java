package com.fisk.dataaccess.utils.httprequest;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.utils.httprequest.Impl.BuildHttpRequestImpl;

/**
 * @author Lock
 * @version 1.3
 * @description 调用第三方api--http请求帮助类
 * @date 2022/4/28 11:03
 */
public class ApiHttpRequestFactoryHelper {


    public static IBuildHttpRequest buildHttpRequest(ApiHttpRequestDTO dto) {

        switch (dto.httpRequestEnum) {
            case GET:
            case POST:
                return new BuildHttpRequestImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
