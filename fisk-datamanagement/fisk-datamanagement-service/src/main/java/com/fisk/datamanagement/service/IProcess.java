package com.fisk.datamanagement.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.process.AddProcessDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IProcess {

    /**
     * 添加process
     * @param dto
     * @return
     */
    ResultEnum addProcess(AddProcessDTO dto);

}
