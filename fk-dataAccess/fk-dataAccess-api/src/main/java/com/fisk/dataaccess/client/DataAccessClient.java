package com.fisk.dataaccess.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lock
 */
@FeignClient("dataAccess-service")
public interface DataAccessClient {

    /**
     *
     * 给task模块提供数据源等信息
     *
     * @param id appid
     * @return 执行结果
     */
    @GetMapping("/appRegistration/dataAccess")
    Long dataAccessConfig(@RequestParam("appid") long id);

}
