package com.fisk.dataaccess.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataaccess.dto.access.NifiAccessDTO;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.dataops.TableInfoDTO;
import com.fisk.dataaccess.dto.ftp.CopyFtpFileDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableVersionDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
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

import java.util.List;

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
    public ResultEntity<Object> importData(@RequestBody ApiImportDataDTO dto);

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
     * @param dto
     * @return
     */
    @ApiOperation(value = "修改数据接入引用的平台配置数据源信息")
    @PostMapping("/datasource/editDataSourceByTask")
    ResultEntity<Boolean> editDataSourceByTask(@RequestBody DataSourceSaveDTO dto);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据SystemDataSourceId获取数据接入引用的数据源信息
     * @param id
     * @return
     */
    @ApiOperation(value = "根据SystemDataSourceId获取数据接入引用的数据源信息")
    @GetMapping("/datasource/getDataSourcesBySystemDataSourceId")
    ResultEntity<List<AppDataSourceDTO>> getDataSourcesBySystemDataSourceId(@RequestParam("id") Integer id);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据appId获取app应用名称
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
    @GetMapping("/getAccessTableByTableName")
    @ApiOperation(value = "通过表名（带架构）获取表信息")
    ResultEntity<Object> getAccessTableByTableName(@RequestParam("tableName") String tableName);
}
