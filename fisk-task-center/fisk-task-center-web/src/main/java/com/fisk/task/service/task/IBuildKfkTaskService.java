package com.fisk.task.service.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.TaskLogQuery;
import com.fisk.task.vo.TaskLogVO;

public interface IBuildKfkTaskService {

    /**
     * 消费投递到队列
     *
     * @param name     提示框内容
     * @param exchange 无
     * @param queue    队列名称
     * @param data     data数据
     * @return
     */
    ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data);

    /**
     * 获取当前用户所有消息
     *
     * @param query query对象
     * @return 消息列表
     */
    Page<TaskLogVO> getUserAllMessage(TaskLogQuery query);
}
