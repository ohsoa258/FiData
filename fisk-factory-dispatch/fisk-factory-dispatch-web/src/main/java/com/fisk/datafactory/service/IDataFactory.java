package com.fisk.datafactory.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;

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
}
