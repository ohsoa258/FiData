package com.fisk.datafactory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.WorkflowTaskGroupDTO;
import com.fisk.datafactory.dto.json.TableJsonSourceDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;

import java.util.List;

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
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 修改单个管道组件
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editWorkflow(NifiCustomWorkflowDetailDTO dto);

    /**
     * 删除单个任务组
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum deleteDataList(WorkflowTaskGroupDTO dto);

    /**
     * 根据不同的类型,获取不同tree
     *
     * @param dto dto
     * @return tree
     */
    List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto);

    /**
     * 查询当前任务下的组件详情集合
     *
     * @param id id
     * @return 执行结果
     */
    List<NifiCustomWorkflowDetailDTO> getComponentList(long id);

    /**
     * @param dto
     * @return
     */
    ChannelDataDTO buildJson(TableJsonSourceDTO dto);
}
