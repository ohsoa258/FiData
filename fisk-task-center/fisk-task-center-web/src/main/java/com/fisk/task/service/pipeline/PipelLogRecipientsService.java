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

    PipelLogRecipientsVO getPipelLogAlarmNotice();

    ResultEnum savePipelLogAlarmNotice(PipelLogRecipientsDTO pipelLogRecipientsDTO);

    ResultEnum deletePipelLogAlarmNotice();

    ResultEnum sendPipelLogSendEmails(String content);
}

