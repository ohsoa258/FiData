package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.entity.EntityQueryDTO;
import com.fisk.mdm.dto.viwGroup.*;
import com.fisk.mdm.service.ViwGroupService;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:29
 * @Version 1.0
 */
@Api(tags = {SwaggerConfig.TAG_7})
@RestController
@RequestMapping("/viwGroup")
public class ViwGroupController {

    @Resource
    ViwGroupService viwGroupService;

    @ApiOperation("根据id查询视图组")
    @GetMapping("/getDataByGroupId")
    @ResponseBody
    public ResultEntity<List<ViwGroupVO>> getDataByGroupId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,viwGroupService.getDataByGroupId(id));
    }

    @ApiOperation("修改自定义视图组")
    @PutMapping("/updateData")
    @ResponseBody
    public ResultEntity<ResultEnum> updateData(@Validated @RequestBody ViwGroupUpdateDTO dto) {
        return ResultEntityBuild.build(viwGroupService.updateData(dto));
    }

    @ApiOperation("根据id删除自定义视图组")
    @DeleteMapping("/deleteGroupById")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteGroupById(Integer id) {
        return ResultEntityBuild.build(viwGroupService.deleteGroupById(id));
    }

    @ApiOperation("视图组新增属性")
    @PostMapping("/addAttribute")
    @ResponseBody
    public ResultEntity<ResultEnum> addAttribute(@RequestBody ViwGroupDetailsAddDTO dto) {
        return ResultEntityBuild.build(viwGroupService.addAttribute(dto));
    }

    @ApiOperation("视图组删除属性(根据属性id删除)")
    @DeleteMapping("/deleteAttribute")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteAttribute(@RequestBody ViwGroupDetailsDTO dto) {
        return ResultEntityBuild.build(viwGroupService.deleteAttribute(dto));
    }

    @ApiOperation("创建自定义视图组")
    @PostMapping("/addViwGroup")
    @ResponseBody
    public ResultEntity<ResultEnum> addViwGroup(@RequestBody ViwGroupDTO dto) {
        return ResultEntityBuild.build(viwGroupService.addViwGroup(dto));
    }

    @ApiOperation("查询视图组(根据实体id)")
    @GetMapping("/getDataByEntityId")
    @ResponseBody
    public ResultEntity<List<ViwGroupVO>> getDataByEntityId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,viwGroupService.getDataByEntityId(id));
    }

    @ApiOperation("根据实体id查询实体关联关系")
    @PostMapping("/getRelationByEntityId")
    @ResponseBody
    public ResultEntity<ViwGroupQueryRelationDTO> getRelationByEntityId(@RequestBody ViwGroupQueryDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,viwGroupService.getRelationByEntityId(dto));
    }

    @ApiOperation("创建自定义视图")
    @GetMapping("/createCustomView")
    @ResponseBody
    public ResultEntity<ResultEnum> createCustomView(Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,viwGroupService.createCustomView(id));
    }

    @ApiOperation("查询视图组属性关系(根据视图组id)")
    @PostMapping("/getRelationDataById")
    @ResponseBody
    public ResultEntity<List<EntityQueryDTO>> getRelationDataById(@RequestBody ViwGroupQueryDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,viwGroupService.getRelationDataById(dto));
    }
}
