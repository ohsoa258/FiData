package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.datamodel.service.IDataService;
import com.fisk.dataservice.dto.isDimensionDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/dataService")
@Slf4j
public class DataServiceController {

    @Resource
    IDataService iDataService;

    @ApiOperation("判断维度与维度、事实与维度是否存在关联")
    @PostMapping("/isExistAssociate")
    public ResultEntity<Boolean> isExistAssociate(@Validated @RequestBody isDimensionDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDataService.isExistAssociate(dto));
    }

    @ApiOperation("根据派生指标id,获取该业务域下日期维度以及字段")
    @GetMapping("/getDimensionDate/{id}")
    public ResultEntity<DimensionTimePeriodDTO> getDimensionDate(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDataService.getDimensionDate(id));
    }

    @ApiOperation("根据时间维度表名称获取所有字段")
    @GetMapping("/getDimensionFieldNameList/{tableName}")
    public ResultEntity<List<String>> getDimensionFieldNameList(@PathVariable("tableName") String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDataService.getDimensionFieldNameList(tableName));
    }

}
