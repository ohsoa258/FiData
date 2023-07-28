package com.fisk.datagovernance.util;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datagovernance.mapper.monitor.ServerMonitorMapper;
import com.fisk.datagovernance.service.monitor.ServerMonitorConfigService;
import com.fisk.datagovernance.vo.monitor.DelayPingVO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorConfigVO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorVO;
import com.fisk.datagovernance.vo.monitor.ServerTableVO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-17
 * @Description:
 */
@Component
public class TaskUtils {
    @Resource
    ServerMonitorMapper mapper;
    @Resource
    ServerMonitorConfigService serverMonitorConfigService;

    @Resource
    RedisUtil redisUtil;
    @Scheduled(cron = "0 0 0 * * ?") // cron表达式：每天凌晨 0点 执行
    public void doTask(){
        List<ServerMonitorConfigVO> serverMonitorConfig = serverMonitorConfigService.getServerConfig();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());
        for (ServerMonitorConfigVO serverMonitorConfigVO : serverMonitorConfig) {
            //定时缓存每周监控数据
            List<DelayPingVO> delayPingCacheTotal = mapper.getDelayPingCacheTotal(serverMonitorConfigVO.getServerIp(),7, 1);
            redisUtil.set(RedisKeyEnum.WEEK_MONITOR_ALL.getName()+":"+serverMonitorConfigVO.getServerIp()+":"+format,delayPingCacheTotal,RedisKeyEnum.WEEK_MONITOR_ALL.getValue());
            List<ServerTableVO> serverTable = mapper.getServerTable(serverMonitorConfigVO.getServerIp());
            List<ServerTableVO> serverTableVOCacheList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(serverTable)) {
                //组装时移ping
                for (ServerTableVO serverTableVO : serverTable) {
                    List<DelayPingVO> serverDelayPingVO = mapper.getServerDelayPingCacheVO(serverMonitorConfigVO.getServerIp(),7, 1,
                            serverTableVO.getServerName(), serverTableVO.getPort());
                    serverTableVO.setDelayPingVO(serverDelayPingVO);
                    serverTableVOCacheList.add(serverTableVO);
                }
            }
            redisUtil.set(RedisKeyEnum.WEEK_MONITOR_SERVER.getName()+":"+serverMonitorConfigVO.getServerIp()+":"+format,serverTableVOCacheList,RedisKeyEnum.WEEK_MONITOR_SERVER.getValue());
            //定时缓存每月监控数据
            List<DelayPingVO> delayPingCacheTotal2 = mapper.getDelayPingCacheTotal(serverMonitorConfigVO.getServerIp(),7, 1);
            redisUtil.set(RedisKeyEnum.MONTH_MONITOR_ALL.getName()+":"+serverMonitorConfigVO.getServerIp()+":"+format,delayPingCacheTotal2,RedisKeyEnum.MONTH_MONITOR_ALL.getValue());
            serverTableVOCacheList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(serverTable)) {
                //组装时移ping
                for (ServerTableVO serverTableVO : serverTable) {
                    List<DelayPingVO> serverDelayPingVO = mapper.getServerDelayPingCacheVO(serverMonitorConfigVO.getServerIp(),7, 1,
                            serverTableVO.getServerName(), serverTableVO.getPort());
                    serverTableVO.setDelayPingVO(serverDelayPingVO);
                    serverTableVOCacheList.add(serverTableVO);
                }
            }
            redisUtil.set(RedisKeyEnum.MONTH_MONITOR_SERVER.getName()+":"+serverMonitorConfigVO.getServerIp()+":"+format,serverTableVOCacheList,RedisKeyEnum.MONTH_MONITOR_SERVER.getValue());

        }
    }
}
