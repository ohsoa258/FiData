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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("systemMonitorService")
public class SystemMonitorServiceImpl extends ServiceImpl<SystemMonitorMapper, SystemMonitorPO> implements SystemMonitorService {

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
    public SystemMonitorVO getSystemMonitor(String ip) {
        QueryWrapper<SystemMonitorPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip",ip);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 1");
        SystemMonitorPO systemMonitorPO = this.getOne(queryWrapper);
        SystemMonitorVO systemMonitorVO = SystemMonitorMap.INSTANCES.poToVo(systemMonitorPO);
        return systemMonitorVO;
    }
}
