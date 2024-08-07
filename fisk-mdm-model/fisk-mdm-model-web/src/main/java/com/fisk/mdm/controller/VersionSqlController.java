package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.versionsql.VersionSqlDTO;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.service.ITableVersionSqlService;
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
@Api(tags = {SwaggerConfig.TAG_16})
@RestController
@RequestMapping("/versionSql")
public class VersionSqlController {

    @Resource
    private ITableVersionSqlService service;

    /**
     * 通过表id和表类型获取表的所有版本sql
     * @param tblId 表id
     * @return
     */
    @GetMapping("/getVersionSqlByTableIdAndType")
    public ResultEntity<List<VersionSqlDTO>> getVersionSqlByTableIdAndType(@RequestParam("tblId") Integer tblId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getVersionSqlByTableIdAndType(tblId));
    }

}
