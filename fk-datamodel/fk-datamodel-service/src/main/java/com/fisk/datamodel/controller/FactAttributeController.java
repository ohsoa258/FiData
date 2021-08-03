package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.factattribute.FactAttributeAddDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.service.IFactAttribute;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = { SwaggerConfig.TAG_1 })
//@Api(description = "事实字段")
@RestController
@RequestMapping("/factAttribute")
@Slf4j
public class FactAttributeController {

    @Resource
    IFactAttribute service;

    @ApiOperation("获取事实字段表列表")
    @GetMapping("/getFactAttributeList/{factId}")
    public ResultEntity<Object> getFactAttributeList(@PathVariable int factId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeList(factId));
    }

    @ApiOperation("添事实字段")
    @PostMapping("/addFactAttribute")
    public ResultEntity<Object> addFactAttribute(@Validated @RequestBody FactAttributeAddDTO dto)
    {
        return ResultEntityBuild.build(service.addFactAttribute(dto.factId,dto.list));
    }

    @ApiOperation("删除事实字段")
    @PostMapping("/deleteFactAttribute")
    public ResultEntity<Object> deleteFactAttribute(@RequestBody List<Integer> ids)
    {
        return ResultEntityBuild.build(service.deleteFactAttribute(ids));
    }

    @ApiOperation("修改事实字段")
    @PutMapping("/editFactAttribute")
    public ResultEntity<Object> editFactAttribute(@Validated @RequestBody FactAttributeUpdateDTO dto) {
        return ResultEntityBuild.build(service.updateFactAttribute(dto));
    }

}
