package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.service.ITableSyncmode;
import com.fisk.dataaccess.vo.datafactory.SyncTableCountVO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/dataReview")
public class DataReviewController {

    @Resource
    private ITableFields service;

    @Resource
    private ITableSyncmode tableSyncmode;

    @PostMapping("/pageFilter")
    @ApiOperation(value = "过滤器")
    public ResultEntity<Page<DataReviewVO>> listData(@RequestBody DataReviewQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

    @GetMapping("/mainPageCheck")
    @ApiOperation(value = "数据接入首页追加，全量，业务主键，业务时间覆盖的统计与展示")
    public ResultEntity<SyncTableCountVO> mainPageCheck(Long appid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableSyncmode.mainPageCheck(appid));
    }

}
