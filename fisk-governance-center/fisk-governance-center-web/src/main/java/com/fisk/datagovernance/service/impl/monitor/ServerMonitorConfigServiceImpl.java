package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.dto.monitor.ServerMonitorConfigDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorConfigPO;
import com.fisk.datagovernance.map.monitor.ServerMonitorConfigMap;
import com.fisk.datagovernance.mapper.monitor.ServerMonitorConfigMapper;
import com.fisk.datagovernance.service.monitor.ServerMonitorConfigService;
import com.fisk.datagovernance.vo.monitor.ServerMonitorConfigVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("serverMonitorConfigService")
public class ServerMonitorConfigServiceImpl extends ServiceImpl<ServerMonitorConfigMapper, ServerMonitorConfigPO> implements ServerMonitorConfigService {


    @Override
    public String getServerMonitorConfig() {
        List<ServerMonitorConfigPO> config = this.list();
        StringBuilder sb = new StringBuilder();
        for (ServerMonitorConfigPO serverMonitorConfigPO : config) {
            sb.append(serverMonitorConfigPO.getServerIp()).append(":").append(serverMonitorConfigPO.getServerPort()).append(":").append(serverMonitorConfigPO.getServerName()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public List<String> getSystemAddress() {
        return this.baseMapper.getSystemAddress();
    }

    @Override
    public List<ServerMonitorConfigVO> getServerConfig() {
        List<ServerMonitorConfigPO> config = this.list();
        List<ServerMonitorConfigVO> result = config.stream().map(ServerMonitorConfigMap.INSTANCES::poToVO
        ).collect(Collectors.toList());
        return result;
    }

    @Override
    public ServerMonitorConfigVO updateServerMonitorConfig(ServerMonitorConfigDTO serverMonitorConfigDTO) {
        ServerMonitorConfigPO serverMonitorConfigPO = ServerMonitorConfigMap.INSTANCES.dtoToPo(serverMonitorConfigDTO);
        if (serverMonitorConfigDTO.getId() == null || serverMonitorConfigDTO.getId() == 0) {
            this.save(serverMonitorConfigPO);
        } else {
            this.updateById(serverMonitorConfigPO);
        }
        return ServerMonitorConfigMap.INSTANCES.poToVO(serverMonitorConfigPO);
    }

    @Override
    public boolean deleteServerMonitorConfig(String id) {
        boolean b = this.removeById(id);
        return b;
    }
}
