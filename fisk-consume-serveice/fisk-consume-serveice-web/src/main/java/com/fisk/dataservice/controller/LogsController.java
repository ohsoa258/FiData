package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.logs.LogQueryDTO;
import com.fisk.dataservice.service.ILogsManageService;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author dick
 * @version v1.0
 * @description api应用服务控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/logs")
public class LogsController {
    @Resource
    private ILogsManageService service;

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<Page<ApiLogVO>> pageFilter(@RequestBody LogQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.pageFilter(dto));
    }
}
