package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.entity.ProcessInfoPO;
import com.fisk.mdm.vo.process.ProcessInfoVO;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
public interface IProcessInfoService extends IService<ProcessInfoPO> {

    /**
     * 根据实体id获取流程实例
     * @param entityId
     * @return
     */
    ProcessInfoVO getProcessInfo(Integer entityId);

    /**
     * 根据实体id逻辑删除流程实例
     * @param entityId
     * @return
     */
    ResultEnum deleteProcessInfo(Integer entityId);
}
