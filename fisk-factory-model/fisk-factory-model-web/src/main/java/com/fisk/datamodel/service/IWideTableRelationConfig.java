package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.widetablerelationconfig.WideTableRelationConfigDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IWideTableRelationConfig {

    /**
     * 宽表关联关系配置
     *
     * @param wideTableId
     * @param dtoList
     * @return
     */
    ResultEnum wideTableRelationConfig(int wideTableId, List<WideTableRelationConfigDTO> dtoList);

}
