package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.vo.UserVO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.service.ConfigureUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/30 14:15
 */
@RestController
@RequestMapping("/User")
public class ConfigureUserController {

    @Resource
    private ConfigureUserService userService;

    @ApiOperation("分页查询所有用户")
    @GetMapping("/getAll")
    public ResultEntity<List<UserVO>> listData(Page<ConfigureUserPO> page) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.listData(page));
    }

    @ApiOperation("添加用户配置服务")
    @PostMapping("/addUser")
    public ResultEntity<Object> addData(@Validated @RequestBody ConfigureUserPO dto ,String apiName) {
        return ResultEntityBuild.build(userService.saveUser(dto,apiName));
    }

    @ApiOperation("编辑用户")
    @PostMapping("/editUser")
    public ResultEntity<Object> editData(@Validated @RequestBody UserDTO dto) {
        return ResultEntityBuild.build(userService.updateUser(dto));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/deleteUser")
    public ResultEntity<Object> deleteDataById(Integer id) {
        return ResultEntityBuild.build(userService.deleteUserById(id));
    }
}
