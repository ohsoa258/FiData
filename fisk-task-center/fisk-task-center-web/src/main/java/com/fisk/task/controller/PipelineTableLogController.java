package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.service.nifi.IPipelineTableLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {SwaggerConfig.PIPELINE_LOG})
@RestController
@RequestMapping("/pipelineLog")
@Slf4j
public class PipelineTableLogController {

    @Resource
    private IPipelineTableLog pipelineTableLog;

    /**
     * 获取数据接入应用下的实时表最后同步时间
     *
     * @param tblIds
     * @return
     */
    @GetMapping("/getRealTimeTblLastSyncTime")
    @ApiOperation("获取数据接入应用下的实时表最后同步时间")
    public ResultEntity<LocalDateTime> getRealTimeTblLastSyncTime(@RequestParam("tblIds") List<Long> tblIds) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, pipelineTableLog.getRealTimeTblLastSyncTime(tblIds));
    }

}
