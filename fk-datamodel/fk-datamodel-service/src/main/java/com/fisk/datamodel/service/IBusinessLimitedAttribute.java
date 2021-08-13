package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedAddDTO;

/**
 * @author cfk
 */
public interface IBusinessLimitedAttribute {
    /**
     * 更新限定条件
     * @param businessLimitedAddDto
     * @return
     */
    ResultEnum updateBusinessLimitedAttribute(BusinessLimitedAddDTO businessLimitedAddDto);
    /**
     * 获取限定条件详情
     * @param businessLimitedId
     * @return
     */
    BusinessLimitedAddDTO getBusinessLimitedAttribute(String businessLimitedId);


}
