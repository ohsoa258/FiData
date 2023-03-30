package com.fisk.datagovernance.service.impl.dataquality;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessTriggerPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessTriggerMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessTriggerMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessTriggerManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTriggerVO;

import java.util.List;

public class BusinessFilter_ProcessTriggerManageImpl
        extends ServiceImpl<BusinessFilter_ProcessTriggerMapper, BusinessFilter_ProcessTriggerPO>
        implements IBusinessFilter_ProcessTriggerManageService {

    @Override
    public List<BusinessFilter_ProcessTriggerPO> getPOList(long ruleId) {
        QueryWrapper<BusinessFilter_ProcessTriggerPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessTriggerPO::getDelFlag, 1)
                .eq(BusinessFilter_ProcessTriggerPO::getRuleId, ruleId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessTriggerVO> getVOList(long ruleId) {
        List<BusinessFilter_ProcessTriggerVO> voList = null;
        List<BusinessFilter_ProcessTriggerPO> poList = getPOList(ruleId);
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessTriggerMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
