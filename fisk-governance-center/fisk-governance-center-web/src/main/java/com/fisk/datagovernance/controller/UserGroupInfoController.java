package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoQueryDTO;
import com.fisk.datagovernance.service.datasecurity.UserGroupInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 * @email jianwen@fisk.com.cn
 * @date 2022-03-28 15:47:33
 */
@Api(tags = SwaggerConfig.USER_GROUP_INFO)
@RestController
@RequestMapping("/userGroupInfo")
public class UserGroupInfoController {

    @Resource
    private UserGroupInfoService service;

    @PostMapping("/getPageList")
    @ApiOperation("分页获取用户组信息")
    public ResultEntity<Object> getPageList(@RequestBody UserGroupInfoQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listUserGroupInfos(dto));
    }

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<UserGroupInfoDTO> getData(@PathVariable("id") long id){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 保存
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加用户组")
    public ResultEntity<Object> addData(@RequestBody UserGroupInfoDTO dto){
        return ResultEntityBuild.build(service.saveData(dto));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    @ApiOperation(value = "编辑用户组")
    public ResultEntity<Object> editData(@RequestBody UserGroupInfoDTO dto){
        return ResultEntityBuild.build(service.updateData(dto));
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除用户组")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/listUserGroupInfoDrops")
    @ApiOperation(value = "获取用户组下拉数据")
    public ResultEntity<Object> listUserGroupInfoDrops(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listUserGroupInfoDrops());
    }

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/listSystemUserDrops")
    @ApiOperation(value = "获取系统用户下拉数据")
    public ResultEntity<Object> listSystemUserDrops(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listSystemUserDrops());
    }

}