package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.components.*;
import com.fisk.chartvisual.service.ComponentsService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 组件菜单管理
 * @author WangYan
 * @date 2022/2/9 15:17
 */
@RestController
@RequestMapping("/component")
public class ComponentsController {

    @Resource
    ComponentsService componentsService;

    @ApiOperation("查询所有菜单")
    @PostMapping("/listData")
    @ResponseBody
    public ResultEntity<List<ComponentsClassDTO>> listData() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,componentsService.listData());
    }

    @ApiOperation("根据菜单id查询组件")
    @GetMapping("/selectClassById")
    @ResponseBody
    public ResultEntity<List<ComponentsDTO>> selectClassById(Integer id) {
        return componentsService.selectClassById(id);
    }

    @ApiOperation("保存菜单")
    @PostMapping("/saveClass")
    @ResponseBody
    public ResultEntity<ResultEnum> saveClass(@RequestBody ComponentsClassDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,componentsService.saveClass(dto));
    }

    @ApiOperation("保存组件并上传服务器")
    @PostMapping("/upload")
    @ResponseBody
    public ResultEntity<String> upload(SaveComponentsDTO dto, @RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,componentsService.saveComponents(dto,file));
    }

    @ApiOperation(value = "打包文件并下载zip文件")
    @GetMapping(value = "/downloadZip")
    public ResultEntity<ResultEnum> downloadZip(Integer id,HttpServletResponse response) {
        return ResultEntityBuild.build(componentsService.downloadFile(id,response));
    }

    @ApiOperation(value = "修改组件")
    @PutMapping(value = "/updateComponents")
    public ResultEntity<ResultEnum> updateComponents(@Validated @RequestBody ComponentsEditDTO dto) {
        return ResultEntityBuild.build(componentsService.updateComponents(dto));
    }

    @ApiOperation(value = "删除组件")
    @DeleteMapping(value = "/deleteComponents")
    public ResultEntity<ResultEnum> deleteComponents(Integer id) {
        return ResultEntityBuild.build(componentsService.deleteComponents(id));
    }

    @ApiOperation(value = "修改菜单")
    @PutMapping(value = "/updateComponentsClass")
    public ResultEntity<ResultEnum> updateComponentsClass(@Validated @RequestBody ComponentsClassEditDTO dto) {
        return ResultEntityBuild.build(componentsService.updateComponentsClass(dto));
    }

    @ApiOperation(value = "删除菜单")
    @DeleteMapping(value = "/deleteComponentsClass")
    public ResultEntity<ResultEnum> deleteComponentsClass(Integer id) {
        return ResultEntityBuild.build(componentsService.deleteComponentsClass(id));
    }

    @ApiOperation("保存组件不同版本信息")
    @PostMapping("/saveComponentsOption")
    @ResponseBody
    public ResultEntity<String> saveComponentsOption( SaveComponentsOptionDTO dto, @RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,componentsService.saveComponentsOption(dto,file));
    }
}
