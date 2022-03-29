package com.fisk.datagovernance.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.TablesecurityConfigDTO;
import com.fisk.datagovernance.service.datasecurity.TablesecurityConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Api(tags = SwaggerConfig.TABLE_SECURITY_CONFIG_CONTROLLER)
@RestController
@RequestMapping("/tableSecurity")
public class TableSecurityConfigController {

    @Resource
    private TablesecurityConfigService service;

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<TablesecurityConfigDTO> getData(@PathVariable("id") long id){

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @GetMapping("/getList")
    @ApiOperation(value = "获取表级安全列表")
    public ResultEntity<List<TablesecurityConfigDTO>> getList() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getList());
    }

    /**
     * 保存
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody TablesecurityConfigDTO tablesecurityConfig){

        return ResultEntityBuild.build(service.addData(tablesecurityConfig));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TablesecurityConfigDTO dto){

        return ResultEntityBuild.build(service.editData(dto));
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

}
