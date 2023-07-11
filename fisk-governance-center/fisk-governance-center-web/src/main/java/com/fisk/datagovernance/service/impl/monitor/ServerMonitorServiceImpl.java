package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.ServerMonitorDTO;
import com.fisk.datagovernance.dto.monitor.ServerMonitorPageDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorPO;
import com.fisk.datagovernance.map.monitor.ServerMonitorMap;
import com.fisk.datagovernance.mapper.monitor.ServerMonitorMapper;
import com.fisk.datagovernance.service.monitor.ServerMonitorService;
import com.fisk.datagovernance.vo.monitor.DelayPingVO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorDetailVO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorVO;
import com.fisk.datagovernance.vo.monitor.ServerTableVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("serverMonitorService")
public class ServerMonitorServiceImpl extends ServiceImpl<ServerMonitorMapper, ServerMonitorPO> implements ServerMonitorService {


    @Override
    public ResultEnum saveServerMonitor(List<ServerMonitorDTO> serverMonitorDTO) {
        List<ServerMonitorPO> serverMonitorPO = ServerMonitorMap.INSTANCES.dtoListToPoList(serverMonitorDTO);
        boolean save = this.saveBatch(serverMonitorPO);
        if (save) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public ServerMonitorVO getServerMonitor(Integer number, Integer type) {
        ServerMonitorVO serverMonitorVO = this.baseMapper.getAllTotal();
        if (serverMonitorVO == null){
            serverMonitorVO = new ServerMonitorVO();
        }
        //获取所有服务时移ping
        List<DelayPingVO> delayPingTotal = this.baseMapper.getDelayPingTotal(number, type);
        serverMonitorVO.setDelayPingVOList(delayPingTotal);
        //获取所有服务列表
        List<ServerTableVO> serverTable = this.baseMapper.getServerTable(number, type);
        List<ServerTableVO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(serverTable)) {
            //组装时移ping
            for (ServerTableVO serverTableVO : serverTable) {
                List<DelayPingVO> serverDelayPingVO = this.baseMapper.getServerDelayPingVO(number, type,
                        serverTableVO.getServerName(), serverTableVO.getPort());
                serverTableVO.setDelayPingVO(serverDelayPingVO);
                list.add(serverTableVO);
            }
        }
        serverMonitorVO.setServerTableVOList(list);
        return serverMonitorVO;
    }

    @Override
    public ServerMonitorDetailVO getServerMonitorDetail(ServerMonitorPageDTO serverMonitorPageDTO) {
        //获取运行时间
        ServerMonitorDetailVO serverMonitorDetailVO =
                this.baseMapper.getRunningStatus(serverMonitorPageDTO.name,
                        serverMonitorPageDTO.port);
        if (serverMonitorDetailVO == null) {
            serverMonitorDetailVO = this.baseMapper.getStatus(serverMonitorPageDTO.name,
                    serverMonitorPageDTO.port);
        }
        //获取时移ping
        List<DelayPingVO> serverDelayPingVO =
                this.baseMapper.getServerDelayPingVO(serverMonitorPageDTO.number,
                        serverMonitorPageDTO.type,
                        serverMonitorPageDTO.name,
                        serverMonitorPageDTO.port);
        serverMonitorDetailVO.setDelayPingVOList(serverDelayPingVO);
        //获取服务详情列表
        Page<ServerTableVO> page = serverMonitorPageDTO.page;
        Page<ServerTableVO> serverTableDetail = this.baseMapper.getServerTableDetail(page, serverMonitorPageDTO);
        serverMonitorDetailVO.setServerTableVOList(serverTableDetail);
        return serverMonitorDetailVO;
    }
}
