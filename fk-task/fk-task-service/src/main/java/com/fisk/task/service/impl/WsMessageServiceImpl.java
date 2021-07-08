package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.enums.task.MessageStatusEnum;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.task.entity.MessageLogPO;
import com.fisk.task.map.WsMessageLogMap;
import com.fisk.task.mapper.MessageLogMapper;
import com.fisk.task.service.IWsMessageService;
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
    UserHelper userHelper;

    @Override
    public List<WsMessageLogVO> getUserUnMessage() {
        UserInfo userInfo = userHelper.getLoginUserInfo();

        List<MessageLogPO> list = this.query()
                .select("id", "msg", "status", "create_time")
                .eq("create_user", userInfo.id)
                .list();
        return WsMessageLogMap.INSTANCES.poToVo(list);
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
