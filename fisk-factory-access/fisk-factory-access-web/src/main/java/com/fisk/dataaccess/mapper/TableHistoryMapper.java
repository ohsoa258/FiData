package com.fisk.dataaccess.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.entity.TableHistoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author JianWenYang
 */
@Mapper
public interface TableHistoryMapper extends FKBaseMapper<TableHistoryPO> {
    /**
     * 发布回调
     *
     * @param id
     * @param subRunId 用来和管道关联的,其值是管道任务id(uuid)
     */
    @Update("update tb_table_history set sub_run_id = #{subRunId} where id = #{id}")
    void updateSubRunId(@Param("id") long id, @Param("subRunId") String subRunId);
}
