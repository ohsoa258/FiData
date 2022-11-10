package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.apioutputparameter.ApiOutputParameterDTO;
import com.fisk.dataaccess.vo.output.apioutputparameter.ApiOutputParameterVO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IApiOutputParameter {

    /**
     * 新增数据目标配置
     *
     * @param dataTargetId
     * @param dtoList
     * @return
     */
    ResultEnum addApiOutputParameter(Long dataTargetId, List<ApiOutputParameterDTO> dtoList);

    /**
     * 获取数据目标参数
     *
     * @param dataTargetId
     * @return
     */
    List<ApiOutputParameterVO> getApiOutputParameter(Long dataTargetId);

}
