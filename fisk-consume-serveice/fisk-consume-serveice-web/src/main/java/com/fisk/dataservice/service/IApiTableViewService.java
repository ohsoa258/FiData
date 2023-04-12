package com.fisk.dataservice.service;

import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;

import java.util.List;

public interface IApiTableViewService {
    /**
     * 获取Api服务&&Table服务&&View服务的所有应用
     * @return
     */
    List<AppBusinessInfoDTO> getApiTableViewService();

    /**
     * 同步接入API服务来源表元数据
     * @return
     */
    List<MetaDataInstanceAttributeDTO> synchronizationAPIAppRegistration();
}
