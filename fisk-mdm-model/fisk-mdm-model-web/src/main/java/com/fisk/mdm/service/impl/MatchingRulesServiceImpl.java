package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.mathingrules.MatchingRulesDto;
import com.fisk.mdm.entity.MatchingRulesPO;
import com.fisk.mdm.map.MatchingRulesMap;
import com.fisk.mdm.mapper.MatchingRulesMapper;
import com.fisk.mdm.service.IMatchingRulesFiledService;
import com.fisk.mdm.service.IMatchingRulesService;
import com.fisk.mdm.service.ISourceSystemMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author JinXingWang
 */
@Service
@Slf4j
public class MatchingRulesServiceImpl
        implements IMatchingRulesService {

    @Resource
    MatchingRulesMapper mapper;

    @Resource
    IMatchingRulesFiledService matchingRulesFiledService;

    @Resource
    ISourceSystemMappingService sourceSystemMappingService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum save(MatchingRulesDto dto) {
        //先删除后插
        LambdaQueryWrapper<MatchingRulesPO> deleteMatchingRulesWrapper=new LambdaQueryWrapper<>();
        deleteMatchingRulesWrapper.eq(MatchingRulesPO::getEntityId,dto.getEntityId());
        mapper.delete(deleteMatchingRulesWrapper);
        MatchingRulesPO matchingRulesPO = MatchingRulesMap.INSTANCES.dtoToPo(dto);
        Integer addMatchingRulesResult= mapper.insert(matchingRulesPO);
        //添加匹配字段
        ResultEnum  matchingRulesFiledResult =matchingRulesFiledService.add(matchingRulesPO.getId(),dto.getMatchingRulesFiledDtoList());
        //添加源系统映射
        ResultEnum sourceSystemMappingResult= sourceSystemMappingService.save(matchingRulesPO.getId(),dto.sourceSystemMappingDtoList);
        if (addMatchingRulesResult==0||matchingRulesFiledResult==ResultEnum.ERROR||sourceSystemMappingResult==ResultEnum.ERROR){
            throw  new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum get(Integer entityId) {
        return null;
    }

}
