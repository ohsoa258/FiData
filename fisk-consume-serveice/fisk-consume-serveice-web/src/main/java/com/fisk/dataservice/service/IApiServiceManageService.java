package com.fisk.dataservice.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataservice.dto.apiservice.RequestEncryptDTO;
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * api服务接口
 * @author dick
 */
public interface IApiServiceManageService {

    /**
     * 获取token
     * @param dto dto
     * @return token
     */
    ResultEntity<Object> getToken(TokenDTO dto);

    /**
     * 获取数据
     * @param dto 请求参数
     * @return 数据
     */
    ResultEntity<Object> getData(RequstDTO dto);

    /**
     * 获取解密key
     * @param dto 请求参数
     * @return 数据
     */
    ResultEntity<Object> getEncryptKey(RequestEncryptDTO dto);

    /**
     * API代理
     * @return 数据
     */
    void proxy(HttpServletRequest request, HttpServletResponse response);
}
