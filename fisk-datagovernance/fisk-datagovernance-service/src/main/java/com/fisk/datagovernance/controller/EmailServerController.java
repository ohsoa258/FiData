package com.fisk.datagovernance.controller;

import com.fisk.datagovernance.config.SwaggerConfig;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器
 * @date 2022/3/22 16:16
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/emailserver")
public class EmailServerController {
}
