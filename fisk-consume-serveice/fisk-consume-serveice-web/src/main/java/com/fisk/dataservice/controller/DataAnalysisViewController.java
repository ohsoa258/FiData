package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.service.IDataViewThemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassName: 数据分析视图服务
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/dataAnalysisView")
public class DataAnalysisViewController {

    @Resource
    private IDataViewThemeService dataViewThemeService;

    @ApiOperation("获取目标数据源")
    @GetMapping("/getTargetDbList")
    public ResultEntity<Object> getTargetDbList(){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataViewThemeService.getTargetDbList());
    }

    @ApiOperation("新增视图")
    @PostMapping("/addViewTheme")
    public ResultEntity<Object> addViewTheme(@Validated @RequestBody DataViewThemeDTO dto){
        return ResultEntityBuild.build(dataViewThemeService.addViewTheme(dto));
    }


}
