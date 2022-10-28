package com.fisk.datafactory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.datasource.ExternalDataSourceDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.WorkflowTaskGroupDTO;
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
     * 添加管道组件集合
     *
     * @param list list
     * @return 执行结果
     */
    List<NifiCustomWorkflowDetailDTO> addDataList(List<NifiCustomWorkflowDetailDTO> list);

    /**
     * 修改管道组件集合
     *
     * @param list list
     * @return 执行结果
     */
    ResultEnum editDataList(List<NifiCustomWorkflowDetailDTO> list);

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
     * access or model 删除操作时,task要同步删除这些数据
     *
     * @param list list
     * @return 执行结果
     */
    ResultEnum editDataByDeleteTable(List<DeleteTableDetailDTO> list);

    /**
     * 获取外部数据源集合
     *
     * @return
     */
    List<ExternalDataSourceDTO> getExternalDataSourceList();

}
