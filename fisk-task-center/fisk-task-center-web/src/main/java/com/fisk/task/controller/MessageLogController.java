package com.fisk.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.MessageLogQuery;
import com.fisk.task.service.task.IWsMessageService;
import com.fisk.task.vo.WsMessageLogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author gy
 */
@Api(tags = {SwaggerConfig.MessageLog})
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageLogController {

    @Resource
    IWsMessageService service;

    @GetMapping("/getUnMsg")
    public ResultEntity<List<WsMessageLogVO>> getWsUnMessage() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getUserUnMessage());
    }

    @PostMapping("/getMsg")
    public ResultEntity<Page<WsMessageLogVO>> getWsMessage(@RequestBody MessageLogQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getUserAllMessage(query));
    }

    @PutMapping("/read")
    public ResultEntity<Object> readMessage(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(service.readMessage(ids));
    }

}
