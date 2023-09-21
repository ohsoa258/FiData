package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datagovernance.dto.monitor.ServerMonitorDTO;
import com.fisk.datagovernance.dto.monitor.ServerMonitorPageDTO;
import com.fisk.datagovernance.dto.monitor.ServerMonitorQueryDTO;
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

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("serverMonitorService")
public class ServerMonitorServiceImpl extends ServiceImpl<ServerMonitorMapper, ServerMonitorPO> implements ServerMonitorService {
    @Resource
    RedisUtil redisUtil;

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
    public ServerMonitorVO getServerMonitor(String ip,Integer number, Integer type) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ServerMonitorVO serverMonitorVO = this.baseMapper.getAllTotal(ip);
        if (serverMonitorVO == null) {
            serverMonitorVO = new ServerMonitorVO();
        }
        String format = simpleDateFormat.format(new Date());
        boolean cache = false;
        if (number == 7 || number == 30) {
            if (type == 3){
                cache = true;
            }
        }
        //缓存中有取缓存数据没有就查数据库并缓存每周每月监控数据
        if (cache) {
            RedisKeyEnum key = null;
            if (number == 7) {
                key = RedisKeyEnum.WEEK_MONITOR_ALL;
            } else {
                key = RedisKeyEnum.MONTH_MONITOR_ALL;
            }
            Object o = redisUtil.get(key.getName() + ":" + ip + ":" + format);
            List<DelayPingVO> delayPingCacheTotal = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(o)) {
                delayPingCacheTotal = (List<DelayPingVO>) o;
            } else {
                delayPingCacheTotal = this.baseMapper.getDelayPingCacheTotal(ip, number, 1);
                redisUtil.set(key.getName() + ":" + ip + ":" + format, delayPingCacheTotal, key.getValue());
            }
            List<DelayPingVO> data = this.baseMapper.getDelayPingCacheTotal(ip, number, 2);
            delayPingCacheTotal.addAll(data);
            serverMonitorVO.setDelayPingVOList(delayPingCacheTotal);
        } else {
            //获取所有服务时移ping
            List<DelayPingVO> delayPingTotal = this.baseMapper.getDelayPingTotal(ip,number, type);
            serverMonitorVO.setDelayPingVOList(delayPingTotal);
        }
        return serverMonitorVO;
    }

    @Override
    public List<ServerTableVO> searchServerMonitor(ServerMonitorQueryDTO serverMonitorQueryDTO) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());
        List<ServerTableVO> tableList = new ArrayList<>();
        boolean cache = false;
        if (serverMonitorQueryDTO.getNumber() == 7 || serverMonitorQueryDTO.getNumber() == 30) {
            if (serverMonitorQueryDTO.getType() == 3){
                cache = true;
            }
        }
        //缓存中有取缓存数据没有就查数据库并缓存每周每月监控数据
        if (cache) {
            RedisKeyEnum serverKey = null;
            List<ServerTableVO> serverTableVOCacheList = new ArrayList<>();
            if (serverMonitorQueryDTO.getNumber() == 7) {
                serverKey = RedisKeyEnum.WEEK_MONITOR_SERVER;
            } else {
                serverKey = RedisKeyEnum.MONTH_MONITOR_SERVER;
            }
            Object o1 = redisUtil.get(serverKey.getName() + ":" + serverMonitorQueryDTO.getIp() + ":" + format);
            List<ServerTableVO> serverTable = this.baseMapper.getServerTable(serverMonitorQueryDTO.getIp(),null,null);
            if (ObjectUtils.isNotEmpty(o1)) {
                serverTable = (List<ServerTableVO>) o1;
            } else {
                if (CollectionUtils.isNotEmpty(serverTable)) {
                    //组装时移ping
                    for (ServerTableVO serverTableVO : serverTable) {
                        List<DelayPingVO> serverDelayPingVO = this.baseMapper.getServerDelayPingCacheVO(serverMonitorQueryDTO.getIp(),serverMonitorQueryDTO.getNumber(), 1,
                                serverTableVO.getServerName(), serverTableVO.getPort());
                        serverTableVO.setDelayPingVO(serverDelayPingVO);
                        serverTableVOCacheList.add(serverTableVO);
                    }
                }
                redisUtil.set(serverKey.getName() + ":" + serverMonitorQueryDTO.getIp() + ":" + format, serverTableVOCacheList, serverKey.getValue());
                serverTable = serverTableVOCacheList;
            }
            tableList = serverTable.stream().filter(i->{
                if (i.getServerType().equals(serverMonitorQueryDTO.getServerType())
                        && i.getStatus().equals(serverMonitorQueryDTO.getStatus())
                        &&(i.getServerName().contains(serverMonitorQueryDTO.getSearchKey())
                    || i.getServerUrl().contains(serverMonitorQueryDTO.getSearchKey()))){
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        } else {
            //获取所有服务列表
            tableList = this.baseMapper.getServerTable(serverMonitorQueryDTO.getIp(),serverMonitorQueryDTO.getStatus(),serverMonitorQueryDTO.getServerType());
            tableList = tableList.stream().filter(i->{
                if (i.getServerName().contains(serverMonitorQueryDTO.getSearchKey())
                        || i.getServerUrl().contains(serverMonitorQueryDTO.getSearchKey())){
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            List<ServerTableVO> list = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(tableList)) {
                //组装时移ping
                for (ServerTableVO serverTableVO : tableList) {
                    List<DelayPingVO> serverDelayPingVO = this.baseMapper.getServerDelayPingVO(serverMonitorQueryDTO.getIp(),serverMonitorQueryDTO.getNumber(), serverMonitorQueryDTO.getType(),
                            serverTableVO.getServerName(), serverTableVO.getPort());
                    serverTableVO.setDelayPingVO(serverDelayPingVO);
                    list.add(serverTableVO);
                }
            }
            tableList = list;
        }
        return tableList;
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
                this.baseMapper.getServerDelayPingVO(serverMonitorPageDTO.ip,serverMonitorPageDTO.number,
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
