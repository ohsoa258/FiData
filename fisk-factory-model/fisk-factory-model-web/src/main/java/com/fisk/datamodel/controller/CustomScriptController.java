package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.customscript.CustomScriptDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.service.ICustomScript;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.CUSTOM_SCRIPT})
@RestController
@RequestMapping("/CustomScript")
public class CustomScriptController {

    @Resource
    ICustomScript service;

    @ApiOperation("新增")
    @PostMapping("/addCustomScript")
    public ResultEntity<Object> addCustomScript(@RequestBody CustomScriptDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addCustomScript(dto));
    }

    @ApiOperation("编辑")
    @PostMapping("/updateCustomScript")
    public ResultEntity<Object> updateCustomScript(@RequestBody CustomScriptDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.updateCustomScript(dto));
    }

    @ApiOperation("详情")
    @GetMapping("/getCustomScript/{id}")
    public ResultEntity<Object> getCustomScript(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCustomScript(id));
    }

    @ApiOperation("删除")
    @DeleteMapping("/batchDelCustomScript")
    public ResultEntity<Object> batchDelCustomScript(@Validated @RequestBody List<Integer> dto) {
        return ResultEntityBuild.build(service.batchDelCustomScript(dto));
    }

    @ApiOperation("列表")
    @PostMapping("/listCustomScript")
    public ResultEntity<List<CustomScriptInfoDTO>> listCustomScript(@RequestBody CustomScriptQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listCustomScript(dto));
    }


    /**
     * 根据表id和类型获取指定数仓表的自定义加载后sql
     *
     * @param tblId
     * @param tblType
     * @return
     */
    @GetMapping("/getCustomSqlByTblIdType")
    @ApiOperation("根据表id和类型获取指定数仓表的自定义加载后sql")
    public ResultEntity<List<CustomScriptInfoDTO>> getCustomSqlByTblIdType(@RequestParam("tblId")Integer tblId, @RequestParam("tblType")Integer tblType) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getCustomSqlByTblIdType(tblId,tblType));
    }

}
