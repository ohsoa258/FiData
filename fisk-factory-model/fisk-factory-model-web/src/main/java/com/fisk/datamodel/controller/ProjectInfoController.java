package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.ProjectInfoDTO;
import com.fisk.datamodel.service.IProjectInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.PROJECT_INFO})
@RestController
@RequestMapping("/projectInfo")
@Slf4j
public class ProjectInfoController {

    @Resource
    private IProjectInfo service;

    @PostMapping("/add")
    @ApiOperation(value = "添加项目空间[对象]")
    public ResultEntity<Object> addData(@RequestBody ProjectInfoDTO dto) {

        return ResultEntityBuild.build(service.addData(dto));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<ProjectInfoDTO> getDataById(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataById(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "项目空间修改(对象)")
    public ResultEntity<Object> editData(@RequestBody ProjectInfoDTO dto) {

        return ResultEntityBuild.build(service.updateProjectInfo(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除项目信息(url拼接)")
    public ResultEntity<Object> deleteDataById(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteDataById(id));
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询(url拼接)")
    public ResultEntity<Page<Map<String, Object>>> listData(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(key, page, rows));
    }

}
