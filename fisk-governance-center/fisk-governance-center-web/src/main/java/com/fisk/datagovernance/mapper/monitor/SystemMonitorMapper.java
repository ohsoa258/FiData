package com.fisk.datagovernance.mapper.monitor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datagovernance.entity.monitor.SystemMonitorPO;
import com.fisk.datagovernance.vo.monitor.SystemCpuDelayPingVO;
import com.fisk.datagovernance.vo.monitor.SystemMemDelayPingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemMonitorMapper extends BaseMapper<SystemMonitorPO> {

    List<SystemCpuDelayPingVO> getSystemCpuDelayPing(@Param("ip") String ip, @Param("number") Integer number, @Param("type") Integer type);
    List<SystemMemDelayPingVO> getSystemMemDelayPing(@Param("ip") String ip, @Param("number") Integer number, @Param("type") Integer type);
}
