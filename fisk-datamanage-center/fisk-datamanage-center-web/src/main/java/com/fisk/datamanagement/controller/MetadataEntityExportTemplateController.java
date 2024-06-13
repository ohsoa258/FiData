package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.EditMetadataExportTemplateDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDetailDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDto;
import com.fisk.datamanagement.service.IMetadataEntityExportTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.EXPORT_TEMPLATE})
@RestController
@RequestMapping("/ExportTemplate")
public class MetadataEntityExportTemplateController {
    @Resource
    IMetadataEntityExportTemplateService service;

    @ApiOperation("获取导出模板")
    @GetMapping("/get/{id}")
    public ResultEntity<Object> get(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.get(id));
    }

    @ApiOperation("添加导出模板")
    @PostMapping("/add")
    public ResultEntity<Object> add(@Validated @RequestBody MetadataExportTemplateDetailDto dto){
        return ResultEntityBuild.build(service.add(dto));

    }

    @ApiOperation("修改导出模板")
    @PostMapping("/edit")
    public ResultEntity<Object> edit(@Validated @RequestBody EditMetadataExportTemplateDto dto){
        return ResultEntityBuild.build(service.edit(dto));
    }

    @ApiOperation("删除导出模板")
    @DeleteMapping("/delete")
    @ResponseBody
    public ResultEntity<Object> delete(Integer id){
        return ResultEntityBuild.build(service.delete(id));
    }

    @ApiOperation("获取导出模板")
    @GetMapping("/getAll")
    public ResultEntity<Object> edit(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getAllTemplate());
    }
}
