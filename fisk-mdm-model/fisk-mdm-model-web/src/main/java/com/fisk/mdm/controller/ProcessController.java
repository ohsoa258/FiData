package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.service.ProcessService;
import com.fisk.mdm.vo.process.ProcessInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Api(tags = {SwaggerConfig.TAG_13})
@RestController
@RequestMapping("/process")
public class ProcessController {

    @Resource
    ProcessService processService;

    @ApiOperation("获取流程实体")
    @GetMapping("/getProcessEntity")
    @ResponseBody
    public ResultEntity<ProcessInfoVO> getProcessEntity(Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getProcess(entityId));
    }

    @ApiOperation("保存流程实体")
    @PostMapping("/saveProcessEntity")
    @ResponseBody
    public ResultEntity<ResultEnum> saveProcessEntity(@RequestBody ProcessInfoDTO dto) {
        return ResultEntityBuild.build(processService.saveProcess(dto));
    }

}
