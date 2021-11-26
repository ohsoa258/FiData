package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_2})
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
    @ApiOperation(value = "根据应用名称,获取物理表名及表对应的字段")
    public ResultEntity<List<TablePyhNameDTO>> queryNonRealTimeTable(@PathVariable("appName") String appName) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFields(appName));
    }

    @GetMapping("/getTableNameAndFieldsByAppId")
    @ApiOperation(value = "根据应用ID,获取物理表名及表对应的字段")
    public ResultEntity<Object> queryNonRealTimeTableByAppId(@RequestParam("appId") long appId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFieldsByAppId(appId));
    }

    @GetMapping("/getTableNameByAppId")
    @ApiOperation(value = "根据应用ID,获取物理表名及表对应的字段")
    public ResultEntity<Object> queryNonRealTimeTable(@RequestParam("appId") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableName(id));
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

        if (atlasIdsVO.code == 0) {
            AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
            atlasEntityQueryDTO.userId = atlasIds.userId;
            // 应用注册id
            atlasEntityQueryDTO.appId = atlasIds.appId;
            atlasEntityQueryDTO.dbId = atlasIds.dbId;
//            ResultEntity<Object> task = publishTaskClient.publishBuildAtlasTableTask(atlasEntityQueryDTO);
//            log.info("task:" + JSON.toJSONString(task));
//            System.out.println(task);
        }

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
        ResultEntity<NifiVO> result = service.deleteData(id);

        log.info("方法返回值,{}", result.data);
        // TODO 删除pg库中的表和nifi流程
        NifiVO nifiVO = result.data;

        PgsqlDelTableDTO pgsqlDelTableDTO = new PgsqlDelTableDTO();
        pgsqlDelTableDTO.userId = nifiVO.userId;
        pgsqlDelTableDTO.appAtlasId = nifiVO.appAtlasId;
        pgsqlDelTableDTO.delApp = false;
        if (CollectionUtils.isNotEmpty(nifiVO.tableList)) {

            pgsqlDelTableDTO.tableList = nifiVO.tableList.stream().map(e -> {
                TableListDTO dto = new TableListDTO();
                dto.tableAtlasId = e.tableAtlasId;
                dto.tableName = e.nifiSettingTableName;
                dto.userId = nifiVO.userId;
                return dto;
            }).collect(Collectors.toList());
        }
        // 删除pg库对应的表
        ResultEntity<Object> task = publishTaskClient.publishBuildDeletePgsqlTableTask(pgsqlDelTableDTO);

        DataModelVO dataModelVO = new DataModelVO();
        dataModelVO.delBusiness=false;
        DataModelTableVO dataModelTableVO = new DataModelTableVO();
        dataModelTableVO.ids=nifiVO.tableIdList;
        dataModelTableVO.type= OlapTableEnum.PHYSICS;
        dataModelVO.physicsIdList=dataModelTableVO;
        dataModelVO.businessId=nifiVO.appId;
        dataModelVO.dataClassifyEnum= DataClassifyEnum.DATAACCESS;
        dataModelVO.userId=nifiVO.userId;
        // 删除nifi流程
        publishTaskClient.deleteNifiFlow(dataModelVO);

        return ResultEntityBuild.build(ResultEnum.SUCCESS,result);
    }

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
            @RequestParam("id") long id, @RequestParam("app_id") long appid) {

        return service.getAtlasBuildTableAndColumn(id, appid);
    }

    @GetMapping("/getBuildPhysicalTableDTO")
    public ResultEntity<BuildPhysicalTableDTO> getBuildPhysicalTableDTO(
            @RequestParam("table_id") long tableId, @RequestParam("app_id") long appId) {

        return service.getBuildPhysicalTableDTO(tableId, appId);
    }


    @GetMapping("/getAtlasWriteBackDataDTO")
    public ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(
            @RequestParam("app_id") long appid,
            @RequestParam("id") long id) {

        return service.getAtlasWriteBackDataDTO(appid, id);
    }

    @PostMapping("/addAtlasTableIdAndDorisSql")
    public ResultEntity<Object> addAtlasTableIdAndDorisSql(@RequestBody AtlasWriteBackDataDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addAtlasTableIdAndDorisSql(dto));
    }

    @GetMapping("/dataAccessConfig")
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(
            @RequestParam("id") long id, @RequestParam("app_id") long appid) {

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

    @ApiOperation("添加")
    @GetMapping("/getDimensionMeta")
    public ResultEntity<Object> getDimensionMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionMeta());
    }

    /**
     * 根据表id，获取表详情
     *
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/getTableAccess/{id}")
    @ApiOperation("修改接口的回显数据")
    public ResultEntity<TableAccessDTO> getTableAccess(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccess(id));
    }

    @ApiOperation("添加同步配置信息")
    @GetMapping("/createPgToDorisConfig")
    public ResultEntity<Object> createPgToDorisConfig(@RequestParam("tableName")String tableName,@RequestParam("selectSql")String selectSql) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.createPgToDorisConfig(tableName,selectSql));
    }

    @GetMapping("/getTableFieldId/{id}")
    @ApiOperation("根据接入表id获取所有字段id")
    public ResultEntity<Object> getTableFieldId(@PathVariable("id") int id){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFieldId(id));
    }

}
