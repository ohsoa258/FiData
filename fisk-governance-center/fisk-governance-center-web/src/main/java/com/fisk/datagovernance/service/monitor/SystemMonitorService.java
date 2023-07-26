package com.fisk.datagovernance.service.monitor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.SystemMonitorDTO;
import com.fisk.datagovernance.entity.monitor.SystemMonitorPO;
import com.fisk.datagovernance.vo.monitor.SystemMonitorVO;

import java.util.List;


public interface SystemMonitorService extends IService<SystemMonitorPO> {

    ResultEnum saveSystemMonitor(SystemMonitorDTO systemMonitorDTO);

    SystemMonitorVO getSystemMonitor(String ip);
}

