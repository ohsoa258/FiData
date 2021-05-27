package com.fisk.dataaccess.controller;

import com.fisk.auth.utils.UserContext;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.vo.PageVO;
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

    /**
     * 分页查询
     * @param key 搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return
     */
    @GetMapping("/page")
    public ResultEntity<Object> queryByPageAppRes(
            // 过滤条件条件非必要
            @RequestParam(value = "key",required = false)String key,
            // 给个默认值,防止不传值时查询全表
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows) {
        PageDTO<AppRegistrationDTO> data = service.listAppRegistration(key,page,rows);
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }



}
