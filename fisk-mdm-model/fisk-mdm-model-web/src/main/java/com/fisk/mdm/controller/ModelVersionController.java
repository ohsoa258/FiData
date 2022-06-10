package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.modelVersion.ModelCopyDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionUpdateDTO;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 模型版本控制器
 *
 * @author ChenYa
 * @date 2022/04/24
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/modelVersion")
public class ModelVersionController {

    @Resource
    private IModelVersionService service;


    @ApiOperation("根据模型id查看模型版本")
    @GetMapping("/getByModelId")
    public ResultEntity<List<ModelVersionVO>> getByModelId(Integer modelId){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getByModelId(modelId));
    }

    @ApiOperation("新增模型版本")
    @PostMapping("/addData")
    public ResultEntity<ResultEnum> addData(@RequestBody ModelVersionDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.addData(dto));
    }

    @ApiOperation("修改模型版本信息")
    @PutMapping("/updateData")
    public ResultEntity<ResultEnum> updateData(@RequestBody ModelVersionUpdateDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.updateData(dto));
    }

    @ApiOperation("根据id删除版本信息")
    @DeleteMapping("/deleteDataById")
    public ResultEntity<ResultEnum> deleteDataById(Integer id){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.deleteDataById(id));
    }

    @ApiOperation("版本复制")
    @PostMapping("/copyDataByModelId")
    public ResultEntity<ResultEnum> copyDataByModelId(@RequestBody ModelCopyDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.copyDataByModelId(dto));
    }
}
