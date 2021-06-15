package com.fisk.dataaccess.controller;

import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
@Api(description = "物理表接口")
@RestController
@RequestMapping("/physicalTable")
public class PhysicalTableController {

    @Autowired
    private IAppRegistration appRService;

    @Autowired
    private ITableAccess tableAccess;

    /**
     * 根据是否为实时,查询应用名称集合
     * @return
     */
    @GetMapping("/getAppType")
    @ApiOperation(value = "查询应用名称集合及是否实时")
    public ResultEntity<List<AppNameDTO>> queryAppName() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, appRService.queryAppName());
    }

    /**
     * 根据应用名称,获取远程数据库的表及表对应的字段
     * @param appName
     * @return
     */
    @GetMapping("/getDataBase/{appName}")
    @ApiOperation(value = "根据应用名称,获取远程数据库的表及表对应的字段")
    public ResultEntity<Map<String, List<String>>> queryDataBase(
            @PathVariable("appName") String appName) throws SQLException, ClassNotFoundException {

        Map<String, List<String>> map = tableAccess.queryDataBase(appName);

        return ResultEntityBuild.build(ResultEnum.SUCCESS,map);
    }

    /**
     * 添加物理表(实时)
     * @param tableAccessDTO
     * @return
     */
    @PostMapping("/addRealTime")
    @ApiOperation(value = "添加物理表(实时)")
    public ResultEntity<Object> addRTData(@RequestBody TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {

        return ResultEntityBuild.build(tableAccess.addRTData(tableAccessDTO));
    }

    /**
     * 根据id查询数据,回显实时表
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    @ApiOperation("修改接口的回显数据")
    public ResultEntity<TableAccessDTO> getData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccess.getData(id));
    }

    /**
     * 修改物理表(实时)
     * @param dto
     * @return
     */
    @PutMapping("/editRealTime")
    @ApiOperation(value = "修改物理表")
    public ResultEntity<Object> editRTData(@RequestBody TableAccessDTO dto) throws SQLException, ClassNotFoundException {
        return ResultEntityBuild.build(tableAccess.updateRTData(dto));
    }

    /**
     * 添加物理表(非实时)
     * @param tableAccessNDTO
     * @return
     */
//    @PostMapping("/addNonRealTime")
//    @ApiOperation(value="添加物理表(非实时)")
    public ResultEntity<Object> addNRTData(@RequestBody TableAccessNDTO tableAccessNDTO) {

        return ResultEntityBuild.build(tableAccess.addNRTData(tableAccessNDTO));
    }

//    @PutMapping("/editNonRealTime")
    public ResultEntity<Object> editNRTData(@RequestBody TableAccessNDTO dto) {
        return ResultEntityBuild.build(tableAccess.updateNRTData(dto));
    }

    /**
     * 物理表接口首页分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return
     */
//    @GetMapping("/page")
//    @ApiOperation(value = "物理表接口首页分页查询")
    public ResultEntity<PageDTO<TablePhyHomeDTO>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows) {

        PageDTO<TablePhyHomeDTO> data = tableAccess.queryByPage(key, page, rows);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }


    /**
     * 删除数据
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除物理表")
    public ResultEntity<Object> deleteData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(tableAccess.deleteData(id));
    }

}
