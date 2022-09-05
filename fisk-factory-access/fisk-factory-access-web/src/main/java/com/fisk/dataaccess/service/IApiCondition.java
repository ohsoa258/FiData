package com.fisk.dataaccess.service;

import com.fisk.dataaccess.dto.apicondition.ApiConditionInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IApiCondition {

    /**
     * 获取api条件列表
     *
     * @return
     */
    List<ApiConditionInfoDTO> getApiConditionList();

}
