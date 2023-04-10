package com.fisk.datagovernance.service.impl.dataquality;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessExpressPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessExpressMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessExpressMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessExpressManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessExpressVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessFilter_ProcessExpressManageImpl
        extends ServiceImpl<BusinessFilter_ProcessExpressMapper, BusinessFilter_ProcessExpressPO>
        implements IBusinessFilter_ProcessExpressManageService {

    @Override
    public List<BusinessFilter_ProcessExpressPO> getPOList(long ruleId) {
        QueryWrapper<BusinessFilter_ProcessExpressPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessExpressPO::getDelFlag, 1)
                .eq(BusinessFilter_ProcessExpressPO::getRuleId, ruleId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessExpressVO> getVOList(long ruleId) {
        List<BusinessFilter_ProcessExpressVO> voList = null;
        List<BusinessFilter_ProcessExpressPO> poList = getPOList(ruleId);
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessExpressMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
