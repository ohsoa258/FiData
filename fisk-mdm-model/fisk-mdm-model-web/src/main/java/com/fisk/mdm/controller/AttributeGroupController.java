package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.attributeGroup.*;
import com.fisk.mdm.service.AttributeGroupService;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;
import com.fisk.mdm.vo.attributeGroup.QueryAttributeGroupVO;
import com.fisk.mdm.vo.entity.EntityViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:33
 * @Version 1.0
 */
@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/attributeGroup")
public class AttributeGroupController {

    @Resource
    AttributeGroupService groupService;

    @ApiOperation("根据组id查询属性组")
    @GetMapping("/getDataByGroupId")
    @ResponseBody
    public ResultEntity<AttributeGroupVO> getDataByGroupId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,groupService.getDataByGroupId(id));
    }

    @ApiOperation("修改属性组信息")
    @PutMapping("/updateData")
    @ResponseBody
    public ResultEntity<ResultEnum> updateData(@Validated @RequestBody AttributeGroupUpdateDTO dto) {
        return ResultEntityBuild.build(groupService.updateData(dto));
    }

    @ApiOperation("根据id删除属性组")
    @DeleteMapping("/deleteGroupById")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteGroupById(Integer id) {
        return ResultEntityBuild.build(groupService.deleteGroupById(id));
    }

    @ApiOperation("属性组新增属性")
    @PostMapping("/addAttribute")
    @ResponseBody
    public ResultEntity<ResultEnum> addAttribute(@RequestBody AttributeGroupDetailsAddDTO dto) {
        return ResultEntityBuild.build(groupService.addAttribute(dto));
    }

    @ApiOperation("属性组根据属性id删除")
    @DeleteMapping("/deleteAttribute")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteAttribute(@RequestBody AttributeGroupDetailsDTO dto) {
        return ResultEntityBuild.build(groupService.deleteAttribute(dto));
    }

    @ApiOperation("创建属性组")
    @PostMapping("/addAttributeGroup")
    @ResponseBody
    public ResultEntity<ResultEnum> addAttributeGroup(@RequestBody AttributeGroupDTO dto) {
        return ResultEntityBuild.build(groupService.addAttributeGroup(dto));
    }

    @ApiOperation("根据模型id查询属性组")
    @GetMapping("/getDataByModelId")
    @ResponseBody
    public ResultEntity<List<AttributeGroupVO>> getDataByModelId(Integer id,String name) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,groupService.getDataByModelId(id,name));
    }

    @ApiOperation("根据组id查询属性组(根据实体进行分组)")
    @GetMapping("/getDataGroupById")
    @ResponseBody
    public ResultEntity<List<QueryAttributeGroupVO>> getDataGroupById(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,groupService.getDataGroupById(id));
    }

    @ApiOperation("获取出属性组存在的属性")
    @PostMapping("/getAttributeExists")
    @ResponseBody
    public ResultEntity<List<EntityViewVO>> getAttributeExists(@RequestBody AttributeInfoQueryDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,groupService.getAttributeExists(dto));
    }
}
