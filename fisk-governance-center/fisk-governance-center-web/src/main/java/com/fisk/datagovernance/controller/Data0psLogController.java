package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataops.DataOpsLogQueryDTO;
import com.fisk.datagovernance.service.dataops.IDataOpsLogManageService;
import com.fisk.datagovernance.vo.dataops.DataOpsLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维日志
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.DATA0PSLOG_CONTROLLER})
@RestController
@RequestMapping("/dataOpsLog")
public class Data0psLogController {
    @Resource
    private IDataOpsLogManageService service;

    @ApiOperation("分页查询数据运维执行的日志")
    @PostMapping("/getAll")
    public ResultEntity<Page<DataOpsLogVO>> getAll(@RequestBody DataOpsLogQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }
}
