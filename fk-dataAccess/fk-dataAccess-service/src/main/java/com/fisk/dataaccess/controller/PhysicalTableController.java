package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.service.impl.AppRegistrationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Lock
 */
@RestController
@RequestMapping("/physicalTable")
public class PhysicalTableController {

    @Autowired
    private AppRegistrationImpl appRService;

    /**
     * 根据是否为实时,查询应用名称集合
     * @param appType
     * @return
     */
    @GetMapping("/getAppType/{appType}")
    public ResultEntity<List<String>> queryAppName(
            @PathVariable("appType") byte appType) {

        List<String> data = appRService.queryAppName(appType);

        return ResultEntityBuild.build(ResultEnum.SUCCESS,data);
    }

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
