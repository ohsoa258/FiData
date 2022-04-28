package com.fisk.mdm.controller;


import com.fisk.common.core.response.ResultEntity;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 主数据控制器
 *
 * @author ChenYa
 * @date 2022/04/27
 */
@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/masterData")
public class MasterDataController {
    @Resource
    private IMasterDataService service;

    @ApiOperation("分页查询所有model")
    @GetMapping("/list")
    public ResultEntity<ResultObjectVO> getAll(Integer entityId , Integer modelVersionId) {
        return service.getAll(entityId ,modelVersionId);
    }

}
