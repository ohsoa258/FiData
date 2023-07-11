package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.SystemMonitorDTO;
import com.fisk.datagovernance.entity.monitor.SystemMonitorPO;
import com.fisk.datagovernance.map.monitor.SystemMonitorMap;
import com.fisk.datagovernance.mapper.monitor.SystemMonitorMapper;
import com.fisk.datagovernance.service.monitor.ServerMonitorService;
import com.fisk.datagovernance.service.monitor.SystemMonitorService;
import com.fisk.datagovernance.vo.monitor.SystemMonitorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service("systemMonitorService")
public class SystemMonitorServiceImpl extends ServiceImpl<SystemMonitorMapper, SystemMonitorPO> implements SystemMonitorService {
    @Resource
    ServerMonitorService serverMonitorService;

    @Override
    public ResultEnum saveSystemMonitor(SystemMonitorDTO systemMonitorDTO) {
        SystemMonitorPO systemMonitorPO = SystemMonitorMap.INSTANCES.dtoToPo(systemMonitorDTO);
        boolean save = this.save(systemMonitorPO);
        if (save) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public SystemMonitorVO getSystemMonitor() {
        QueryWrapper<SystemMonitorPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("limit 1");
        SystemMonitorPO systemMonitorPO = this.getOne(queryWrapper);
        SystemMonitorVO systemMonitorVO = SystemMonitorMap.INSTANCES.poToVo(systemMonitorPO);
        return systemMonitorVO;
    }
}
