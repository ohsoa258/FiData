package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.entity.mainpage.DataModelCountVO;
import com.fisk.datamodel.service.IBusinessArea;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.MainPage})
@RestController
@RequestMapping("/modelMain")
@Slf4j
public class DataModelMainPageController {

    @Resource
    private IBusinessArea service;

    /**
     * 获取当前业务域的首页计数信息
     *
     * @param areaId 业务域id
     * @return
     */
    @GetMapping("/mainPageCount")
    @ApiOperation(value = "获取当前业务域的首页计数信息")
    public ResultEntity<DataModelCountVO> mainPageCount(@RequestParam("areaId") Integer areaId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.mainPageCount(areaId));
    }

}
