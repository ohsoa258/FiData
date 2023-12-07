package com.fisk.dataaccess.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.app.AppNameDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datamodel.TableQueryDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableAccessQueryDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    DataManageClient dataManageClient;
    @Autowired
    private AppDataSourceImpl dataSource;
    @Resource
    private IAppRegistration appRegistration;

    @Value("${spring.open-metadata}")
    private Boolean openMetadata;

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

    @PostMapping("/getTableNames")
    @ApiOperation(value = "根据应用ID,获取物理表名及表对应的字段")
    public ResultEntity<Object> getTableNames(@RequestBody TableQueryDTO tableQueryDTO) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableNames(tableQueryDTO));
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

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addNonRealTimeData(dto));
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
        int code = result.getCode();
        //如果物理表已经被配置到了管道，则不允许删除，并给出提示在哪个管道中配置
        if (code == ResultEnum.ACCESS_PHYTABLE_EXISTS_IN_DISPATCH.getCode()) {
            return ResultEntityBuild.build(ResultEnum.ACCESS_PHYTABLE_EXISTS_IN_DISPATCH, result);
        }

        log.info("方法返回值,{}", result.data);
        // TODO 删除pg库中的表和nifi流程
        NifiVO nifiVO = result.data;

        PgsqlDelTableDTO pgsqlDelTableDTO = new PgsqlDelTableDTO();
        pgsqlDelTableDTO.userId = nifiVO.userId;
        pgsqlDelTableDTO.appAtlasId = nifiVO.appAtlasId;
        pgsqlDelTableDTO.delApp = false;
        pgsqlDelTableDTO.businessTypeEnum = BusinessTypeEnum.DATAINPUT;

        List<AppDataSourceDTO> appSourcesByAppId = dataSource.getAppSourcesByAppId(Long.parseLong(nifiVO.appId));
        pgsqlDelTableDTO.setAppSources(appSourcesByAppId);
        AppRegistrationDTO appById = appRegistration.getAppById(Long.parseLong(nifiVO.appId));
        pgsqlDelTableDTO.appAbbreviation = appById.getAppAbbreviation();

        if (CollectionUtils.isNotEmpty(nifiVO.tableList)) {

            pgsqlDelTableDTO.tableList = nifiVO.tableList.stream().map(e -> {
                TableListDTO dto = new TableListDTO();
                dto.tableAtlasId = e.tableAtlasId;
                dto.tableName = e.tableName;
                dto.userId = nifiVO.userId;
                return dto;
            }).collect(Collectors.toList());
        }
        log.info("删表对象信息: " + JSON.toJSONString(pgsqlDelTableDTO));
        // 删除pg库对应的表
        ResultEntity<Object> task = publishTaskClient.publishBuildDeletePgsqlTableTask(pgsqlDelTableDTO);

        DataModelVO dataModelVO = new DataModelVO();
        dataModelVO.delBusiness = false;
        DataModelTableVO dataModelTableVO = new DataModelTableVO();
        dataModelTableVO.ids = nifiVO.tableIdList;
        dataModelTableVO.type = OlapTableEnum.PHYSICS;
        dataModelVO.physicsIdList = dataModelTableVO;
        dataModelVO.businessId = nifiVO.appId;
        dataModelVO.dataClassifyEnum = DataClassifyEnum.DATAACCESS;
        dataModelVO.userId = nifiVO.userId;
        // 删除nifi流程
        publishTaskClient.deleteNifiFlow(dataModelVO);

        // 删除factory-dispatch对应的表配置
        List<DeleteTableDetailDTO> list = new ArrayList<>();
        DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
        deleteTableDetailDto.appId = nifiVO.appId;
        deleteTableDetailDto.tableId = String.valueOf(id);
        deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DATALAKE_TASK;
        list.add(deleteTableDetailDto);
        dataFactoryClient.editByDeleteTable(list);

        if (openMetadata) {
            // 删除元数据
            MetaDataDeleteAttributeDTO metaDataDeleteAttributeDto = new MetaDataDeleteAttributeDTO();
            metaDataDeleteAttributeDto.setQualifiedNames(nifiVO.qualifiedNames);
            metaDataDeleteAttributeDto.setClassifications(nifiVO.classifications);
            new Thread(() -> dataManageClient.deleteMetaData(metaDataDeleteAttributeDto)).start();
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    /**
     * 删除数据 - hudi入仓配置删除物理表
     *
     * @param id 请求参数
     * @return 返回值
     */
    @DeleteMapping("/deleteHudiConfig/{id}")
    @ApiOperation(value = "删除物理表")
    public ResultEntity<Object> deleteHudiConfig(@PathVariable("id") long id) {
        ResultEntity<NifiVO> result = service.deleteData(id);
        log.info("方法返回值,{}", result.data);
        NifiVO nifiVO = result.data;
        if (openMetadata) {
            // 删除元数据
            MetaDataDeleteAttributeDTO metaDataDeleteAttributeDto = new MetaDataDeleteAttributeDTO();
            metaDataDeleteAttributeDto.setQualifiedNames(nifiVO.qualifiedNames);
            metaDataDeleteAttributeDto.setClassifications(nifiVO.classifications);
            new Thread(() -> dataManageClient.deleteMetaData(metaDataDeleteAttributeDto)).start();
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
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
    @ApiOperation(value = "获取Atlas构建表和列")
    public ResultEntity<AtlasEntityDbTableColumnDTO> getAtlasBuildTableAndColumn(
            @RequestParam("id") long id, @RequestParam("app_id") long appid) {

        return service.getAtlasBuildTableAndColumn(id, appid);
    }

    @GetMapping("/getBuildPhysicalTableDTO")
    @ApiOperation(value = "获取构建物理表DTO")
    public ResultEntity<BuildPhysicalTableDTO> getBuildPhysicalTableDTO(
            @RequestParam("table_id") long tableId, @RequestParam("app_id") long appId) {

        return service.getBuildPhysicalTableDTO(tableId, appId);
    }


    @GetMapping("/getAtlasWriteBackDataDTO")
    @ApiOperation(value = "获取地图集写回数据DTO")
    public ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(
            @RequestParam("app_id") long appid,
            @RequestParam("id") long id) {

        return service.getAtlasWriteBackDataDTO(appid, id);
    }

    @PostMapping("/addAtlasTableIdAndDorisSql")
    @ApiOperation(value = "添加Atlas表Id和Doris Sql")
    public ResultEntity<Object> addAtlasTableIdAndDorisSql(@RequestBody AtlasWriteBackDataDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addAtlasTableIdAndDorisSql(dto));
    }

    @GetMapping("/dataAccessConfig")
    @ApiOperation(value = "数据访问配置")
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(
            @RequestParam("id") long id, @RequestParam("app_id") long appid) {

        return service.dataAccessConfig(id, appid);
    }

    @ApiOperation("获取数据接入表名以及字段")
    @GetMapping("/getDataAccessMeta")
    public ResultEntity<Object> getDataAccessMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataAccessMeta());
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
    public ResultEntity<Object> createPgToDorisConfig(@RequestParam("tableName") String tableName, @RequestParam("selectSql") String selectSql) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.createPgToDorisConfig(tableName, selectSql));
    }

    @GetMapping("/getTableFieldId/{id}")
    @ApiOperation("根据接入表id获取所有字段id")
    public ResultEntity<Object> getTableFieldId(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFieldId(id));
    }

    @ApiOperation("修改物理表发布状态")
    @PutMapping("/updateTablePublishStatus")
    public void updateTablePublishStatus(@RequestBody ModelPublishStatusDTO dto) {
        service.updateTablePublishStatus(dto);
    }

}
