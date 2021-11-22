package com.fisk.datafactory;

import com.fisk.common.response.ResultEntity;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Lock
 */
@FeignClient("data-factory")
public interface DataFactoryClient {
    @PostMapping("/nifiPort/fliterData")
    ResultEntity<NifiPortsDTO> getFilterData(@RequestBody PortRequestParamDTO dto);
}
