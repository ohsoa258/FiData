package com.fisk.mdm.controller;

import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.service.CodeRuleService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author WangYan
 * @Date 2022/6/23 14:06
 * @Version 1.0
 */
@Api(tags = {SwaggerConfig.TAG_10})
@RestController
@RequestMapping("/rule")
public class CodeRuleController {

    @Autowired
    CodeRuleService ruleService;


}
