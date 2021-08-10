package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedAddDTO;

/**
 * @author cfk
 */
public interface IBusinessLimitedAttribute {
    //更新限定条件
    ResultEnum updateBusinessLimitedAttribute(BusinessLimitedAddDTO businessLimitedAddDTO);
    //获取限定条件详情
    BusinessLimitedAddDTO getBusinessLimitedAttribute(String businessLimitedId);


}
