package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.attribute.*;
import com.fisk.mdm.entity.Entity;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ChenYa
 * @date 2022/4/14 20:35
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/attribute")
public class AttributeController {
    @Resource
    AttributeService service;

    @ApiOperation("分页查询所有attribute")
    @PostMapping("/list")
    public ResultEntity<Page<AttributeVO>> getAll(@RequestBody AttributeQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("根据id查询attribute")
    @GetMapping("/get")
    public ResultEntity<AttributeVO> detail(Integer id) {
        return service.getById(id);
    }

    @ApiOperation("添加attribute")
    @PostMapping("/insert")
    public ResultEntity<ResultEnum> addData(@RequestBody AttributeDTO attributeDTO) {
        return ResultEntityBuild.build(service.addData(attributeDTO));
    }

    @ApiOperation("编辑attribute")
    @PutMapping("/update")
    public ResultEntity<ResultEnum> editData(@Validated @RequestBody AttributeUpdateDTO attributeUpdateDTO) {
        return ResultEntityBuild.build(service.editData(attributeUpdateDTO));
    }

    @ApiOperation("删除attribute")
    @DeleteMapping("/delete")
    public ResultEntity<ResultEnum> deleteData(Integer id) {
        return ResultEntityBuild.build(service.deleteDataById(id));
    }

    @ApiOperation("提交待添加和待修改的属性")
    @GetMapping("/getNotSubmittedData")
    public ResultEntity<ResultEnum> getNotSubmittedData(Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getNotSubmittedData(entityId));
    }

    @ApiOperation("获取实体、属性信息")
    @GetMapping("/getER")
    public ResultEntity<List<Entity>> getER() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getER());
    }

    @ApiOperation("根据id集合查询属性信息")
    @PostMapping("/getByIds")
    public ResultEntity<List<AttributeInfoDTO>> getByIds(@RequestBody List<Integer> ids) {
        return service.getByIds(ids);
    }

    @ApiOperation("根据domainId查询属性")
    @PostMapping("/getByDomainId")
    public ResultEntity<AttributeInfoDTO> getByDomainId(@RequestBody AttributeDomainDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getByDomainId(dto));
    }

    @ApiOperation("修改属性状态")
    @PutMapping("/updateStatus")
    public ResultEntity<ResultEnum> updateStatus(@Validated @RequestBody AttributeStatusDTO statusDto) {
        return ResultEntityBuild.build(service.updateStatus(statusDto));
    }
}
