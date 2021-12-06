package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.service.ITableAccess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/v3/tableAccess")
public class TableAccessController {

    @Resource
    ITableAccess service;

    /**
     * 添加物理表
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addTableAccessData(@RequestBody TbTableAccessDTO dto) {

        return ResultEntityBuild.build(service.addTableAccessData(dto));
    }

    /**
     * 根据id回显数据
     *
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显")
    public ResultEntity<TbTableAccessDTO> getTableAccessData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccessData(id));
    }

    /**
     * 修改
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PutMapping("/edit")
    @ApiOperation(value = "修改物理表信息&保存sql_script")
    public ResultEntity<Object> editData(@RequestBody TbTableAccessDTO dto) {
        return ResultEntityBuild.build(service.updateTableAccessData(dto));
    }

    /**
     * 删除
     *
     * @param id 请求参数
     * @return 返回值
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteTableAccessData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteTableAccessData(id));
    }

    /**
     * 根据appId获取物理表列表
     *
     * @param appId appId
     * @return 返回值
     */
    @GetMapping("/getList/{appId}")
    @ApiOperation(value = "根据appId获取物理表列表")
    public ResultEntity<List<TbTableAccessDTO>> getTableAccessListData(
            @PathVariable("appId") long appId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccessListData(appId));
    }


}
