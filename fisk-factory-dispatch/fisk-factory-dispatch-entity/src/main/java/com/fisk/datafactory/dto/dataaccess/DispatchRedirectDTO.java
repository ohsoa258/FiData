package com.fisk.datafactory.dto.dataaccess;

import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 2.5
 * @description 调度跳转页面的详情
 * @date 2022/6/14 14:59
 */
@Data
public class DispatchRedirectDTO {

    /**
     * 管道
     */
    private NifiCustomWorkflowDTO pipeDto;

    /**
     * 管道下的组件
     */
    private List<NifiCustomWorkflowDetailDTO> componentList;
}
