package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ISystemVariables {

    /**
     * 新增系统变量
     *
     * @param tableAccessId
     * @param dtoList
     * @return
     */
    ResultEnum addSystemVariables(Long tableAccessId, List<DeltaTimeDTO> dtoList);

    /**
     * 系统变量详情
     *
     * @param tableAccessId
     * @return
     */
    List<DeltaTimeDTO> getSystemVariable(Long tableAccessId);

}
