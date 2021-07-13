package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.AreaBusinessDTO;
import com.fisk.datamodel.service.IAreaBusiness;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(description = "业务域接口2.0")
@RestController
@RequestMapping("/business2.0")
@Slf4j
public class AreaBusinessController {

    @Resource
    private IAreaBusiness service;

    @PostMapping("/add")
    @ApiOperation(value = "添加业务域[对象]")
    public ResultEntity<Object> addData(@RequestBody AreaBusinessDTO dto) {

        return ResultEntityBuild.build(service.addData(dto));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<AreaBusinessDTO> getDataById(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataById(id));
    }

/*    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改(对象)")
    public ResultEntity<Object> editData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.updateBusinessArea(businessAreaDTO));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除业务域(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteBusinessArea(id));
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询(url拼接)")
    public ResultEntity<Page<Map<String, Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }*/


}
