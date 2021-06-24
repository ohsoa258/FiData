package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.dto.BusinessNameDTO;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.entity.DataAreaPO;

import java.util.List;

/**
 * @author: Lock
 */
public interface IDataArea extends IService<DataAreaPO> {

    List<BusinessNameDTO> getBusinessName();

    ResultEnum addData(DataAreaDTO dataAreaDTO);

    DataAreaDTO getData(long id);

    ResultEnum updateDataArea(DataAreaDTO dataAreaDTO);

    ResultEnum deleteDataArea(long id);
}
