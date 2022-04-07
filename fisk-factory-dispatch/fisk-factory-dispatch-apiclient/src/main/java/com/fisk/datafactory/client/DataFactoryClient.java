package com.fisk.datafactory.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @PostMapping("/dataFactory/getNIfiPortHierarchy")
    @ApiOperation(value = "获取管道层级关系")
    ResultEntity<NifiPortsHierarchyDTO> getNifiPortHierarchy(@Validated @RequestBody NifiGetPortHierarchyDTO dto);
}
