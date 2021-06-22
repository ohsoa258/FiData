package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;

/**
 * @author: Lock
 */
public interface IBusinessArea extends IService<BusinessAreaPO> {
    ResultEnum addData(BusinessAreaDTO businessAreaDTO);

    BusinessAreaDTO getData(long id);

    ResultEnum updateBusinessArea(BusinessAreaDTO businessAreaDTO);

    ResultEnum deleteBusinessArea(long id);

    PageDTO<BusinessAreaDTO> listBusinessArea(String key, Integer page, Integer rows);
}
