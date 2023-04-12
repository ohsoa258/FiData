package com.fisk.apiservice.service;

import com.fisk.apiservice.dto.dataservice.RequstDTO;
import com.fisk.apiservice.dto.dataservice.TokenDTO;
import com.fisk.common.core.response.ResultEntity;

/**
 * api服务接口
 * @author dick
 */
public interface IDataServiceManageService {

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
}
