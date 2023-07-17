package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.monitor.ServerMonitorConfigPO;
import com.fisk.datagovernance.mapper.monitor.ServerMonitorConfigMapper;
import com.fisk.datagovernance.service.monitor.ServerMonitorConfigService;
import org.springframework.stereotype.Service;

@Service("serverMonitorConfigService")
public class ServerMonitorConfigServiceImpl extends ServiceImpl<ServerMonitorConfigMapper, ServerMonitorConfigPO> implements ServerMonitorConfigService {


}
