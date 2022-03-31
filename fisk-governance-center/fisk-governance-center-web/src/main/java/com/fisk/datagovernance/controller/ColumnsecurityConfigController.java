package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.ColumnsecurityConfigDTO;
import com.fisk.datagovernance.service.datasecurity.ColumnsecurityConfigService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@RestController
@RequestMapping("/columnsecurityconfig")
public class ColumnsecurityConfigController {

    @Autowired
    private ColumnsecurityConfigService service;

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<ColumnsecurityConfigDTO> getData(@PathVariable("id") long id){

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 保存
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody ColumnsecurityConfigDTO columnsecurityConfig){

        return ResultEntityBuild.build(service.addData(columnsecurityConfig));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody ColumnsecurityConfigDTO dto){

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
