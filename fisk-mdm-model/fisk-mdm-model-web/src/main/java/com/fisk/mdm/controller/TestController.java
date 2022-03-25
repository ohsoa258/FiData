package com.fisk.mdm.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangYan
 * @date 2022/3/25 17:27
 */
@Api(tags = {SwaggerConfig.TAG_1})
@RestController
@RequestMapping("/test")
public class TestController {

    @ApiOperation("查询测试")
    @PostMapping("/listData")
    @ResponseBody
    public ResultEntity<String> listData() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,"1111");
    }
}
