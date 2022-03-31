package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/pageFilter")
    @ApiOperation(value = "过滤器")
    public ResultEntity<Page<DataReviewVO>> listData(@RequestBody DataReviewQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

}
