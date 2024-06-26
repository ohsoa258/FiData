package com.fisk.dataaccess.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataaccess.dto.access.NifiAccessDTO;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.datamodel.TableQueryDTO;
import com.fisk.dataaccess.dto.dataops.TableInfoDTO;
import com.fisk.dataaccess.dto.ftp.CopyFtpFileDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.dto.table.TableVersionDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.dataaccess.vo.CDCAppDbNameVO;
import com.fisk.dataaccess.vo.CDCAppNameAndTableVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@FeignClient("dataAccess-service")
public interface DataAccessClient {

    /**
     * 元数据实例&DB构建
     *
     * @param id appid
     * @return 执行结果
     */
    @GetMapping("/appRegistration/getAtlasEntity")
    ResultEntity<AtlasEntityDTO> getAtlasEntity(@RequestParam("id") long id);

    /**
     * 元数据Table&Column构建
     *
     * @param id    物理表id
     * @param appid appid
     * @return AtlasEntityDbTableColumnDTO
     */
    @GetMapping("/physicalTable/getAtlasBuildTableAndColumn")
    ResultEntity<AtlasEntityDbTableColumnDTO> getAtlasBuildTableAndColumn(
            @RequestParam("id") long id, @RequestParam("app_id") long appid);

    /**
     * 应用注册回写GUID
     * atlas通过appid,将atlasInstanceId和atlasDbId保存下来
     *
     * @param appid           appid
     * @param atlasInstanceId atlasInstanceId
     * @param atlasDbId       atlasDbId
     * @return 执行结果
     */
    @PostMapping("/appRegistration/addAtlasInstanceIdAndDbId")
    ResultEntity<Object> addAtlasInstanceIdAndDbId(
            @RequestParam("app_id") long appid,
            @RequestParam("atlas_instance_id") String atlasInstanceId,
            @RequestParam("atlas_db_id") String atlasDbId);

    /**
     * 根据应用注册id和物理表id,查询atlasInstanceId和atlasDbId
     *
     * @param appid 应用注册id
     * @param id    物理表id
     * @return AtlasWriteBackDataDTO
     */
    @GetMapping("/physicalTable/getAtlasWriteBackDataDTO")
    ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(
            @RequestParam("app_id") long appid,
            @RequestParam("id") long id);

    /**
     * 物理表回写
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/physicalTable/addAtlasTableIdAndDorisSql")
    ResultEntity<Object> addAtlasTableIdAndDorisSql(@RequestBody AtlasWriteBackDataDTO dto);

    @PostMapping("/physicalTable/getTableNames")
    ResultEntity<Object> getTableNames(@RequestBody TableQueryDTO tableQueryDTO);


    /**
     * 提供给nifi的数据
     *
     * @param id    物理表id
     * @param appid 应用注册id
     * @return DataAccessConfigDTO
     */
    @GetMapping("/physicalTable/dataAccessConfig")
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(
            @RequestParam("id") long id, @RequestParam("app_id") long appid);

    /**
     * 回写componentId
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/physicalTable/addComponentId")
    public ResultEntity<Object> addComponentId(@RequestBody NifiAccessDTO dto);

    /**
     * getTableField
     *
     * @param id
     * @return
     */
    @PostMapping("/tableFields/getTableField")
    public ResultEntity<Object> getTableField(@RequestParam("id") int id);


    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/appRegistration/get/{id}")
    @ApiOperation(value = "回显")
    public ResultEntity<AppRegistrationDTO> getData(@PathVariable("id") long id);

    /**
     * 根据表id，获取表详情
     *
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/physicalTable/getTableAccess/{id}")
    @ApiOperation("修改接口的回显数据")
    public ResultEntity<TableAccessDTO> getTableAccess(@PathVariable("id") int id);

    /**
     * 提供给nifi的数据
     *
     * @param tableName tableName
     * @param selectSql selectSql
     * @return DataAccessConfigDTO
     */
    @GetMapping("/physicalTable/createPgToDorisConfig")
    public ResultEntity<Object> createPgToDorisConfig(@RequestParam("tableName") String tableName, @RequestParam("selectSql") String selectSql);


