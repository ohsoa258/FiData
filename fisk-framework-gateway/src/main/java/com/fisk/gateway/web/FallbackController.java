package com.fisk.gateway.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lock
 * @date 2021/5/14 15:14
 */
@RestController
@Slf4j
public class FallbackController {
    /**
     *
     * 默认的超时时间提醒: 504状态
     * @return 熔断
     */
    @GetMapping("/hystrix/fallback")
    public ResultEntity<Object> fallbackController() {
        log.error("---------------熔断---------------");
        return ResultEntityBuild.build(ResultEnum.SERVER_FUSE);
    }
}
