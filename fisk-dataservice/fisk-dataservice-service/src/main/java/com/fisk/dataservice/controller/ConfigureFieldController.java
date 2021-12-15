package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.vo.ApiFieldDataVO;
import com.fisk.dataservice.dto.ApiConfigureFieldEditDTO;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.service.ApiConfigureFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/8 10:04
 */
@Api(tags = {SwaggerConfig.TAG_2})
@RestController
@RequestMapping("/apiField")
public class ConfigureFieldController {

    @Resource
    private ApiConfigureFieldService configureFieldService;

    @ApiOperation("添加字段配置")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody ApiFieldDataVO dto) {
        return ResultEntityBuild.build(configureFieldService.saveConfigure(dto));
    }

    @ApiOperation("删除字段")
    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteDataById(Integer id) {
        return ResultEntityBuild.build(configureFieldService.deleteDataById(id));
    }

    @ApiOperation("修改字段")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody ApiConfigureFieldEditDTO dto) {
        return ResultEntityBuild.build(configureFieldService.updateField(dto));
    }

    @ApiOperation("根据Api的id查询字段")
    @GetMapping("/getByConfigureId")
    public ResultEntity<List<ApiConfigureFieldPO>> getDataById(Integer configureId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.getDataById(configureId));
    }

    @ApiOperation("根据id查询字段")
    @GetMapping("/getById")
    public ResultEntity<Object> getById(Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.getById(id));
    }

    @ApiOperation("获取字段拼接")
    @GetMapping("/getAllField")
    public ResultEntity<Object> getAllField() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.getAllField());
    }
}
