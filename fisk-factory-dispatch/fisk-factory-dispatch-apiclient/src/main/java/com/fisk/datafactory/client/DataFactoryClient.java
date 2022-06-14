package com.fisk.datafactory.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
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
    @GetMapping("/dataFactory/getNifiPortTaskListById/{id}")
    @ApiOperation(value = "根据管道主键id查询管道内第一批任务")
    ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskListById(@PathVariable("id") Long id);

    /**
     * 根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/dataFactory/redirect")
    @ApiOperation(value = "根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用")
    ResultEntity<List<DispatchRedirectDTO>> redirect(@RequestBody NifiCustomWorkflowDetailDTO dto);
}
