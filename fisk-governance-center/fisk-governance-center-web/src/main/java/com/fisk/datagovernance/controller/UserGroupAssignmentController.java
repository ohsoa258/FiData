package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.usergroupassignment.AddUserGroupAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupassignment.UserGroupAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDTO;
import com.fisk.datagovernance.service.datasecurity.UserGroupAssignmentService;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.userinfo.UserGroupQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 * @email jianwen@fisk.com.cn
 * @date 2022-03-28 15:47:33
 */
@Api(tags = SwaggerConfig.USER_GROUP_ASSIGNMENT)
@RestController
@RequestMapping("/userGroupAssignment")
public class UserGroupAssignmentController {

    @Resource
    private UserGroupAssignmentService service;

    @PostMapping("/getUserPage")
    @ApiOperation("分页获取所有用户信息")
    public ResultEntity<Object> getPageUserData(@RequestBody UserGroupQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getPageUserData(dto));
    }

    @PostMapping("/add")
    @ApiOperation("用户组添加系统用户")
    public ResultEntity<Object> userGroupAssignment(@RequestBody AddUserGroupAssignmentDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.saveData(dto));
    }

    @GetMapping("/getSelectedUser/{id}")
    @ApiOperation(value = "获取用户组下所有系统用户id")
    public ResultEntity<Object> getSelectedUser(@PathVariable("id") long id){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSelectedUser(id));
    }

}
