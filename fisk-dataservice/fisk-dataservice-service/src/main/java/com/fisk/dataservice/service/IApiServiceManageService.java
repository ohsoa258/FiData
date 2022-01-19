package com.fisk.dataservice.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.vo.apiservice.ResponseVO;


import java.util.List;

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
    ResultEntity<ResponseVO> getData(RequstDTO dto);
}
