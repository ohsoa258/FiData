package com.fisk.task.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.task.dto.MessageLogQuery;
import com.fisk.task.entity.MessageLogPO;
import com.fisk.task.vo.WsMessageLogVO;

/**
 * @author gy
 */
public interface MessageLogMapper extends FKBaseMapper<MessageLogPO> {

    /**
     * 查询用户所有的消息
     *
     * @param page 分页对象
     * @param query query对象
     * @return
     */
    Page<WsMessageLogVO> listMessageLog(Page<WsMessageLogVO> page, MessageLogQuery query);
}
