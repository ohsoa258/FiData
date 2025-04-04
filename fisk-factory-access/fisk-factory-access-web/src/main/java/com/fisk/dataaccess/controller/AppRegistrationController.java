package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.aesutils.AesEncryptionDecryptionUtils;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.AesKeyDTO;
import com.fisk.dataaccess.dto.SyncOneTblForHudiDTO;
import com.fisk.dataaccess.dto.access.OdsFieldQueryDTO;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datafactory.AccessRedirectDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceFullInfoDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataaccess.dto.doris.DorisTblSchemaDTO;
import com.fisk.dataaccess.dto.hudi.HudiReSyncDTO;
import com.fisk.dataaccess.dto.hudi.HudiSyncDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobParameterDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.service.IApiConfig;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.service.impl.TableAccessImpl;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.CDCAppDbNameVO;
import com.fisk.dataaccess.vo.CDCAppNameAndTableVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.table.CDCAppNameVO;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private AppDataSourceImpl dataSource;
    @Resource
    private IApiConfig apiConfig;

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

        //todo:hudi入仓配置暂时不去删底表和nifi 因为目前没有
        if (nifiVO.ifSyncAllTables == null || nifiVO.appType != 2) {
            PgsqlDelTableDTO pgsqlDelTableDTO = new PgsqlDelTableDTO();
            pgsqlDelTableDTO.userId = nifiVO.userId;
            pgsqlDelTableDTO.appAtlasId = nifiVO.appAtlasId;
            pgsqlDelTableDTO.delApp = true;
            pgsqlDelTableDTO.businessTypeEnum = BusinessTypeEnum.DATAINPUT;
            List<AppDataSourceDTO> appSourcesByAppId = dataSource.getAppSourcesByAppId(Long.parseLong(nifiVO.appId));
            pgsqlDelTableDTO.setAppSources(appSourcesByAppId);
            pgsqlDelTableDTO.appAbbreviation = nifiVO.getAppAbbreviation();
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
                dataModelVO.delBusiness = true;
                DataModelTableVO dataModelTableVO = new DataModelTableVO();
                dataModelTableVO.ids = nifiVO.tableIdList;
                dataModelTableVO.type = OlapTableEnum.PHYSICS;
                dataModelVO.physicsIdList = dataModelTableVO;
                dataModelVO.businessId = nifiVO.appId;
                dataModelVO.dataClassifyEnum = DataClassifyEnum.DATAACCESS;
                dataModelVO.userId = nifiVO.userId;
                // 删除nifi流程
                publishTaskClient.deleteNifiFlow(dataModelVO);
                log.info("task删除应用{}", task);
            }
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

    @GetMapping("/getDriveTypeByAppId/{appId}")
    @ApiOperation(value = "通过appid获取特定数据源驱动类型")
    public ResultEntity<List<AppDriveTypeDTO>> getDriveTypeByAppId(@PathVariable("appId") Long appid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDriveTypeByAppId(appid));
    }

    @PostMapping("/pageFilter")
    @ApiOperation(value = "应用筛选器 不包含doris外部目录应用")
    public ResultEntity<Page<AppRegistrationVO>> listData(@RequestBody AppRegistrationQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

    /**
     * 应用筛选器 包含doris外部目录应用
     *
     * @param query
     * @return
     */
    @PostMapping("/getDorisCatalogs")
    @ApiOperation(value = "应用筛选器 只包含doris外部目录应用")
    public ResultEntity<Page<AppRegistrationVO>> getDorisCatalogs(@RequestBody AppRegistrationQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDorisCatalogs(query));
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
    public ResultEntity<Object> getRepeatAppAbbreviation(@RequestParam("appAbbreviation") String appAbbreviation,
                                                         @RequestParam("whetherSchema") boolean whetherSchema) {
        return service.getRepeatAppAbbreviation(appAbbreviation, whetherSchema);
    }

    @ApiOperation(value = "获取所有应用以及表、字段数据")
    @GetMapping("/getDataAppRegistrationMeta")
    public ResultEntity<Object> getDataAppRegistrationMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.getDataAppRegistrationMeta());
    }

    @ApiOperation(value = "获取不同ods数据源对应的应用以及表、字段数据")
    @GetMapping("/getAllDataAppRegistrationMeta")
    public ResultEntity<Object> getAllDataAppRegistrationMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.getAllDataAppRegistrationMeta());
    }

    /**
     * 获取指定doris外部目录catalog下的所有db以及所有表
     *
     * @param dbID        平台配置数据库id
     * @param catalogName 目录名
     */
    @ApiOperation(value = "获取指定doris外部目录catalog下的所有db以及所有表")
    @GetMapping("/getDorisCatalogTreeByCatalogName")
    public ResultEntity<Map<String, List<String>>> getDorisCatalogTreeByCatalogName(@RequestParam("dbID") Integer dbID,
                                                                                    @RequestParam("catalogName") String catalogName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.getDorisCatalogTreeByCatalogName(dbID, catalogName));
    }

    /**
     * 获取指定doris外部目录catalog下的指定表的表结构
     *
     * @param dbID        平台配置数据库id
     * @param catalogName 目录名
     * @param dbName      数据库名
     * @param tblName     表名
     */
    @ApiOperation(value = "获取指定doris外部目录catalog下的指定表的表结构")
    @GetMapping("/getDorisCatalogTblSchema")
    public ResultEntity<List<DorisTblSchemaDTO>> getDorisCatalogTblSchema(@RequestParam("dbID") Integer dbID,
                                                                          @RequestParam("catalogName") String catalogName,
                                                                          @RequestParam("dbName") String dbName,
                                                                          @RequestParam("tblName") String tblName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,
                tableAccessImpl.getDorisCatalogTblSchema(dbID, catalogName, dbName, tblName));
    }

    /**
     * 刷新doris外部目录catalog存储的redis缓存
     *
     * @param dbID        平台配置数据库id
     * @param catalogName 目录名
     */
    @ApiOperation(value = "刷新doris外部目录catalog存储的redis缓存")
    @GetMapping("/refreshDorisCatalog")
    public ResultEntity<Map<String, List<String>>> refreshDorisCatalog(@RequestParam("dbID") Integer dbID,
                                                                       @RequestParam("catalogName") String catalogName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessImpl.refreshDorisCatalog(dbID, catalogName));
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

    @PostMapping("/getDataTableStructure")
    @ApiOperation(value = "获取数据接入表结构")
    public ResultEntity<List<FiDataMetaDataTreeDTO>> getDataAccessTableStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataAccessTableStructure(dto));
    }

    @PostMapping("/setDataStructure")
    @ApiOperation(value = "刷新数据接入结构")
    public ResultEntity<Object> setDataAccessStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setDataAccessStructure(dto));
    }

    /**
     * 数据质量左侧Tree接口-获取ods数据源文件夹表层级
     *
     * @return
     */
    @PostMapping("/dataQuality_GetOdsFolderTableTree")
    @ApiOperation(value = "数据质量左侧Tree接口-获取ods数据源文件夹表层级")
    public ResultEntity<DataQualityDataSourceTreeDTO> getOdsFolderTableTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getOdsFolderTableTree());
    }

    /**
     * 数据质量左侧Tree接口-根据表ID获取表下面的字段
     *
     * @return
     */
    @PostMapping("/dataQuality_GetOdsTableFieldByTableId")
    @ApiOperation(value = "数据质量左侧Tree接口-根据表ID获取表下面的字段")
    public ResultEntity<List<DataQualityDataSourceTreeDTO>> getOdsTableFieldByTableId(@RequestBody OdsFieldQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getOdsTableFieldByTableId(dto));
    }


    @PostMapping("/getTableDataStructure")
    @ApiOperation(value = "获取数据接入表结构(数据标准用)")
    public ResultEntity<Object> getTableDataStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableDataStructure(dto));
    }

    @ApiOperation("获取数据接入字段结构(数据标准用)")
    @PostMapping("/getFieldsDataStructure")
    public ResultEntity<Object> getFieldsDataStructure(@RequestBody ColumnQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFieldsDataStructure(dto));
    }

    @ApiOperation("搜索数据元关联字段(数据标准用)")
    @GetMapping("/searchStandardBeCitedField")
    public ResultEntity<Object> searchStandardBeCitedField(@RequestParam("key") String key) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.searchStandardBeCitedField(key));
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

    @GetMapping("/getAppList")
    @ApiOperation(value = "获取所有应用信息")
    public ResultEntity<List<AppBusinessInfoDTO>> getAppList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppList());
    }

    @PostMapping("/getApiToken")
    @ApiOperation(value = "jwt验证方式,测试获取token")
    public ResultEntity<Object> getApiToken(@RequestBody AppDataSourceDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getApiToken(dto));
    }

    @PostMapping("/buildCdcJobScript")
    @ApiOperation(value = "获取cdc任务脚本")
    public ResultEntity<Object> buildCdcJobScript(@RequestBody CdcJobParameterDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.buildCdcJobScript(dto));
    }

    @GetMapping("/getFiDataDataSource")
    @ApiOperation(value = "获取fidata数据源")
    public ResultEntity<Object> getFiDataDataSource() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFiDataDataSource());
    }

    /**
     * 数仓建模获取fidata数据源（ods & lake） 不包含HUDI
     *
     * @return
     */
    @GetMapping("/getFiDataOdsAndLakeSource")
    @ApiOperation(value = "数仓建模获取fidata数据源（ods & lake） 不包含HUDI")
    public ResultEntity<Object> getFiDataOdsAndLakeSource() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFiDataOdsAndLakeSource());
    }

    @GetMapping("/getDataTypeList/{appId}")
    @ApiOperation(value = "获取FiData ODS数据类型")
    public ResultEntity<Object> getDataTypeList(@PathVariable("appId") Integer appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.dataTypeList(appId));
    }

    @GetMapping("/synchronizationAppRegistration")
    @ApiOperation(value = "元数据同步应用信息")
    public ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAppRegistration() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.synchronizationAppRegistration());
    }

    @GetMapping("/synchronizationAccessTable")
    @ApiOperation(value = "元数据同步所有接入表")
    public ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAccessTable() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.synchronizationAccessTable());
    }

    /**
     * 元数据根据最近同步时间同步接入表
     *
     * @param lastSyncTime
     * @return
     */
    @GetMapping("/synchronizationAccessTableByLastSyncTime")
    @ApiOperation(value = "元数据根据最近同步时间同步接入表")
    public ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAccessTableByLastSyncTime(@RequestParam("lastSyncTime") String lastSyncTime) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.synchronizationAccessTableByLastSyncTime(lastSyncTime));
    }

    @PostMapping("/getBatchTargetDbIdByAppIds")
    @ApiOperation(value = "依据应用id集合查询目标源id集合")
    public ResultEntity<List<AppRegistrationInfoDTO>> getBatchTargetDbIdByAppIds(@RequestBody List<Integer> appIds) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBatchTargetDbIdByAppIds(appIds));
    }

    @GetMapping("/getAppNameById")
    @ApiOperation(value = "根据appId获取app应用名称")
    public ResultEntity<AppRegistrationDTO> getAppNameById(@RequestParam("id") Long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppNameById(id));
    }

    @GetMapping("/getAppByAppName")
    @ApiOperation(value = "根据应用名称获取单个应用详情")
    public ResultEntity<AppRegistrationDTO> getAppByAppName(@RequestParam("appName") String appName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppByAppName(appName));
    }

    /**
     * 数据接入--应用级别修改应用下的接口是否允许推送数据
     *
     * @param appId
     * @return
     */
    @PostMapping("/appIfAllowDataTransfer")
    @ApiOperation(value = "数据接入--应用级别修改应用下的接口是否允许推送数据")
    public ResultEntity<Object> appIfAllowDataTransfer(@RequestParam("appId") Long appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.appIfAllowDataTransfer(appId));
    }

    /**
     * hudi入仓配置 -- 新增表时 获取表名
     *
     * @param dbId
     * @return
     */
    @GetMapping("/getHudiConfigFromDb")
    @ApiOperation(value = "hudi入仓配置 -- 新增表时 获取表名")
    public ResultEntity<List<TablePyhNameDTO>> getHudiConfigFromDb(@RequestParam("dbId") Integer dbId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getHudiConfigFromDb(dbId));
    }

    /**
     * hudi入仓配置 -- 配置单张表
     *
     * @param dto
     * @return
     */
    @PostMapping("/syncOneTblForHudi")
    @ApiOperation(value = "hudi入仓配置 -- 配置单张表")
    public ResultEntity<Object> syncOneTblForHudi(@RequestBody SyncOneTblForHudiDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.syncOneTblForHudi(dto));
    }

    /**
     * 获取cdc类型所有应用及表名
     *
     * @return
     */
    @GetMapping("/getCDCAppNameAndTables")
    @ApiOperation(value = "获取cdc类型所有应用及表名")
    public ResultEntity<List<CDCAppNameAndTableVO>> getCDCAppNameAndTables(@RequestParam("appId") Integer appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCDCAppNameAndTables(appId));
    }

    /**
     * 获取cdc类型所有应用的库名
     *
     * @return
     */
    @GetMapping("/getCDCAppDbName")
    @ApiOperation(value = "获取cdc类型所有应用的库名")
    public ResultEntity<List<CDCAppDbNameVO>> getCDCAppDbName() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCDCAppDbName());
    }

    /**
     * 获取cdc类型所有应用及表名
     *
     * @return
     */
    @GetMapping("/getAllCDCAppName")
    @ApiOperation(value = "获取cdc类型所有应用")
    public ResultEntity<List<CDCAppNameVO>> getAllCDCAppName() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllCDCAppName());
    }

    /**
     * 获取数据接入所有应用和应用下的所有物理表
     *
     * @return
     */
    @ApiOperation("获取数据接入所有应用和应用下的所有物理表")
    @GetMapping("/getAllAppAndTables")
    public ResultEntity<List<AccessAndModelAppDTO>> getAllAppAndTables() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllAppAndTables());
    }

    /**
     * 通过物理表id获取应用详情
     *
     * @return
     */
    @ApiOperation("通过物理表id获取应用详情")
    @GetMapping("/getAppByTableAccessId")
    public ResultEntity<AppRegistrationDTO> getAppByTableAccessId(@RequestParam("tblId") Integer tblId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppByTableAccessId(tblId));
    }

    /**
     * hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
     * 同步方式 1全量  2增量
     *
     * @param syncDto
     */
    @ApiOperation("hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库 同步方式 1全量  2增量")
    @PostMapping("/hudiSyncAllTables")
    public ResultEntity<Object> hudiSyncAllTables(@RequestBody HudiSyncDTO syncDto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.hudiSyncAllTables(syncDto));
    }

    /**
     * hudi入仓配置 重新同步单个指定表
     * 某张表结构变了  想重新同步一下   这张表的字段  已有的不要动  不存在的删掉  新加的再同步过来
     *
     * @param syncDto
     */
    @ApiOperation("hudi入仓配置 重新同步单个指定表")
    @PostMapping("/hudiReSyncOneTable")
    public ResultEntity<Object> hudiReSyncOneTable(@RequestBody HudiReSyncDTO syncDto) {
        return ResultEntityBuild.build(service.hudiReSyncOneTable(syncDto));
    }

    /**
     * 获取所有被应用引用的数据源信息
     */
    @ApiOperation("获取所有被应用引用的数据源信息")
    @GetMapping("/getAppSources")
    public List<AppDataSourceDTO> getAppSources() {
        return service.getAppSources();
    }

    /**
     * 获取元数据地图 数据湖（数据接入）
     */
    @ApiOperation("获取元数据地图 数据湖（数据接入）")
    @GetMapping("/accessGetMetaMap")
    public List<MetaMapDTO> accessGetMetaMap() {
        return service.accessGetMetaMap();
    }

    /**
     * 元数据地图 获取应用下的表
     */
    @ApiOperation("元数据地图 获取应用下的表")
    @GetMapping("/accessGetMetaMapTableDetail")
    public List<MetaMapTblDTO> accessGetMetaMapTableDetail(@RequestParam("appId") Integer appId) {
        return service.accessGetMetaMapTableDetail(appId);
    }

    /**
     * 根据应用id 获取当前应用引用的系统数据源和目标库的系统数据源   id+名称
     *
     * @return
     */
    @ApiOperation("根据应用id 获取当前应用引用的系统数据源和目标库的系统数据源   id+名称")
    @GetMapping("/getAppSourceAndTarget")
    public ResultEntity<List<DataSourceInfoDTO>> getAppSourceAndTarget(@RequestParam("appId") Integer appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppSourceAndTarget(appId));
    }

    /**
     * 根据应用id 获取当前应用引用的系统数据源和目标库的系统数据源详细信息
     *
     * @return
     */
    @ApiOperation("根据应用id 获取当前应用引用的系统数据源和目标库的系统数据源详细信息")
    @GetMapping("/getAppSourceAndTargetFullInfo")
    public ResultEntity<List<DataSourceFullInfoDTO>> getAppSourceAndTargetFullInfo(@RequestParam("appId") Integer appId,
                                                                                   @RequestParam("tblId") Integer tblId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppSourceAndTargetFullInfo(appId, tblId));
    }

    /**
     * 生成AES密钥
     *
     * @return
     */
    @ApiOperation("生成AES密钥")
    @GetMapping("/generateAesKey")
    public ResultEntity<SecretKey> generateAesKey() {
        try {
            //返回值示例
            //{
            //  "code": 0,
            //  "msg": "成功",
            //  "data": {
            //    "algorithm": "AES",
            //    "encoded": "sOY25BXdGfFF3cYCsdyyjw==",
            //    "format": "RAW",
            //    "destroyed": false
            //  },
            //  "traceId": null
            //}
            //这个encoded的值是base64编码的密钥，使用时需要解析
            return ResultEntityBuild.build(ResultEnum.SUCCESS, AesEncryptionDecryptionUtils.generateAes128Key());
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
        }
        return ResultEntityBuild.build(ResultEnum.GENERATE_AES_ERROR);
    }

    /**
     * 根据api Id获取密钥
     *
     * @return
     */
    @ApiOperation("根据api Id获取密钥")
    @PostMapping("/getAesKey")
    public ResultEntity<Object> getAesKeyByApiCode(@RequestBody AesKeyDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, apiConfig.getAesKeyByApiCode(dto.getApiCode()));
    }

}
