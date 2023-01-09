package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/BloodCompensation")
public class BloodCompensationController {

    @Resource
    IBloodCompensation service;

    @ApiOperation("血缘补偿")
    @GetMapping("/systemSynchronousBlood")
    public ResultEntity<Object> systemSynchronousBlood() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.systemSynchronousBlood());
    }

}
