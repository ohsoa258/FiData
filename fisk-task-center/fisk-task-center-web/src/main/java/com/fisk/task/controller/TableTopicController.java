package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.service.pipeline.ITableTopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = {SwaggerConfig.TableTopic})
@RestController
@RequestMapping("/TableTopic")
@Slf4j
public class TableTopicController {
    @Resource
    ITableTopicService tableTopicService;

    @ApiOperation(value = "根据组件Id删除表主题")
    @PostMapping("/deleteTableTopicByComponentId")
    public ResultEntity<Object> deleteTableTopicByComponentId(@RequestParam("ids") List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableTopicService.deleteTableTopicByComponentId(ids));
    }

    @ApiOperation(value = "删除表主题组")
    @PostMapping("/deleteTableTopicGroup")
    public ResultEntity<Object> deleteTableTopicGroup(@RequestParam("dtos") List<TableTopicDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableTopicService.deleteTableTopicGroup(dtos));
    }

}
