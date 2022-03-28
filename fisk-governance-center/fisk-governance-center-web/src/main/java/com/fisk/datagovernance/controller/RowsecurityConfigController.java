package com.fisk.datagovernance.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowsecurityConfigDTO;
import com.fisk.datagovernance.service.datasecurity.RowsecurityConfigService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@RestController
@RequestMapping("/rowsecurityconfig")
public class RowsecurityConfigController {

    @Autowired
    private RowsecurityConfigService service;

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<RowsecurityConfigDTO> getData(@PathVariable("id") long id){

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 保存
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody RowsecurityConfigDTO rowsecurityConfig){

        return ResultEntityBuild.build(service.addData(rowsecurityConfig));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody RowsecurityConfigDTO dto){

        return ResultEntityBuild.build(service.editData(dto));
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

}
