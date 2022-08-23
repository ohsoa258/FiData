package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.widetablefieldconfig.WideTableFieldConfigsDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IWideTableFieldConfig {

    /**
     * 宽表字段配置
     *
     * @param wideTableId
     * @param dtoList
     * @return
     */
    ResultEnum wideTableFieldConfig(int wideTableId, List<WideTableFieldConfigsDTO> dtoList);

    /**
     * 获取宽表字段
     *
     * @param wideTableId
     * @return
     */
    List<WideTableFieldConfigsDTO> getWideTableFieldConfig(int wideTableId);

}
