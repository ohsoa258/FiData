package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.entity.TaskPgTableStructurePO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author JianWenYang
 */
public interface TaskPgTableStructureMapper extends FKBaseMapper<TaskPgTableStructurePO> {
    /**
     * 失败修改有效版本
     *
     * @param version 表版本
     * @return void
     */
    @Update("update tb_task_pg_table_structure set valid_version=0 where version=#{version}")
    void updatevalidVersion(@Param("version") String version);
}
