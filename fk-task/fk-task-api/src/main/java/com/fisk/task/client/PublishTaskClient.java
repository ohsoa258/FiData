package com.fisk.task.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.nifi.NifiRemoveDTO;
import com.fisk.task.dto.olap.BuildCreateModelTaskDto;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 发送任务
 *
 * @author gy
 */
@FeignClient("task-center")
public interface PublishTaskClient {

    /**
     * 发送任务创建消息
     *
     * @param data dto
     * @return 发送结果
     */
    @PostMapping("/publishTask/nifiFlow")
    ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data);

    /**
     * 元数据实例&DB构建
     *
     * @param ArDto dto
     * @return 构建结果
     */
    @PostMapping("/publishTask/atlasBuildInstance")
    ResultEntity<Object> publishBuildAtlasInstanceTask(@RequestBody AtlasEntityQueryDTO ArDto);

    /**
     * 元数据Table&Column构建
     *
     * @param ArDto dto
     * @return 构建结果
     */

    @PostMapping("/publishTask/atlasBuildTableAndColumn")
    ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody AtlasEntityQueryDTO ArDto);
    /**
     * 元数据删除
     * @param entityId
     * @return
     */
    @PostMapping("/publishTask/atlasEntityDelete")
    ResultEntity<Object> publishBuildAtlasEntityDeleteTask(@RequestBody String entityId);
    /**
     * doris创建表BUILD_DATAMODEL_DORIS_TABLE
     * @param atlasEntityQueryDTO
     * @return
     */
    @PostMapping("/publishTask/atlasDorisTable")
    ResultEntity<Object> publishBuildAtlasDorisTableTask(@RequestBody DimensionAttributeAddDTO atlasEntityQueryDTO);


    /**
     * 建模
     * @param buildCreateModelTaskDto
     * @return
     */
    @PostMapping("/olapTask/CreateModel")
    ResultEntity<Object> publishOlapCreateModel(@RequestBody BuildCreateModelTaskDto buildCreateModelTaskDto);

    /**
     * pgsql 删除表
     *
     * @param delTable
     * @return
     */
    @PostMapping("/publishTask/deletePgsqlTable")
    public ResultEntity<Object> publishBuildDeletePgsqlTableTask(@RequestBody PgsqlDelTableDTO delTable);

    /*
    * 修改调度
    *
    * */
    @PostMapping("/nifi/modifyScheduling")
    public ResultEntity<Object> modifyScheduling(@RequestParam("groupId")String groupId, @RequestParam("ProcessorId")String ProcessorId,@RequestParam("schedulingStrategy") String schedulingStrategy,@RequestParam("schedulingPeriod") String schedulingPeriod);

    /*
    * 删除nifi流程
    * */
    @PostMapping("/nifi/deleteNifiFlow")
    public ResultEntity<Object> deleteNifiFlow(@RequestBody List<NifiRemoveDTO> nifiRemoveDTOList);
}
