package com.fisk.datagovernance.service.monitor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.monitor.MonitorDropDownBoxPO;
import com.fisk.datagovernance.vo.monitor.MonitorDropDownBoxVO;

import java.util.List;

public interface MonitorDropDownBoxService extends IService<MonitorDropDownBoxPO> {

    List<MonitorDropDownBoxVO> getMonitorDropDownBox();
}

