package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import com.fisk.datafactory.service.INifiPort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = SwaggerConfig.NIFI_PORT)
@RestController
@RequestMapping("/nifiPort")
public class NifiPortController {

    @Resource
    INifiPort service;

    @PostMapping("/fliterData")
    @ApiOperation(value = "过滤当前节点inport及outport")
    public ResultEntity<NifiPortsDTO> getFilterData(@RequestBody PortRequestParamDTO dto) {

        return service.getFilterData(dto);
    }

}
