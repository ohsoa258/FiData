package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.systemlog.SystemLogDTO;
import com.fisk.system.service.systemlog.SystemLog;
import com.fisk.system.vo.systemlog.SystemLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lishiji
 * 数据运维-系统运维-系统日志
 */
@RestController
@RequestMapping("/systemLog")
@Api(tags = {SwaggerConfig.SYSTEM_LOG_CONTROLLER})
public class SystemLogController {

    @Resource
    private SystemLog systemLog;

    @ApiOperation("获取某个服务当天的所有日志名称（目录）")
    @PostMapping("/systemLogs")
    public ResultEntity<Object> getSystemLogs(@RequestBody SystemLogDTO dto) {
        List<String> systemLogNames = systemLog.getSystemLogNames(dto);
        if (CollectionUtils.isEmpty(systemLogNames)){
            return ResultEntityBuild.build(ResultEnum.LOG_NOT_EXISTS);
        }else {
            return ResultEntityBuild.build(ResultEnum.SUCCESS, systemLogNames);
        }
    }

    @ApiOperation("获取某个服务某天的所有日志")
    @PostMapping("/systemLogsByDate/{serviceType}/{logName}")
    public ResultEntity<Object> getSystemLogBylogName(@PathVariable("serviceType")Integer serviceType,@PathVariable("logName")String logName) {
        SystemLogVO result = systemLog.getSystemLogBylogName(serviceType,logName);
        //判断日志是否存在
        if (result == null){
            return ResultEntityBuild.build(ResultEnum.LOG_NOT_EXISTS);
        }else {
            return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
        }
    }

}
