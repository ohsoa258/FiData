package com.fisk.datagovernance.mapper.monitor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.dto.monitor.ServerMonitorPageDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorPO;
import com.fisk.datagovernance.vo.monitor.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ServerMonitorMapper extends BaseMapper<ServerMonitorPO> {

    List<DelayPingVO> getDelayPingTotal(@Param("ip") String ip,@Param("number") Integer number, @Param("type") Integer type);

    List<DelayPingVO> getDelayPingCacheTotal(@Param("ip") String ip,@Param("number") Integer number, @Param("type") Integer type);
    ServerMonitorVO getAllTotal(@Param("ip") String ip);
    List<ServerTableVO> getServerTable(@Param("ip") String ip,@Param("status") Integer status,@Param("serverType") Integer serverType);

    List<DelayPingVO> getServerDelayPingVO(@Param("ip") String ip ,@Param("number") Integer number, @Param("type") Integer type,
                                           @Param("name") String name,@Param("port") Integer port);

    List<DelayPingVO> getServerDelayPingCacheVO(@Param("ip") String ip ,@Param("number") Integer number, @Param("type") Integer type,
                                           @Param("name") String name,@Param("port") Integer port);
    ServerMonitorDetailVO getRunningStatus(@Param("name") String name, @Param("port") Integer port);

    ServerMonitorDetailVO getStatus(@Param("name") String name, @Param("port") Integer port);
    Page<ServerTableVO> getServerTableDetail(Page<ServerTableVO> serverTableVOPage, @Param("query") ServerMonitorPageDTO query);

    List<SystemServerVO> getSystemServerList();
}
