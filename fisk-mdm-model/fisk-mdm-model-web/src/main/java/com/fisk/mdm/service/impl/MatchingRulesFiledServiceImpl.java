package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.MatchingRulesFiledDto;
import com.fisk.mdm.entity.MatchingRulesFiledPO;
import com.fisk.mdm.map.MatchingRulesFiledMap;
import com.fisk.mdm.mapper.MatchingRulesFiledMapper;
import com.fisk.mdm.service.IMatchingRulesFiledService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JinXingWang
 */
@Service
@Slf4j
public class MatchingRulesFiledServiceImpl
        extends ServiceImpl<MatchingRulesFiledMapper,MatchingRulesFiledPO>
        implements IMatchingRulesFiledService {
    @Resource
    MatchingRulesFiledMapper mapper;

    @Override
    public ResultEnum add(long matchingRulesId,List<MatchingRulesFiledDto> dtoList) {
        List<MatchingRulesFiledPO> matchingRulesFiledPOS= MatchingRulesFiledMap.INSTANCES.dtoToPo(dtoList);
        matchingRulesFiledPOS.stream().forEach(e->{
            e.setMatchingRulesId(matchingRulesId);
        });
        this.saveBatch(matchingRulesFiledPOS);
        return  ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum save(long matchingRulesId, List<MatchingRulesFiledDto> dtoList) {
        ResultEnum deleteResult= delete(matchingRulesId);
        ResultEnum addResult= add(matchingRulesId,dtoList);
        return addResult!=ResultEnum.SUCCESS?ResultEnum.SUCCESS:ResultEnum.ERROR;
    }

    public ResultEnum delete(long matchingRulesId){
        LambdaQueryWrapper<MatchingRulesFiledPO> queryWrapper= new  LambdaQueryWrapper<>();
        queryWrapper
                .eq(MatchingRulesFiledPO::getMatchingRulesId,matchingRulesId);
        mapper.delete(queryWrapper);
        return ResultEnum.SUCCESS;
    }
}
