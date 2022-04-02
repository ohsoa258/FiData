package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/4/2 12:50
 */
@Api(tags = {SwaggerConfig.UPLOAD})
@RestController
@RequestMapping("/uplod")
public class UploadController {

    @Resource
    UploadService uploadService;

    @ApiOperation("图片上传到服务器")
    @PostMapping("/upload")
    @ResponseBody
    public ResultEntity<String> upload(@RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, uploadService.upload(file));
    }
}
