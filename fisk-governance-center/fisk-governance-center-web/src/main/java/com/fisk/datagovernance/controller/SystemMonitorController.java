package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.monitor.*;
import com.fisk.datagovernance.service.monitor.*;
import com.fisk.datagovernance.vo.monitor.ServerMonitorConfigVO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorTypeVO;
import com.fisk.datagovernance.vo.monitor.SystemServerVO;
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
    private ServerMonitorTypeService serverMonitorTypeService;
    @Resource
    private MonitorDropDownBoxService monitorDropDownBoxService;
    @Resource
    private MonitorRecipientsService monitorRecipientsService;
    @Resource
    private ServerMonitorConfigService serverMonitorConfigService;


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
    public ResultEntity<Object> getSystemMonitor(@RequestParam("ip") String ip){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,systemMonitorService.getSystemMonitor(ip));
    }

    @ApiOperation("获取系统监控cpu时移图")
    @GetMapping("/getSystemCpuDelayPing")
    public ResultEntity<Object> getSystemCpuDelayPing(@RequestParam("ip") String ip,@RequestParam("number") Integer number,@RequestParam("type") Integer type){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,systemMonitorService.getSystemCpuDelayPing(ip,number,type));
    }

    @ApiOperation("获取系统监控mem时移图")
    @GetMapping("/getSystemMemDelayPing")
    public ResultEntity<Object> getSystemMemDelayPing(@RequestParam("ip") String ip,@RequestParam("number") Integer number,@RequestParam("type") Integer type){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,systemMonitorService.getSystemMemDelayPing(ip,number,type));
    }

    @ApiOperation("获取服务监控参数")
    @GetMapping("/getServerMonitor")
    public ResultEntity<Object> getServerMonitor(@RequestParam("ip") String ip,
                                                 @RequestParam("number") Integer number,
                                                 @RequestParam("type") Integer type){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorService.getServerMonitor(ip,number,type));
    }

    @ApiOperation("查询服务监控信息")
    @PostMapping("/searchServerMonitor")
    public ResultEntity<Object> searchServerMonitor(@RequestBody ServerMonitorQueryDTO serverMonitorQueryDTO){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorService.searchServerMonitor(serverMonitorQueryDTO));
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
    @ApiOperation("获取服务监控配置(脚本用)")
    @GetMapping("/getServerMonitorConfig")
    public String getServerMonitorConfig(){
        return serverMonitorConfigService.getServerMonitorConfig();
    }
    @ApiOperation("获取服务监控配置信息")
    @GetMapping("/getServerConfig")
    public ResultEntity<List<ServerMonitorConfigVO>> getServerConfig(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorConfigService.getServerConfig());
    }

    @ApiOperation("修改服务监控配置信息")
    @PostMapping("/updateServerMonitorConfig")
    public ResultEntity<Object> updateServerMonitorConfig(@RequestBody ServerMonitorConfigDTO serverMonitorConfigDTO){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorConfigService.updateServerMonitorConfig(serverMonitorConfigDTO));
    }

    @ApiOperation("删除服务监控配置信息")
    @PostMapping("/deleteServerMonitorConfig")
    public ResultEntity<Object> deleteServerMonitorConfig(@RequestParam ("id") String id){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorConfigService.deleteServerMonitorConfig(id));
    }

    @ApiOperation("获取服务器地址")
    @GetMapping("/getSystemAddress")
    public ResultEntity<Object> getSystemAddress(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorConfigService.getSystemAddress());
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

    @ApiOperation("获取主页系统状态信息")
    @GetMapping("/getSystemServerList")
    public ResultEntity<List<SystemServerVO>> getSystemServerList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorService.getSystemServerList());
    }

    @ApiOperation("添加服务自定义类型")
    @PostMapping("/addOrUpdateServerMonitorType")
    public ResultEntity<Object> addOrUpdateServerMonitorType(@RequestBody ServerMonitorTypeDTO serverMonitorTypeDTO) {
        return ResultEntityBuild.build(serverMonitorTypeService.addOrUpdateServerMonitorType(serverMonitorTypeDTO));
    }
    @ApiOperation("删除服务自定义类型")
    @PostMapping("/deleteServerMonitorType")
    public ResultEntity<Object> deleteServerMonitorType(@RequestParam ("id") Integer id) {
        return ResultEntityBuild.build(serverMonitorTypeService.deleteServerMonitorType(id));
    }

    @ApiOperation("获取服务自定义类型")
    @GetMapping("/getServerMonitorType")
    public ResultEntity<List<ServerMonitorTypeVO>> getServerMonitorType() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,serverMonitorTypeService.getServerMonitorType());
    }
}
