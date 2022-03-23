package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.IMetadataKinship;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.META_DATA_KINSHIP})
@RestController
@RequestMapping("/MetadataKinship")
@Slf4j
public class MetadataKinshipController {

    @Resource
    IMetadataKinship metadataKinship;

    @ApiOperation("获取元数据血缘关系")
    @GetMapping("/getMetadataKinship")
    public ResultEntity<Object> getMetadataKinship() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, metadataKinship.getInstance());
    }

}