    /**
     * 根据接入表id获取所有字段id
     *
     * @param id id
     * @return list
     */
    @GetMapping("/physicalTable/getTableFieldId/{id}")
    public ResultEntity<Object> getTableFieldId(@PathVariable("id") int id);

    /**
     * 获取所有物理表id
     *
     * @return list
     */
    @GetMapping("/dataAccessTree/getTableId")
    public ResultEntity<List<ChannelDataDTO>> getTableId();

    @PostMapping("/dataAccessTree/getTableId")
    public ResultEntity<List<ChannelDataDTO>> getTableId(@RequestBody NifiComponentsDTO dto);

    /**
     * 封装参数给nifi
     *
     * @param tableId tableId
     * @param appId   appId
     * @return dto
     */
    @GetMapping("/physicalTable/getBuildPhysicalTableDTO")
    public ResultEntity<BuildPhysicalTableDTO> getBuildPhysicalTableDTO(
            @RequestParam("table_id") long tableId, @RequestParam("app_id") long appId);

    /**
     * 更新发布状态
     *
     * @param dto dto
     */
    @ApiOperation("修改物理表发布状态")
    @PutMapping("/physicalTable/updateTablePublishStatus")
    public void updateTablePublishStatus(@RequestBody ModelPublishStatusDTO dto);

    /**
     * 根据appId和tableId 获取appName和tableName
     *
     * @param dto dto
     * @return 查询结果
     */
    @ApiOperation("根据appId和tableId 获取appName和tableName")
    @PostMapping("/dataAccessTree/getAppNameAndTableName")
    public ResultEntity<Object> getAppNameAndTableName(@RequestBody DataAccessIdsDTO dto);

    /**
     * 获取数据接入已发布的元数据对象
     *
     * @return 元数据对象
     */
    @GetMapping("/dataAccessTree/getDataAccessMetaData")
    public ResultEntity<List<DataAccessSourceTableDTO>> getDataAccessMetaData();

    /**
     * 获取数据接入已发布的元数据对象
     *
     * @return 元数据对象
     */
    @GetMapping("/dataAccessTree/getDataAccessMetaDataByTableName")
    public ResultEntity<DataAccessSourceTableDTO> getDataAccessMetaDataByTableName(String tableName);

    /**
     * 修改api发布状态
     *
     * @param dto dto
     */
    @ApiOperation("修改api发布状态")
    @PutMapping("/apiConfig/updateApiPublishStatus")
    public void updateApiPublishStatus(@RequestBody ModelPublishStatusDTO dto);

    /**
     * 调度调用第三方api,接收数据,并导入到FiData平台
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/apiConfig/importData")
    @ApiOperation(value = "调度调用第三方api,接收数据,并导入到FiData平台")
    ResultEntity<Object> importData(@RequestBody ApiImportDataDTO dto);

    /**
     * 通过appId和apiId查询表名集合
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/appRegistration/getTableNameListByAppIdAndApiId")
    @ApiOperation(value = "通过appId和apiId查询表名集合")
    public ResultEntity<List<LogMessageFilterVO>> getTableNameListByAppIdAndApiId(@RequestBody PipelineTableQueryDTO dto);

    /**
     * 根据sql语句,获取字段列表(数据建模)
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @PostMapping("/appRegistration/getTableAccessQueryList")
    ResultEntity<OdsResultDTO> getTableAccessQueryList(@RequestBody OdsQueryDTO query);

    /**
     * 构建业务元数据其他数据信息
     *
     * @param dto dto
     * @return 查询结果
     */
    @PostMapping("/appRegistration/buildTableRuleInfo")
    @ApiOperation(value = "构建业务元数据其他数据信息")
    ResultEntity<TableRuleInfoDTO> buildTableRuleInfo(@RequestBody TableRuleParameterDTO dto);

