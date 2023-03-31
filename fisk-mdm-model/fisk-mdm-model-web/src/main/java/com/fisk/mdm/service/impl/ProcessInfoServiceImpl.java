package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.entity.ProcessInfoPO;
import com.fisk.mdm.map.ProcessInfoMap;
import com.fisk.mdm.mapper.ProcessInfoMapper;
import com.fisk.mdm.service.IProcessInfoService;
import com.fisk.mdm.vo.process.ProcessInfoVO;
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
    public ProcessInfoVO getProcessInfo(Integer entityId) {
        LambdaQueryWrapper<ProcessInfoPO> processInfoPOLambdaQueryWrapper=new LambdaQueryWrapper<>();
        processInfoPOLambdaQueryWrapper.eq(ProcessInfoPO::getEntityId,entityId)
                .eq(ProcessInfoPO::getDelFlag,1);
        ProcessInfoPO processInfoPO = baseMapper.selectOne(processInfoPOLambdaQueryWrapper);
        if (processInfoPO != null){
            return ProcessInfoMap.INSTANCES.poToVo(processInfoPO);
        }
        return null;
    }

    @Override
    public ResultEnum deleteProcessInfo(Integer entityId) {
        LambdaQueryWrapper<ProcessInfoPO> processInfoPOLambdaQueryWrapper=new LambdaQueryWrapper<>();
        processInfoPOLambdaQueryWrapper.eq(ProcessInfoPO::getDelFlag,1)
                .eq(ProcessInfoPO::getEntityId,entityId);
        baseMapper.delete(processInfoPOLambdaQueryWrapper);
        return ResultEnum.SUCCESS;
    }
}
