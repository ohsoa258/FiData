package com.fisk.datagovernance.service.impl.dataquality;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessFieldRulePO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessFieldRuleMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessFieldRuleMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessFieldRuleManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessFieldRuleVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessFilter_ProcessFieldRuleManageImpl
        extends ServiceImpl<BusinessFilter_ProcessFieldRuleMapper, BusinessFilter_ProcessFieldRulePO>
        implements IBusinessFilter_ProcessFieldRuleManageService {

    @Override
    public List<BusinessFilter_ProcessFieldRulePO> getPOList(long ruleId) {
        QueryWrapper<BusinessFilter_ProcessFieldRulePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessFieldRulePO::getDelFlag, 1)
                .eq(BusinessFilter_ProcessFieldRulePO::getRuleId, ruleId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessFieldRuleVO> getVOList(long ruleId) {
        List<BusinessFilter_ProcessFieldRuleVO> voList = null;
        List<BusinessFilter_ProcessFieldRulePO> poList = getPOList(ruleId);
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessFieldRuleMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
