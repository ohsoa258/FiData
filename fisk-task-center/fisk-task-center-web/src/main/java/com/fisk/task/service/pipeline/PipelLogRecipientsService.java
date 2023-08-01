package com.fisk.task.service.pipeline;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.statistics.PipelLogRecipientsDTO;
import com.fisk.task.entity.PipelLogRecipientsPO;
import com.fisk.task.vo.statistics.PipelLogRecipientsVO;

import java.util.Map;


/**
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-07-24 17:29:50
 */
public interface PipelLogRecipientsService extends IService<PipelLogRecipientsPO> {

    /**
     * 查询管道日志订阅通知配置
     * @return
     */
    PipelLogRecipientsVO getPipelLogAlarmNotice();

    /**
     * 保存管道日志订阅通知配置
     * @param pipelLogRecipientsDTO
     * @return
     */
    ResultEnum savePipelLogAlarmNotice(PipelLogRecipientsDTO pipelLogRecipientsDTO);

    /**
     * 删除管道日志订阅通知配置
     * @return
     */
    ResultEnum deletePipelLogAlarmNotice();

    /**
     * 发送管道日志订阅通知
     * @param content
     * @return
     */
    ResultEnum sendPipelLogSendEmails(String content);
}

