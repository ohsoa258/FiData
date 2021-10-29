package com.fisk.dataaccess.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.NifiAccessDTO;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
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
     *  根据应用注册id和物理表id,查询atlasInstanceId和atlasDbId
     *
     * @param appid 应用注册id
     * @param id 物理表id
     * @return AtlasWriteBackDataDTO
     */
    @GetMapping("/physicalTable/getAtlasWriteBackDataDTO")
    ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(
            @RequestParam("app_id") long appid,
            @RequestParam("id") long id);

    /**
     *  物理表回写
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/physicalTable/addAtlasTableIdAndDorisSql")
    ResultEntity<Object> addAtlasTableIdAndDorisSql(@RequestBody AtlasWriteBackDataDTO dto);


    /**
     * 提供给nifi的数据
     *
     * @param id 物理表id
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
    public ResultEntity<Object> createPgToDorisConfig(@RequestParam("tableName")String tableName,@RequestParam("selectSql")String selectSql);

    @GetMapping("/dataAccessTree/getComponentId")
    public ResultEntity<Object> getComponentId(@RequestBody DataAccessIdsDTO dto);


    /**
     * 根据接入表id获取所有字段id
     * @param id
     * @return
     */
    @GetMapping("/physicalTable/getTableFieldId/{id}")
    public ResultEntity<Object> getTableFieldId(@PathVariable("id") int id);

    @GetMapping("/dataAccessTree/getTableIds")
    public ResultEntity<List<ChannelDataDTO>> getTableId();
}