    /**
     * 构建元数据查询对象(表及下面的字段)
     *
     * @param dto dto
     * @return 元数据对象
     */
    @ApiOperation("构建元数据查询对象(表及下面的字段)")
    @PostMapping("/dataAccessTree/buildFiDataTableMetaData")
    ResultEntity<List<FiDataTableMetaDataDTO>> buildFiDataTableMetaData(@RequestBody FiDataTableMetaDataReqDTO dto);

    /**
     * 刷新数据接入结构
     *
     * @param dto dto
     * @return 元数据对象
     */
    @ApiOperation("刷新数据接入结构")
    @PostMapping("/appRegistration/setDataStructure")
    ResultEntity<Object> setDataAccessStructure(@RequestBody FiDataMetaDataReqDTO dto);

    /**
     * 根据表信息/字段ID,获取表/字段基本信息
     *
     * @param dto dto
     * @return 查询结果
     */
    @PostMapping("/appRegistration/getFiDataTableMetaData")
    @ApiOperation(value = "根据表信息/字段ID,获取表/字段基本信息")
    ResultEntity<List<FiDataTableMetaDataDTO>> getFiDataTableMetaData(@RequestBody FiDataTableMetaDataReqDTO dto);

    /**
     * 获取所有应用信息
     *
     * @return list
     */
    @GetMapping("/appRegistration/getAppList")
    @ApiOperation(value = "获取所有应用信息")
    ResultEntity<List<AppBusinessInfoDTO>> getAppList();

    /**
     * 获取http请求返回的结果
     *
     * @param dto dto
     * @return 查询结果
     */
    @PostMapping("/apiConfig/getHttpRequestResult")
    @ApiOperation(value = "获取http请求返回的结果")
    String getHttpRequestResult(@RequestBody ApiHttpRequestDTO dto);

    /**
     * 数据源驱动类型
     *
     * @return
     */
    @GetMapping("/appRegistration/getDriveType")
    @ApiOperation(value = "数据源驱动类型")
    ResultEntity<List<AppDriveTypeDTO>> getDriveType();

    /**
     * 复制ftp文件到新目录
     *
     * @return
     */
    @ApiOperation(value = "复制ftp文件到新目录")
    @PostMapping("/ftp/copyFtpFile")
    ResultEntity<Object> copyFtpFile(@Validated @RequestBody CopyFtpFileDTO dto);

    /**
     * 删除表版本
     *
     * @return
     */
    @PostMapping("/tableFields/delTableVersion")
    @ApiOperation(value = "删除表版本")
    ResultEntity<Object> delVersionData(@Validated @RequestBody TableVersionDTO dto);

    /**
     * 根据表名获取接入表信息
     *
     * @param tableName
     * @return
     */
    @ApiOperation("根据表名获取接入表信息")
    @PostMapping("/DataOps/getTableInfo")
    ResultEntity<TableInfoDTO> getTableInfo(@Validated @RequestBody String tableName);

    /**
     * 根据表名字段显示名称
     *
     * @param tableName
     * @return
     */
    @ApiOperation("根据表名字段显示名称")
    @PostMapping("/DataOps/getTableColumnDisplay")
    ResultEntity<List<String[]>> getTableColumnDisplay(@Validated @RequestBody String tableName);

    /**
     * 元数据同步应用信息
     *
     * @return
     */
    @GetMapping("/appRegistration/synchronizationAppRegistration")
    @ApiOperation(value = "元数据同步应用信息")
    ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAppRegistration();

    /**
     * 元数据同步所有接入表
     *
     * @return
     */
    @GetMapping("/appRegistration/synchronizationAccessTable")
    @ApiOperation(value = "元数据同步所有接入表")
    ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAccessTable();

    /**
     * 元数据同步所有接入表
     *
     * @return
     */
    @GetMapping("/appRegistration/synchronizationAccessTableByLastSyncTime")
    @ApiOperation(value = "元数据同步所有接入表")
    ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAccessTableByLastSyncTime(@RequestParam("lastSyncTime")String lastSyncTime);

