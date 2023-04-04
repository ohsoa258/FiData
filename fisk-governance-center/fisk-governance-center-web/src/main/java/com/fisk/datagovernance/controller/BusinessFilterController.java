package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_SaveProcessDTO;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.filterresult.BusinessFilterResultVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.TestVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessAssemblyVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTaskVO;
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

    @ApiOperation("清洗规则，查询全部清洗规则")
    @PostMapping("/getAllRule")
    public ResultEntity<List<BusinessFilterVO>> getAllRule(@RequestBody BusinessFilterQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllRule(dto));
    }

    @ApiOperation("清洗规则，添加业务清洗规则")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody BusinessFilterDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("清洗规则，编辑业务清洗规则")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody BusinessFilterEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("清洗规则，删除业务清洗规则")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    @ApiOperation("清洗规则，修改业务清洗规则执行顺序")
    @PutMapping("/editFilterRuleSort")
    public ResultEntity<Object> editFilterRuleSort(@RequestBody List<BusinessFilterSortDto> dto) {
        return ResultEntityBuild.build(service.editFilterRuleSort(dto));
    }

    @ApiOperation("清洗流程，查询工作区组件")
    @GetMapping("/getProcessAssembly")
    public ResultEntity<List<BusinessFilter_ProcessAssemblyVO>> getProcessAssembly() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getProcessAssembly());
    }

    @ApiOperation("清洗流程，强随机获取工作区taskCode")
    @GetMapping("/getProcessTaskCode")
    public ResultEntity<String> getProcessTaskCode() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.getProcessTaskCode());
    }

    @ApiOperation("清洗流程，查询工作区流程")
    @GetMapping("/getProcessDetail/{ruleId}")
    public ResultEntity<List<BusinessFilter_ProcessTaskVO>> getProcessDetail(@PathVariable("ruleId") long ruleId) {
        return service.getProcessDetail(ruleId);
    }

    @ApiOperation("清洗流程，保存工作区流程")
    @PostMapping("/saveProcess")
    public ResultEntity<Object> saveProcess(@RequestBody BusinessFilter_SaveProcessDTO dto) {
        return ResultEntityBuild.build(service.saveProcess(dto));
    }

    @ApiOperation("清洗流程，执行清洗流程")
    @PostMapping("/collProcess")
    public ResultEntity<List<BusinessFilterResultVO>> collProcess(@RequestParam("ruleId") long ruleId) {
        return service.collProcess(ruleId);
    }

    @ApiOperation("API清洗，调用授权API获取Token")
    @PostMapping("/collAuthApi")
    public ResultEntity<String> collAuthApi(@RequestBody BusinessFilterSaveDTO dto) {
        return service.collAuthApi(dto);
    }

    @ApiOperation("API清洗，调用API清洗数据")
    @PostMapping("/collApi")
    public ResultEntity<Object> collApi(@RequestBody BusinessFilterSaveDTO dto) {
        return ResultEntityBuild.build(service.collApi(dto));
    }

    @ApiOperation("API清洗，获取测试数据")
    @PostMapping("/getCollApiTestData")
    public Object getCollApiTestData(@RequestBody TestVO dto) {
        TestVO testVO = new TestVO();
        testVO.setId(dto.getId());
        testVO.setSource("AAD");
        return testVO;
    }
}
