package com.fisk.datamanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionDTO;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionQueryDTO;
import com.fisk.datamanagement.service.CodeCollectionService;
import com.fisk.datamanagement.vo.CodeCollectionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@Api(tags = {SwaggerConfig.CODECOLLECTION})
@RestController
@RequestMapping("/CodeCollection")
@Slf4j
public class CodeCollectionController {
    @Resource
    private CodeCollectionService codeCollectionService;

    @ApiOperation("添加代码集合")
    @PostMapping("/addCodeCollection")
    public ResultEntity<Object> addCodeCollection(@RequestBody CodeCollectionDTO dto) {
        return ResultEntityBuild.build(codeCollectionService.addCodeCollection(dto));
    }
    @ApiOperation("修改代码集合")
    @PostMapping("/updateCodeCollection")
    public ResultEntity<Object> updateCodeCollection(@RequestBody CodeCollectionDTO dto) {
        return ResultEntityBuild.build(codeCollectionService.updateCodeCollection(dto));
    }

    @ApiOperation("删除代码集合")
    @DeleteMapping("/delCodeCollection/{id}")
    public ResultEntity<Object> delCodeCollection(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(codeCollectionService.delCodeCollection(id));
    }

    @ApiOperation("分页查询代码集合")
    @PostMapping("/pageCollection")
    public ResultEntity<Page<CodeCollectionVO>> getCodeCollection(@RequestBody CodeCollectionQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, codeCollectionService.getCodeCollection(query));
    }
    @ApiOperation("分页查询代码集合")
    @PostMapping("/pageCollectionList")
    public ResultEntity<Page<CodeCollectionVO>> pageCollectionList(@RequestBody CodeCollectionQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, codeCollectionService.pageCollectionList(query));
    }
}
