package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.AddSourceSystemFiledMappingDto;

import java.util.List;

/**
 * @author JinXingWang
 */
public interface ISourceSystemFiledMappingService {
    ResultEnum save(List<AddSourceSystemFiledMappingDto> poList);
}
