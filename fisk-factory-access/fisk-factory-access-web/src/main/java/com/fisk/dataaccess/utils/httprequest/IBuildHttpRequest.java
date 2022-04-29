package com.fisk.dataaccess.utils.httprequest;

import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/4/28 11:11
 */
public interface IBuildHttpRequest {

    /**
     * 请求第三方平台api返回的参数
     *
     * @param dto dto
     * @return jsonObject
     */
    JSONObject httpRequest(ApiHttpRequestDTO dto);

    /**
     * 获取api的临时token
     *
     * @param dto dto
     * @return token
     */
    String getRequestToken(ApiHttpRequestDTO dto);
}
