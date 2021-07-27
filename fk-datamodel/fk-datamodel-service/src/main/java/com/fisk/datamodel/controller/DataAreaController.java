package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessNameDTO;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.dto.DataAreaQueryDTO;
import com.fisk.datamodel.service.IDataArea;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Api(description = "数据域接口")
@RestController
@RequestMapping("/dataArea")
@Slf4j
public class DataAreaController {

    @Resource
    private IDataArea service;

    @GetMapping("/getBusinessName")
    @ApiOperation(value = "添加数据域时,显示所有业务域")
    public ResultEntity<List<BusinessNameDTO>> getBusinessName() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getBusinessName());
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加数据域(对象)")
    public ResultEntity<Object> addData(@RequestBody DataAreaDTO dataAreaDTO) {

        return ResultEntityBuild.build(service.addData(dataAreaDTO));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<DataAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改(对象)")
    public ResultEntity<Object> editData(@RequestBody DataAreaDTO dataAreaDTO) {

        return ResultEntityBuild.build(service.updateDataArea(dataAreaDTO));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除数据域(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteDataArea(id));
    }

    @GetMapping("/page")
    @ApiOperation(value = "数据域首页分页查询(url拼接)")
    public ResultEntity<Page<Map<String,Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }

    @PostMapping("/dataFilter")
    @ApiOperation(value = "筛选器")
    public ResultEntity<Page<DataAreaDTO>> dataFilter(@RequestBody DataAreaQueryDTO query){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.dataFilter(query));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取数据域表字段")
    public ResultEntity<Object> getDataColumn(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getDataAreaColumn());
    }

}
