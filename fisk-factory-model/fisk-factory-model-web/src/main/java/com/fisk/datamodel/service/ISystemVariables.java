package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.datamodel.enums.CreateTypeEnum;

import java.util.List;

/**
 * @author SongJianJian
 */
public interface ISystemVariables {

    /**
     * 新增系统变量
     *
     * @param id
     * @param dtoList
     * @param value
     * @return
     */
    ResultEnum addSystemVariables(Integer id, List<DeltaTimeDTO> dtoList, int value);

    /**
     * 系统变量详情
     *
     * @param tableAccessId
     * @return
     */
    List<DeltaTimeDTO> getSystemVariable(Integer id, Integer type);

}
