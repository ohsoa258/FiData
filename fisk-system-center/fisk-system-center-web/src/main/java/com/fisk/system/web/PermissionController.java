package com.fisk.system.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.userinfo.UserGroupQueryDTO;
import com.fisk.system.dto.userinfo.UserPowerDTO;
import com.fisk.system.service.IRoleInfoService;
import com.fisk.system.service.IRoleServiceAssignmentService;
import com.fisk.system.service.IRoleUserAssignmentService;
import com.fisk.system.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.PERMISSION})
@RestController
@RequestMapping("/auth")
@Slf4j
public class PermissionController {

    @Resource
    IRoleInfoService service;
    @Resource
    IUserService userService;
    @Resource
    IRoleUserAssignmentService assignmentService;
    @Resource
    IRoleServiceAssignmentService registerService;

    @GetMapping("/getRolePage")
    @ApiOperation("分页获取所有角色信息")
    public ResultEntity<Object> getRolePageList(QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getPageRoleData(dto));
    }

    @GetMapping("/getUserPage")
    @ApiOperation("分页获取所有用户信息")
    public ResultEntity<Object> getUserPageList(QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.getPageUserData(dto));
    }

    @PostMapping("/getUserGroupAssignmentPageList")
    @ApiOperation("数据治理-分页获取所有用户信息")
    public ResultEntity<Page<UserPowerDTO>> getUserGroupAssignmentPageList(@RequestBody UserGroupQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.userGroupQuery(dto));
    }

    @PostMapping("/userGroupQuery")
    @ApiOperation("数据治理-用户组筛选系统用户")
    public ResultEntity<Page<UserPowerDTO>> userGroupQuery(@RequestBody UserGroupQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.userGroupQuery(dto));
    }

    @GetMapping("/getRoleUser/{id}")
    @ApiOperation("根据角色id,获取关联角色id")
    public ResultEntity<Object> getRoleUserList(@PathVariable("id") int roleId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, assignmentService.getRoleUserList(roleId));
    }

    @GetMapping("/getRoleService/{id}")
    @ApiOperation("根据角色id,获取关联服务id")
    public ResultEntity<Object> getRoleServiceList(@PathVariable("id")int roleId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, registerService.getRoleServiceList(roleId));
    }

    @PostMapping("/addRoleUser")
    @ApiOperation("根据角色,添加关联用户")
    public ResultEntity<Object> addRoleUser(@Validated @RequestBody AssignmentDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, assignmentService.addRoleUserAssignment(dto));
    }

    @PostMapping("/addService")
    @ApiOperation("根据角色,添加关联服务")
    public ResultEntity<Object> addRoleService(@Validated @RequestBody AssignmentDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, registerService.addRoleServiceAssignment(dto));
    }

    @GetMapping("/getService")
    @ApiOperation("根据当前登录人,获取服务列表")
    public ResultEntity<Object> getLoginService() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, registerService.getServiceList());
    }

    @GetMapping("/getAllMenuList")
    @ApiOperation("获取服务列表")
    public ResultEntity<Object> getAllMenuList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, registerService.getAllMenuList());
    }
}
