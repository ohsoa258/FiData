package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.ApiConfigureDTO;
import com.fisk.dataservice.dto.ConfigureUserDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.service.ApiFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2021/7/13 13:26
 */
@Api(tags = {SwaggerConfig.TAG_1})
@RestController
@RequestMapping("/api")
public class ApiFieldController {

    @Resource
    private ApiFieldService configureFieldService;

    @ApiOperation("根据路径查询")
    @RequestMapping("/query/{apiRoute}")
    public ResultEntity<List<Map>> queryData(@PathVariable("apiRoute") String apiRoute,
                                             Integer currentPage, Integer pageSize,
                                             @RequestBody ConfigureUserDTO user) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,configureFieldService.queryField(apiRoute,currentPage,pageSize,user));
    }

    @ApiOperation("查询无需身份验证")
    @RequestMapping("/get/{apiRoute}")
    public ResultEntity<List<Map>> queryData(@PathVariable("apiRoute") String apiRoute,
                                             Integer currentPage,
                                             Integer pageSize) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,configureFieldService.queryField(apiRoute,currentPage,pageSize));
    }

    @ApiOperation("分页查询所有Api服务")
    @GetMapping("/getAll")
    public ResultEntity<Page<ApiConfigureDTO>> listData(Page<ApiConfigurePO> page,String apiName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.queryAll(page,apiName));
    }

    @ApiOperation("修改Api服务")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody ApiConfigureDTO dto) {
        return ResultEntityBuild.build(configureFieldService.updateApiConfigure(dto));
    }

    @ApiOperation("删除Api服务")
    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteDataById(Integer id) {
        return ResultEntityBuild.build(configureFieldService.deleteApiById(id));
    }

    @ApiOperation("根据id查询Api服务")
    @GetMapping("/getById")
    public ResultEntity<Object> getById(Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, configureFieldService.getById(id));
    }
}
