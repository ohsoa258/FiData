package com.fisk.dataservice.controller;

import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.service.IApiServiceManageService;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author dick
 * @version v1.0
 * @description 代理服务控制器
 * @date 2022/1/6 14:51
 */
@Api(tags = {SwaggerConfig.TAG_10})
@Controller
public class ReverseProxyController {
    @Resource
    private IApiServiceManageService service;

    // @RequestMapping(value = "/proxy/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
    @RequestMapping(value = "/proxy/**", method = {RequestMethod.GET, RequestMethod.POST})
    public void proxy(HttpServletRequest request, HttpServletResponse response) {
        service.proxy(request, response);
        return;
    }
}
