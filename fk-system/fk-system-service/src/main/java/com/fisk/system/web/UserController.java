package com.fisk.system.web;

import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.UserContext;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.UserDTO;
import com.fisk.system.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(description = "用户中心服务")
@RestController
@RequestMapping("/info")
@Slf4j
public class UserController {

    @Resource
    private IUserService service;


    @GetMapping("/page")
    @ApiOperation("用户列表")
    public ResultEntity<Object> getUserList()
    {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listUserData());
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
    public ResultEntity<Object> register(@Validated @RequestBody UserDTO dto)
    {
        return ResultEntityBuild.build(service.register(dto));
    }

    /*@GetMapping("/getDataPage")
    public ResultEntity<Object> getDataPage(int pages,int size)
    {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getPageUserData(pages,size));
    }*/

    /**
     * 根据用户名和密码查询用户
     *
     * @param username username
     * @param password password
     * @return 用户实体 为null说明不存在
     */
    @GetMapping
    public ResultEntity<Object> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password)
    {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.queryUser(username,password));
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
    public ResultEntity<Object> deleteUser(@PathVariable("id") int id)
    {
        return ResultEntityBuild.build(service.deleteUser(id));
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

}
