package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.map.NifiComponentMap;
import com.fisk.datafactory.mapper.NifiComponentsMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.NIFI_COMPONENT})
@RestController
@RequestMapping("/nifiComponent")
public class NifiComponentController {

    @Resource
    NifiComponentsMapper mapper;

    @GetMapping("/getList")
    @ApiOperation(value = "可视化视图-组件列表")
    public ResultEntity<List<NifiComponentsDTO>> getList() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, NifiComponentMap.INSTANCES.listPoToDto(mapper.selectList(null)));
    }
}
