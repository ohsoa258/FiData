package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.dto.sftp.SftpPreviewQueryDTO;
import com.fisk.dataaccess.service.ISftp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.SFTP})
@RestController
@RequestMapping("/Sftp")
public class SftpController {

    @Resource
    ISftp service;

    @ApiOperation("测试ftp数据源连接")
    @PostMapping("/connectFtp")
    public ResultEntity<Object> connectFtp(@RequestBody DbConnectionDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.connectSftp(dto));
    }

    @ApiOperation("获取sftp文件和文件夹")
    @PostMapping("/getSftpFile")
    public ResultEntity<Object> getSftpFile(@RequestBody FtpPathDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFile(dto));
    }

    @ApiOperation("预览sftp文件")
    @PostMapping("/previewContent")
    public ResultEntity<Object> previewContent(@RequestBody SftpPreviewQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.previewContent(dto));
    }

    @ApiOperation("上传文件")
    @PostMapping("/uploadFile")
    @ResponseBody
    @ControllerAOPConfig(printParams = false)
    public ResultEntity<Object> uploadFile(String abbreviationName, @RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.uploadSecretKeyFile(abbreviationName, file));
    }

}
