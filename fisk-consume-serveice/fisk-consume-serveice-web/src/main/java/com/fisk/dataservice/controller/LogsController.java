package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.logs.LogQueryBasicsDTO;
import com.fisk.dataservice.dto.logs.LogQueryDTO;
import com.fisk.dataservice.service.ILogsManageService;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import com.fisk.dataservice.vo.logs.TableApiServiceLogVO;
import com.fisk.dataservice.vo.logs.TableServiceLogVO;
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
 * @description api日志服务控制器
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

    @ApiOperation(value = "表服务日志")
    @PostMapping("/pageTableServiceLog")
    public ResultEntity<TableServiceLogVO> pageTableServiceLog(@RequestBody LogQueryBasicsDTO dto) {
        return service.pageTableServiceLog(dto);
    }

    @ApiOperation(value = "数据分发api日志")
    @PostMapping("/pageTableApiServiceLog")
    public ResultEntity<TableApiServiceLogVO> pageTableApiServiceLog(@RequestBody LogQueryBasicsDTO dto) {
        return service.pageTableApiServiceLog(dto);
    }
}
