package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.viwGroup.UpdateViwGroupDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDetailsDTO;
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

    @ApiOperation("根据组id查询属性组")
    @GetMapping("/getDataByGroupId")
    @ResponseBody
    public ResultEntity<ViwGroupVO> getDataByGroupId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,viwGroupService.getDataByGroupId(id));
    }

    @ApiOperation("修改自定义视图组")
    @PutMapping("/updateData")
    @ResponseBody
    public ResultEntity<ResultEnum> updateData(@Validated @RequestBody UpdateViwGroupDTO dto) {
        return ResultEntityBuild.build(viwGroupService.updateData(dto));
    }

    @ApiOperation("根据id删除自定义视图组")
    @DeleteMapping("/deleteGroupById")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteGroupById(Integer id) {
        return ResultEntityBuild.build(viwGroupService.deleteGroupById(id));
    }

    @ApiOperation("属性组新增属性")
    @PostMapping("/addAttribute")
    @ResponseBody
    public ResultEntity<ResultEnum> addAttribute(@RequestBody ViwGroupDetailsDTO dto) {
        return ResultEntityBuild.build(viwGroupService.addAttribute(dto));
    }

//    @ApiOperation("属性组根据属性id删除")
//    @DeleteMapping("/deleteAttribute")
//    @ResponseBody
//    public ResultEntity<ResultEnum> deleteAttribute(@RequestBody AttributeGroupDetailsDTO dto) {
//        return ResultEntityBuild.build(viwGroupService.deleteAttribute(dto));
//    }

    @ApiOperation("创建自定义视图组")
    @PostMapping("/addViwGroup")
    @ResponseBody
    public ResultEntity<ResultEnum> addViwGroup(@RequestBody ViwGroupDTO dto) {
        return ResultEntityBuild.build(viwGroupService.addViwGroup(dto));
    }

    @ApiOperation("根据实体id查询")
    @GetMapping("/getDataByEntityId")
    @ResponseBody
    public ResultEntity<List<ViwGroupVO>> getDataByEntityId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,viwGroupService.getDataByEntityId(id));
    }
}
