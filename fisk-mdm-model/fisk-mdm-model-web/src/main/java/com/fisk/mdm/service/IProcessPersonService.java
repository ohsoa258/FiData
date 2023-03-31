package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.mdm.entity.ProcessPersonPO;
import com.fisk.mdm.vo.process.ProcessPersonVO;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
public interface IProcessPersonService extends IService<ProcessPersonPO> {

    /**
     * 根据流程节点ids获取流程节点人员信息
     * @param processNodeIds
     * @return
     */
    List<ProcessPersonVO> getPersonByNodeIds(List<Integer> processNodeIds);
}
