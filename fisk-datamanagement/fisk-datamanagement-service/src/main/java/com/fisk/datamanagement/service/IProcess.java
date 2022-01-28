package com.fisk.datamanagement.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.process.AddProcessDTO;
import com.fisk.datamanagement.dto.process.ProcessDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IProcess {

    /**
     * 获取process详情
     * @param processGuid
     * @return
     */
    ProcessDTO getProcess(String processGuid);

    /**
     * 添加process
     * @param dto
     * @return
     */
    ResultEnum addProcess(AddProcessDTO dto);

    /**
     * 修改process
     * @param dto
     * @return
     */
    ResultEnum updateProcess(ProcessDTO dto);

}
