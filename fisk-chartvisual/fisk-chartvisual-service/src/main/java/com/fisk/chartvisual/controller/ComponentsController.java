package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ComponentsDTO;
import com.fisk.chartvisual.service.ComponentsService;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 组件管理
 * @author WangYan
 * @date 2022/2/9 15:17
 */
@RestController
@RequestMapping("/component")
public class ComponentsController {

    @Resource
    ComponentsService componentsService;


    @ApiOperation("保存组件")
    @PostMapping("/upload")
    @ResponseBody
    public ResultEntity<String> upload( ComponentsDTO dto,@RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,componentsService.saveComponents(dto,file));
    }
}
