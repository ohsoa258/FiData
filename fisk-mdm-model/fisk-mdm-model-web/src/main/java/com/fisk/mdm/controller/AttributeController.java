package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeQueryDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.model.ModelVO;
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
    public ResultEntity<ResultEnum> getNotSubmittedData() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getNotSubmittedData());
    }

    @ApiOperation("获取实体下的属性名称")
    @GetMapping("/getER/{entityId}")
    public ResultEntity<List<String>> getER(@PathVariable("entityId") int entityId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getER(entityId));
    }

}
