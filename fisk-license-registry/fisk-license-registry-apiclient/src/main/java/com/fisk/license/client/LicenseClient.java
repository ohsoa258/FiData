package com.fisk.license.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dick
 * @version 1.0
 * @description license服务接口
 * @date 2022/11/9 13:09
 */
@FeignClient("licenseRegistry-service")
public interface LicenseClient {
}
