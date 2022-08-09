package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datafactory.AccessRedirectDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.impl.TableAccessImpl;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
    @Resource
    private DataManageClient dataManageClient;

    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody AppRegistrationDTO dto) {

        ResultEntity<AtlasEntityQueryVO> resultEntity = service.addData(dto);
        AtlasEntityQueryVO vo = resultEntity.data;
        if (vo == null) {
            return ResultEntityBuild.buildData(resultEntity.code, resultEntity.msg);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, resultEntity);
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显")
    public ResultEntity<AppRegistrationDTO> getData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

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

    @PutMapping("/edit")
    @ApiOperation(value = "修改")
    public ResultEntity<Object> editData(@Validated @RequestBody AppRegistrationEditDTO dto) {
        return ResultEntityBuild.build(service.updateAppRegistration(dto));
    }

    @PutMapping("/editAppBasicInfo")
    @ApiOperation(value = "修改应用基本信息")
    public ResultEntity<Object> editAppBasicInfo(@Validated @RequestBody AppRegistrationEditDTO dto) {
        return ResultEntityBuild.build(service.editAppBasicInfo(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteData(
            @PathVariable("id") long id) {

        ResultEntity<NifiVO> result = service.deleteAppRegistration(id);

        // TODO 删除pg库对应的表和nifi流程
        log.info("方法返回值,{}", result.data);
        NifiVO nifiVO = result.data;

        PgsqlDelTableDTO pgsqlDelTableDTO = new PgsqlDelTableDTO();
        pgsqlDelTableDTO.userId = nifiVO.userId;
        pgsqlDelTableDTO.appAtlasId = nifiVO.appAtlasId;
        pgsqlDelTableDTO.delApp = true;
        pgsqlDelTableDTO.businessTypeEnum= BusinessTypeEnum.DATAINPUT;
        if (CollectionUtils.isNotEmpty(nifiVO.tableList)) {

            pgsqlDelTableDTO.tableList = nifiVO.tableList.stream().map(e -> {
                TableListDTO dto = new TableListDTO();
                dto.tableAtlasId = e.tableAtlasId;
                dto.userId = nifiVO.userId;
                dto.tableName = e.tableName;
                return dto;
            }).collect(Collectors.toList());
        }

        // 只有存在表时才会删除
        if (CollectionUtils.isNotEmpty(nifiVO.tableList) && CollectionUtils.isNotEmpty(nifiVO.tableIdList)) {
            // 删除pg库里对应的表
            log.info("当前用户id为,{}", nifiVO.userId);
            pgsqlDelTableDTO.userId = nifiVO.userId;
            log.info("删除pg库的数据为,{}", pgsqlDelTableDTO);
            ResultEntity<Object> task = publishTaskClient.publishBuildDeletePgsqlTableTask(pgsqlDelTableDTO);
            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.delBusiness=true;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.ids=nifiVO.tableIdList;
            dataModelTableVO.type= OlapTableEnum.PHYSICS;
            dataModelVO.physicsIdList=dataModelTableVO;
            dataModelVO.businessId=nifiVO.appId;
            dataModelVO.dataClassifyEnum= DataClassifyEnum.DATAACCESS;
            dataModelVO.userId=nifiVO.userId;
            // 删除nifi流程
            publishTaskClient.deleteNifiFlow(dataModelVO);
            log.info("task删除应用{}", task);
        }

        // 删除元数据
        if (CollectionUtils.isNotEmpty(nifiVO.qualifiedNames)) {
            MetaDataDeleteAttributeDTO metaDataDeleteAttributeDto = new MetaDataDeleteAttributeDTO();
            metaDataDeleteAttributeDto.setQualifiedNames(nifiVO.getQualifiedNames());
            dataManageClient.deleteMetaData(metaDataDeleteAttributeDto);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

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
    @ApiOperation(value = "筛选器")
    public ResultEntity<Page<AppRegistrationVO>> listData(@RequestBody AppRegistrationQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "筛选器字段")
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

    @ApiOperation("测试连接")
    @PostMapping("/connect")
    public ResultEntity<Object> connectDb(@RequestBody DbConnectionDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.connectDb(dto));
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

    @ApiOperation(value = "获取所有应用以及表、字段数据")
    @GetMapping("/getDataAppRegistrationMeta")
    public ResultEntity<Object> getDataAppRegistrationMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.getDataAppRegistrationMeta());
    }

    @ApiOperation(value = "根据sql语句,获取字段列表(数据建模)")
    @PostMapping("/getTableAccessQueryList")
    public ResultEntity<OdsResultDTO> getTableAccessQueryList(@RequestBody OdsQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.getTableFieldByQuery(query));
    }

    @ApiOperation(value = "根据sql语句,获取字段列表(数据接入)")
    @PostMapping("/getDataAccessQueryList")
    public ResultEntity<Object> getDataAccessQueryList(@RequestBody OdsQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.getDataAccessQueryList(query));
    }

    @PostMapping("/logMessageFilter")
    @ApiOperation(value = "日志分页筛选器")
    public ResultEntity<Page<PipelineTableLogVO>> logMessageFilter(@RequestBody PipelineTableQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.logMessageFilter(dto));
    }

    @PostMapping("/getTableNameListByAppIdAndApiId")
    @ApiOperation(value = "通过appId和apiId查询表名集合")
    public ResultEntity<List<LogMessageFilterVO>> getTableNameListByAppIdAndApiId(@RequestBody PipelineTableQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableNameListByAppIdAndApiId(dto));
    }

    @PostMapping("/redirect")
    @ApiOperation(value = "跳转页面: 查询出当前(表、api、ftp)具体在哪个管道中使用,并给跳转页面提供数据")
    public ResultEntity<Object> redirect(@Validated @RequestBody AccessRedirectDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.redirect(dto));
    }

    @PostMapping("/getDataStructure")
    @ApiOperation(value = "获取数据接入结构")
    public ResultEntity<List<FiDataMetaDataDTO>> getDataAccessStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataAccessStructure(dto));
    }

    @PostMapping("/setDataStructure")
    @ApiOperation(value = "刷新数据接入结构")
    public ResultEntity<Object> setDataAccessStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setDataAccessStructure(dto));
    }

    @PostMapping("/buildTableRuleInfo")
    @ApiOperation(value = "构建业务元数据其他数据信息")
    public ResultEntity<TableRuleInfoDTO> buildTableRuleInfo(@RequestBody TableRuleParameterDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.buildTableRuleInfo(dto));
    }

    @PostMapping("/getFiDataTableMetaData")
    @ApiOperation(value = "根据表信息/字段ID,获取表/字段基本信息")
    public ResultEntity<List<FiDataTableMetaDataDTO>> getFiDataTableMetaData(@RequestBody FiDataTableMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFiDataTableMetaData(dto));
    }
}