    /**
     * 依据应用id集合批量查询目标源id集合
     *
     * @param appIds
     * @return
     */
    @PostMapping("/appRegistration/getBatchTargetDbIdByAppIds")
    @ApiOperation(value = "依据应用id集合查询目标源id集合")
    ResultEntity<List<AppRegistrationInfoDTO>> getBatchTargetDbIdByAppIds(@RequestBody List<Integer> appIds);

    /**
     * 测试ftp数据源连接
     *
     * @param dto
     * @return
     */
    @ApiOperation("测试ftp数据源连接")
    @PostMapping("/ftp/connectFtp")
    ResultEntity<Object> connectFtp(@RequestBody DbConnectionDTO dto);

    /**
     * 测试sftp数据源连接
     *
     * @param dto
     * @return
     */
    @ApiOperation("测试sftp数据源连接")
    @PostMapping("/Sftp/connectFtp")
    ResultEntity<Object> connectSftp(@RequestBody DbConnectionDTO dto);

    /**
     * api选择jwt验证方式,测试获取token
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "jwt验证方式,测试获取token")
    @PostMapping("/appRegistration/getApiToken")
    ResultEntity<Object> getApiToken(@RequestBody AppDataSourceDTO dto);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 配合task模块，当平台配置修改数据源信息时，数据接入引用的数据源信息一并修改
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "修改数据接入引用的平台配置数据源信息")
    @PostMapping("/datasource/editDataSourceByTask")
    ResultEntity<Boolean> editDataSourceByTask(@RequestBody DataSourceSaveDTO dto);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据SystemDataSourceId获取数据接入引用的数据源信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "根据SystemDataSourceId获取数据接入引用的数据源信息")
    @GetMapping("/datasource/getDataSourcesBySystemDataSourceId")
    ResultEntity<List<AppDataSourceDTO>> getDataSourcesBySystemDataSourceId(@RequestParam("id") Integer id);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据appId获取app应用名称
     *
     * @param id
     * @return
     */
    @GetMapping("/appRegistration/getAppNameById")
    @ApiOperation(value = "根据appId获取app应用名称")
    ResultEntity<AppRegistrationDTO> getAppNameById(@RequestParam("id") Long id);

    /**
     * 通过表名（带架构）获取表信息
     *
     * @param tableName
     * @return
     */
    @GetMapping("/v3/tableAccess/getAccessTableByTableName")
    @ApiOperation(value = "通过表名（带架构）获取表信息")
    ResultEntity<TableAccessDTO> getAccessTableByTableName(@RequestParam("tableName") String tableName);

    /**
     * 获取数据接入引用的数据源id
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取数据接入引用的数据源id")
    @GetMapping("/datasource/getAccessDataSources")
    ResultEntity<AppDataSourceDTO> getAccessDataSources(@RequestParam("id") Long id);

    /**
     * 仅供数据资产模块调用使用,通过tb_app_datasource表内数据的id获取系统模块对应的数据源的信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "仅供数据资产模块调用使用,通过tb_app_datasource表内数据的id获取系统模块对应的数据源的信息")
    @GetMapping("/datasource/getSystemDataSourceById")
    ResultEntity<DataSourceDTO> getSystemDataSourceById(@RequestParam("id") Integer id);

    @GetMapping("/appRegistration/getAppByAppName")
    @ApiOperation(value = "根据应用名称获取单个应用详情")
    ResultEntity<AppRegistrationDTO> getAppByAppName(@RequestParam("appName") String appName);

    /**
     * 获取指定doris外部目录catalog下的所有db以及所有表
     *
     * @param dbID        平台配置数据库id
     * @param catalogName 目录名
     */
    @ApiOperation(value = "获取指定doris外部目录catalog下的所有db以及所有表")
    @GetMapping("/getDorisCatalogTreeByCatalogName")
    ResultEntity<Map<String, List<String>>> getDorisCatalogTreeByCatalogName(@RequestParam("dbID") Integer dbID,
                                                                                    @RequestParam("catalogName") String catalogName);

