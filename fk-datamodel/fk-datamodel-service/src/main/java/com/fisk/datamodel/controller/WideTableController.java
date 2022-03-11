package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.service.IWideTable;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
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

    @ApiOperation("查询数据")
    @PostMapping("/addWideTable")
    public ResultEntity<Object> addWideTable(@Validated @RequestBody WideTableFieldConfigDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.executeWideTableSql(dto));
    }

}
