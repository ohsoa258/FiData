package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IApiResultConfig {

    /**
     * api返回参数配置
     *
     * @param appDatasourceId
     * @param dto
     * @return
     */
    ResultEnum apiResultConfig(long appDatasourceId, List<ApiResultConfigDTO> dto);

}
