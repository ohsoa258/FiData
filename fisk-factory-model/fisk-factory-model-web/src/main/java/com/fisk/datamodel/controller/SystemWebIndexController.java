package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.service.IBusinessArea;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/systemWebIndex")
@Slf4j
public class SystemWebIndexController {
    @Resource
    IBusinessArea service;

    @ApiOperation("根据id获取维度详情")
    @GetMapping("/getBusinessArea")
    public ResultEntity<Object> getBusinessArea() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessArea());
    }
}
