package com.fisk.datafactory.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;

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
    ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskListById(Long id);

    /**
     * 根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<List<DispatchRedirectDTO>> redirect(NifiCustomWorkflowDetailDTO dto);
}
