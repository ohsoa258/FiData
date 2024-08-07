package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.api.VersionSqlDTO;
import com.fisk.dataservice.service.ITableVersionSqlService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lsj
 */
@Api(tags = {SwaggerConfig.TAG_12})
@RestController
@RequestMapping("/versionSql")
public class VersionSqlController {

    @Resource
    private ITableVersionSqlService service;

    /**
     * 通过表id和表类型获取表的所有版本sql
     * @param apiId 表id
     * @return
     */
    @GetMapping("/getVersionSqlByTableIdAndType")
    public ResultEntity<List<VersionSqlDTO>> getVersionSqlByTableIdAndType(@RequestParam("apiId") Integer apiId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getVersionSqlByTableIdAndType(apiId));
    }

}
