package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.SourceSystemMappingDto;
import com.fisk.mdm.entity.SourceSystemMappingPO;

import java.util.List;

/**
 * @author JinXingWang
 */
public interface ISourceSystemMappingService {
    ResultEnum save(long matchingRulesId, List<SourceSystemMappingDto> dtoList);
    ResultEnum delete(long matchingRulesId);
}
