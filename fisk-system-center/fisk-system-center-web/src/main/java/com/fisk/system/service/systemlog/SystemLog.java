package com.fisk.system.service.systemlog;

import com.fisk.system.dto.systemlog.SystemLogDTO;
import com.fisk.system.vo.systemlog.SystemLogVO;
import java.util.List;

/**
 * @author lishiji
 */
public interface SystemLog {

    /**
     * 获取某个服务某天的所有日志列表
     *
     * @param dto
     * @return
     */
    List<String> getSystemLogNames(SystemLogDTO dto);

    /**
     * 获取某个服务某天的所有日志
     *
     * @param logName
     * @return
     */
    SystemLogVO getSystemLogBylogName(Integer serviceType,String logName);
}
