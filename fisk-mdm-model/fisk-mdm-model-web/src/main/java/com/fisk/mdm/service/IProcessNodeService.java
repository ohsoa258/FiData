package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.mdm.entity.ProcessNodePO;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
public interface IProcessNodeService extends IService<ProcessNodePO> {
    /**
     * 根据流程id获取流程节点
     *
     * @param processInfoId
     * @return
     */
    List<ProcessNodePO> getProcessNodes(Integer processInfoId);
}
