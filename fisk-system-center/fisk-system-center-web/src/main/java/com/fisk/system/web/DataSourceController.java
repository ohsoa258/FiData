package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.TestConnectionDTO;
import com.fisk.system.service.IDataSourceManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description FiData数据源控制器
 * @date 2022/6/13 14:51
 */

@Api(tags = {SwaggerConfig.DATASOURCE})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource
    private IDataSourceManageService service;

    @PostMapping("/getAll")
    @ApiOperation("获取所有数据源连接信息,外部接口")
    public ResultEntity<List<DataSourceDTO>> getAll() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll());
    }

    @PostMapping("/getAllDataSourec")
    @ApiOperation("获取所有数据源连接信息")
    public ResultEntity<List<DataSourceDTO>> getAllDataSourec() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllDataSourec());
    }

    @PutMapping("/edit")
    @ApiOperation("编辑数据源连接信息")
    public ResultEntity<Object> editData(@Validated @RequestBody DataSourceDTO dto) {
        return ResultEntityBuild.build(service.updateDataSource(dto));
    }

    @PostMapping("/test")
    @ApiOperation("测试数据源连接")
    public ResultEntity<Object> testConnection(@Validated @RequestBody TestConnectionDTO dto) {
        return ResultEntityBuild.build(service.testConnection(dto));
    }

    @GetMapping("/getById/{datasourceId}")
    @ApiOperation("获取单条数据源连接信息")
    public ResultEntity<DataSourceDTO> getById(@RequestParam("datasourceId") int datasourceId) {
        return service.getById(datasourceId);
    }
}
