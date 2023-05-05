package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessSqlScriptPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessSqlScriptMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessSqlScriptMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessSqlScriptManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessSqlScriptVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessFilter_ProcessSqlScriptManageImpl
        extends ServiceImpl<BusinessFilter_ProcessSqlScriptMapper, BusinessFilter_ProcessSqlScriptPO>
        implements IBusinessFilter_ProcessSqlScriptManageService {

    @Override
    public List<BusinessFilter_ProcessSqlScriptPO> getPOList(long ruleId) {
        QueryWrapper<BusinessFilter_ProcessSqlScriptPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessSqlScriptPO::getDelFlag, 1)
                .eq(BusinessFilter_ProcessSqlScriptPO::getRuleId, ruleId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessSqlScriptVO> getVOList(long ruleId) {
        List<BusinessFilter_ProcessSqlScriptVO> voList = null;
        List<BusinessFilter_ProcessSqlScriptPO> poList = getPOList(ruleId);
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessSqlScriptMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
