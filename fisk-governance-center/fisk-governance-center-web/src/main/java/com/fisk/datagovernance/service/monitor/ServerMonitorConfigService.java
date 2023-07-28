package com.fisk.datagovernance.service.monitor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.dto.monitor.ServerMonitorConfigDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorConfigPO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorConfigVO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-07-14 16:21:58
 */
public interface ServerMonitorConfigService extends IService<ServerMonitorConfigPO> {

    String getServerMonitorConfig();

    List<String> getSystemAddress();
    List<ServerMonitorConfigVO> getServerConfig();

    ServerMonitorConfigVO updateServerMonitorConfig(ServerMonitorConfigDTO serverMonitorConfigDTO);

    boolean deleteServerMonitorConfig(String id);
}

