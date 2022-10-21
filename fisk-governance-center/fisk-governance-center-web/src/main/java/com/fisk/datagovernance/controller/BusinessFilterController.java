package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.BUSINESS_FILTER_CONTROLLER})
@RestController
@RequestMapping("/businessfilter")
public class BusinessFilterController {
    @Resource
    private IBusinessFilterManageService service;

    @ApiOperation("分页查询业务清洗模板组件")
    @PostMapping("/page")
    public ResultEntity<Page<BusinessFilterVO>> getAll(@RequestBody BusinessFilterQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("添加业务清洗模板组件")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody BusinessFilterDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑业务清洗模板组件")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody BusinessFilterEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除业务清洗模板组件")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    @ApiOperation("修改业务清洗模板组件执行顺序")
    @PutMapping("/editModuleExecSort")
    public ResultEntity<Object> editModuleExecSort(@RequestBody List<BusinessFilterSortDto> dto) {
        return ResultEntityBuild.build(service.editModuleExecSort(dto));
    }

    @ApiOperation("API清洗，调用授权API获取Token")
    @PostMapping("/collAuthApi")
    public ResultEntity<String> collAuthApi(@RequestBody BusinessFilterDTO dto) {
        return service.collAuthApi(dto);
    }

    @ApiOperation("API清洗，调用API清洗数据")
    @PostMapping("/collApi")
    public ResultEntity<Object> collApi(@RequestBody BusinessFilterDTO dto) {
        return ResultEntityBuild.build(service.collApi(dto));
    }
}
