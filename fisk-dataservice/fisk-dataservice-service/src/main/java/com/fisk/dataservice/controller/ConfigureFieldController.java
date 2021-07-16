package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.service.ApiConfigureFieldService;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/8 10:04
 */
@RestController
@RequestMapping("/config")
public class ConfigureFieldController {

    @Resource
    private ApiConfigureFieldService configureFieldService;

    @ApiOperation("添加字段配置")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody List<ApiConfigureFieldPO> dto,String apiName,String apiInfo,Integer distinctData) {
        return ResultEntityBuild.build(configureFieldService.saveConfigure(dto,apiName,apiInfo,distinctData));
    }

    @ApiOperation("删除字段")
    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteDataById(Integer id) {
        return ResultEntityBuild.build(configureFieldService.deleteDataById(id));
    }

    @ApiOperation("修改字段")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody ApiConfigureFieldPO dto) {
        return ResultEntityBuild.build(configureFieldService.updateField(dto));
    }

    @ApiOperation("根据id查询字段")
    @GetMapping("/get")
    public ResultEntity<ApiConfigureFieldPO> getDataById(Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.getDataById(id));
    }

    @ApiOperation("分页查询所有字段")
    @GetMapping("/getAll")
    public ResultEntity<List<ApiConfigureFieldPO>> listData(Integer currentPage,Integer pageSize) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.listData(currentPage,pageSize));
    }

}
