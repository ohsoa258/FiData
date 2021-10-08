package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.UserConfigureDTO;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.service.ConfigureUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/30 14:15
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/User")
public class ConfigureUserController {

    @Resource
    private ConfigureUserService userService;

    @ApiOperation("分页查询所有用户")
    @GetMapping("/getAll")
    public ResultEntity<Page<UserDTO>> getAll(Page<ConfigureUserPO> page,String downSystemName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.listData(page,downSystemName));
    }

    @ApiOperation("添加用户配置服务")
    @PostMapping("/addUserConfigure")
    public ResultEntity<Object> addUserConfigure(@Validated @RequestBody UserConfigureDTO dto) {
        return ResultEntityBuild.build(userService.saveUserConfigure(dto));
    }

    @ApiOperation("添加用户")
    @PostMapping("/addUser")
    public ResultEntity<Object> addUser(@Validated @RequestBody ConfigureUserPO dto) {
        return ResultEntityBuild.build(userService.saveUser(dto));
    }

    @ApiOperation("编辑用户")
    @PutMapping("/editUser")
    public ResultEntity<Object> editUser(@Validated @RequestBody UserDTO dto) {
        return ResultEntityBuild.build(userService.updateUser(dto));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/deleteUserById")
    public ResultEntity<Object> deleteUserById(Integer id) {
        return ResultEntityBuild.build(userService.deleteUserById(id));
    }

    @ApiOperation("删除用户下的Api接口")
    @DeleteMapping("/deleteUserApiById")
    public ResultEntity<Object> deleteUserApiById(UserConfigureDTO dto) {
        return ResultEntityBuild.build(userService.deleteUserApiById(dto));
    }

    @ApiOperation("根据id查询用户信息")
    @GetMapping("/byUserId")
    public ResultEntity<Object> byUserId(Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.byUserId(id));
    }

    @ApiOperation("根据用户id查询Api服务")
    @GetMapping("/configureByUserId")
    public ResultEntity<Object> configureByUserId(Integer id,Integer currentPage, Integer pageSize) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.configureByUserId(id,currentPage,pageSize));
    }

    @ApiOperation("根据用户id查询对应api服务")
    @GetMapping("/getByUserId")
    public ResultEntity<Object> getByUserId(Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.configureByUserId(id));
    }
}
