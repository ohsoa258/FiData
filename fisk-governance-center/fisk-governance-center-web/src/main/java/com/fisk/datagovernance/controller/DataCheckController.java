package com.fisk.datagovernance.controller;

import com.fisk.datagovernance.config.SwaggerConfig;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验
 * @date 2022/3/22 16:17
 */
@Api(tags = {SwaggerConfig.TAG_2})
@RestController
@RequestMapping("/datacheck")
public class DataCheckController {

}
