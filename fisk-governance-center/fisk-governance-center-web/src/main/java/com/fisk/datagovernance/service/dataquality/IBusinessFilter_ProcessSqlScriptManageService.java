package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessSqlScriptPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessSqlScriptVO;

import java.util.List;

public interface IBusinessFilter_ProcessSqlScriptManageService extends IService<BusinessFilter_ProcessSqlScriptPO> {
    List<BusinessFilter_ProcessSqlScriptPO> getPOList(long ruleId);

    List<BusinessFilter_ProcessSqlScriptVO> getVOList(long ruleId);
}