package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.service.ApiConfigureFieldService;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/8 10:04
 */
@RestController
@RequestMapping("/config")
public class ConfigureFieldController {

    @Resource
    private ApiConfigureFieldService configureFieldService;

    @ApiOperation("添加字段配置")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody List<ApiConfigureFieldPO> dto,String apiName,String apiInfo,Integer distinctData) {
        return ResultEntityBuild.build(configureFieldService.saveConfigureField(dto,apiName,apiInfo,distinctData));
    }
}
