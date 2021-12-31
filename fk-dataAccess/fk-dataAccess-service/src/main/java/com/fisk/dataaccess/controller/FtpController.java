package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.service.IFtp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 * @version 1.0
 * @description ftp数据源API
 * @date 2021/12/28 10:47
 */
@Api(tags = {SwaggerConfig.FTP})
@RestController
@RequestMapping("/ftp")
public class FtpController {

    @Resource
    private IFtp service;

    @ApiOperation("测试ftp数据源连接")
    @PostMapping("/connectFtp")
    public ResultEntity<Object> connectFtp(@RequestBody DbConnectionDTO dto) {

        return service.connectFtp(dto);
    }

    @ApiOperation(value = "点击文件预览内容")
    @PostMapping("/previewContent")
    public ResultEntity<Object> previewContent(@RequestBody OdsQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.previewContent(query));
    }

    @ApiOperation(value = "加载ftp文件系统")
    @PostMapping("/loadFtpFileSystem")
    public ResultEntity<Object> loadFtpFileSystem(@RequestBody FtpPathDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.loadFtpFileSystem(dto));
    }

}
