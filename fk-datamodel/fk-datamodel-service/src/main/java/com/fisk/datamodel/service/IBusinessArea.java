package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;

import java.util.Map;

/**
 * @author: Lock
 */
public interface IBusinessArea extends IService<BusinessAreaPO> {
    ResultEnum addData(BusinessAreaDTO businessAreaDTO);

    BusinessAreaDTO getData(long id);

    ResultEnum updateBusinessArea(BusinessAreaDTO businessAreaDTO);

    ResultEnum deleteBusinessArea(long id);

    Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows);
}
