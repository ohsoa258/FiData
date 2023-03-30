package com.fisk.datagovernance.service.dataquality;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessFieldAssignPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessFieldAssignVO;

import java.util.List;

public interface IBusinessFilter_ProcessFieldAssignManageService extends IService<BusinessFilter_ProcessFieldAssignPO> {
    List<BusinessFilter_ProcessFieldAssignPO> getPOList(long ruleId);

    List<BusinessFilter_ProcessFieldAssignVO> getVOList(long ruleId);
}
