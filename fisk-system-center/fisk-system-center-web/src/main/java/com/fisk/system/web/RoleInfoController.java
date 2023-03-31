package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.roleinfo.RoleInfoQueryDTO;
import com.fisk.system.service.IRoleInfoService;
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
@Api(tags = {SwaggerConfig.ROLE_INFO})
@RestController
@RequestMapping("/role")
@Slf4j
public class RoleInfoController {
    @Resource
    IRoleInfoService service;

    @PostMapping("/page")
    @ApiOperation("获取所有角色信息")
    public ResultEntity<Object> getRoleList(@RequestBody RoleInfoQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listRoleData(query));
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

    @ApiOperation("根据ids获取角色列表详情")
    @PostMapping("/getRoles")
    public ResultEntity<List<RoleInfoDTO>> getRoles(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRoleByIds(ids));
    }

    @ApiOperation("修改角色")
    @PutMapping("/edit")
    public ResultEntity<Object> editRole(@Validated @RequestBody RoleInfoDTO dto) {
        return ResultEntityBuild.build(service.updateRole(dto));
    }

    @GetMapping("/getColumn")
    @ApiOperation("获取角色筛选器列表")
    public ResultEntity<Object> getColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRoleInfoColumn());
    }

    @GetMapping("/getTreeRols")
    @ApiOperation("获取所有角色及角色下用户列表")
    public ResultEntity<Object> getTreeRols() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTreeRols());
    }
}
