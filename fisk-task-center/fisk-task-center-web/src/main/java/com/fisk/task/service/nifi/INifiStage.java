package com.fisk.task.service.nifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.task.dto.nifi.NifiStageMessageDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.entity.NifiStagePO;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

/**
 * @author cfk
 */
public interface INifiStage {

    /**
     * 获取一张表的nifi阶段
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return NifiStagePO
     */
    List<NifiStageDTO> getNifiStage(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO);

    /**
     * 更新一张表nifi流程的运行状态
     *
     * @param nifiStagePO nifiStagePO
     * @return NifiStagePO
     */
    NifiStagePO saveNifiStage(String nifiStagePO, Acknowledgment acke);


}
