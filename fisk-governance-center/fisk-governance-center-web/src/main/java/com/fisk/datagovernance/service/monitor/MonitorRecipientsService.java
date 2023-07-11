package com.fisk.datagovernance.service.monitor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.MonitorRecipientsDTO;
import com.fisk.datagovernance.entity.monitor.MonitorRecipientsPO;
import com.fisk.datagovernance.vo.monitor.MonitorRecipientsVO;

import java.util.Map;

public interface MonitorRecipientsService extends IService<MonitorRecipientsPO> {

    MonitorRecipientsVO getSystemMonitorAlarmNotice();

    ResultEnum saveSystemMonitorAlarmNotice(MonitorRecipientsDTO monitorRecipientsDTO);

    ResultEnum deleteSystemMonitorAlarmNotice();

    ResultEnum sendSystemMonitorSendEmails(Map<String, String> body);
}

