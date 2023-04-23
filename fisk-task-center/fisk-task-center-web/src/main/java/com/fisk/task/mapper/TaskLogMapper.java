package com.fisk.task.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.TaskLogQuery;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.vo.TaskLogVO;

/**
 * @author gy
 */
public interface TaskLogMapper extends FKBaseMapper<TaskLogPO> {

    /**
     * 查询用户所有的消息
     *
     * @param page 分页对象
     * @param query query对象
     * @return
     */
    Page<TaskLogPO> listTaskLog(Page<TaskLogVO> page, TaskLogQuery query);
}
