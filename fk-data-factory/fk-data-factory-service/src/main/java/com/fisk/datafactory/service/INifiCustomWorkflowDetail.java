package com.fisk.datafactory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;

/**
 * @author Lock
 */
public interface INifiCustomWorkflowDetail extends IService<NifiCustomWorkflowDetailPO> {
    /**
     * 添加管道详情
     *
     * @param dto dto
     * @return 执行结果
     */
    NifiCustomWorkflowDetailDTO addData(NifiCustomWorkflowDetailDTO dto);

    /**
     * 回显数据
     *
     * @param id id
     * @return dto
     */
    NifiCustomWorkflowDetailDTO getData(long id);

    /**
     * 修改管道详情
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<NifiCustomWorkListDTO> editData(NifiCustomWorkflowDetailVO dto);

    /**
     * 删除管道详情
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 修改单个管道组件
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editWorkflow(NifiCustomWorkflowDetailDTO dto);
}
