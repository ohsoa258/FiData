package com.fisk.system.web;

import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.UserContext;
import com.fisk.common.response.ResultEntity;
import com.fisk.system.dto.UserDTO;
import com.fisk.system.entity.User;
import com.fisk.system.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author: Lock
 * @data: 2021/5/14 16:42
 */
@Api(description = "用户中心服务")
@RestController
@RequestMapping("/info")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验手机号或用户名是否存在
     * @param data 用户名或手机号
     * @param type 数据类型：1是用户名；2是手机；其它是参数有误
     * @return true：可以使用; false：不可使用
     */
//    @GetMapping("/exists/{data}/{type}")
//    public ResponseEntity<Boolean> exist(
//            @PathVariable("data") String data,
//            @PathVariable("type") Integer type) {
//        return ResponseEntity.ok(userService.exist(data, type));
//    }

    /**
     * 注册功能
     *
     * @param user
     * @return
     * @Valid: 被注释的元素是一个对象，需要检查此对象的所有字段值
     * <p>
     * 表单提交的方式,不需要加@RequestBody来接对象,如果是json格式就需要了
     */
    @PostMapping
    public ResponseEntity<Void> register(
            @Valid User user) {

        userService.register(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     *
     * @param username account
     * @param password password
     * @return 用户实体 为null说明不存在
     */
    @GetMapping
    public ResultEntity<UserDTO> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        return userService.queryUser(username, password);
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
