package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.BusinessPageResultDTO;
import com.fisk.datamodel.dto.BusinessQueryDTO;
import com.fisk.datamodel.service.IBusinessArea;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.BUSINESS_AREA})
@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessAreaController {

    @Resource
    private IBusinessArea service;

    @PostMapping("/add")
    @ApiOperation(value = "添加业务域[对象]")
    public ResultEntity<Object> addData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.addData(businessAreaDTO));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<BusinessAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改(对象)")
    public ResultEntity<Object> editData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.updateBusinessArea(businessAreaDTO));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除业务域(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteBusinessArea(id));
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询(url拼接)")
    public ResultEntity<Page<Map<String, Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取业务域表字段")
    public ResultEntity<Object> getBusinessColumn(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getBusinessAreaColumn());
    }

    @PostMapping("/getDataList")
    @ApiOperation(value = "获取业务域数据列表")
    public ResultEntity<Page<BusinessPageResultDTO>> getDataList(@RequestBody BusinessQueryDTO query){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getDataList(query));
    }

    @GetMapping("/getBusinessAreaPublicData")
    @ApiOperation(value = "根据业务域id,获取相关维度以及原子指标")
    public ResultEntity<BusinessAreaGetDataDTO> getBusinessAreaPublicData(@RequestParam("id") List<Integer> id) {
        return service.getBusinessAreaPublicData(id);
    }

    @GetMapping("/businessAreaPublic")
    @ApiOperation(value = "根据业务域id,推送业务域下相关维度以及原子指标数据")
    public ResultEntity<Object> businessAreaPublic(@RequestParam("id") int id) {
        return service.businessAreaPublic(id);
    }

    @ApiOperation("修改业务域发布状态")
    @PutMapping("/editBusinessAreaPublishStatus")
    public void editBusinessAreaPublishStatus(@RequestParam("id")int id,@RequestParam("isSuccess")int isSuccess) {
        service.updatePublishStatus(id,isSuccess);
    }

}
