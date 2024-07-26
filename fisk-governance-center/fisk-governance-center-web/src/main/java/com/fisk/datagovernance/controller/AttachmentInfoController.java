package com.fisk.datagovernance.controller;

import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.service.dataquality.IAttachmentInfoManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dick
 * @version 1.0
 * @description 附件控制器
 * @date 2024/7/25 16:15
 */
@Api(tags = {SwaggerConfig.ATTACHMENT_INFO})
@RestController
@RequestMapping("/attachmentInfo")
public class AttachmentInfoController {

    @Resource
    private IAttachmentInfoManageService service;

    /**
     * 下载Excel文件
     *
     * @return 文件流
     */
    @ApiOperation("下载Excel文件")
    @GetMapping("/downloadExcelFile")
    @ControllerAOPConfig(printParams = false)
    public void downloadExcelFile(@RequestParam("objectId") String objectId,
                                  @RequestParam("category") String category,
                                  HttpServletResponse response) {
        service.downloadExcelFile(objectId, category, response);
    }

}
