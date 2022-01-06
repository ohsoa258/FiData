package com.fisk.datafactory;

import com.fisk.common.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
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
}
