package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.mdm.entity.ProcessPersonPO;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
public interface IProcessPersonService extends IService<ProcessPersonPO> {
    /**
     * 根据节点ids获取节点人员实例
     *
     * @param processNodeIds
     * @return
     */
    List<ProcessPersonPO> getProcessPersons(List<Integer> processNodeIds);
    /**
     * 根据节点id获取节点人员实例
     * @param processNodeId
     * @return
     */
    List<ProcessPersonPO> getProcessPersons(Integer processNodeId);
}
