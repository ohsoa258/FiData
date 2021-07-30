package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.service.IFactAttribute;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(description = "事实字段")
@RestController
@RequestMapping("/factAttribute")
@Slf4j
public class FactAttributeController {

    @Resource
    IFactAttribute service;

    @ApiOperation("获取事实字段表列表")
    @GetMapping("/getFactAttributeList/{factId}")
    public ResultEntity<Object> getFactAttributeList(@PathVariable int factId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeList(factId));
    }

}
