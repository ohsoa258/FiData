package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.codeRule.*;
import com.fisk.mdm.service.CodeRuleService;
import com.fisk.mdm.vo.codeRule.CodeRuleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/23 14:06
 * @Version 1.0
 */
@Api(tags = {SwaggerConfig.TAG_11})
@RestController
@RequestMapping("/rule")
public class CodeRuleController {

    @Autowired
    CodeRuleService ruleService;

    @ApiOperation("根据id查询编码规则组")
    @GetMapping("/getDataByGroupId")
    @ResponseBody
    public ResultEntity<List<CodeRuleVO>> getDataByGroupId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,ruleService.getDataByGroupId(id));
    }

    @ApiOperation("修改自定义编码规则组")
    @PutMapping("/updateData")
    @ResponseBody
    public ResultEntity<ResultEnum> updateData(@Validated @RequestBody CodeRuleGroupUpdateDTO dto) {
        return ResultEntityBuild.build(ruleService.updateData(dto));
    }

    @ApiOperation("根据id删除编码规则组")
    @DeleteMapping("/deleteGroupById")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteGroupById(Integer id) {
        return ResultEntityBuild.build(ruleService.deleteGroupById(id));
    }

    @ApiOperation("编码规则组新增规则")
    @PostMapping("/addCodeRule")
    @ResponseBody
    public ResultEntity<ResultEnum> addCodeRule(@RequestBody CodeRuleAddDTO dto) {
        return ResultEntityBuild.build(ruleService.addCodeRule(dto));
    }

    @ApiOperation("编码规则组删除规则(根据规则id)")
    @DeleteMapping("/deleteCodeRuleById")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteCodeRuleById(@RequestBody CodeRuleDeleteDTO dto) {
        return ResultEntityBuild.build(ruleService.deleteCodeRuleById(dto));
    }

    @ApiOperation("创建编码规则组")
    @PostMapping("/addRuleGroup")
    @ResponseBody
    public ResultEntity<ResultEnum> addRuleGroup(@RequestBody CodeRuleGroupDTO dto) {
        return ResultEntityBuild.build(ruleService.addRuleGroup(dto));
    }

    @ApiOperation("查询编码规则组(根据实体id)")
    @GetMapping("/getDataByEntityId")
    @ResponseBody
    public ResultEntity<List<CodeRuleVO>> getDataByEntityId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,ruleService.getDataByEntityId(id));
    }
}
