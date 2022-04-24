package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("/getByModelId")
    @GetMapping
    public ResultEntity<List<ModelVersionVO>> getByModelId(Integer modelId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getByModelId(modelId));
    }
}
