package com.fisk.system.web;

import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.UserContext;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.ChangePasswordDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.dto.userinfo.UserQueryDTO;
import com.fisk.system.dto.userinfo.UserValidDTO;
import com.fisk.system.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.USER})
@RestController
@RequestMapping("/info")
@Slf4j
public class UserController {

    @Resource
    private IUserService service;


    @PostMapping("/page")
    @ApiOperation("用户列表")
    public ResultEntity<Object> getUserList(@RequestBody UserQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listUserData(query));
    }

    /**
     * 注册功能
     *
     * @param dto
     * @return 执行结果
     * <p>
     * 表单提交的方式,不需要加@RequestBody来接对象,如果是json格式就需要了
     */
    @PostMapping("/register")
    @ApiOperation("添加用户")
    public ResultEntity<Object> register(@Validated @RequestBody UserDTO dto) {
        return ResultEntityBuild.build(service.register(dto));
    }

    /**
     * 根据用户名和密码查询用户
     *
     * @param userAccount userAccount
     * @param password    password
     * @return 用户实体 为null说明不存在
     */
    @GetMapping
    public ResultEntity<Object> queryUser(
            @RequestParam("userAccount") String userAccount,
            @RequestParam("password") String password) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryUser(userAccount, password));
    }

    /**
     * 编辑用户保存
     *
     * @param dto
     * @return 用户实体 为null说明不存在
     */
    @PutMapping("/editUser")
    @ApiOperation("修改用户")
    public ResultEntity<Object> editUser(@Validated @RequestBody UserDTO dto) {
        return ResultEntityBuild.build(service.updateUser(dto));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/deleteUser/{id}")
    public ResultEntity<Object> deleteUser(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteUser(id));
    }

    @PutMapping("/updateUserValid")
    @ApiOperation("设置用户是否有效")
    public ResultEntity<Object> updateUserValid(@Validated @RequestBody UserValidDTO dto) {
        return ResultEntityBuild.build(service.updateUserValid(dto));
    }

    @ApiOperation("根据id获取用户详情")
    @GetMapping("/getUser/{id}")
    public ResultEntity<Object> getUser(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getUser(id));
    }

    /**
     * 获取当前登录的用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserDetail> me() {
        return ResponseEntity.ok(UserContext.getUser());
    }

    @ApiOperation("获取登录人信息")
    @GetMapping("/getCurrentUserInfo")
    public ResultEntity<Object> getCurrentUserInfo() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCurrentUserInfo());
    }

    @PutMapping("/changePassword")
    @ApiOperation("修改用户密码")
    public ResultEntity<Object> changePassword(@Validated @RequestBody ChangePasswordDTO dto) {
        return ResultEntityBuild.build(service.changePassword(dto));
    }

    @GetMapping("/getColumn")
    @ApiOperation("获取用户筛选器列表")
    public ResultEntity<Object> getColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getUserInfoColumn());
    }

    @PutMapping("/updatePassword")
    @ApiOperation("用户修改个人密码")
    public ResultEntity<Object> updatePassword(@Validated @RequestBody ChangePasswordDTO dto) {
        return ResultEntityBuild.build(service.updatePassword(dto));
    }

    @PostMapping("/getUserListByIds")
    @ApiOperation("批量查询用户信息")
    public ResultEntity<List<UserDTO>> getUserListByIds(@RequestBody List<Long> ids) {
        return service.getUserListByIds(ids);
    }

}
