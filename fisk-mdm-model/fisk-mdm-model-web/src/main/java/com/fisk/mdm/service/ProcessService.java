package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.vo.process.ProcessInfoVO;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程服务
 */
public interface ProcessService {
    /**
     * 保存流程
     * @param dto
     * @return
     */
    ResultEnum saveProcess(ProcessInfoDTO dto);

    ProcessInfoVO getProcess(Integer entityId);
}
