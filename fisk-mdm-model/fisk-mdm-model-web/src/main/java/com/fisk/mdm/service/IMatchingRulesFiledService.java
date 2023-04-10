package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.MatchingRulesFiledDto;

import java.util.List;

/**
 * @author JinXingWang
 */
public interface IMatchingRulesFiledService {
    ResultEnum add(long matchingRulesId, List<MatchingRulesFiledDto> dtoList);
    ResultEnum save(long matchingRulesId, List<MatchingRulesFiledDto> dtoList);
}
