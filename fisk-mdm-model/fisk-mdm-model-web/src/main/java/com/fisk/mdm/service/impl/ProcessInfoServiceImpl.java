package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.entity.ProcessInfoPO;
import com.fisk.mdm.mapper.ProcessInfoMapper;
import com.fisk.mdm.service.IProcessInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Slf4j
@Service
public class ProcessInfoServiceImpl extends ServiceImpl<ProcessInfoMapper, ProcessInfoPO> implements IProcessInfoService {
    @Override
    public ProcessInfoPO getProcessInfo(Integer entityId) {
        LambdaQueryWrapper<ProcessInfoPO> poLambdaQueryWrapper=new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessInfoPO::getDelFlag,1)
                .eq(ProcessInfoPO::getEntityId,entityId);
        return baseMapper.selectOne(poLambdaQueryWrapper);
    }

    @Override
    public ResultEnum deleteProcessInfo(Integer entityId) {
        LambdaQueryWrapper<ProcessInfoPO> poLambdaQueryWrapper=new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessInfoPO::getDelFlag,1)
                .eq(ProcessInfoPO::getEntityId,entityId);
        baseMapper.delete(poLambdaQueryWrapper);
        return ResultEnum.SUCCESS;
    }
}
