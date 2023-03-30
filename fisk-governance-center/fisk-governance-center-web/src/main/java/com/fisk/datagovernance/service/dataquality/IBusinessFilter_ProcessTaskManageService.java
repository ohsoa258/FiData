package com.fisk.datagovernance.service.dataquality;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessTaskPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTaskVO;

import java.util.List;

public interface IBusinessFilter_ProcessTaskManageService extends IService<BusinessFilter_ProcessTaskPO> {
    List<BusinessFilter_ProcessTaskPO> getPOList(long ruleId);

    List<BusinessFilter_ProcessTaskVO> getVOList(long ruleId);
}
