package com.fisk.task.service.task.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.task.MessageLevelEnum;
import com.fisk.common.core.enums.task.MessageStatusEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.task.dto.MessageLogQuery;
import com.fisk.task.entity.MessageLogPO;
import com.fisk.task.map.WsMessageLogMap;
import com.fisk.task.mapper.MessageLogMapper;
import com.fisk.task.service.task.IWsMessageService;
import com.fisk.task.vo.WsMessageLogVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author gy
 */
@Service
public class WsMessageServiceImpl extends ServiceImpl<MessageLogMapper, MessageLogPO> implements IWsMessageService {

    @Resource
    MessageLogMapper mapper;
    @Resource
    UserHelper userHelper;

    @Override
    public List<WsMessageLogVO> getUserUnMessage() {
        UserInfo userInfo = userHelper.getLoginUserInfo();

        List<MessageLogPO> list = this.query()
                .select("id", "msg", "status", "create_time", "level")
                .eq("create_user", userInfo.id)
                .eq("status", MessageStatusEnum.UNREAD.getValue())
                .ne("level", MessageLevelEnum.LOW.getValue())
                .orderByDesc("create_time")
                .list();
        return WsMessageLogMap.INSTANCES.poToVo(list);
    }

    @Override
    public Page<WsMessageLogVO> getUserAllMessage(MessageLogQuery query) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        query.userId = userInfo.id;
        return mapper.listMessageLog(query.page, query);
    }

    @Override
    public ResultEnum readMessage(List<Integer> ids) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        List<MessageLogPO> data = this.query().in("id", ids).list();
        data.forEach(e -> {
            e.status = MessageStatusEnum.READ;
            e.updateUser = userInfo.id.toString();
        });
        return this.updateBatchById(data) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
