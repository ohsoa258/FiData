package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.AreaBusinessDTO;

/**
 * @author Lock
 */
public interface IAreaBusiness{

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(AreaBusinessDTO dto);

    /**
     * 回显
     *
     * @param id id
     * @return 查询结果
     */
    AreaBusinessDTO getDataById(long id);
}
