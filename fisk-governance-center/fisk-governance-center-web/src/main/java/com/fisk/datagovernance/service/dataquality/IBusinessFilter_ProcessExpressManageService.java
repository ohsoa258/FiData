package com.fisk.datagovernance.service.dataquality;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessExpressPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessExpressVO;

import java.util.List;

public interface IBusinessFilter_ProcessExpressManageService extends IService<BusinessFilter_ProcessExpressPO> {
    List<BusinessFilter_ProcessExpressPO> getPOList(long ruleId);

    List<BusinessFilter_ProcessExpressVO> getVOList(long ruleId);
}
