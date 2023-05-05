package com.fisk.datagovernance.service.impl.dataquality;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessFieldAssignPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessFieldAssignMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessFieldAssignMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessFieldAssignManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessFieldAssignVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessFilter_ProcessFieldAssignManageImpl
        extends ServiceImpl<BusinessFilter_ProcessFieldAssignMapper, BusinessFilter_ProcessFieldAssignPO>
        implements IBusinessFilter_ProcessFieldAssignManageService {

    @Override
    public List<BusinessFilter_ProcessFieldAssignPO> getPOList(long ruleId) {
        QueryWrapper<BusinessFilter_ProcessFieldAssignPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessFieldAssignPO::getDelFlag, 1)
                .eq(BusinessFilter_ProcessFieldAssignPO::getRuleId, ruleId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessFieldAssignVO> getVOList(long ruleId) {
        List<BusinessFilter_ProcessFieldAssignVO> voList = null;
        List<BusinessFilter_ProcessFieldAssignPO> poList = getPOList(ruleId);
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessFieldAssignMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
