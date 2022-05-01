package com.fisk.dataaccess.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.NifiAccessDTO;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
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
    @PostMapping("/dataAccessTree/getTableId")
    public ResultEntity<List<ChannelDataDTO>> getTableId(NifiComponentsDTO dto);

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
}
