package com.fisk.datagovernance.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.service.dataquality.ITemplateManageService;
import com.fisk.datagovernance.vo.dataquality.template.TemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 模板配置
 * @date 2022/3/22 16:14
 */
@Api(tags = {SwaggerConfig.TAG_7})
@RestController
@RequestMapping("/template")
public class TemplateController {

    @Resource
    private ITemplateManageService service;

    @GetMapping("/getAll")
    @ApiOperation("获取数据质量所有模板")
    public ResultEntity<List<TemplateVO>> getAll()  {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll());
    }
}
