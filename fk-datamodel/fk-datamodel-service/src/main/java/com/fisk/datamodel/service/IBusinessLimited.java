package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedQueryDTO;
/**
 * @author cfk
 */
public interface IBusinessLimited {
     //获取限定列表
     Page<BusinessLimitedDTO> getBusinessLimitedDTOPage (BusinessLimitedQueryDTO businessLimitedQueryDTO);
     //删除
     ResultEnum deleteBusinessLimitedById(String id);
}
