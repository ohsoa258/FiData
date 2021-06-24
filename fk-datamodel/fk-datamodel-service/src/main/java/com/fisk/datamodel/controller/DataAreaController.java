package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.dto.BusinessNameDTO;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.service.IDataArea;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Lock
 */
@Api(description = "数据域接口")
@RestController
@RequestMapping("/dataArea")
@Slf4j
public class DataAreaController {

    @Autowired
    private IDataArea service;

    /**
     * 添加数据域时,显示所有业务域
     * @return
     */
    @GetMapping("/getBusinessName")
    @ApiOperation(value = "添加数据域时,显示所有业务域")
    public ResultEntity<List<BusinessNameDTO>> getBusinessName() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getBusinessName());
    }

    /**
     * 添加数据域
     * @param
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加数据域")
    public ResultEntity<Object> addData(@RequestBody DataAreaDTO dataAreaDTO) {

        return ResultEntityBuild.build(service.addData(dataAreaDTO));
    }

    /**
     * 回显数据: 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询")
    public ResultEntity<DataAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 业务域修改
     *
     * @param dataAreaDTO
     * @return
     */
    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改")
    public ResultEntity<Object> editData(@RequestBody DataAreaDTO dataAreaDTO) {

        return ResultEntityBuild.build(service.updateDataArea(dataAreaDTO));
    }

    /**
     * 删除业务域
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除业务域")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteDataArea(id));
    }


}
