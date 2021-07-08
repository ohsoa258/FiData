package com.fisk.task.service;

import com.fisk.common.response.ResultEnum;
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
     * @param ids id
     * @return 执行结果
     */
    ResultEnum readMessage(List<Integer> ids);
}
