package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.service.pipeline.ITableTopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cfk
 */
@RestController
@RequestMapping("/TableTopic")
@Slf4j
public class TableTopicController {
    @Resource
    ITableTopicService tableTopicService;

    @PostMapping("/deleteTableTopicByComponentId")
    public ResultEntity<Object> deleteTableTopicByComponentId(@RequestParam("ids") List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableTopicService.deleteTableTopicByComponentId(ids));
    }

}
