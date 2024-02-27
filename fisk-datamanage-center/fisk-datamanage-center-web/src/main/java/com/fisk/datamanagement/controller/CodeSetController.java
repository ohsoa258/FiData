package com.fisk.datamanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.dto.DataSet.CodeSetQueryDTO;
import com.fisk.datamanagement.service.ICodeSetService;
import com.fisk.datamanagement.vo.CodeSetVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Api(tags = {SwaggerConfig.CODESET})
@RestController
@RequestMapping("/CodeSet")
public class CodeSetController {
    @Resource
    private ICodeSetService codeSetService;
    @ApiOperation("分页查询代码集")
    @PostMapping("/page")
    public ResultEntity<Page<CodeSetVO>> getAll(@RequestBody CodeSetQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, codeSetService.getAll(query));
    }
    @ApiOperation("添加代码集")
    @PostMapping("/addCodeSet")
    public ResultEntity<Object> addCodeSet(@RequestBody CodeSetDTO dto) {
        return ResultEntityBuild.build(codeSetService.addCodeSet(dto));
    }

    @ApiOperation("修改代码集")
    @PostMapping("/updateCodeSet")
    public ResultEntity<Object> updateCodeSet(@RequestBody CodeSetDTO dto) {
        return ResultEntityBuild.build(codeSetService.updateCodeSet(dto));
    }

    @ApiOperation("删除代码集")
    @DeleteMapping("/delCodeSet/{id}")
    public ResultEntity<Object> delCodeSet(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(codeSetService.delCodeSet(id));
    }
}
