package com.fisk.task.service.nifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.task.dto.AccessDataSuccessAndFailCountDTO;
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
    void saveNifiStage(String nifiStagePO, Acknowledgment acke);

    /**
     * 建模覆盖方式代码预览
     *
     * @param dto
     * @return
     */
    Object overlayCodePreview(OverLoadCodeDTO dto);

    /**
     * 数据接入--首页展示信息--当日接入数据总量
     *
     * @return
     */
    Long accessDataTotalCount();

    /**
     * 数据接入--首页展示信息--当日接入数据的成功次数和失败次数
     *
     * @return
     */
    AccessDataSuccessAndFailCountDTO accessDataSuccessAndFailCount();
}
