package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.service.IApiTableViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-04-03 14:40
 * @description
 */
@Api(tags = SwaggerConfig.TAG_8)
@RestController
@RequestMapping("/apiTableViewService")
public class ApiTableViewServiceController {
    @Resource
    IApiTableViewService apiTableViewService;

    @ApiOperation("获取API服务&Table服务&View服务的所有应用")
    @GetMapping("/getApiTableViewService")
    public ResultEntity<List<AppBusinessInfoDTO>> getApiTableViewService() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, apiTableViewService.getApiTableViewService());
    }

    @ApiOperation(value = "元数据同步API服务应用信息")
    @GetMapping("/synchronizationAPIAppRegistration")
    public ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAPIAppRegistration() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, apiTableViewService.synchronizationAPIAppRegistration());
    }
}
