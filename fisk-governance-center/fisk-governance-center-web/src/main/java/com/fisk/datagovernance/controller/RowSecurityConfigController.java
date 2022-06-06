package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.RowSecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.service.datasecurity.RowSecurityConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Api(tags = SwaggerConfig.ROW_SECURITY_CONFIG_CONTROLLER)
@RestController
@RequestMapping("/rowSecurityConfig")
public class RowSecurityConfigController {

    @Resource
    private RowSecurityConfigService service;

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<RowSecurityConfigDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PostMapping("/getList")
    @ApiOperation(value = "获取行级安全列表")
    public ResultEntity<List<RowSecurityConfigDTO>> getList(@Validated @RequestBody DataSourceIdDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getList(dto));
    }

    /**
     * 保存
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody RowSecurityConfigDTO dto) {

        return ResultEntityBuild.build(service.addData(dto));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    @ApiOperation(value = "修改")
    public ResultEntity<Object> editData(@RequestBody RowSecurityConfigDTO dto) {

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

    @PutMapping("/editDefaultConfig/{defaultConfig}")
    @ApiOperation(value = "修改表级缺省配置")
    public ResultEntity<Object> editDefaultConfig(@PathVariable("defaultConfig") long defaultConfig) {

        return ResultEntityBuild.build(service.editDefaultConfig(defaultConfig));
    }

}
