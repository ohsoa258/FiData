package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.service.IPipelineProcessMonitor;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/3/17 16:42
 */
@Service
@Slf4j
public class PipelineProcessMonitorImpl implements IPipelineProcessMonitor {

    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private NifiCustomWorkflowDetailImpl customWorkflowDetailImpl;

    @Override
    public List<PipelineTableLogDTO> getPipelineMonitorLogs(String workflowId) {

        // 1.根据管道workflowId查询表组件
        List<NifiCustomWorkflowDetailPO> listPo = customWorkflowDetailImpl.query()
                .eq("workflow_id", workflowId)
                // 过滤开始组件
                .ne("components_id", 1)
                // 过滤任务组组件
                .ne("components_id", 2).list();

        if (CollectionUtils.isNotEmpty(listPo)) {
            // 2.po->dto
            List<NifiCustomWorkflowDetailDTO> listDto = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(listPo);
            // 3.feign调用task,获取入参表的日志与状态
            ResultEntity<List<PipelineTableLogDTO>> result = publishTaskClient.getPipelineTableLogs(listDto);
            if (result.code == ResultEnum.SUCCESS.getCode()) {
                return result.data;
            } else {
                log.error("远程调用失败，方法名：【task-center:getPipelineTableLogs】");
                return null;
            }
        }
        return null;
    }

    @Override
    public NifiStageDTO getNifiStage(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {

        if (nifiCustomWorkflowDetailDTO == null) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }
        ResultEntity<List<NifiStageDTO>> result = publishTaskClient.getNifiStage(nifiCustomWorkflowDetailDTO);
        List<NifiStageDTO> data = result.data;
        if (result.code == ResultEnum.SUCCESS.getCode()&&CollectionUtils.isNotEmpty(data)) {
            // todo 后续改为list
            return result.data.get(0);
        } else {
            log.error("远程调用失败，方法名：【task-center:getNifiStage】");
            return null;
        }
    }
}
