package com.fisk.dataservice.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;

import java.util.List;

/**
 * @author wangyan
 */
public interface ApiConfigureFieldService {

    /**
     * 接口字段保存方法
     * @param dto
     * @param apiName
     * @param apiInfo
     * @param distinctData
     * @return
     */
    ResultEnum saveConfigureField(List<ApiConfigureFieldPO> dto,String apiName,String apiInfo,Integer distinctData);
}
