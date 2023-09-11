package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.AccessMainPageVO;
import com.fisk.dataaccess.service.ITableAccess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.MAIN_PAGE})
@RestController
@RequestMapping("/main")
public class MainPageController {

    @Resource
    private ITableAccess tableAccess;

    /**
     * 首页--回显统计当前数据接入总共有多少表,多少重点接口，当日数据量等信息
     *
     * @return
     */
    @GetMapping("/countTotal")
    @ApiOperation(value = "首页--回显统计当前数据接入总共有多少表,多少重点接口，当日数据量等信息")
    public ResultEntity<AccessMainPageVO> countTblTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccess.countTotal());
    }

}
