package com.fisk.datafactory.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@RestController
@RequestMapping("/nifiCustomWorkflowDetail")
@Slf4j
public class NifiCustomWorkflowDetailController {
    @Resource
    INifiCustomWorkflowDetail service;

    @ApiOperation("添加管道详情")
    @PostMapping("/add")
    public ResultEntity<NifiCustomWorkflowDetailDTO> addData(@RequestBody NifiCustomWorkflowDetailDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.addData(dto));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据")
    public ResultEntity<NifiCustomWorkflowDetailDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改管道详情")
    public ResultEntity<Object> editData(@RequestBody NifiCustomWorkflowDetailVO dto) {

        return ResultEntityBuild.build(service.editData(dto));
    }

    @PutMapping("/editComponent")
    @ApiOperation(value = "修改单个管道组件")
    public ResultEntity<Object> editData(@RequestBody NifiCustomWorkflowDetailDTO dto) {

        return ResultEntityBuild.build(service.editWorkflow(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除管道详情")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }
}
