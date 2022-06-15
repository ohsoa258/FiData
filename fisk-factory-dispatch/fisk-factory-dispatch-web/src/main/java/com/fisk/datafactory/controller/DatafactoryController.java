package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.service.IDataFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description 对外提供的feign接口API
 * @date 2022/1/11 11:51
 */
@RestController
@RequestMapping("/dataFactory")
public class DatafactoryController {

    @Resource
    private IDataFactory service;

    @PostMapping("/loadDepend")
    @ApiOperation(value = "判断物理表是否在管道使用")
    public boolean loadDepend(@RequestBody LoadDependDTO dto) {

        return service.loadDepend(dto);
    }

    @PostMapping("/getNIfiPortHierarchy")
    @ApiOperation(value = "获取管道层级关系")
    public ResultEntity<NifiPortsHierarchyDTO> getNifiPortHierarchy(@Validated @RequestBody NifiGetPortHierarchyDTO dto) {

        return service.getNifiPortHierarchy(dto);
    }

    @GetMapping("/getNifiPortTaskFirstListById/{id}")
    @ApiOperation(value = "根据管道主键id查询管道内第一批任务")
    public ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskFirstListById(@PathVariable("id") Long id) {

        return service.getNifiPortTaskFirstListById(id);
    }

    @GetMapping("/getNifiPortTaskLastListById/{id}")
    @ApiOperation(value = "根据管道主键id查询管道内最后一批任务")
    public ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskLastListById(@PathVariable("id") Long id) {

        return service.getNifiPortTaskLastListById(id);
    }

    @PostMapping("/redirect")
    @ApiOperation(value = "根据componentType,appId,tableId查询出表具体在哪些管道,哪些组件中使用")
    public ResultEntity<List<DispatchRedirectDTO>> redirect(@RequestBody NifiCustomWorkflowDetailDTO dto) {

        return service.redirect(dto);
    }
}
