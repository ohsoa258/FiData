package com.fisk.datamanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.metasynctime.ClassificationTypeDTO;
import com.fisk.datamanagement.dto.metasynctime.MetaSyncDTO;
import com.fisk.datamanagement.service.MetaSyncTimePOService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/syncTime")
@Api(tags = {SwaggerConfig.META_SYNC_TIME})
public class MetaSyncTimeController {


    @Resource
    private MetaSyncTimePOService service;

    /**
     * 获取服务类型树
     *
     * @return
     */
    @ApiOperation("获取服务类型树")
    @GetMapping("/getServiceType")
    public ResultEntity<List<ClassificationTypeDTO>> getServiceType() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getServiceType());
    }

    /**
     * 根据服务类型获取服务的元数据同步日志 分页
     *
     * @return
     */
    @ApiOperation("根据服务类型获取服务的元数据同步日志  分页")
    @GetMapping("/getMetaSyncLogByType")
    public ResultEntity<Page<MetaSyncDTO>> getMetaSyncLogByType(ClassificationTypeEnum type, Integer current, Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaSyncLogByType(type,current,size));
    }

}
