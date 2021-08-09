package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.DataSourceAreaDTO;
import com.fisk.datamodel.service.IDataSourceArea;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.DATASOURCE_AREA})
@RestController
@RequestMapping("/datasource")
public class DataSourceAreaController {

    @Resource
    private IDataSourceArea service;

    @PostMapping("/add")
    @ApiOperation(value = "添加计算数据源[对象]")
    public ResultEntity<Object> addData(@RequestBody DataSourceAreaDTO dto) {

        return ResultEntityBuild.build(service.addData(dto));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<DataSourceAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "计算数据源修改(对象)")
    public ResultEntity<Object> editData(@RequestBody DataSourceAreaDTO dto) {

        return ResultEntityBuild.build(service.updateDataSourceArea(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "计算数据源删除(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteDataSourceArea(id));
    }

    @GetMapping("/getListDataSource")
    @ApiOperation(value = "计算数据源首页展示(不用传参)")
    public ResultEntity<List<DataSourceAreaDTO>> listDataSource() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listDataSource());
    }

}
