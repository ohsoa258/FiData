package com.fisk.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.TaskLogQuery;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.vo.TaskLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TaskLog})
@RestController
@RequestMapping("/taskLogController")
@Slf4j
public class TaskLogController {

    @Resource
    IBuildKfkTaskService iBuildKfkTaskService;

    /**
     * 通过trace_id聚合查卡夫卡消费日志
     *
     * @param query
     * @return
     */
    @ApiOperation(value = "通过trace_id聚合查卡夫卡消费日志")
    @PostMapping("/getMsg")
    public ResultEntity<Page<TaskLogVO>> getWsMessage(@RequestBody TaskLogQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iBuildKfkTaskService.getUserAllMessage(query));
    }

}
