package com.fisk.datagovernance.service.dataquality;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessTriggerPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTriggerVO;

import java.util.List;

public interface IBusinessFilter_ProcessTriggerManageService extends IService<BusinessFilter_ProcessTriggerPO>
{
    List<BusinessFilter_ProcessTriggerPO> getPOList(long ruleId);

    List<BusinessFilter_ProcessTriggerVO> getVOList(long ruleId);
}
