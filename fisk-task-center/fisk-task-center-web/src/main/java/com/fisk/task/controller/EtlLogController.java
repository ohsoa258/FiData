package com.fisk.task.controller;


import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.service.pipeline.IEtlLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {SwaggerConfig.ETL_LOG})
@RestController
@RequestMapping("/etlLog")
@Slf4j
public class EtlLogController {

    @Resource
    private IEtlLog etlLog;

    /**
     * 获取数据接入表最后同步时间
     *
     * @param tblNames
     * @return
     */
    @GetMapping("/getAccessTblLastSyncTime")
    @ApiOperation("获取数据接入表最后同步时间")
    public ResultEntity<LocalDateTime> getAccessTblLastSyncTime(@RequestParam("tblNames") List<String> tblNames) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, etlLog.getAccessTblLastSyncTime(tblNames));
    }

}
