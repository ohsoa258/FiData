package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedQueryDTO;
/**
 * @author cfk
 */
public interface IBusinessLimited {
     /**
      * 获取限定列表
      * @param businessLimitedQueryDto
      * @return
      */
     Page<BusinessLimitedDTO> getBusinessLimitedDtoPage (BusinessLimitedQueryDTO businessLimitedQueryDto);
     /**
      * 删除
      * @param id
      * @return
      */
     ResultEnum deleteBusinessLimitedById(String id);
}
