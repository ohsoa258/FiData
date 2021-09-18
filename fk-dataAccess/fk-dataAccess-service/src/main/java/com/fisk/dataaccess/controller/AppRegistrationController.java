package com.fisk.dataaccess.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.impl.TableAccessImpl;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_1})
@RestController
@RequestMapping("/appRegistration")
@Slf4j
public class AppRegistrationController {

    @Resource
    private IAppRegistration service;
    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Value("${spring.datasource.username}")
    private String username;

    /**
     * 添加应用
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody AppRegistrationDTO dto) {

        ResultEntity<AtlasEntityQueryVO> resultEntity = service.addData(dto);
        AtlasEntityQueryVO vo = resultEntity.data;
        if (vo == null) {
            return ResultEntityBuild.buildData(resultEntity.code, resultEntity.msg);
        }

        // TODO: atlas对接应用注册
        AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
        atlasEntityQueryDTO.appId = vo.appId;
        atlasEntityQueryDTO.userId = vo.userId;
        ResultEntity<Object> task = publishTaskClient.publishBuildAtlasInstanceTask(atlasEntityQueryDTO);
        log.info("task:" + JSON.toJSONString(task));
        System.out.println("task = " + task);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, resultEntity);
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

        ResultEntity<NifiVO> result = service.deleteAppRegistration(id);

        // TODO 删除Atlas和nifi流程
        log.info("方法返回值,{}", result.data);
        NifiVO nifiVO = result.data;

        PgsqlDelTableDTO pgsqlDelTableDTO = new PgsqlDelTableDTO();
        pgsqlDelTableDTO.userId = nifiVO.userId;
        pgsqlDelTableDTO.appAtlasId = nifiVO.appAtlasId;
        pgsqlDelTableDTO.delApp = true;
        if (CollectionUtils.isNotEmpty(nifiVO.tableList)) {
            List<TableListDTO> collect = nifiVO.tableList.stream().map(e -> {
                TableListDTO dto = new TableListDTO();
                dto.tableAtlasId = e.tableAtlasId;
                dto.tableName = e.nifiSettingTableName;
                dto.userId = nifiVO.userId;
                return dto;
            }).collect(Collectors.toList());

            pgsqlDelTableDTO.tableList = collect;
        }

        ResultEntity<Object> task = publishTaskClient.publishBuildDeletePgsqlTableTask(pgsqlDelTableDTO);
        log.info("task删除应用{}", task);
        System.out.println(task);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
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
    public ResultEntity<Page<AppRegistrationVO>> listData(@RequestBody AppRegistrationQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "过滤器字段")
    public ResultEntity<Object> getColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }

    @GetMapping("/getAtlasEntity")
    public ResultEntity<AtlasEntityDTO> getAtlasEntity(@RequestParam("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAtlasEntity(id));
    }

    @PostMapping("/addAtlasInstanceIdAndDbId")
    public ResultEntity<Object> addAtlasInstanceIdAndDbId(
            @RequestParam("app_id") long appid,
            @RequestParam("atlas_instance_id") String atlasInstanceId,
            @RequestParam("atlas_db_id") String atlasDbId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addAtlasInstanceIdAndDbId(appid, atlasInstanceId, atlasDbId));
    }

    @ApiOperation(value = "获取应用注册名称")
    @GetMapping("/getAppName")
    public ResultEntity<List<AppNameDTO>> getAppNameAndId() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataList());
    }

    @GetMapping("/test")
    public ResultEntity<Object> test() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, username);
    }

    @ApiOperation("测试连接")
    @PostMapping("/connect")
    public ResultEntity<Object> connectDb(@RequestBody DbConnectionDTO dto) {

        return service.connectDb(dto);
    }

    @ApiOperation("判断应用名称是否重复")
    @PostMapping("/getRepeatAppName")
    public ResultEntity<Object> getRepeatAppName(@RequestParam("appName") String appName) {
        return service.getRepeatAppName(appName);
    }

    @ApiOperation("判断应用简称是否重复")
    @PostMapping("/getRepeatAppAbbreviation")
    public ResultEntity<Object> getRepeatAppAbbreviation(@RequestParam("appAbbreviation") String appAbbreviation) {
        return service.getRepeatAppAbbreviation(appAbbreviation);
    }


}
