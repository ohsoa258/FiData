package com.fisk.datamanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.email.*;
import com.fisk.datamanagement.service.EmailUserPOService;
import com.fisk.datamanagement.service.IEmailGroupService;
import com.fisk.datamanagement.service.IEmailGroupUserMapService;
import com.fisk.datamanagement.service.impl.IEmailGroupServiceImpl;
import com.fisk.system.dto.userinfo.UserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = {SwaggerConfig.EMAIL_GROUP})
@RestController
@RequestMapping("/email")
public class ManageEmailController {

    @Resource
    private IEmailGroupService emailGroupService;

    @Resource
    private EmailUserPOService emailUserPOService;

    @Resource
    private IEmailGroupServiceImpl impl;

    /**
     * 分页查询邮件组
     *
     * @param currentPage 当前页
     * @param size        页大小
     * @return
     */
    @ApiOperation("分页查询邮件组")
    @GetMapping("/pageFilterE")
    public ResultEntity<Page<EmailGroupDTO>> pageFilterE(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.pageFilterE(currentPage,size));
    }

    /**
     * 获取单个邮件组详情
     *
     * @param groupId 组id
     * @return
     */
    @ApiOperation("获取单个邮件组详情")
    @GetMapping("/getEmailGroupDetailById")
    public ResultEntity<EmailGroupDetailDTO> getEmailGroupDetailById(@RequestParam("groupId") Integer groupId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.getEmailGroupDetailById(groupId));
    }


    /**
     * 获取所有邮件组
     *
     * @return
     */
    @ApiOperation("获取所有邮件组")
    @GetMapping("/getAllEGroups")
    public ResultEntity<List<EmailGroupDTO>> getAllEGroups() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.getAllEGroups());
    }


    /**
     * 添加或编辑邮件组
     *
     * @param dto
     * @return
     */
    @ApiOperation("添加或编辑邮件组")
    @PostMapping("/editGroup")
    public ResultEntity<Object> editGroup(@RequestBody EmailGroupDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.editGroup(dto));
    }

    /**
     * 设置邮箱组和用户的关联关系
     *
     * @param dto
     * @return
     */
    @ApiOperation("设置邮箱组和用户的关联关系")
    @PostMapping("/mapGroupWithUser")
    public ResultEntity<Object> mapGroupWithUser(@RequestBody EmailGroupUserMapAddDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.mapGroupWithUser(dto));
    }

    /**
     * 删除邮件组
     *
     * @param dto
     * @return
     */
    @ApiOperation("删除邮件组")
    @PostMapping("/deleteGroupById")
    public ResultEntity<Object> deleteGroupById(@RequestBody EmailGroupDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.deleteGroupById(dto));
    }

    /**
     * 获取系统模块的邮件服务器信息 id+邮件服务器名称
     *
     * @return
     */
    @ApiOperation("获取系统模块的邮件服务器信息 id+邮件服务器名称")
    @GetMapping("/getSystemEmailServer")
    public ResultEntity<List<EmailServerDTO>> getSystemEmailServer() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailGroupService.getSystemEmailServer());
    }

    /**
     * 获取邮箱用户配置的所有用户信息
     *
     * @return
     */
    @ApiOperation("获取邮箱用户配置的所有用户信息")
    @GetMapping("/getUserInfo")
    public ResultEntity<List<EmailUserDTO>> getUserInfo() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailUserPOService.getUserInfo());
    }

    /**
     * 添加或编辑邮件组
     *
     * @param dto
     * @return
     */
    @ApiOperation("添加或编辑用户信息")
    @PostMapping("/editUser")
    public ResultEntity<Object> editUser(@RequestBody EmailUserDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailUserPOService.editUser(dto));
    }

    /**
     * 批量或单一删除用户信息
     *
     * @param dtos
     * @return
     */
    @ApiOperation("批量或单一删除用户信息")
    @PostMapping("/delUser")
    public ResultEntity<Object> delUser(@RequestBody List<EmailUserDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailUserPOService.delUser(dtos));
    }

    /**
     * 测试生命周期发送邮件
     *
     * @return
     */
    @ApiOperation("测试生命周期发送邮件")
    @GetMapping("/testemail")
    public void testemail() {
        impl.sendEmail();
    }

}
