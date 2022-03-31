package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.datagovernance.config.SwaggerConfig;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 * @version 1.3
 * @description 测试
 * @date 2022/3/21 11:15
 */
@Api(tags = SwaggerConfig.TEST)
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    UserHelper userHelper;

    @GetMapping
    public ResultEntity<Object> test() {

        String username = null;

        try {
            UserInfo loginUserInfo = userHelper.getLoginUserInfo();
            username = loginUserInfo.username;
        } catch (Exception e) {
            username = "没有用户";
        }


        return ResultEntityBuild.build(ResultEnum.SUCCESS, "测试服务有效" + username);
    }

}
