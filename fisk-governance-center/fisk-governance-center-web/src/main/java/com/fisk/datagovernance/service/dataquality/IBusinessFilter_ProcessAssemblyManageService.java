package com.fisk.datagovernance.service.dataquality;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessAssemblyPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessAssemblyVO;

import java.util.List;

public interface IBusinessFilter_ProcessAssemblyManageService extends IService<BusinessFilter_ProcessAssemblyPO>
{
    List<BusinessFilter_ProcessAssemblyPO> getPOList();

    List<BusinessFilter_ProcessAssemblyVO> getVOList();
}
