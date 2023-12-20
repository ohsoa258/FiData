package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.model.ProcessorEntity;
import com.davis.client.model.ProcessorRunStatusEntity;
import com.davis.client.model.ProcessorStatusDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.task.enums.ScheduleEnum;
import com.fisk.task.mapper.NifiSchedulingComponentMapper;
import com.fisk.task.po.NifiSchedulingComponentPO;
import com.fisk.task.service.pipeline.INifiSchedulingComponentService;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author cfk
 */
@Service
@Slf4j
public class NifiSchedulingComponentImpl extends ServiceImpl<NifiSchedulingComponentMapper, NifiSchedulingComponentPO> implements INifiSchedulingComponentService {

    @Resource
    RedisUtil redisUtil;

    @Override
    public ResultEnum runOnce(Long nifiCustomWorkflowDetailId) {
        try {
            boolean setnx = redisUtil.setnx(RedisKeyEnum.DISPATCH_RUN_ONCE.getName() + ":" + nifiCustomWorkflowDetailId, 100, TimeUnit.SECONDS);
            if (setnx) {
                boolean flag = false;
                LambdaQueryWrapper<NifiSchedulingComponentPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(NifiSchedulingComponentPO::getNifiCustomWorkflowDetailId, nifiCustomWorkflowDetailId);
                NifiSchedulingComponentPO one = this.getOne(queryWrapper);
                ProcessorEntity processorEntity = NifiHelper.getProcessorsApi().getProcessor(one.getComponentId());
                ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
                processorRunStatusEntity.setRevision(processorEntity.getRevision());
                processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
                String schedulingStrategy = processorEntity.getComponent().getConfig().getSchedulingStrategy();
                ScheduleEnum scheduleEnum = ScheduleEnum.valueOf(schedulingStrategy);
                switch (scheduleEnum) {
                    case TIMER_DRIVEN:
                        processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
                        NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(), processorRunStatusEntity);
                        ProcessorEntity processor = null;
                        int i = 0;
                        do {
                            Thread.sleep(2000);
                            processor = NifiHelper.getProcessorsApi().getProcessor(one.getComponentId());
                            if (processor.getStatus().getRunStatus() == ProcessorStatusDTO.RunStatusEnum.STOPPED) {
                                flag = true;
                                log.info("rounce第一步停止组件成功");
                            }
                            i++;
                        } while (processor.getStatus().getRunStatus() != ProcessorStatusDTO.RunStatusEnum.STOPPED || i == 3);
                        if (flag == true) {
                            processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUNNING);
                            NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(), processorRunStatusEntity);
                        }
                        break;
                    case CRON_DRIVEN:
                        processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
                        NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(), processorRunStatusEntity);
                        ProcessorEntity processor1 = null;
                        int b = 0;
                        do {
                            Thread.sleep(2000);
                            processor1 = NifiHelper.getProcessorsApi().getProcessor(one.getComponentId());
                            if (processor1.getStatus().getRunStatus() == ProcessorStatusDTO.RunStatusEnum.STOPPED) {
                                flag = true;
                                log.info("rounce第一步停止组件成功");
                            }
                            b++;
                        } while (processor1.getStatus().getRunStatus() != ProcessorStatusDTO.RunStatusEnum.STOPPED || b == 3);
                        processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUN_ONCE);
                        NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(), processorRunStatusEntity);
                        Thread.sleep(1000);
                        processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUNNING);
                        NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(), processorRunStatusEntity);
                        break;
                    default:
                        break;
                }
                if (flag == true) {
                    return ResultEnum.SUCCESS;
                } else {
                    return ResultEnum.RUN_ONCE_ERROR;
                }
            } else {
                return ResultEnum.RUN_ONCE_LOCK;
            }

        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.RUN_ONCE_ERROR;
        } finally {
            redisUtil.del(RedisKeyEnum.DISPATCH_RUN_ONCE.getName() + ":" + nifiCustomWorkflowDetailId);
        }

    }
}
