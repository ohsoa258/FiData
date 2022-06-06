package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.DatamaskingConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.service.datasecurity.DatamaskingConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:48
 */
@Api(tags = SwaggerConfig.DATA_MASKING_CONFIG_CONTROLLER)
@RestController
@RequestMapping("/datamaskingConfig")
public class DatamaskingConfigController {

    @Resource
    private DatamaskingConfigService service;

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<DatamaskingConfigDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PostMapping("/getList")
    @ApiOperation(value = "获取数据脱敏列表")
    public ResultEntity<List<DatamaskingConfigDTO>> getList(@Validated @RequestBody DataSourceIdDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getList(dto));
    }

    /**
     * 保存
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@Validated @RequestBody DatamaskingConfigDTO datamaskingConfig) {

        return ResultEntityBuild.build(service.addData(datamaskingConfig));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody DatamaskingConfigDTO dto) {

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
