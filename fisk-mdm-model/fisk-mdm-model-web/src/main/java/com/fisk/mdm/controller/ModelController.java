package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.vo.model.ModelInfoVO;
import com.fisk.mdm.vo.model.ModelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chenYa
 */
@Api(tags = {SwaggerConfig.TAG_2})
@RestController
@RequestMapping("/model")
public class ModelController {

    @Resource
    private IModelService service;

    @ApiOperation("分页查询所有model")
    @PostMapping("/list")
    public ResultEntity<Page<ModelVO>> getAll(@RequestBody ModelQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }


    @ApiOperation("根据id查询model")
    @GetMapping("/get")
    public ResultEntity<ModelVO> detail(Integer id) {
        return service.getById(id);
    }

    @ApiOperation("添加model")
    @PostMapping("/insert")
    public ResultEntity<ResultEnum> addData(@RequestBody ModelDTO model) {
        return ResultEntityBuild.build(service.addData(model));
    }

    @ApiOperation("编辑model")
    @PutMapping("/update")
    public ResultEntity<ResultEnum> editData(@Validated @RequestBody ModelUpdateDTO model) {
        return ResultEntityBuild.build(service.editData(model));
    }

    @ApiOperation("删除model")
    @DeleteMapping("/delete")
    public ResultEntity<ResultEnum> deleteData(Integer id) {
        return ResultEntityBuild.build(service.deleteDataById(id));
    }

    @ApiOperation("根据模型id获取实体")
    @GetMapping("/getEntityById")
    @ResponseBody
    public ResultEntity<ModelInfoVO> getEntityById(Integer id,String name){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getEntityById(id,name));
    }

    @ApiOperation("刷新主数据结构")
    @PostMapping("/setDataStructure")
    @ResponseBody
    public ResultEntity<List<FiDataMetaDataDTO>> setDataStructure(@RequestBody FiDataMetaDataReqDTO dto){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.setDataStructure(dto));
    }

    @ApiOperation("获取主数据结构")
    @PostMapping("/getDataStructure")
    @ResponseBody
    public ResultEntity<List<FiDataMetaDataDTO>> getDataStructure(@RequestBody FiDataMetaDataReqDTO dto){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getDataStructure(dto));
    }

}
