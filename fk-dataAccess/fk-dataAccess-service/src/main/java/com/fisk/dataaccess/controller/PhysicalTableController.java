package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Lock
 */
@RestController
@RequestMapping("/physicalTable")
public class PhysicalTableController {

    /**
     * 添加物理表
     * @param appRegistrationDTO
     * @return
     */
/*    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody AppRegistrationDTO appRegistrationDTO){

        return ResultEntityBuild.build(service.addData(appRegistrationDTO));
    }*/



}
