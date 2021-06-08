package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.impl.AppRegistrationImpl;
import com.fisk.dataaccess.service.impl.TableAccessImpl;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Lock
 */
@Api(description = "物理表接口")
@RestController
@RequestMapping("/physicalTable")
public class PhysicalTableController {

    @Autowired
    private IAppRegistration appRService;

    @Autowired
    private ITableAccess tableAccess;

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
     * 添加物理表(实时)
     * @param tableAccessDTO
     * @return
     */
    @PostMapping("/addRealTime")
    public ResultEntity<Object> addRTData(@RequestBody TableAccessDTO tableAccessDTO){

        return ResultEntityBuild.build(tableAccess.addRTData(tableAccessDTO));
    }

    /**
     * 添加物理表(非实时)
     * @param tableAccessNDTO
     * @return
     */
    @PostMapping("/addNonRealTime")
    public ResultEntity<Object> addNRTData(
            @RequestBody TableAccessNDTO tableAccessNDTO) {

        return ResultEntityBuild.build(tableAccess.addNRTData(tableAccessNDTO));
    }

    /**
     * 删除数据
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(tableAccess.deleteData(id));
    }

}
