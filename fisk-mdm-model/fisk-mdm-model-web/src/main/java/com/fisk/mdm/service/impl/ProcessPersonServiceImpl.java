package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.ProcessPersonPO;
import com.fisk.mdm.map.ProcessPersonMap;
import com.fisk.mdm.mapper.ProcessPersonMapper;
import com.fisk.mdm.service.IProcessPersonService;
import com.fisk.mdm.vo.process.ProcessPersonVO;
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
    public List<ProcessPersonVO> getPersonByNodeIds(List<Integer> processNodeIds) {
        if (processNodeIds.size()>0){
            LambdaQueryWrapper<ProcessPersonPO> processPersonPOLambdaQueryWrapper=new LambdaQueryWrapper<>();
            processPersonPOLambdaQueryWrapper.eq(ProcessPersonPO::getDelFlag,1)
                    .in(ProcessPersonPO::getRocessNodeId,processNodeIds);
            List<ProcessPersonPO> personPOS = this.getBaseMapper()
                    .selectList(processPersonPOLambdaQueryWrapper);
            if (personPOS.size()>0){
                return ProcessPersonMap.INSTANCES.poListToVoList(personPOS);
            }
        }
        return null;
    }
}
