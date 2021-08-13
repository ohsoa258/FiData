package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.service.DataDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2021/8/12 11:29
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/Datamation")
public class DataDomainController {

    @Resource
    private DataDomainService domainService;

    @ApiOperation("获取数据域")
    @GetMapping("/getAll")
    public ResultEntity<Object> getAll(String businessName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, domainService.getDataDomain(businessName));
    }
}
