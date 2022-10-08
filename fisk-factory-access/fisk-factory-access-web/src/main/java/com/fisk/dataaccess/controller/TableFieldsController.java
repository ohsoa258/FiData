package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.access.OperateTableDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.service.ITableFields;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/tableFields")
@Slf4j
public class TableFieldsController {
    @Resource
    public ITableFields service;

    @PostMapping("/getTableField")
    @ApiOperation(value = "查询表字段")
    public ResultEntity<TableFieldsDTO> getTableField(@RequestParam("id") int id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableField(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加物理表字段--保存&发布")
    public ResultEntity<Object> addData(@Validated @RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改物理表字段--保存&发布")
    public ResultEntity<Object> editData(@Validated @RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.updateData(dto));
    }

    @PostMapping("/loadDepend")
    @ApiOperation(value = "对表进行操作时,查询依赖")
    public ResultEntity<Object> loadDepend(@RequestBody OperateTableDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.loadDepend(dto));
    }

    @GetMapping("/test")
    @ApiOperation(value = "对表进行操作时,查询依赖")
    public void test() {
        service.test();
    }

}
