package com.fisk.dataaccess.controller;

import com.fisk.auth.utils.UserContext;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Lock
 * @data: 2021/5/26 14:15
 */
@RestController
@RequestMapping("/data")
public class AppRegistrationController {

    @Autowired
    private IAppRegistration service;

    /**
     * 添加应用
     * @param appRegistrationDTO
     * @return
     */
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody AppRegistrationDTO appRegistrationDTO) throws Exception{

        return ResultEntityBuild.build(service.addData(appRegistrationDTO));
    }

    @GetMapping("/get")
    public ResultEntity<Object> getData() {
        List<AppRegistrationVO> data = service.listAppRegistration();
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }



}
