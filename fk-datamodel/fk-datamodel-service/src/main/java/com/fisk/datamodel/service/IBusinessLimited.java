package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDataDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedQueryDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedUpdateDTO;
import com.fisk.datamodel.entity.BusinessLimitedPO;

import java.util.List;

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
     /**
      * 业务限定下拉
      * @param factId
      * @return
      */
     List<BusinessLimitedPO> getBusinessLimitedList(String factId);
     /**
      * 根据业务限定id,获取业务限定信息以及业务限定字段列表
      * @param businessLimitedId
      * @return
      */
     BusinessLimitedDataDTO getBusinessLimitedAndAttributeList(int businessLimitedId);

     /**
      * 修改业务限定
      * @param dto
      * @return
      */
     ResultEnum BusinessLimitedUpdate(BusinessLimitedUpdateDTO dto);
}
