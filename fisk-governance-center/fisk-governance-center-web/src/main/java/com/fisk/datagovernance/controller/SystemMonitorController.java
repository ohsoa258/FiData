package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.monitor.*;
import com.fisk.datagovernance.service.monitor.MonitorDropDownBoxService;
import com.fisk.datagovernance.service.monitor.MonitorRecipientsService;
import com.fisk.datagovernance.service.monitor.ServerMonitorService;
import com.fisk.datagovernance.service.monitor.SystemMonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = SwaggerConfig.SYSTEM_MONITOR)
@RestController
@RequestMapping("/systemMonitor")
@Slf4j
public class SystemMonitorController {
    @Resource
    private SystemMonitorService systemMonitorService;
    @Resource
    private ServerMonitorService serverMonitorService;
    @Resource
    private MonitorDropDownBoxService monitorDropDownBoxService;
    @Resource
    private MonitorRecipientsService monitorRecipientsService;


    @ApiOperation("新增系统监控参数")
    @PostMapping("/saveSystemMonitor")
    public ResultEntity<Object> saveSystemMonitor(@RequestBody SystemMonitorDTO systemMonitorDTO){
        return ResultEntityBuild.build(systemMonitorService.saveSystemMonitor(systemMonitorDTO));
    }

    @ApiOperation("新增服务监控参数")
    @PostMapping("/saveServerMonitor")
    public ResultEntity<Object> saveServerMonitor(@RequestBody List<ServerMonitorDTO> serverMonitorDTO){
        return ResultEntityBuild.build(serverMonitorService.saveServerMonitor(serverMonitorDTO));
    }

    @ApiOperation("获取系统监控参数")
    @GetMapping("/getSystemMonitor")
    public ResultEntity<Object> getSystemMonitor(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,systemMonitorService.getSystemMonitor());
    }

    @ApiOperation("获取服务监控参数")
    @GetMapping("/getServerMonitor")
    public ResultEntity<Object> getServerMonitor(@RequestParam("number") Integer number,@RequestParam("type") Integer type){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorService.getServerMonitor(number,type));
    }

    @ApiOperation("获取服务监控详情")
    @PostMapping("/getServerMonitorDetail")
    public ResultEntity<Object> getServerMonitorDetail(@RequestBody ServerMonitorPageDTO serverMonitorPageDTO){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorService.getServerMonitorDetail(serverMonitorPageDTO));
    }

    @ApiOperation("获取服务监控下拉框")
    @GetMapping("/getMonitorDropDownBox")
    public ResultEntity<Object> getMonitorDropDownBox(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,monitorDropDownBoxService.getMonitorDropDownBox());
    }
    @ApiOperation("查询系统监控告警通知配置")
    @GetMapping("/getSystemMonitorAlarmNotice")
    public ResultEntity<Object> getSystemMonitorAlarmNotice() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, monitorRecipientsService.getSystemMonitorAlarmNotice());
    }

    @ApiOperation("保存系统监控告警通知配置")
    @PostMapping("/saveSystemMonitorAlarmNotice")
    public ResultEntity<Object> saveSystemMonitorAlarmNotice(@RequestBody MonitorRecipientsDTO systemRecipientsDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, monitorRecipientsService.saveSystemMonitorAlarmNotice(systemRecipientsDTO));
    }

    @ApiOperation("删除系统监控告警通知配置")
    @GetMapping("/deleteSystemMonitorAlarmNotice")
    public ResultEntity<Object> deleteSystemMonitorAlarmNotice() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, monitorRecipientsService.deleteSystemMonitorAlarmNotice());
    }

    @ApiOperation("发送系统监控告警通知")
    @PostMapping("/sendSystemMonitorSendEmails")
    public ResultEntity<Object> sendSystemMonitorSendEmails(@RequestBody Map<String, String> body) {
        return ResultEntityBuild.build(monitorRecipientsService.sendSystemMonitorSendEmails(body));
    }
}
