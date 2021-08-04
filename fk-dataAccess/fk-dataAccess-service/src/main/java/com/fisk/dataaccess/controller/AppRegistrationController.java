package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(description = "应用注册接口")
@RestController
@RequestMapping("/appRegistration")
@Slf4j
public class AppRegistrationController {

    @Resource
    private IAppRegistration service;

    /**
     * 添加应用
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody AppRegistrationDTO dto) {

        return ResultEntityBuild.build(service.addData(dto));
    }

    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显")
    public ResultEntity<AppRegistrationDTO> getData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }


    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 返回值
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页")
    public ResultEntity<PageDTO<AppRegistrationDTO>> queryByPageAppRes(
            // 过滤条件条件非必要
            @RequestParam(value = "key", required = false) String key,
            // 给个默认值,防止不传值时查询全表
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows) {
        PageDTO<AppRegistrationDTO> data = service.listAppRegistration(key, page, rows);
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }

    /**
     * 应用注册-修改
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PutMapping("/edit")
    @ApiOperation(value = "修改")
    public ResultEntity<Object> editData(@Validated @RequestBody AppRegistrationEditDTO dto) {
        return ResultEntityBuild.build(service.updateAppRegistration(dto));
    }

    /**
     * 删除
     *
     * @param id 请求参数
     * @return 返回值
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteAppRegistration(id));
    }

    /**
     * 查询应用数据，按照创建时间倒序排序，查出top 10的数据
     *
     * @return 返回值
     */
    @GetMapping("/getDescDate")
    @ApiOperation(value = "查询应用数据，按照创建时间倒序排序，查出top 10的数据")
    public ResultEntity<List<AppRegistrationDTO>> getDescDate() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDescDate());
    }

    @GetMapping("/getDriveType")
    @ApiOperation(value = "数据源驱动类型")
    public ResultEntity<List<AppDriveTypeDTO>> getDriveType() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDriveType());
    }

    @PostMapping("/pageFilter")
    @ApiOperation(value = "过滤器")
    public ResultEntity<Page<AppRegistrationVO>> listData(@RequestBody AppRegistrationQueryDTO query){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.listData(query));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "过滤器字段")
    public ResultEntity<Object> getColumn(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getColumn());
    }

    @GetMapping("/getAtlasEntity")
    public ResultEntity<AtlasEntityDTO> getAtlasEntity(@RequestParam("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAtlasEntity(id));
    }

    @PostMapping("/addAtlasInstanceIdAndDbId")
    public ResultEntity<Object> addAtlasInstanceIdAndDbId(
            @RequestParam("appid") long appid,
            @RequestParam("atlas_instance_id") String atlasInstanceId,
            @RequestParam("atlas_db_id") String atlasDbId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addAtlasInstanceIdAndDbId(appid, atlasInstanceId, atlasDbId));
    }

    @ApiOperation(value = "获取应用注册名称")
    @GetMapping("/getAppName")
    public ResultEntity<List<AppNameDTO>> getAppNameAndId() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataList());
    }
}
