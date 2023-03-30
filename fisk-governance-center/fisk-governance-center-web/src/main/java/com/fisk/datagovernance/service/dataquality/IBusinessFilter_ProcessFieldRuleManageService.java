package com.fisk.datagovernance.service.dataquality;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessFieldRulePO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessFieldRuleVO;

import java.util.List;

public interface IBusinessFilter_ProcessFieldRuleManageService extends IService<BusinessFilter_ProcessFieldRulePO>
{
    List<BusinessFilter_ProcessFieldRulePO> getPOList(long ruleId);

    List<BusinessFilter_ProcessFieldRuleVO> getVOList(long ruleId);
}
