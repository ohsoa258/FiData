package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.MatchingRulesDto;
import com.fisk.mdm.dto.mathingrules.UpdateMatchingRulesDto;

public interface IMatchingRulesService {
    ResultEnum save(MatchingRulesDto dto);
    ResultEnum get(Integer entityId);
}