    /**
     * 调度调用第三方api,接收数据,并导入到FiData平台
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/apiConfig/importDataV2")
    @ApiOperation(value = "调度调用第三方api,接收数据,并导入到FiData平台")
    ResultEntity<Object> importDataV2(@RequestBody ApiImportDataDTO dto);


    /**
     * 获取cdc类型所有应用及表名
     * @param appId
     * @return
     */
    @GetMapping("/appRegistration/getCDCAppNameAndTables")
    @ApiOperation(value = "获取cdc类型所有应用及表名")
    ResultEntity<List<CDCAppNameAndTableVO>> getCDCAppNameAndTables(@RequestParam("appId") Integer appId);

    /**
     * 获取cdc类型所有应用的库名
     * @param
     * @return
     */
    @GetMapping("/appRegistration/getCDCAppDbName")
    ResultEntity<List<CDCAppDbNameVO>>getCDCAppDbName();
    @ApiOperation(value = "根据appId获取所有数据源")
    @GetMapping("/datasource/getAppSourcesByAppId")
    ResultEntity<List<AppDataSourceDTO>> getAppSourcesByAppId(@RequestParam("appId") Integer appId);

    /**
     * 获取数据接入所有应用和应用下的所有物理表
     *
     * @return
     */
    @ApiOperation("获取数据接入所有应用和应用下的所有物理表")
    @GetMapping("/appRegistration/getAllAppAndTables")
    ResultEntity<List<AccessAndModelAppDTO>> getAllAppAndTables();

    /**
     * 通过物理表id获取应用详情
     *
     * @return
     */
    @ApiOperation("通过物理表id获取应用详情")
    @GetMapping("/appRegistration/getAppByTableAccessId")
    ResultEntity<AppRegistrationDTO> getAppByTableAccessId(@RequestParam("tblId") Integer tblId);

    /**
     * 获取所有被应用引用的数据源信息
     */
    @ApiOperation("获取所有被应用引用的数据源信息")
    @GetMapping("/appRegistration/getAppSources")
    List<AppDataSourceDTO> getAppSources();

    /**
     * 获取数据接入表结构
     *
     * @param dto dto
     * @return 元数据对象
     */
    @ApiOperation("获取数据接入表结构")
    @PostMapping("/appRegistration/getTableDataStructure")
    ResultEntity<Object> getTableDataStructure(@RequestBody FiDataMetaDataReqDTO dto);


    /**
     * 获取数据接入字段结构
     *
     * @param dto dto
     * @return 元数据对象
     */
    @ApiOperation("获取数据接入字段结构")
    @PostMapping("/appRegistration/getFieldsDataStructure")
    ResultEntity<Object> getFieldsDataStructure(@RequestBody ColumnQueryDTO dto);

    /**
     * 获取元数据地图 数据湖（数据接入）
     */
    @ApiOperation("获取元数据地图 数据湖（数据接入）")
    @GetMapping("/appRegistration/accessGetMetaMap")
    List<MetaMapDTO> accessGetMetaMap();

    /**
     * 元数据地图 获取应用下的表
     */
    @ApiOperation("元数据地图 获取应用下的表")
    @GetMapping("/appRegistration/accessGetMetaMapTableDetail")
    List<MetaMapTblDTO> accessGetMetaMapTableDetail(@RequestParam("appId") Integer appId);

    /**
     * 获取所有物理表
     *
     * @return
     */
    @PostMapping("/v3/tableAccess/getAllAccessTbls")
    @ApiOperation(value = "获取所有物理表")
    ResultEntity<List<TableAccessDTO>> getAllAccessTbls();

    /**
     * 根据字段id集合获取字段详情集合
     *
     * @param fieldIds
     * @return
     */
    @GetMapping("/tableFields/getFieldInfosByIds")
    @ApiOperation(value = "根据字段id集合获取字段详情集合")
    ResultEntity<List<TableFieldsDTO>> getFieldInfosByIds(@RequestParam("fieldIds") List<Integer> fieldIds);

    @ApiOperation(value = "获取所有应用以及表、字段数据")
    @GetMapping("/appRegistration/getDataAppRegistrationMeta")
    ResultEntity<Object> getDataAppRegistrationMeta();

}
