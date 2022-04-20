package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.vo.model.ModelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author chenYa
 */
@Api(tags = {SwaggerConfig.TAG_2})
@RestController
@RequestMapping("/model")
public class ModelController {

    @Resource
    private IModelService service;

    @ApiOperation("分页查询所有model")
    @PostMapping("/list")
    public ResultEntity<Page<ModelVO>> getAll(@RequestBody ModelQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }


    @ApiOperation("根据id查询model")
    @GetMapping("/get")
    public ResultEntity<ModelVO> detail(Integer id) {
        return service.getById(id);
    }

    @ApiOperation("添加model")
    @PostMapping("/insert")
    public ResultEntity<ResultEnum> addData(@RequestBody ModelDTO model) {
        return ResultEntityBuild.build(service.addData(model));
    }

    @ApiOperation("编辑model")
    @PutMapping("/update")
    public ResultEntity<ResultEnum> editData(@Validated @RequestBody ModelUpdateDTO model) {
        return ResultEntityBuild.build(service.editData(model));
    }

    @ApiOperation("删除model")
    @DeleteMapping("/delete")
    public ResultEntity<ResultEnum> deleteData(Integer id) {
        return ResultEntityBuild.build(service.deleteDataById(id));
    }

}
