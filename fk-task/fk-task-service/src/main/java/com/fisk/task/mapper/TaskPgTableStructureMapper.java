package com.fisk.task.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.task.dto.taskpgtablestructure.TaskPgTableStructureParameterDTO;
import com.fisk.task.entity.TaskPgTableStructurePO;

/**
 * @author JianWenYang
 */
public interface TaskPgTableStructureMapper extends FKBaseMapper<TaskPgTableStructurePO> {
    /**
     * 调用存储过程,修改表结构
     * @param dto
     * @return
     */
    String pgCheckTableStructure(TaskPgTableStructureParameterDTO dto);
}
