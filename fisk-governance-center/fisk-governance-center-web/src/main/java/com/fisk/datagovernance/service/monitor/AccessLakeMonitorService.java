package com.fisk.datagovernance.service.monitor;

import com.fisk.datagovernance.vo.monitor.AccessLakeMonitorVO;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
public interface AccessLakeMonitorService {
    AccessLakeMonitorVO getAccessLakeMonitor(Integer appId);
}
