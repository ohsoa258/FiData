package com.fisk.system.web;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.RoleInfoDTO;
import com.fisk.system.service.IRoleInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.ROLE_INFO})
@RestController
@RequestMapping("/role")
@Slf4j
public class RoleInfoController {
    @Resource
    IRoleInfoService service;

    @GetMapping("/page")
    @ApiOperation("获取所有角色信息")
    public ResultEntity<Object> getRoleList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listRoleData());
    }

    @ApiOperation("添加角色")
    @PostMapping("/addRole")
    public ResultEntity<Object> addRole(@Validated @RequestBody RoleInfoDTO dto) {
        return ResultEntityBuild.build(service.addRole(dto));
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/deleteRole/{id}")
    public ResultEntity<Object> deleteRole(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteRole(id));
    }

    @ApiOperation("根据id获取角色详情")
    @GetMapping("/getRole/{id}")
    public ResultEntity<Object> getRole(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRoleById(id));
    }

    @ApiOperation("修改角色")
    @PutMapping("/edit")
    public ResultEntity<Object> editRole(@Validated @RequestBody RoleInfoDTO dto) {
        return ResultEntityBuild.build(service.updateRole(dto));
    }

}
