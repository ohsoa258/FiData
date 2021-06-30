package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.service.IBusinessArea;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: Lock
 */
@Api(description = "业务域接口")
@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessAreaController {

    @Autowired
    private IBusinessArea service;

    /**
     * 添加业务域
     * @param businessAreaDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加业务域[对象]")
    public ResultEntity<Object> addData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.addData(businessAreaDTO));
    }

    /**
     * 回显数据: 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<BusinessAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 业务域修改
     *
     * @param businessAreaDTO
     * @return
     */
    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改(对象)")
    public ResultEntity<Object> editData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.updateBusinessArea(businessAreaDTO));
    }

    /**
     * 删除业务域
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除业务域(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteBusinessArea(id));
    }


    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询(url拼接)")
    public ResultEntity<Page<Map<String, Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }


}
