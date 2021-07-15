package com.fisk.dataaccess.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.dto.AtlasAccessDTO;
import com.fisk.dataaccess.dto.NifiAccessDTO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam("id") long id, @RequestParam("appid") long appid);

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
            @RequestParam("appid") long appid,
            @RequestParam("atlas_instance_id") String atlasInstanceId,
            @RequestParam("atlas_db_id") String atlasDbId);

    /**
     *
     *
     * @param appid 应用注册id
     * @param id 物理表id
     * @return AtlasWriteBackDataDTO
     */
    @GetMapping("/physicalTable/getAtlasWriteBackDataDTO")
    ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(
            @RequestParam("appid") long appid,
            @RequestParam("id") long id);

    /**
     *  物理表回写
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/physicalTable/addAtlasTableIdAndDorisSql")
    ResultEntity<Object> addAtlasTableIdAndDorisSql(@RequestBody AtlasAccessDTO dto);


    /**
     * 提供给nifi的数据
     *
     * @param id 物理表id
     * @param appid 应用注册id
     * @return DataAccessConfigDTO
     */
    @GetMapping("/physicalTable/dataAccessConfig")
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(
            @RequestParam("id") long id, @RequestParam("appid") long appid);

    /**
     * 回写componentId
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/physicalTable/addComponentId")
    public ResultEntity<Object> addComponentId(@RequestBody NifiAccessDTO dto);
}
