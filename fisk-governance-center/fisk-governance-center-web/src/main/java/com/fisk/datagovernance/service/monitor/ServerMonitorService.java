package com.fisk.datagovernance.service.monitor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.ServerMonitorDTO;
import com.fisk.datagovernance.dto.monitor.ServerMonitorPageDTO;
import com.fisk.datagovernance.dto.monitor.ServerMonitorQueryDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorPO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorDetailVO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorVO;
import com.fisk.datagovernance.vo.monitor.ServerTableVO;

import java.util.List;


public interface ServerMonitorService extends IService<ServerMonitorPO> {

    ResultEnum saveServerMonitor(List<ServerMonitorDTO> serverMonitorDTO);

    ServerMonitorVO getServerMonitor(String ip,Integer number, Integer type);

    List<ServerTableVO> searchServerMonitor(ServerMonitorQueryDTO serverMonitorQueryDTO);

    ServerMonitorDetailVO getServerMonitorDetail(ServerMonitorPageDTO serverMonitorPageDTO);
}

