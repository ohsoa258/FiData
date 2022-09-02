package com.fisk.dataaccess.service;

import com.fisk.dataaccess.dto.apicondition.ApiConditionDTO;

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
    List<ApiConditionDTO> getApiConditionList();

}
