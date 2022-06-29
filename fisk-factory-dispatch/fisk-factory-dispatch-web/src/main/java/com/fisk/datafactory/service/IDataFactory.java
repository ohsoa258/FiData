package com.fisk.datafactory.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.PipeDagDTO;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.dto.dispatchlog.PipelStageLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/11 11:54
 */
public interface IDataFactory {
    /**
     * 判断物理表是否在管道使用
     *
     * @param dto dto
     * @return boolean
     */
    boolean loadDepend(LoadDependDTO dto);

    /**
     * 获取当前组件的层级关系
     *
     * @param dto dto
     * @return 查询结果
     */
    ResultEntity<NifiPortsHierarchyDTO> getNifiPortHierarchy(NifiGetPortHierarchyDTO dto);

    /**
     * 根据管道主键id查询管道内第一批任务
     *
     * @param id 管道主键id
     * @return list
     */
    ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskFirstListById(Long id);

    /**
     * 根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<List<DispatchRedirectDTO>> redirect(NifiCustomWorkflowDetailDTO dto);

    /**
     * 根据管道主键id查询管道内最后一批任务
     *
     * @param id 管道主键id
     * @return list
     */
    ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskLastListById(Long id);

    /**
     * 获取管道日志
     *
     * @param dto dto
     * @return 查询结果
     */
    ResultEntity<List<PipelJobLogVO>> getPipeJobLog(List<PipelJobLogVO> dto);

    /**
     * 获取阶段日志
     *
     * @param taskId taskId
     * @return 执行结果
     */
    ResultEntity<List<PipelStageLogVO>> getPipeStageLog(String taskId);

    /**
     * 获取阶段日志
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<List<PipelTaskLogVO>> getPipeTaskLog(PipelTaskLogVO dto);

    /**
     * 根据管道主键id,手动将管道的task结构更新到redis
     *
     * @param id id
     * @return 执行结果
     */
    ResultEntity<PipeDagDTO> setTaskLinkedList(Long id);

    /**
     * 根据管道主键id,获取redis里面的task结构
     *
     * @param id id
     * @return 执行结果
     */
    ResultEntity<PipeDagDTO> getTaskLinkedList(Long id);
}
