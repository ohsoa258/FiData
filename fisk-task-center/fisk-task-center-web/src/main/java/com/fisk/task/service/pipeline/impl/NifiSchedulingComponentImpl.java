package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessorEntity;
import com.davis.client.model.ProcessorRunStatusEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.enums.ScheduleEnum;
import com.fisk.task.mapper.NifiSchedulingComponentMapper;
import com.fisk.task.po.NifiSchedulingComponentPO;
import com.fisk.task.service.pipeline.INifiSchedulingComponentService;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author cfk
 */
@Service
@Slf4j
public class NifiSchedulingComponentImpl extends ServiceImpl<NifiSchedulingComponentMapper, NifiSchedulingComponentPO> implements INifiSchedulingComponentService {

    @Override
    public ResultEnum runOnce(Long nifiCustomWorkflowDetailId) {
        try {
            LambdaQueryWrapper<NifiSchedulingComponentPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NifiSchedulingComponentPO::getNifiCustomWorkflowDetailId,nifiCustomWorkflowDetailId);
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
                    NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(),processorRunStatusEntity);
                    processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUNNING);
                    NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(),processorRunStatusEntity);
                    break;
                case CRON_DRIVEN:
                    processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
                    NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(),processorRunStatusEntity);
                    processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUN_ONCE);
                    NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(),processorRunStatusEntity);
                    processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUNNING);
                    NifiHelper.getProcessorsApi().updateRunStatus(one.getComponentId(),processorRunStatusEntity);
                    break;
                default:
                    break;
            }
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.TASK_PUBLISH_ERROR);
        }
        return ResultEnum.SUCCESS;
    }
}
