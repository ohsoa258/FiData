package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.SYNCHRONIZATION_DATA})
@RestController
@Slf4j
@RequestMapping("/BloodCompensation")
public class BloodCompensationController {

    @Resource
    IBloodCompensation service;

    @ApiOperation("同步元数据")
    @GetMapping("/systemSynchronousBlood")
    public ResultEntity<Object> systemSynchronousBlood(@RequestParam("currUserName") String currUserName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.systemSynchronousBlood(currUserName));
    }



}
