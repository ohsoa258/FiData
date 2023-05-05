package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.access.DeltaTimeDTO;
import com.fisk.mdm.entity.SystemVariablesPO;

import java.util.List;

/**
 * @author wangjian
 */
public interface ISystemVariables extends IService<SystemVariablesPO> {

    /**
     * 新增系统变量
     *
     * @param id
     * @param dtoList
     * @return
     */
    ResultEnum addSystemVariables(Integer id, List<DeltaTimeDTO> dtoList);

    /**
     * 系统变量详情
     *
     * @param id
     * @return
     */
    List<DeltaTimeDTO> getSystemVariable(Integer id);

}
