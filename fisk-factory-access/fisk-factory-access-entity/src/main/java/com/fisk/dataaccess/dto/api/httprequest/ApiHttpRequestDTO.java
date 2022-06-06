package com.fisk.dataaccess.dto.api.httprequest;

import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.enums.HttpRequestEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author Lock
 * @version 1.3
 * @description 调用第三方api--请求参数dto
 * @date 2022/4/28 11:58
 */
@Data
public class ApiHttpRequestDTO {
    @ApiModelProperty(value = "请求方式", required = true)
    public HttpRequestEnum httpRequestEnum;
    @ApiModelProperty(value = "请求地址", required = true)
    public String uri;
    @ApiModelProperty(value = "post请求携带的请求参数raw-josn类型")
    public JSONObject jsonObject;
    //    @ApiModelProperty(value = "post请求携带的请求参数form-data类型")
//    public Map<String, String> formDataParams;
    @ApiModelProperty(value = "post请求携带的请求头信息")
    public Map<String, String> headersParams;
    @ApiModelProperty(value = "请求头的token信息")
    public String requestHeader;
    @ApiModelProperty(value = "post请求携带的请求参数form-data类型")
    public Map<String, String> formDataParams;

    /**
     * jwt身份验证方式的账户&密码
     */
    public JwtRequestDTO jwtRequestDTO;

    /**
     * OAuth 2.0身份验证方式的账户&密码
     */
    public JwtRequestDTO oauth2;

    /**
     * 返回数据的结构key
     */
    public String jsonDataKey;
}
