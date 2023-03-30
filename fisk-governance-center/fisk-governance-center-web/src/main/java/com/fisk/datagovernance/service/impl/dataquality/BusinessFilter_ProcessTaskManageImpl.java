package com.fisk.datagovernance.service.impl.dataquality;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessTaskPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessTaskMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessTaskMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessTaskManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTaskVO;

import java.util.List;

public class BusinessFilter_ProcessTaskManageImpl
        extends ServiceImpl<BusinessFilter_ProcessTaskMapper, BusinessFilter_ProcessTaskPO>
        implements IBusinessFilter_ProcessTaskManageService {

    @Override
    public List<BusinessFilter_ProcessTaskPO> getPOList(long ruleId) {
        QueryWrapper<BusinessFilter_ProcessTaskPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessTaskPO::getDelFlag, 1)
                .eq(BusinessFilter_ProcessTaskPO::getRuleId, ruleId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessTaskVO> getVOList(long ruleId) {
        List<BusinessFilter_ProcessTaskVO> voList = null;
        List<BusinessFilter_ProcessTaskPO> poList = getPOList(ruleId);
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessTaskMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
