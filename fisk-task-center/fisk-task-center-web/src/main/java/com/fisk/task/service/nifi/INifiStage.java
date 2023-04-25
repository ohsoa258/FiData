package com.fisk.task.service.nifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
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
     * @param list
     * @return NifiStagePO
     */
    List<NifiStageDTO> getNifiStage(List<NifiCustomWorkflowDetailDTO> list);

    /**
     * 更新一张表nifi流程的运行状态
     *
     * @param nifiStagePO nifiStagePO
     * @return NifiStagePO
     */
    NifiStagePO saveNifiStage(String nifiStagePO, Acknowledgment acke);

    /**
     * 建模覆盖方式代码预览
     *
     * @param dto
     * @return
     */
    Object overlayCodePreview(OverLoadCodeDTO dto);

}
