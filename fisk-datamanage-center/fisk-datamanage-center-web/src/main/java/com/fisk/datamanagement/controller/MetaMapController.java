package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
import com.fisk.datamanagement.service.IEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = {SwaggerConfig.META_MAP})
@RestController
@RequestMapping("/metaMap")
public class MetaMapController {

    @Resource
    private IEntity service;

    /**
     * 根据类型获取获取元数据地图 0数据湖 1数仓
     *
     * @param type 0数据湖 1数仓
     * @return
     */
    @ApiOperation("根据类型获取获取元数据地图 0数据湖 1数仓")
    @GetMapping("/getMetaMapByType")
    public ResultEntity<List<MetaMapDTO>> getMetaMapByType(@ApiParam(value = "类型:0数据湖 1数仓")
                                                           @RequestParam("type") Integer type) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaMapByType(type));
    }

    /**
     * 元数据地图根据应用id或业务过程id获取表 0数据湖 1数仓
     *
     * @param type  0数据湖 1数仓
     * @param appId 应用id/业务过程id
     * @return
     */
    @ApiOperation("元数据地图根据应用id或业务过程id获取表 0数据湖 1数仓")
    @GetMapping("/getMetaMapTableDetailByType")
    public ResultEntity<List<MetaMapTblDTO>> getMetaMapTableDetailByType(
            @ApiParam(value = "类型:0数据湖 1数仓")
            @RequestParam("type") Integer type,
            @ApiParam(value = "应用/业务过程id")
            @RequestParam("appId") Integer appId,
            @ApiParam(value = "业务类型 0数据接入物理表 1数仓维度文件夹 2数仓业务过程")
            @RequestParam("businessType") Integer businessType
    ) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaMapTableDetailByType(type, appId,businessType));
    }


}
