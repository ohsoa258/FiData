package com.fisk.mdm.controller;

import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.service.AttributeGroupService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:33
 * @Version 1.0
 */
@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/entity")
public class AttributeGroupController {

    @Resource
    AttributeGroupService groupService;

}
