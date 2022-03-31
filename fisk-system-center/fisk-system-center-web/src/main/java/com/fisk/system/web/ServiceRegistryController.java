package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.service.IServiceRegistryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.SERVICE_REGISTRY})
@RestController
@RequestMapping("/ServiceRegistry")
@Slf4j
public class ServiceRegistryController {

    @Resource
    private IServiceRegistryService service;

    /**
     * 获取服务注册树形结构
     *
     * @return 返回值
     */
    @GetMapping("/getList")
    @ApiOperation(value = "获取服务注册列表")
    public ResultEntity<Object> getList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listServiceRegistry());
    }

    /**
     * 添加服务注册
     *
     * @return 返回值
     */
    @PostMapping("/addData")
    @ApiOperation(value = "添加服务注册[对象]")
    public ResultEntity<Object> addData(@RequestBody ServiceRegistryDTO dto) {
        return ResultEntityBuild.build(service.addServiceRegistry(dto));
    }

    /**
     * 删除
     * @param id 请求参数
     * @return 返回值
     */
    @DeleteMapping("/deleteData/{id}")
    @ApiOperation(value = "删除服务注册(url拼接)")
    public ResultEntity<Object> deleteData(
            @PathVariable("id") int id) {
        return ResultEntityBuild.build(service.delServiceRegistry(id));
    }

    /**
     * 根据id查询数据,用于数据回显
     * @param id 请求参数
     * @return 返回值
     */
    @GetMapping("/getData/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<Object> getData(
            @PathVariable("id") int id)
    {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getDataDetail(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改服务注册(对象)")
    public ResultEntity<Object> editData(@RequestBody ServiceRegistryDTO dto)
    {
        return ResultEntityBuild.build(service.updateServiceRegistry(dto));
    }

    @GetMapping("/getServiceRegistryList")
    @ApiOperation(value = "获取服务名称列表")
    public ResultEntity<Object> getServiceRegistryList()
    {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getServiceRegistryList());
    }

}
