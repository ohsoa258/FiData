package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.ProcessNodePO;
import com.fisk.mdm.entity.ProcessPersonPO;
import com.fisk.mdm.mapper.ProcessPersonMapper;
import com.fisk.mdm.service.IProcessPersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Slf4j
@Service
public class ProcessPersonServiceImpl extends ServiceImpl<ProcessPersonMapper, ProcessPersonPO> implements IProcessPersonService {
    @Override
    public List<ProcessPersonPO> getProcessPersons(List<Integer> processNodeIds) {
        LambdaQueryWrapper<ProcessPersonPO> poLambdaQueryWrapper=new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.in(ProcessPersonPO::getRocessNodeId,processNodeIds);
        return baseMapper.selectList(poLambdaQueryWrapper);
    }

    @Override
    public List<ProcessPersonPO> getProcessPersons(Integer processNodeId) {
        LambdaQueryWrapper<ProcessPersonPO> poLambdaQueryWrapper=new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessPersonPO::getRocessNodeId,processNodeId);
        return baseMapper.selectList(poLambdaQueryWrapper);
    }
}
