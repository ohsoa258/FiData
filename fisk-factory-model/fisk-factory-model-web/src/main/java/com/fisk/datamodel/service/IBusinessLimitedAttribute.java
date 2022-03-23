package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedAddDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;

/**
 * @author cfk
 */
public interface IBusinessLimitedAttribute {

    /**
     * 更新业务限定字段
     * @param dto
     * @return
     */
    ResultEnum updateBusinessLimitedAttribute(BusinessLimitedAttributeDataDTO dto);

    /**
     * 删除业务限定字段
     * @param id
     * @return
     */
    ResultEnum delBusinessLimitedAttribute(int id);

    /**
     * 添加业务限定字段
     * @param dto
     * @return
     */
    ResultEnum addBusinessLimitedAttribute(BusinessLimitedAttributeDataDTO dto);


}
