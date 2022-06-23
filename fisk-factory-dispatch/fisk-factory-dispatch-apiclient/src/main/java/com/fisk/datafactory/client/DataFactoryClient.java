package com.fisk.datafactory.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.dto.dispatchlog.PipelStageLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lock
 */
@FeignClient("data-factory")
public interface DataFactoryClient {
    /**
     * nifi管道需要的数据
     *
     * @param dto dto
     * @return dto
     */
    @PostMapping("/nifiPort/fliterData")
    ResultEntity<NifiPortsDTO> getFilterData(@RequestBody PortRequestParamDTO dto);

    /**
     * 修改管道发布状态
     *
     * @param dto dto
     */
    @ApiOperation("修改管道发布状态")
    @PutMapping("/nifiCustomWorkflow/updatePublishStatus")
    void updatePublishStatus(@RequestBody NifiCustomWorkflowDTO dto);

    /**
     * 判断物理表是否在管道使用
     *
     * @param dto dto
     * @return boolean
     */
    @PostMapping("/dataFactory/loadDepend")
    @ApiOperation(value = "判断物理表是否在管道使用")
    boolean loadDepend(@RequestBody LoadDependDTO dto);

    /**
     * 获取管道层级关系
     *
     * @param dto dto
     * @return 查询结果
     */
    @PostMapping("/dataFactory/getNIfiPortHierarchy")
    @ApiOperation(value = "获取管道层级关系")
    ResultEntity<NifiPortsHierarchyDTO> getNifiPortHierarchy(@Validated @RequestBody NifiGetPortHierarchyDTO dto);

    /**
     * 根据管道主键id查询管道内第一批任务
     *
     * @param id 管道主键id
     * @return 查询结果集合
     */
    @GetMapping("/dataFactory/getNifiPortTaskFirstListById/{id}")
    @ApiOperation(value = "根据管道主键id查询管道内第一批任务")
    ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskFirstListById(@PathVariable("id") Long id);

    /**
     * 根据管道主键id查询管道内最后一批任务
     *
     * @param id 管道主键id
     * @return list
     */
    @GetMapping("/dataFactory/getNifiPortTaskLastListById/{id}")
    @ApiOperation(value = "根据管道主键id查询管道内最后一批任务")
    ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskLastListById(@PathVariable("id") Long id);

    /**
     * 根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/dataFactory/redirect")
    @ApiOperation(value = "根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用")
    ResultEntity<List<DispatchRedirectDTO>> redirect(@RequestBody NifiCustomWorkflowDetailDTO dto);

    /**
     * 获取管道日志
     *
     * @param dto dto
     * @return 查询结果
     */
    @PostMapping("/dataFactory/getPipeJobLog")
    @ApiOperation(value = "获取管道日志")
    ResultEntity<List<PipelJobLogVO>> getPipeJobLog(@RequestBody List<PipelJobLogVO> dto);

    /**
     * 获取阶段日志
     *
     * @param taskId dto
     * @return 执行结果
     */
    @PostMapping("/dataFactory/getPipeStageLog")
    @ApiOperation(value = "获取阶段日志")
    ResultEntity<List<PipelStageLogVO>> getPipeStageLog(@RequestParam String taskId);

    /**
     * 获取阶段日志
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/dataFactory/getPipeTaskLog")
    @ApiOperation(value = "获取表日志")
    ResultEntity<List<PipelTaskLogVO>> getPipeTaskLog(@RequestBody PipelTaskLogVO dto);

    /**
     * access or model 删除操作时,task要同步删除这些数据
     *
     * @param list list
     * @return 执行结果
     */
    @ApiOperation("access or model 删除操作时,task要同步删除这些数据")
    @PostMapping("/nifiCustomWorkflowDetail/editByDeleteTable")
    ResultEntity<Object> editByDeleteTable(@Validated @RequestBody List<DeleteTableDetailDTO> list);
}
