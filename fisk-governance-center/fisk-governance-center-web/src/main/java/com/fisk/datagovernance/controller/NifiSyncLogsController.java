package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.accessAndModel.AccessAndModelTreeDTO;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.service.nifilogs.INifiLogs;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.NIFI_LOGS})
@RestController
@RequestMapping("/nifiLogs")
public class NifiSyncLogsController {

    @Resource
    private INifiLogs nifiLogs;

    /**
     * 同步日志页面获取数接和数仓的 应用--表   树形结构
     *
     * @return
     */
    @GetMapping("/getAccessAndModelTree")
    public ResultEntity<AccessAndModelTreeDTO> getAccessAndModelTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, nifiLogs.getAccessAndModelTree());
    }

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型
     *
     * @return
     */
    @PostMapping("/getTableNifiLogs")
    public ResultEntity<Page<NifiLogResultDTO>> getTableNifiLogs(@RequestBody LogPageQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, nifiLogs.getTableNifiLogs(dto));
    }


}
