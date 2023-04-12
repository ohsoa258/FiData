package com.fisk.apiservice.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dick
 * @version 1.0
 * @description 数据服务
 * @date 2023/3/21 10:27
 */
@FeignClient("api-data-service")
public interface DataServiceClient {

}
