package com.fisk.datagovernance.controller;

import com.fisk.datagovernance.config.SwaggerConfig;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.TAG_1})
@RestController
@RequestMapping("/businessfilter")
public class BusinessFilterController {
}
