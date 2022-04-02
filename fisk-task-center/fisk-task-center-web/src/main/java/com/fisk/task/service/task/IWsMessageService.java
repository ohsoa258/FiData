package com.fisk.task.service.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.MessageLogQuery;
import com.fisk.task.vo.WsMessageLogVO;

import java.util.List;

/**
 * @author gy
 */
public interface IWsMessageService {

    /**
     * 获取当前用户所有未读消息
     *
     * @return 未读消息列表
     */
    List<WsMessageLogVO> getUserUnMessage();

    /**
     * 获取当前用户所有消息
     * @param query query对象
     * @return 消息列表
     */
    Page<WsMessageLogVO> getUserAllMessage(MessageLogQuery query);

    /**
     * 已读消息
     * @param ids id
     * @return 执行结果
     */
    ResultEnum readMessage(List<Integer> ids);
}
