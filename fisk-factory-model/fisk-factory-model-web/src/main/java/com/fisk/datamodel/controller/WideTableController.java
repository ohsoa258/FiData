package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.service.IWideTable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.WIDE_TABLE})
@RestController
@RequestMapping("/wideTable")
public class WideTableController {

    @Resource
    IWideTable service;

    @ApiOperation("根据业务域id,获取宽表列表")
    @GetMapping("/getWideTableList/{id}")
    public ResultEntity<Object> getWideTableList(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getWideTableList(id));
    }

    @ApiOperation("查询宽表关联数据")
    @PostMapping("/executeWideTableSql")
    public ResultEntity<Object> executeWideTableSql(@Validated @RequestBody WideTableFieldConfigDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.executeWideTableSql(dto));
    }

    @ApiOperation("添加宽表")
    @PostMapping("/addWideTable")
    public ResultEntity<Object> addWideTable(@Validated @RequestBody WideTableConfigDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addWideTable(dto));
    }

    @ApiOperation("获取宽表详情")
    @GetMapping("/getWideTableDetails/{id}")
    public ResultEntity<Object> getWideTableDetails(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getWideTable(id));
    }

    @ApiOperation("修改宽表")
    @PostMapping("/updateWideTable")
    public ResultEntity<Object> updateWideTable(@Validated @RequestBody WideTableConfigDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.updateWideTable(dto));
    }

    @ApiOperation("删除宽表")
    @DeleteMapping("/deleteWideTable/{id}")
    public ResultEntity<Object> deleteWideTable(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteWideTable(id));
    }

    @ApiOperation("修改宽表doris发布状态")
    @PutMapping("/updateWideTablePublishStatus")
    public void updateWideTablePublishStatus(@RequestBody ModelPublishStatusDTO dto){
        service.updateWideTablePublishStatus(dto);
    }

}
