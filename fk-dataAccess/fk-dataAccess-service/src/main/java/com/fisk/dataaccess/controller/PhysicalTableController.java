package com.fisk.dataaccess.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Api(description = "物理表接口")
@RestController
@RequestMapping("/physicalTable")
@Slf4j
public class PhysicalTableController {

    @Resource
    private IAppRegistration appRegService;
    @Resource
    private ITableAccess service;
    @Resource
    private PublishTaskClient publishTaskClient;

    /**
     * 根据是否为实时,查询应用名称集合
     *
     * @return 返回值
     */
    @GetMapping("/getAppType")
    @ApiOperation(value = "查询应用名称集合及是否实时")
    public ResultEntity<List<AppNameDTO>> queryAppName() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, appRegService.queryAppName());
    }


    /**
     * 获取非实时应用名称
     *
     * @return 返回值
     */
    @GetMapping("/getNonRTName")
    @ApiOperation(value = "获取非实时应用名称")
    public ResultEntity<List<AppNameDTO>> queryNonTimeAppName() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, appRegService.queryNoneRealTimeAppName());
    }

    /**
     * 根据应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appName 请求参数
     * @return 返回值
     */
    @GetMapping("/getFields/{appName}")
    @ApiOperation(value = "根据应用名称,获取物理表名及表对应的字段(非实时)")
    public ResultEntity<List<TablePyhNameDTO>> queryNonRealTimeTable(@PathVariable("appName") String appName) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFields(appName));
    }


    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO 请求参数
     * @return 返回值
     */
    @PostMapping("/addRealTime")
    @ApiOperation(value = "添加物理表(实时)")
    public ResultEntity<Object> addRealTimeData(@RequestBody TableAccessDTO tableAccessDTO) {

        return ResultEntityBuild.build(service.addRealTimeData(tableAccessDTO));
    }

    /**
     * 修改物理表(实时)
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PutMapping("/editRealTime")
    @ApiOperation(value = "修改物理表(实时)")
    public ResultEntity<Object> editRealTimeData(@RequestBody TableAccessDTO dto) {
        return ResultEntityBuild.build(service.updateRealTimeData(dto));
    }

    /**
     * 添加物理表(非实时)
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/addNonRealTime")
    @ApiOperation(value = "添加物理表(非实时)")
    public ResultEntity<Object> addNonRealTimeData(@RequestBody TableAccessNonDTO dto) {
        ResultEntity<AtlasIdsVO> atlasIdsVO = service.addNonRealTimeData(dto);

        AtlasIdsVO atlasIds = atlasIdsVO.data;

        if (atlasIds == null) {
            return ResultEntityBuild.buildData(atlasIdsVO.code,atlasIdsVO.msg);
        }

        AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
        atlasEntityQueryDTO.userId = atlasIds.userId;
        // 应用注册id
        atlasEntityQueryDTO.appId = atlasIds.appId;
        atlasEntityQueryDTO.dbId = atlasIds.dbId;
        ResultEntity<Object> task = publishTaskClient.publishBuildAtlasTableTask(atlasEntityQueryDTO);
        log.info("task:" + JSON.toJSONString(task));
        System.out.println(task);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, atlasIdsVO);
    }

    /**
     * 修改物理表(非实时)
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PutMapping("/editNonRealTime")
    @ApiOperation(value = "修改物理表(非实时)")
    public ResultEntity<Object> editNonRealTimeData(@RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.updateNonRealTimeData(dto));
    }

    /**
     * 根据id查询数据,回显实时表
     *
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/get/{id}")
    @ApiOperation("修改接口的回显数据")
    public ResultEntity<TableAccessNonDTO> getData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 物理表接口首页分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 返回值
     */
    @GetMapping("/page")
    @ApiOperation(value = "物理表接口首页分页查询")
    public ResultEntity<Page<Map<String, Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }

    /**
     * 删除数据
     *
     * @param id 请求参数
     * @return 返回值
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除物理表")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }


    /*@GetMapping("/getTableAndField/{appName}")
    @ApiOperation(value = "测试获取表及表字段")
    public ResultEntity<Object> listTableAndField(@PathVariable("appName")String appName) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS*//*,tableAccess.listTableAndField(appName)*//*);
    }*/

    @PostMapping("/pageFilter")
    @ApiOperation(value = "筛选器")
    public ResultEntity<Page<TableAccessVO>> listData(@RequestBody TableAccessQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "筛选器表字段")
    public ResultEntity<Object> getColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }


    @GetMapping("/getAtlasBuildTableAndColumn")
    public ResultEntity<AtlasEntityDbTableColumnDTO> getAtlasBuildTableAndColumn(
            @RequestParam("id") long id, @RequestParam("appid") long appid) {

        return service.getAtlasBuildTableAndColumn(id, appid);
    }

    @GetMapping("/getAtlasWriteBackDataDTO")
    public ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(
            @RequestParam("appid") long appid,
            @RequestParam("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAtlasWriteBackDataDTO(appid, id));
    }

    @PostMapping("/addAtlasTableIdAndDorisSql")
    public ResultEntity<Object> addAtlasTableIdAndDorisSql(@RequestBody AtlasWriteBackDataDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addAtlasTableIdAndDorisSql(dto));
    }

    @GetMapping("/dataAccessConfig")
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(
            @RequestParam("id") long id, @RequestParam("appid") long appid) {

        return service.dataAccessConfig(id, appid);
    }

    @PostMapping("/addComponentId")
    public ResultEntity<Object> addComponentId(@RequestBody NifiAccessDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addComponentId(dto));
    }

    @ApiOperation("获取数据接入表名以及字段")
    @GetMapping("/getDataAccessMeta")
    public ResultEntity<Object> getDataAccessMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataAccessMeta());
    }

}
