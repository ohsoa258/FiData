package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.ProcessNodePO;
import com.fisk.mdm.map.ProcessNodeMap;
import com.fisk.mdm.mapper.ProcessNodeMapper;
import com.fisk.mdm.service.IProcessNodeService;
import com.fisk.mdm.vo.process.ProcessNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Slf4j
@Service
public class ProcessNodeServiceImpl extends ServiceImpl<ProcessNodeMapper, ProcessNodePO> implements IProcessNodeService {
    @Override
    public List<ProcessNodeVO> getProcessNode(Integer processInfoId) {
        LambdaQueryWrapper<ProcessNodePO> processNodePOLambdaQueryWrapper=new LambdaQueryWrapper<>();
        processNodePOLambdaQueryWrapper.eq(ProcessNodePO::getProcessId,processInfoId)
                .eq(ProcessNodePO::getDelFlag,1);
        List<ProcessNodePO> processNodePOS = this.getBaseMapper()
                .selectList(processNodePOLambdaQueryWrapper);
        if (processNodePOS.size()>0){
            return ProcessNodeMap.INSTANCES.poListToVoList(processNodePOS);
        }
        return null;
    }
}
