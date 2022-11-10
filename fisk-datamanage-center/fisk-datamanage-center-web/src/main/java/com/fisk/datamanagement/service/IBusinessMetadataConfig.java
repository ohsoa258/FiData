package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.businessmetadataconfig.BusinessMetadataConfigDTO;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-06-28 14:25
 */
public interface IBusinessMetadataConfig {

    /**
     * 获取业务元数据配置列表
     *
     * @return
     */
    List<BusinessMetadataConfigDTO> getBusinessMetadataConfigList();

}
