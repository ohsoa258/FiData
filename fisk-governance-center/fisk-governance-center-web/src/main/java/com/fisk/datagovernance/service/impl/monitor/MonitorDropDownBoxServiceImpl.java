package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.monitor.MonitorDropDownBoxPO;
import com.fisk.datagovernance.map.monitor.MonitorDropDownBoxMap;
import com.fisk.datagovernance.mapper.monitor.MonitorDropDownBoxMapper;
import com.fisk.datagovernance.service.monitor.MonitorDropDownBoxService;
import com.fisk.datagovernance.vo.monitor.MonitorDropDownBoxVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("monitorDropDownBoxService")
public class MonitorDropDownBoxServiceImpl extends ServiceImpl<MonitorDropDownBoxMapper, MonitorDropDownBoxPO> implements MonitorDropDownBoxService {


    @Override
    public List<MonitorDropDownBoxVO> getMonitorDropDownBox() {
        MonitorDropDownBoxPO monitorDropDownBoxPO = new MonitorDropDownBoxPO();
        List<MonitorDropDownBoxPO> list = this.list();
        List<MonitorDropDownBoxVO> monitorDropDownBoxVOS = MonitorDropDownBoxMap.INSTANCES.poListToVoList(list);
        return monitorDropDownBoxVOS;
    }
}
