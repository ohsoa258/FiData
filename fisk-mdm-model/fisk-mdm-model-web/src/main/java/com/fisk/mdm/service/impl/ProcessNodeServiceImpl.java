package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.ProcessInfoPO;
import com.fisk.mdm.entity.ProcessNodePO;
import com.fisk.mdm.mapper.ProcessNodeMapper;
import com.fisk.mdm.service.IProcessNodeService;
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
    public List<ProcessNodePO> getProcessNodes(Integer processInfoId) {
        LambdaQueryWrapper<ProcessNodePO> poLambdaQueryWrapper=new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessNodePO::getProcessId,processInfoId);
        return baseMapper.selectList(poLambdaQueryWrapper);
    }
}
